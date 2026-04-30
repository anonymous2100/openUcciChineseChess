package com.ctgu.network;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Embedded chess server.
 * Supports user register/login, room creation, PvP / spectating, and chat (danmaku).
 * Start via {@link #startAsync(int)} from the EDT; shutdown via {@link #stop()}.
 */
public class ChessServer
{
  public static final int DEFAULT_PORT = 9527;
  private static final Logger LOG = Logger.getLogger(ChessServer.class.getName());
  private static final Gson GSON = new Gson();

  // username -> SHA-256(password)
  private final Map<String, String> userStore  = new ConcurrentHashMap<>();
  // username -> session
  private final Map<String, ClientSession> sessions = new ConcurrentHashMap<>();
  // roomId -> room
  private final Map<String, GameRoom> rooms     = new ConcurrentHashMap<>();

  private ServerSocket serverSocket;
  private volatile boolean running = false;

  // ── Public API ───────────────────────────────────────────────────

  public void startAsync(int port)
  {
    Thread t = new Thread(() ->
    {
      try
      {
        serverSocket = new ServerSocket(port);
        running = true;
        LOG.info("Chess server started on port " + port);
        while(running)
        {
          try
          {
            Socket client = serverSocket.accept();
            new ClientSession(client).start();
          }
          catch(IOException e)
          {
            if(running)
            {
              LOG.log(Level.WARNING, "Accept error", e);
            }
          }
        }
      }
      catch(IOException e)
      {
        LOG.log(Level.SEVERE, "Cannot start server on port " + port, e);
      }
    });
    t.setDaemon(true);
    t.setName("chess-server-accept");
    t.start();
  }

  public void stop()
  {
    running = false;
    try
    {
      if(serverSocket != null && !serverSocket.isClosed())
      {
        serverSocket.close();
      }
    }
    catch(IOException ignored)
    {
    }
  }

  public boolean isRunning()
  {
    return running;
  }

  // ── Inner: per-client session ────────────────────────────────────

  private class ClientSession extends Thread
  {
    private final Socket socket;
    private PrintWriter out;
    String username = null;
    String roomId   = null;

    ClientSession(Socket socket)
    {
      this.socket = socket;
      setDaemon(true);
      setName("chess-client-" + socket.getRemoteSocketAddress());
    }

    @Override
    public void run()
    {
      try(BufferedReader in = new BufferedReader(
          new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)))
      {
        out = new PrintWriter(
            new java.io.OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

        String line;
        while(!socket.isClosed() && (line = in.readLine()) != null)
        {
          NetMessage msg = NetMessage.fromJson(line);
          if(msg != null)
          {
            handle(msg);
          }
        }
      }
      catch(IOException e)
      {
        LOG.fine("Client disconnected: " + (username != null ? username : socket.getRemoteSocketAddress()));
      }
      finally
      {
        onDisconnect();
      }
    }

    void send(NetMessage msg)
    {
      if(out != null && !socket.isClosed())
      {
        out.println(msg.toJson());
      }
    }

    // ── Message dispatch ─────────────────────────────────────────

    private void handle(NetMessage msg)
    {
      switch(msg.getType())
      {
      case MessageType.REGISTER:
        handleRegister(msg);
        break;
      case MessageType.LOGIN:
        handleLogin(msg);
        break;
      case MessageType.GET_ROOMS:
        handleGetRooms();
        break;
      case MessageType.CREATE_ROOM:
        handleCreateRoom(msg);
        break;
      case MessageType.JOIN_ROOM:
        handleJoinRoom(msg);
        break;
      case MessageType.SPECTATE:
        handleSpectate(msg);
        break;
      case MessageType.MOVE:
        handleMove(msg);
        break;
      case MessageType.CHAT:
        handleChat(msg);
        break;
      case MessageType.LEAVE_ROOM:
        handleLeaveRoom();
        break;
      case MessageType.HEARTBEAT:
        break; // no response needed
      default:
        send(NetMessage.of(MessageType.ERROR).put("msg", "Unknown type: " + msg.getType()));
        break;
      }
    }

    // ── Handlers ─────────────────────────────────────────────────

    private void handleRegister(NetMessage msg)
    {
      String u = msg.getString("username");
      String p = msg.getString("password");
      if(u == null || u.trim().isEmpty() || p == null || p.isEmpty())
      {
        send(NetMessage.of(MessageType.REG_ERR).put("msg", "用户名或密码不能为空"));
        return;
      }
      u = u.trim();
      if(u.length() < 2 || u.length() > 16)
      {
        send(NetMessage.of(MessageType.REG_ERR).put("msg", "用户名长度须在 2~16 之间"));
        return;
      }
      if(userStore.containsKey(u))
      {
        send(NetMessage.of(MessageType.REG_ERR).put("msg", "用户名已存在"));
        return;
      }
      userStore.put(u, sha256(p));
      send(NetMessage.of(MessageType.REG_OK));
    }

    private void handleLogin(NetMessage msg)
    {
      String u = msg.getString("username");
      String p = msg.getString("password");
      if(u == null || p == null)
      {
        send(NetMessage.of(MessageType.LOGIN_ERR).put("msg", "参数缺失"));
        return;
      }
      u = u.trim();
      String stored = userStore.get(u);
      if(stored == null || !stored.equals(sha256(p)))
      {
        send(NetMessage.of(MessageType.LOGIN_ERR).put("msg", "用户名或密码错误"));
        return;
      }
      if(sessions.containsKey(u))
      {
        // Kick old session
        ClientSession old = sessions.get(u);
        old.onDisconnect();
      }
      username = u;
      sessions.put(u, this);
      send(NetMessage.of(MessageType.LOGIN_OK).put("username", u));
    }

    private void handleGetRooms()
    {
      if(!checkLoggedIn())
      {
        return;
      }
      List<Map<String, Object>> list = new ArrayList<>();
      for(GameRoom r : rooms.values())
      {
        list.add(roomToMap(r));
      }
      send(NetMessage.of(MessageType.ROOM_LIST).put("rooms", list));
    }

    private void handleCreateRoom(NetMessage msg)
    {
      if(!checkLoggedIn())
      {
        return;
      }
      String name = msg.getString("name");
      String mode = msg.getString("mode");
      if(name == null || name.trim().isEmpty())
      {
        name = username + "的房间";
      }
      if(!"ai".equals(mode))
      {
        mode = "pvp";
      }
      String id = UUID.randomUUID().toString().substring(0, 8);
      GameRoom room = new GameRoom(id, name.trim(), mode, username);
      rooms.put(id, room);
      roomId = id;
      send(NetMessage.of(MessageType.ROOM_CREATED).put("room", roomToMap(room)));
      broadcastRoomUpdate(room);
    }

    private void handleJoinRoom(NetMessage msg)
    {
      if(!checkLoggedIn())
      {
        return;
      }
      String rid = msg.getString("roomId");
      GameRoom room = rooms.get(rid);
      if(room == null)
      {
        send(NetMessage.of(MessageType.ROOM_ERR).put("msg", "房间不存在"));
        return;
      }
      if(!"waiting".equals(room.status))
      {
        send(NetMessage.of(MessageType.ROOM_ERR).put("msg", "房间游戏已开始或已结束"));
        return;
      }
      if(username.equals(room.redPlayer))
      {
        send(NetMessage.of(MessageType.ROOM_ERR).put("msg", "不能加入自己创建的房间"));
        return;
      }
      room.blackPlayer = username;
      room.status = "playing";
      roomId = rid;
      // Notify joiner: they play Black
      send(NetMessage.of(MessageType.ROOM_JOINED)
          .put("room", roomToMap(room))
          .put("side", "black"));
      // Notify host: opponent joined, they play Red
      ClientSession host = sessions.get(room.redPlayer);
      if(host != null)
      {
        host.send(NetMessage.of(MessageType.GAME_START)
            .put("roomId", rid)
            .put("redPlayer", room.redPlayer)
            .put("blackPlayer", room.blackPlayer));
      }
      // Send game_start to guest as well
      send(NetMessage.of(MessageType.GAME_START)
          .put("roomId", rid)
          .put("redPlayer", room.redPlayer)
          .put("blackPlayer", room.blackPlayer));
      broadcastRoomUpdate(room);
    }

    private void handleSpectate(NetMessage msg)
    {
      if(!checkLoggedIn())
      {
        return;
      }
      String rid = msg.getString("roomId");
      GameRoom room = rooms.get(rid);
      if(room == null)
      {
        send(NetMessage.of(MessageType.ROOM_ERR).put("msg", "房间不存在"));
        return;
      }
      room.spectators.add(username);
      roomId = rid;
      send(NetMessage.of(MessageType.SPECTATE_OK)
          .put("room", roomToMap(room))
          .put("moves", new ArrayList<>(room.moveHistory)));
      broadcastRoomUpdate(room);
    }

    private void handleMove(NetMessage msg)
    {
      if(!checkLoggedIn())
      {
        return;
      }
      String rid = msg.getString("roomId");
      String moveName = msg.getString("move");
      GameRoom room = rooms.get(rid);
      if(room == null || moveName == null)
      {
        return;
      }
      // Validate it's this player's turn
      int moveCount = room.moveHistory.size();
      boolean isRedTurn = (moveCount % 2 == 0);
      boolean isRed   = username.equals(room.redPlayer);
      boolean isBlack = username.equals(room.blackPlayer);
      if((isRedTurn && !isRed) || (!isRedTurn && !isBlack))
      {
        send(NetMessage.of(MessageType.ERROR).put("msg", "不是您的回合"));
        return;
      }
      room.moveHistory.add(moveName);
      // Relay to opponent
      String opponentName = isRed ? room.blackPlayer : room.redPlayer;
      ClientSession opponent = sessions.get(opponentName);
      if(opponent != null)
      {
        opponent.send(NetMessage.of(MessageType.OPP_MOVE)
            .put("roomId", rid).put("move", moveName));
      }
      // Relay to spectators
      for(String spec : new ArrayList<>(room.spectators))
      {
        ClientSession s = sessions.get(spec);
        if(s != null)
        {
          s.send(NetMessage.of(MessageType.OPP_MOVE)
              .put("roomId", rid).put("move", moveName)
              .put("byPlayer", username));
        }
      }
    }

    private void handleChat(NetMessage msg)
    {
      if(!checkLoggedIn())
      {
        return;
      }
      String rid = msg.getString("roomId");
      String text = msg.getString("text");
      if(rid == null || text == null || text.trim().isEmpty())
      {
        return;
      }
      GameRoom room = rooms.get(rid);
      if(room == null)
      {
        return;
      }
      NetMessage chatMsg = NetMessage.of(MessageType.CHAT_MSG)
          .put("roomId", rid)
          .put("username", username)
          .put("text", text.trim());
      // Broadcast to everyone in the room
      broadcastToRoom(room, chatMsg, null);
    }

    private void handleLeaveRoom()
    {
      if(roomId == null)
      {
        return;
      }
      GameRoom room = rooms.get(roomId);
      if(room != null)
      {
        leaveRoomInternal(room);
      }
      roomId = null;
    }

    // ── Helpers ──────────────────────────────────────────────────

    private boolean checkLoggedIn()
    {
      if(username == null)
      {
        send(NetMessage.of(MessageType.ERROR).put("msg", "请先登录"));
        return false;
      }
      return true;
    }

    private void leaveRoomInternal(GameRoom room)
    {
      boolean wasPlayer = username.equals(room.redPlayer) || username.equals(room.blackPlayer);
      room.spectators.remove(username);
      if(wasPlayer)
      {
        // Notify opponent
        String opponentName = username.equals(room.redPlayer) ? room.blackPlayer : room.redPlayer;
        if(opponentName != null)
        {
          ClientSession opp = sessions.get(opponentName);
          if(opp != null)
          {
            opp.send(NetMessage.of(MessageType.PLAYER_LEFT)
                .put("roomId", room.id).put("username", username));
          }
        }
        // Remove room if host left (or mark finished)
        if(username.equals(room.redPlayer))
        {
          rooms.remove(room.id);
          broadcastAll(NetMessage.of(MessageType.ROOM_UPDATE).put("removed", room.id));
          return;
        }
        else
        {
          room.blackPlayer = null;
          room.status = "waiting";
        }
      }
      broadcastRoomUpdate(room);
    }

    private void onDisconnect()
    {
      if(username != null)
      {
        sessions.remove(username);
        if(roomId != null)
        {
          GameRoom room = rooms.get(roomId);
          if(room != null)
          {
            leaveRoomInternal(room);
          }
        }
      }
      try
      {
        socket.close();
      }
      catch(IOException ignored)
      {
      }
    }
  }

  // ── Broadcast helpers ────────────────────────────────────────────

  private void broadcastToRoom(GameRoom room, NetMessage msg, String except)
  {
    List<String> recipients = new ArrayList<>();
    if(room.redPlayer   != null)
    {
      recipients.add(room.redPlayer);
    }
    if(room.blackPlayer != null)
    {
      recipients.add(room.blackPlayer);
    }
    recipients.addAll(room.spectators);
    for(String u : recipients)
    {
      if(u.equals(except))
      {
        continue;
      }
      ClientSession s = sessions.get(u);
      if(s != null)
      {
        s.send(msg);
      }
    }
  }

  private void broadcastRoomUpdate(GameRoom room)
  {
    NetMessage upd = NetMessage.of(MessageType.ROOM_UPDATE).put("room", roomToMap(room));
    for(ClientSession s : sessions.values())
    {
      s.send(upd);
    }
  }

  private void broadcastAll(NetMessage msg)
  {
    for(ClientSession s : sessions.values())
    {
      s.send(msg);
    }
  }

  // ── Inner: GameRoom ──────────────────────────────────────────────

  static class GameRoom
  {
    String id;
    String name;
    String mode;          // "pvp" | "ai"
    String status;        // "waiting" | "playing" | "finished"
    String redPlayer;
    String blackPlayer;
    List<String> spectators   = Collections.synchronizedList(new ArrayList<>());
    List<String> moveHistory  = Collections.synchronizedList(new ArrayList<>());

    GameRoom(String id, String name, String mode, String host)
    {
      this.id        = id;
      this.name      = name;
      this.mode      = mode;
      this.status    = "waiting";
      this.redPlayer = host;
    }
  }

  // ── Utility ──────────────────────────────────────────────────────

  private static Map<String, Object> roomToMap(GameRoom r)
  {
    Map<String, Object> m = new java.util.LinkedHashMap<>();
    m.put("id",             r.id);
    m.put("name",           r.name);
    m.put("mode",           r.mode);
    m.put("status",         r.status);
    m.put("redPlayer",      r.redPlayer   != null ? r.redPlayer   : "");
    m.put("blackPlayer",    r.blackPlayer != null ? r.blackPlayer : "");
    m.put("spectatorCount", r.spectators.size());
    return m;
  }

  private static String sha256(String input)
  {
    try
    {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for(byte b : digest)
      {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    }
    catch(Exception e)
    {
      return input; // fallback (shouldn't happen)
    }
  }
}

