package com.ctgu.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ctgu.enums.EngineState;
import com.ctgu.util.IniFileUtil;

public class UcciEngine
{
	private static File enginePath;
	public static final String CMD_GO_HEADER = "go";
	public static final String CMD_GO_TIME_PATTERN = "go time %d";
	public static final String CMD_QUIT = "quit";
	public static final String CMD_STOP = "stop";
	public static final String CMD_UCCI = "ucci";
	public static final String RSP_BEST_MOVE_HEADER = "bestmove";
	public static final String RSP_NO_BEST_MOVE = "nobestmove";
	public static final String RSP_READY_OK = "readyok";
	public static final String RSP_UCCI_OK = "ucciok";
	public static final String Tag = "UCCI";
	private static UcciEngine instance;
	private BufferedReader error;
	private BufferedReader input;
	private EngineMonitor monitor;
	private PrintWriter output;
	private Process process;
	private volatile EngineState state = EngineState.Gone;
	private final Object outputLock = new Object();
	private ExecutorService ioExecutor;
	private static final Logger logger = Logger.getLogger(UcciEngine.class.getName());

	// We use an ExecutorService to manage IO reader tasks (daemon threads)

	public static UcciEngine getInstance()
	{
		if (instance == null)
		{
			instance = new UcciEngine();
		}
		return instance;
	}

	private UcciEngine()
	{
	}

	public static File getEngineFile()
	{
		enginePath = new File(IniFileUtil.getBasePath(), "engines");
		return enginePath;
	}

	private void onResponse(String line)
	{
		if (line == null)
		{
			return;
		}
		if (line.equals(RSP_READY_OK) || line.equals(RSP_UCCI_OK) || line.startsWith(RSP_BEST_MOVE_HEADER))
		{
			state = EngineState.Ready;
		}
		else if (line.startsWith(RSP_NO_BEST_MOVE) && state.isRunning())
		{
			state = EngineState.Ready;
		}
		if (state.isRunning() && monitor != null)
		{
			try
			{
				monitor.onResponse(line);
			}
			catch (Throwable t)
			{
				logger.log(Level.WARNING, "Engine monitor onResponse threw", t);
			}
		}
	}

	private void onError(String line)
	{
		if (monitor != null)
		{
			try
			{
				monitor.onError(line);
			}
			catch (Throwable t)
			{
				logger.log(Level.WARNING, "Engine monitor onError threw", t);
			}
		}
	}

	public static boolean installed(String fileName)
	{
		File exeFile = new File(getEngineFile(), fileName);
		if (!exeFile.exists())
		{
			extractEngineFiles(fileName);
		}
		return exeFile.exists();
	}

	/** 从类路径复制引擎文件到工作目录 */
	private static void extractEngineFiles(String fileName)
	{
		String dirName = fileName.contains(File.separator)
				? fileName.substring(0, fileName.indexOf(File.separator))
				: "";
		if (dirName.isEmpty())
		{
			return;
		}
		try
		{
			URL dirUrl = UcciEngine.class.getResource("/config/engines/" + dirName);
			if (dirUrl == null)
			{
				return;
			}
			// 在 IDE / 展开目录模式下，类路径资源是 file:// 协议
			if ("file".equalsIgnoreCase(dirUrl.getProtocol()))
			{
				File srcDir = new File(dirUrl.toURI());
				if (srcDir.isDirectory())
				{
					File targetDir = new File(getEngineFile(), dirName);
					copyDirectory(srcDir, targetDir);
				}
			}
			else if ("jar".equalsIgnoreCase(dirUrl.getProtocol()))
			{
				// JAR 模式：尝试逐个复制已知引擎文件（需要提交文件清单）
				String resourceRoot = "/config/engines/" + dirName + "/";
				String exeName = fileName.contains(File.separator)
						? fileName.substring(fileName.indexOf(File.separator) + 1)
						: fileName;
				copyFromClasspath(resourceRoot + exeName, new File(getEngineFile(), fileName));
			}
		}
		catch (Exception e)
		{
			logger.log(Level.FINE, "Failed to extract engine files for: " + fileName, e);
		}
	}

