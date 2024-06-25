package com.ctgu.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

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
	private EngineState state = EngineState.Gone;

	private class ErrorResponseThread extends Thread
	{
		private ErrorResponseThread()
		{
		}

		public void run()
		{
			while (true)
			{
				try
				{
					String line = UcciEngine.this.error.readLine();
					if (line != null)
					{
						UcciEngine.this.onError(line);
					}
					else
					{
						return;
					}
				}
				catch (Throwable th)
				{
					th.printStackTrace();
					return;
				}
			}
		}
	}

	private class NormalResponseThread extends Thread
	{
		private NormalResponseThread()
		{
		}

		public void run()
		{
			while (true)
			{
				try
				{
					String line = UcciEngine.this.input.readLine();
					if (line != null)
					{
						UcciEngine.this.onResponse(line);
					}
					else
					{
						UcciEngine.this.state = EngineState.Gone;
						return;
					}
				}
				catch (Throwable e)
				{
					e.printStackTrace();
					return;
				}
			}
		}
	}

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
		enginePath = new File(IniFileUtil.getBasePath() + "/engines/");
		return enginePath;
	}

	private void onResponse(String line) throws IOException
	{
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
			monitor.onResponse(line);
		}
	}

	private void onError(String line) throws IOException
	{
		if (monitor != null)
		{
			monitor.onError(line);
		}
	}

	public static boolean installed(String fileName)
	{
		File EXE_FILE = new File(getEngineFile(), fileName);
		return EXE_FILE.exists();
	}

	public boolean startup(String fileName)
	{
		try
		{
			File exeFile = new File(getEngineFile(), fileName);
			process = Runtime.getRuntime().exec(exeFile.getPath(), new String[0], getEngineFile());
			input = new BufferedReader(new InputStreamReader(process.getInputStream()));
			error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			output = new PrintWriter(process.getOutputStream());
			new NormalResponseThread().start();
			new ErrorResponseThread().start();
			state = EngineState.Initialized;
			setProtocol();
			return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
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
				process.waitFor();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
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
		try
		{
			process.waitFor();
		}
		catch (InterruptedException e)
		{
		}
		state = EngineState.Gone;
		return process.exitValue();
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
		output.println(line);
		output.flush();

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
