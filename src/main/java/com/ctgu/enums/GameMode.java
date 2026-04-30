package com.ctgu.enums;

/**
 * 对战模式
 */
public enum GameMode
{
  HUMAN_VS_AI("人机对战"),
  AI_VS_AI("AI 对战"),
  HUMAN_VS_HUMAN("双人对战"),
  ONLINE_PVP("在线对战"),
  ONLINE_SPECTATE("在线观战");

  private final String displayName;

  GameMode(String displayName)
  {
    this.displayName = displayName;
  }

  public String getDisplayName()
  {
    return displayName;
  }

  @Override
  public String toString()
  {
    return displayName;
  }
}

