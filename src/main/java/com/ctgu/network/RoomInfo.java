package com.ctgu.network;

/**
 * Lightweight room descriptor sent in room-list responses.
 */
public class RoomInfo
{
  private String id;
  private String name;
  private String mode;         // "pvp" or "ai"
  private String status;       // "waiting" | "playing" | "finished"
  private String redPlayer;
  private String blackPlayer;
  private int    spectatorCount;

  public RoomInfo()
  {
  }

  public RoomInfo(String id, String name, String mode)
  {
    this.id = id;
    this.name = name;
    this.mode = mode;
    this.status = "waiting";
  }

  public String getId()                       { return id; }
  public void setId(String id)               { this.id = id; }
  public String getName()                     { return name; }
  public void setName(String name)           { this.name = name; }
  public String getMode()                     { return mode; }
  public void setMode(String mode)           { this.mode = mode; }
  public String getStatus()                   { return status; }
  public void setStatus(String status)       { this.status = status; }
  public String getRedPlayer()               { return redPlayer; }
  public void setRedPlayer(String p)         { this.redPlayer = p; }
  public String getBlackPlayer()             { return blackPlayer; }
  public void setBlackPlayer(String p)       { this.blackPlayer = p; }
  public int getSpectatorCount()             { return spectatorCount; }
  public void setSpectatorCount(int n)       { this.spectatorCount = n; }

  @Override
  public String toString()
  {
    return String.format("[%s] %s (%s) 红:%s 黑:%s 观:%d",
        status, name, mode,
        redPlayer   != null ? redPlayer   : "空",
        blackPlayer != null ? blackPlayer : "空",
        spectatorCount);
  }
}

