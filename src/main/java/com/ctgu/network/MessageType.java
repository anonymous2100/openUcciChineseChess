package com.ctgu.network;

/**
 * Network protocol message type constants.
 * Client→Server and Server→Client directions are both listed here.
 */
public final class MessageType
{
  private MessageType() {}

  // ── Client → Server ──────────────────────────────────────────────
  public static final String REGISTER     = "register";      // {username, password}
  public static final String LOGIN        = "login";         // {username, password}
  public static final String GET_ROOMS    = "get_rooms";     // {}
  public static final String CREATE_ROOM  = "create_room";   // {name, mode("pvp"/"ai")}
  public static final String JOIN_ROOM    = "join_room";     // {roomId}
  public static final String SPECTATE     = "spectate";      // {roomId}
  public static final String LEAVE_ROOM   = "leave_room";    // {roomId}
  public static final String MOVE         = "move";          // {roomId, move("e2e4")}
  public static final String CHAT         = "chat";          // {roomId, text}
  public static final String HEARTBEAT    = "heartbeat";     // {}

  // ── Server → Client ──────────────────────────────────────────────
  public static final String REG_OK       = "reg_ok";        // {}
  public static final String REG_ERR      = "reg_err";       // {msg}
  public static final String LOGIN_OK     = "login_ok";      // {username}
  public static final String LOGIN_ERR    = "login_err";     // {msg}
  public static final String ROOM_LIST    = "room_list";     // {rooms:[RoomInfo...]}
  public static final String ROOM_CREATED = "room_created";  // {room: RoomInfo}
  public static final String ROOM_JOINED  = "room_joined";   // {room: RoomInfo, side:"red"/"black"}
  public static final String SPECTATE_OK  = "spectate_ok";   // {room: RoomInfo, moves:[...]}
  public static final String ROOM_ERR     = "room_err";      // {msg}
  public static final String GAME_START   = "game_start";    // {roomId, redPlayer, blackPlayer}
  public static final String OPP_MOVE     = "opp_move";      // {roomId, move, fen}
  public static final String CHAT_MSG     = "chat_msg";      // {roomId, username, text}
  public static final String GAME_OVER    = "game_over";     // {roomId, reason, winner}
  public static final String PLAYER_LEFT  = "player_left";   // {roomId, username}
  public static final String ROOM_UPDATE  = "room_update";   // {room: RoomInfo}
  public static final String ERROR        = "error";         // {msg}
}

