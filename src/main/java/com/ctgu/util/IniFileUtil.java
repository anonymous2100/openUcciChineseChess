package com.ctgu.util;

import com.ctgu.config.ChessConfig;
import com.ctgu.config.Config;
import com.ctgu.model.IniFileEntity;
import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IniFileUtil
{
  public static File configFile;

  private static final Logger logger = Logger.getLogger(IniFileUtil.class.getName());

  /**
   * 返回工作目录，尾部带 {@link File#separatorChar}。
   * 引擎等需要实际文件系统路径的地方继续使用此方法。
   */
  public static String getBasePath()
  {
    return ensureTrailingSlash(System.getProperty("user.dir"));
  }

  /**
   * 返回可写的配置文件 {@code {user.dir}/chessConfig.ini}。
   * 首次调用时若文件不存在，会从类路径资源 {@code /chessConfig.ini} 复制过来。
   */
  public static File getConfigFile()
  {
    File localFile = new File(getBasePath() + "chessConfig.ini");
    if (!localFile.exists())
    {
      try (InputStream in = IniFileUtil.class.getResourceAsStream("/chessConfig.ini"))
      {
        if (in != null)
        {
          Files.createDirectories(localFile.getParentFile().toPath());
          Files.copy(in, localFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
          logger.info("已从类路径复制 chessConfig.ini 到: " + localFile.getAbsolutePath());
        }
        else
        {
          logger.warning("类路径中未找到 /chessConfig.ini，使用默认配置");
        }
      }
      catch (IOException e)
      {
        logger.log(Level.WARNING, "复制 chessConfig.ini 失败", e);
      }
    }
    configFile = localFile;
    return configFile;
  }

  private static String ensureTrailingSlash(String path)
  {
    return (path != null && !path.endsWith(File.separator)) ? path + File.separator : path;
  }

  public static boolean creatIniFile(String filePath, List<IniFileEntity> fileContentList) throws IOException
  {
    File file = new File(filePath);
    if(file.exists())
    {
      return false;
    }
    file.createNewFile();
    Ini ini = new Ini();
    ini.load(file);
    for(IniFileEntity entity : fileContentList)
    {
      ini.add(entity.getSection(), entity.getKey(), entity.getValue() == null ? "" : entity.getValue());
    }
    ini.store(file);
    return true;
  }

  public static Map<String, String> getAllPropertyValue(String filename) throws IOException
  {
    Ini ini = new Ini(new File(filename));
    Map<String, String> propertyMap = new HashMap<>();
    for(Ini.Section section : ini.values())
    {
      for(Map.Entry<String, String> entry : section.entrySet())
      {
        propertyMap.put(entry.getKey(), entry.getValue());
      }
    }
    return propertyMap;
  }

  public static ChessConfig readFileContent(String filename)
  {
    try
    {
      ChessConfig chessConfig = new ChessConfig();
      Ini ini = new Ini(new File(filename));
      for(Ini.Section section : ini.values())
      {
        for(Map.Entry<String, String> entry : section.entrySet())
        {
          ReflectUtil.setFieldValueByFieldName(chessConfig, entry.getKey(), entry.getValue());
        }
      }
      return chessConfig;
    }
    catch(IOException e)
    {
      logger.log(Level.WARNING, "Failed to read chess config: " + filename + ", using defaults", e);
      return null;
    }
  }

  public static String getProfileString(String section, String variable, String defaultValue) throws IOException
  {
    Ini ini = new Ini(getConfigFile());
    Ini.Section sec = ini.get(section);
    if(sec == null)
    {
      return defaultValue;
    }
    String val = sec.get(variable);
    return val == null ? defaultValue : val;
  }

  public static boolean setProfileString(String section, String variable, String value) throws IOException
  {
    Ini ini = new Ini(getConfigFile());
    Ini.Section sec = ini.get(section);
    if(sec == null)
    {
      sec = ini.add(section);
    }
    sec.put(variable, value == null ? "" : value);
    ini.store(getConfigFile());
    return true;
  }

  public static ChessConfig updateFileContent(String filePath, ChessConfig chessConfig)
  {
    try
    {
      Map<String, String> propertyMap = getAllPropertyValue(filePath);
      for(Map.Entry<String, String> entry : propertyMap.entrySet())
      {
        String key = entry.getKey();
        String value = ReflectUtil.getFieldValueByFieldName(chessConfig, key);
        setProfileString(Config.get().getSection(key), key, value == null ? "" : value);
      }
    }
    catch(IOException e)
    {
      logger.log(Level.WARNING, "Failed to update config file content: " + filePath, e);
    }

    return chessConfig;
  }
}