	private static void copyDirectory(File src, File dest) throws IOException
	{
		if (!src.isDirectory())
		{
			if (src.isFile())
			{
				Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			return;
		}
		Files.createDirectories(dest.toPath());
		File[] files = src.listFiles();
		if (files == null)
		{
			return;
		}
		for (File f : files)
		{
			File target = new File(dest, f.getName());
			if (f.isDirectory())
			{
				copyDirectory(f, target);
			}
			else
			{
				Files.copy(f.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}

	private static void copyFromClasspath(String resourcePath, File target) throws IOException
	{
		if (target.exists())
		{
			return;
		}
		Files.createDirectories(target.getParentFile().toPath());
		try (InputStream in = UcciEngine.class.getResourceAsStream(resourcePath))
		{
			if (in != null)
			{
				Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
				target.setExecutable(true);
			}
		}
	}

	public boolean startup(String fileName)
	{
		try
		{
			File exeFile = new File(getEngineFile(), fileName);
			if (!exeFile.exists())
			{
				extractEngineFiles(fileName);
			}
			ProcessBuilder pb = new ProcessBuilder(exeFile.getAbsolutePath());
			pb.directory(getEngineFile());
			process = pb.start();
			input = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
			error = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
			output = new PrintWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8), true);

			// Executor for IO threads
			ioExecutor = Executors.newCachedThreadPool(r -> {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			});

			ioExecutor.submit(() -> {
				try
				{
					String line;
					while ((line = input.readLine()) != null)
					{
						onResponse(line);
					}
					state = EngineState.Gone;
				}
				catch (Throwable t)
				{
					if (state.isStopped())
					{
						logger.log(Level.FINE, "Engine stdout reader closed during shutdown", t);
					}
					else
					{
						logger.log(Level.WARNING, "Exception in engine stdout reader", t);
					}
				}
			});

			ioExecutor.submit(() -> {
				try
				{
					String line;
					while ((line = error.readLine()) != null)
					{
						onError(line);
					}
				}
				catch (Throwable t)
				{
					if (state.isStopped())
					{
						logger.log(Level.FINE, "Engine stderr reader closed during shutdown", t);
					}
					else
					{
						logger.log(Level.WARNING, "Exception in engine stderr reader", t);
					}
				}
			});

			state = EngineState.Initialized;
			setProtocol();
			return true;
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, "Failed to startup engine", e);
			return false;
		}
	}

	public void restart(String fileName)
	{
		if (process != null)
		{
			sendQuit();
			try
			{
				if (!process.waitFor(2000, TimeUnit.MILLISECONDS))
				{
					process.destroy();
					process.waitFor(1000, TimeUnit.MILLISECONDS);
				}
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
			finally
			{
				// close streams
				closeStreams();
			}
		}
		startup(fileName);
	}

	public int shutdown()
	{
		if (process == null)
		{
			return -1;
		}
		sendQuit();
		state = EngineState.Exiting;
		int exit = -1;
		try
		{
			if (!process.waitFor(2000, TimeUnit.MILLISECONDS))
			{
				process.destroy();
				process.waitFor(1000, TimeUnit.MILLISECONDS);
			}
			try
			{
				exit = process.exitValue();
			}
			catch (IllegalThreadStateException itse)
			{
				exit = -1;
			}
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
		finally
		{
			state = EngineState.Gone;
			closeStreams();
			if (ioExecutor != null)
			{
				ioExecutor.shutdownNow();
				try
				{
					ioExecutor.awaitTermination(1, TimeUnit.SECONDS);
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}
			}
		}
		return exit;
	}

	private void closeStreams()
	{
		try
		{
			if (output != null)
			{
				output.flush();
				output.close();
			}
		}
		catch (Throwable t)
		{
			logger.log(Level.FINE, "Error closing engine output", t);
		}
		try
		{
			if (input != null)
			{
				input.close();
			}
		}
		catch (Throwable t)
		{
			logger.log(Level.FINE, "Error closing engine input", t);
		}
		try
		{
			if (error != null)
			{
				error.close();
			}
		}
		catch (Throwable t)
		{
			logger.log(Level.FINE, "Error closing engine error stream", t);
		}
		try
		{
			if (process != null)
			{
				process.destroy();
			}
		}
		catch (Throwable t)
		{
			logger.log(Level.FINE, "Error destroying engine process", t);
		}
		// end of closeStreams
	}

	public boolean sendCommand(String line)
	{
		if (state.isStopped())
		{
			return false;
		}
		if (line.startsWith(CMD_GO_HEADER))
		{
			state = EngineState.Thinking;
		}
		synchronized (outputLock)
		{
			if (output == null)
			{
				return false;
			}
			output.println(line);
			output.flush();
		}
		return true;
	}

	public void sendStop()
	{
		sendCommand(CMD_STOP);
	}

	private void setProtocol()
	{
		sendCommand(CMD_UCCI);
	}

	private void sendQuit()
	{
		sendCommand(CMD_QUIT);
	}

	public boolean isReady()
	{
		return state.isReady();
	}

	public boolean isThinking()
	{
		return state.isThinking();
	}

	public EngineState getState()
	{
		return state;
	}

	public void setMonitor(EngineMonitor engineMonitor)
	{
		monitor = engineMonitor;
	}
}
