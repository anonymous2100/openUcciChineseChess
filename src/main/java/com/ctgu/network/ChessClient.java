package com.ctgu.network;

import com.ctgu.constant.ChessConstant;
import com.ctgu.event.EventMessage;
import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton client that maintains a TCP connection to the ChessServer.
 * Incoming messages are posted as {@link EventMessage} on the EventBus so that
 * any subscriber (ChessPanel, LobbyDialog, …) can react independently.
 */
public class ChessClient
{
  private static final Logger LOG = Logger.getLogger(ChessClient.class.getName());

  // Singleton
  private static ChessClient instance;

  public static synchronized ChessClient getInstance()
  {
    if(instance == null)
    {
      instance = new ChessClient();
    }
    return instance;
  }

  // ── State ────────────────────────────────────────────────────────
  private Socket     socket;
  private PrintWriter out;
  private String     loggedInUser;
  private volatile boolean connected = false;

  private ChessClient()
  {
  }

  // ── Connection ───────────────────────────────────────────────────

  /**
   * Connect (blocking, call from a background thread or use {@link #connectAsync}).
   */
  public void connect(String host, int port) throws IOException
  {
    socket = new Socket(host, port);
    out = new PrintWriter(
        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
    connected = true;

    Thread reader = new Thread(() ->
    {
      try(BufferedReader in = new BufferedReader(
          new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)))
      {
        String line;
        while(!socket.isClosed() && (line = in.readLine()) != null)
        {
          NetMessage msg = NetMessage.fromJson(line);
          if(msg != null)
          {
            dispatch(msg);
          }
        }
      }
      catch(IOException e)
      {
        LOG.fine("Disconnected from server: " + e.getMessage());
      }
      finally
      {
        connected = false;
        loggedInUser = null;
        EventBus.getDefault().post(
            new EventMessage(ChessConstant.MSG_NET_DISCONNECTED, "与服务器的连接已断开"));
      }
    });
    reader.setDaemon(true);
    reader.setName("chess-net-reader");
    reader.start();
  }

  /**
   * Connect asynchronously; posts {@link ChessConstant#MSG_NET_CONNECTED} on success
   * or {@link ChessConstant#MSG_NET_ERROR} on failure.
   */
  public void connectAsync(String host, int port)
  {
    Thread t = new Thread(() ->
    {
      try
      {
        connect(host, port);
        EventBus.getDefault().post(
            new EventMessage(ChessConstant.MSG_NET_CONNECTED, host + ":" + port));
      }
      catch(IOException e)
      {
        EventBus.getDefault().post(
            new EventMessage(ChessConstant.MSG_NET_ERROR, "连接失败: " + e.getMessage()));
      }
    });
    t.setDaemon(true);
    t.setName("chess-net-connect");
    t.start();
  }

  public void disconnect()
  {
    connected = false;
    try
    {
      if(socket != null && !socket.isClosed())
      {
        socket.close();
      }
    }
    catch(IOException ignored)
    {
    }
  }

  public boolean isConnected()
  {
    return connected && socket != null && !socket.isClosed();
  }

  public String getLoggedInUser()
  {
    return loggedInUser;
  }

  // ── Sending ──────────────────────────────────────────────────────

  public void send(NetMessage msg)
  {
    if(out != null && isConnected())
    {
      out.println(msg.toJson());
    }
  }

  // ── Dispatch incoming messages → EventBus ────────────────────────

  private void dispatch(NetMessage msg)
  {
    String type = msg.getType();
    if(type == null)
    {
      return;
    }
    int evtType;
    switch(type)
    {
    case MessageType.REG_OK:
    case MessageType.REG_ERR:
      evtType = ChessConstant.MSG_NET_REGISTER;
      break;
    case MessageType.LOGIN_OK:
      loggedInUser = msg.getString("username");
      evtType = ChessConstant.MSG_NET_LOGIN;
      break;
    case MessageType.LOGIN_ERR:
      evtType = ChessConstant.MSG_NET_LOGIN;
      break;
    case MessageType.ROOM_LIST:
      evtType = ChessConstant.MSG_NET_ROOM_LIST;
      break;
    case MessageType.ROOM_CREATED:
    case MessageType.ROOM_JOINED:
    case MessageType.SPECTATE_OK:
    case MessageType.ROOM_ERR:
    case MessageType.ROOM_UPDATE:
      evtType = ChessConstant.MSG_NET_ROOM_EVENT;
      break;
    case MessageType.GAME_START:
      evtType = ChessConstant.MSG_NET_GAME_START;
      break;
    case MessageType.OPP_MOVE:
      evtType = ChessConstant.MSG_NET_OPP_MOVE;
      break;
    case MessageType.CHAT_MSG:
      evtType = ChessConstant.MSG_NET_CHAT;
      break;
    case MessageType.GAME_OVER:
      evtType = ChessConstant.MSG_NET_GAME_OVER;
      break;
    case MessageType.PLAYER_LEFT:
      evtType = ChessConstant.MSG_NET_PLAYER_LEFT;
      break;
    default:
      evtType = ChessConstant.MSG_NET_ERROR;
      break;
    }
    EventBus.getDefault().post(new EventMessage(evtType, msg));
  }
}

