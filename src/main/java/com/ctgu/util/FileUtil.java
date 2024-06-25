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
import java.util.List;

import com.ctgu.view.ChessPanel;

/**
 * 
 **/
public class FileUtil
{
	private static final String TEMP_FILE_NAME = "chess-temp-log.txt";

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
}
