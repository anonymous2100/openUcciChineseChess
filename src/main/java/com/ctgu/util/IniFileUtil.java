package com.ctgu.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ini4j.Ini;

import com.ctgu.config.ChessConfig;
import com.ctgu.config.Config;
import com.ctgu.model.IniFileEntity;

public class IniFileUtil
{
	public static File configFile;

	public static File getConfigFile()
	{
		configFile = new File(getBasePath() + "/chessConfig.ini");
		return configFile;
	}

	public static String getBasePath()
	{
		String basePath = IniFileUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		try
		{
			basePath = URLDecoder.decode(basePath, StandardCharsets.UTF_8.name());
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		basePath = basePath.replace('/', System.getProperty("file.separator").charAt(0));

		int firstIndex = basePath.indexOf(System.getProperty("file.separator")) + 1;
		int lastIndex = basePath.lastIndexOf(System.getProperty("file.separator")) + 1;
		basePath = basePath.substring(firstIndex, lastIndex);

		basePath = basePath + "config" + System.getProperty("file.separator");
		return basePath;
	}

	public static boolean creatIniFile(String filePath, List<IniFileEntity> fileContentList) throws IOException
	{
		File file = new File(filePath);
		if (file.exists())
		{
			return false;
		}
		file.createNewFile();
		Ini ini = new Ini();
		ini.load(file);
		for (IniFileEntity entity : fileContentList)
		{
			ini.add(entity.getSection(), entity.getKey(), entity.getValue() == null ? "" : entity.getValue());
		}
		ini.store(file);
		return true;
	}

	public static Map<String, String> getAllPropertyValue(String filename) throws IOException
	{
		FileReader fileReader = new FileReader(filename);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String temp;
		ArrayList<String> contentList = new ArrayList<>();
		while ((temp = bufferedReader.readLine()) != null)
		{
			if (!temp.isEmpty())
			{
				contentList.add(temp);
			}
		}
		bufferedReader.close();
		List<String> titleList = new ArrayList<>();
		for (String line : contentList)
		{
			if (line.contains("["))
			{
				titleList.add(line.trim());
			}
		}
		HashMap<String, List<String>> sections = new HashMap<>();
		for (int t = 0; t < titleList.size(); t++)
		{
			int sectionStart = 0;
			int sectionStop = 0;
			sectionStart = contentList.indexOf(titleList.get(t)) + 1;
			if (t == (titleList.size() - 1))
			{
				sectionStop = contentList.size() - 1;
			}
			else
			{
				sectionStop = contentList.indexOf(titleList.get(t + 1)) - 1;
			}
			List<String> temp2 = contentList.subList(sectionStart, sectionStop + 1);
			sections.put(titleList.get(t), temp2);
		}
		Map<String, String> propertyMap = new HashMap<>();
		for (String section : sections.keySet())
		{
			List<String> sectionDataList = sections.get(section);
			for (String sectionData : sectionDataList)
			{
				String[] sectionKeyValue = sectionData.split("=");
				propertyMap.put(sectionKeyValue[0].trim(), sectionKeyValue[1].trim());
			}
		}
		return propertyMap;
	}

	public static ChessConfig readFileContent(String filename)
	{
		ChessConfig chessConfig = new ChessConfig();
		Map<String, String> propertyMap;
		try
		{
			propertyMap = getAllPropertyValue(filename);
			for (Map.Entry<String, String> entry : propertyMap.entrySet())
			{
				String key = entry.getKey();
				String value = entry.getValue();
				ReflectUtil.setFieldValueByFieldName(chessConfig, key, value);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return chessConfig;
	}

	public static String getProfileString(String section, String variable, String defaultValue) throws IOException
	{
		String strLine, value = "";
		BufferedReader bufferedReader = new BufferedReader(new FileReader(configFile));
		boolean isInSection = false;
		try
		{
			while ((strLine = bufferedReader.readLine()) != null)
			{
				strLine = strLine.trim();
				strLine = strLine.split("[;]")[0];
				Pattern p;
				Matcher m;
				p = Pattern.compile("\\[\\s*.*\\s*\\]");
				m = p.matcher((strLine));
				if (m.matches())
				{
					p = Pattern.compile("\\[\\s*" + section + "\\s*\\]");
					m = p.matcher(strLine);
					if (m.matches())
					{
						isInSection = true;
					}
					else
					{
						isInSection = false;
					}
				}
				if (isInSection == true)
				{
					strLine = strLine.trim();
					String[] strArray = strLine.split("=");
					if (strArray.length == 1)
					{
						value = strArray[0].trim();
						if (value.equalsIgnoreCase(variable))
						{
							value = "";
							return value;
						}
					}
					else if (strArray.length == 2)
					{
						value = strArray[0].trim();
						if (value.equalsIgnoreCase(variable))
						{
							value = strArray[1].trim();
							return value;
						}
					}
					else if (strArray.length > 2)
					{
						value = strArray[0].trim();
						if (value.equalsIgnoreCase(variable))
						{
							value = strLine.substring(strLine.indexOf("=") + 1).trim();
							return value;
						}
					}
				}
			}
		}
		finally
		{
			bufferedReader.close();
		}
		return defaultValue;
	}

	public static boolean setProfileString(String section, String variable, String value) throws IOException
	{
		String fileContent, allLine, strLine, newLine, remarkStr;
		String getValue;
		BufferedReader bufferedReader = new BufferedReader(new FileReader(configFile));
		boolean isInSection = false;
		fileContent = "";
		try
		{
			while ((allLine = bufferedReader.readLine()) != null)
			{
				allLine = allLine.trim();
				if (allLine.split("[;]").length > 1)
				{
					remarkStr = ";" + allLine.split(";")[1];
				}
				else
				{
					remarkStr = "";
				}
				strLine = allLine.split(";")[0];
				Pattern p;
				Matcher m;
				p = Pattern.compile("\\[\\s*.*\\s*\\]");
				m = p.matcher((strLine));
				if (m.matches())
				{
					p = Pattern.compile("\\[\\s*" + section + "\\s*\\]");
					m = p.matcher(strLine);
					if (m.matches())
					{
						isInSection = true;
					}
					else
					{
						isInSection = false;
					}
				}
				if (isInSection == true)
				{
					strLine = strLine.trim();
					String[] strArray = strLine.split("=");
					getValue = strArray[0].trim();
					if (getValue.equalsIgnoreCase(variable))
					{
						newLine = getValue + " = " + value + " " + remarkStr;
						fileContent += newLine + "\r\n";
						while ((allLine = bufferedReader.readLine()) != null)
						{
							fileContent += allLine + "\r\n";
						}
						bufferedReader.close();
						BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(configFile, false));
						bufferedWriter.write(fileContent);
						bufferedWriter.flush();
						bufferedWriter.close();
						return true;
					}
				}
				fileContent += allLine + "\r\n";
			}
		}
		catch (IOException ex)
		{
			throw ex;
		}
		finally
		{
			bufferedReader.close();
		}
		return false;
	}

	public static ChessConfig updateFileContent(String filePath, ChessConfig chessConfig)
	{
		Map<String, String> propertyMap;
		try
		{
			propertyMap = getAllPropertyValue(filePath);
			for (Map.Entry<String, String> entry : propertyMap.entrySet())
			{
				String key = entry.getKey();
				String value = ReflectUtil.getFieldValueByFieldName(chessConfig, key);
				// System.out.println("key=" + key + ", value=" + value);
				setProfileString(Config.get().getSection(key), key, value);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return chessConfig;
	}
}