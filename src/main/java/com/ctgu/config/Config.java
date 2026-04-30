package com.ctgu.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ctgu.util.IniFileUtil;

public class Config
{
	private static Config instance = new Config();

	private static ChessConfig chessConfig;

	public static Map<String, List<String>> SECTION_MAP = new HashMap<>();

	static
	{
		List<String> uiList = Arrays.asList("board", "pieces", "coordinate");
		SECTION_MAP.put("ui", uiList);
		List<String> engineList = Arrays.asList("engine", "timeLimit");
		SECTION_MAP.put("engine", engineList);
		List<String> operateList = Arrays.asList("computerFirst", "playBgSound");
		SECTION_MAP.put("operate", operateList);

		chessConfig = IniFileUtil.readFileContent(IniFileUtil.getConfigFile().getAbsolutePath());
		if (chessConfig == null)
		{
			chessConfig = new ChessConfig(false, false, 500, 0, 2, 0, 0);
		}
	}

	private Config()
	{
	}

	public static Config get()
	{
		return instance;
	}

	public String getSection(String keyName)
	{
		for (Map.Entry<String, List<String>> entry : SECTION_MAP.entrySet())
		{
			String key = entry.getKey();
			List<String> valueList = entry.getValue();
			if (valueList.contains(keyName))
			{
				return key;
			}
		}
		return null;
	}

	public ChessConfig getChessConfig()
	{
		return chessConfig;
	}

	public boolean isPlayBgSound()
	{
		return getChessConfig().getPlayBgSound();
	}

	public void setPlayBgSound(boolean playBgSound)
	{
		getChessConfig().setPlayBgSound(playBgSound);
		IniFileUtil.updateFileContent(IniFileUtil.getConfigFile().getAbsolutePath(), getChessConfig());
	}

	public boolean isComputerFirst()
	{
		return getChessConfig().getComputerFirst();
	}

	public void setComputerFirst(boolean computerFirst)
	{
		getChessConfig().setComputerFirst(computerFirst);
		IniFileUtil.updateFileContent(IniFileUtil.getConfigFile().getAbsolutePath(), getChessConfig());
	}

	public int getTimeLimit()
	{
		return getChessConfig().getTimeLimit();
	}

	public void setTimeLimit(int timeLimit)
	{
		getChessConfig().setTimeLimit(timeLimit);
		IniFileUtil.updateFileContent(IniFileUtil.getConfigFile().getAbsolutePath(), getChessConfig());
	}

	public int getEngine()
	{
		return getChessConfig().getEngine();
	}

	public void setEngine(int engine)
	{
		getChessConfig().setEngine(engine);
		IniFileUtil.updateFileContent(IniFileUtil.getConfigFile().getAbsolutePath(), getChessConfig());
	}

	public int getCoordinate()
	{
		return getChessConfig().getCoordinate();
	}

	public void setCoordinate(int coordinate)
	{
		getChessConfig().setCoordinate(coordinate);
		IniFileUtil.updateFileContent(IniFileUtil.getConfigFile().getAbsolutePath(), getChessConfig());
	}

	public int getPieces()
	{
		return getChessConfig().getPieces();
	}

	public void setPieces(int pieces)
	{
		getChessConfig().setPieces(pieces);
		IniFileUtil.updateFileContent(IniFileUtil.getConfigFile().getAbsolutePath(), getChessConfig());
	}

	public int getBoard()
	{
		return getChessConfig().getBoard();
	}

	public void setBoard(int board)
	{
		getChessConfig().setBoard(board);
		IniFileUtil.updateFileContent(IniFileUtil.getConfigFile().getAbsolutePath(), getChessConfig());
	}
}
