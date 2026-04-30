package com.ctgu.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic network message wrapper: {"type":"...", "data":{...}}
 */
public class NetMessage
{
  private static final Gson GSON = new GsonBuilder().create();
  private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

  private String type;
  private Map<String, Object> data = new HashMap<>();

  public NetMessage() {}
  public NetMessage(String type) { this.type = type; }

  public NetMessage put(String key, Object value) { data.put(key, value); return this; }

  public String getString(String key) { Object v = data.get(key); return v != null ? v.toString() : null; }

  public int getInt(String key, int def)
  {
    Object v = data.get(key);
    if(v == null) return def;
    try { return (int)Double.parseDouble(v.toString()); }
    catch(NumberFormatException ignored) { return def; }
  }

  public Object get(String key) { return data.get(key); }

  public String toJson()
  {
    Map<String, Object> envelope = new HashMap<>();
    envelope.put("type", type);
    envelope.put("data", data);
    return GSON.toJson(envelope);
  }

  public static NetMessage fromJson(String json)
  {
    try
    {
      Map<String, Object> envelope = GSON.fromJson(json, MAP_TYPE);
      NetMessage msg = new NetMessage();
      msg.type = (String)envelope.get("type");
      Object d = envelope.get("data");
      if(d instanceof Map)
      {
        @SuppressWarnings("unchecked") Map<String, Object> dm = (Map<String, Object>)d;
        msg.data = dm;
      }
      return msg;
    }
    catch(Exception e) { return null; }
  }

  public static NetMessage of(String type) { return new NetMessage(type); }
  public static NetMessage login(String u, String p) { return of(MessageType.LOGIN).put("username", u).put("password", p); }
  public static NetMessage register(String u, String p) { return of(MessageType.REGISTER).put("username", u).put("password", p); }
  public static NetMessage createRoom(String name, String mode) { return of(MessageType.CREATE_ROOM).put("name", name).put("mode", mode); }
  public static NetMessage joinRoom(String roomId) { return of(MessageType.JOIN_ROOM).put("roomId", roomId); }
  public static NetMessage spectate(String roomId) { return of(MessageType.SPECTATE).put("roomId", roomId); }
  public static NetMessage move(String roomId, String moveName) { return of(MessageType.MOVE).put("roomId", roomId).put("move", moveName); }
  public static NetMessage chat(String roomId, String text) { return of(MessageType.CHAT).put("roomId", roomId).put("text", text); }
  public static NetMessage leaveRoom(String roomId) { return of(MessageType.LEAVE_ROOM).put("roomId", roomId); }
  public static NetMessage getRooms() { return of(MessageType.GET_ROOMS); }

  public String getType() { return type; }
  public void setType(String type) { this.type = type; }
  public Map<String, Object> getData() { return data; }

  @Override
  public String toString() { return toJson(); }
}

