package com.ctgu.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.ctgu.view.ChessPanel;

/**
 * 
 **/
public class FileUtil
{
	private static final String TEMP_FILE_NAME = "chess-temp-log.txt";

	/**
	 * 文件读取
	 *
	 * @param sourceFilePath
	 *            源文件路径
	 * @throws IOException
	 *             文件写入异常
	 */
	public static byte[] readFile(String sourceFilePath) throws IOException
	{
		final File file = new File(sourceFilePath);
		if (!file.exists() || !file.isFile())
		{
			return new byte[0];
		}
		byte[] buf = new byte[(int) file.length()];
		try (FileInputStream in = new FileInputStream(sourceFilePath))
		{
			final int read = in.read(buf);
			assert read == file.length();
		}
		return buf;
	}

	/**
	 * 动态字节流文件读取
	 *
	 * @param classPath
	 *            源文件路径
	 * @throws IOException
	 *             文件写入异常
	 */
	public static byte[] readFromResource(String classPath) throws IOException
	{
		final ClassLoader classLoader = ChessPanel.class.getClassLoader();
		byte[] buf = new byte[8 * 1024];
		try (InputStream in = classLoader.getResourceAsStream(classPath))
		{
			if (in == null)
			{
				throw new FileNotFoundException("未发现资源文件 : " + classPath);
			}
			// 动态字节流
			ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
			int len;
			while ((len = in.read(buf)) != -1)
			{
				arrayOutputStream.write(buf, 0, len);
			}
			return arrayOutputStream.toByteArray();
		}
	}

	public static String readFromFile(String path)
	{
		StringBuffer sb = new StringBuffer("");
		String filePath = null;
		if (path == null || path.length() == 0)
		{
			filePath = System.getProperty("user.dir") + File.separator + TEMP_FILE_NAME;
		}
		else
		{
			filePath = path;
		}
		try
		{
			File file = new File(filePath);
			// System.out.println("read log file from " + file.getAbsolutePath());
			FileReader reader = new FileReader(file);
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			while ((str = br.readLine()) != null)
			{
				sb.append(str + "\n");
			}
			br.close();
			reader.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return sb.toString();
	}

	public static void writeToFile(String filePath, String content)
	{
		try
		{
			File file = null;
			if (filePath == null || filePath.length() == 0)
			{
				file = new File(System.getProperty("user.dir") + File.separator + TEMP_FILE_NAME);
			}
			else
			{
				file = new File(filePath);
			}
			// System.out.println("write log file to " + file.getAbsolutePath());
			FileWriter writer = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(writer);
			bw.write(content + "\n");
			bw.close();
			writer.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void createLogFile()
	{
		try
		{
			File file = new File(System.getProperty("user.dir") + File.separator + TEMP_FILE_NAME);
			// System.out.println("create log file " + file.getAbsolutePath());
			if (file.exists() == false)
			{
				file.createNewFile();
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void deleteLogFile()
	{
		File file = new File(System.getProperty("user.dir") + File.separator + TEMP_FILE_NAME);
		if (file.exists())
		{
			file.delete();
		}
	}

	public static List<File> getAllSubFolders(String path)
	{
		File destFile = new File(path);
		if (!destFile.exists())
		{
			return new ArrayList<>();
		}
		File[] files = destFile.listFiles();
		if (files == null || files.length == 0)
		{
			return new ArrayList<>();
		}
		List<File> fileList = new ArrayList<>();
		for (File file : files)
		{
			if (file.isDirectory())
			{
				fileList.add(file);
			}
		}
		return fileList;
	}

	/**
	 * 遍历此路径的文件夹 返回里面的所有文件 用一个集合list 将文件装起来 由于递归不能在里面定义 所以作为参数传进来
	 * 
	 * @param path
	 * @return
	 */
	public static void getAllFiles(String path, List<String> fileList, List<String> fileTypes)
	{
		File file = new File(path);
		if (!file.exists())
		{
			return;
		}
		File[] files = file.listFiles();
		if (files == null || files.length == 0)
		{
			return;
		}
		for (File subFile : files)
		{
			if (subFile.isDirectory())
			{
				getAllFiles(subFile.getName(), fileList, fileTypes);
			}
			else
			{
				String suffix = subFile.getName().substring(subFile.getName().lastIndexOf("."));
				if (fileTypes.contains(suffix))
				{
					fileList.add(subFile.getName());
				}
			}
		}
	}

	public static void getFiles(String path, List<File> fileList)
	{
		File file = new File(path);
		if (!file.exists())
		{
			return;
		}
		File[] files = file.listFiles();
		if (files == null || files.length == 0)
		{
			return;
		}
		for (File subFile : files)
		{
			if (subFile.isDirectory())
			{
				getFiles(subFile.getName(), fileList);
			}
			else
			{
				fileList.add(subFile);
			}
		}
		shuffle(fileList);
	}

	public static List<File> shuffle(List<File> fileList)
	{
		String[] regulation = { "rr", "rn", "rb", "ra", "rk", "rc", "rp", "oos", "br", "bn", "bb", "ba", "bk", "bc", "bp", "oos2" };
		List<String> regulationOrder = Arrays.asList(regulation);
		Collections.sort(fileList, new Comparator<File>()
		{
			public int compare(File o1, File o2)
			{
				String o1Name = o1.getName().substring(0, o1.getName().indexOf("."));
				String o2Name = o2.getName().substring(0, o2.getName().indexOf("."));
				// System.out.println("o1Name=" + o1Name + ", o2Name=" + o2Name);
				int io1 = regulationOrder.indexOf(o1Name);
				int io2 = regulationOrder.indexOf(o2Name);
				return (io1 == -1 || io2 == -1) ? (io2 - io1) : (io1 - io2);
			}
		});
		return fileList;
	}
}
