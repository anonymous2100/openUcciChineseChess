package com.ctgu.view;

import com.ctgu.constant.ChessConstant;
import com.ctgu.enums.GameMode;
import com.ctgu.enums.Side;
import com.ctgu.event.EventMessage;
import com.ctgu.network.ChessClient;
import com.ctgu.network.NetMessage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;

/**
 * Room lobby: lists available rooms, lets the user create / join / spectate.
 * When the user successfully enters a room the dialog disposes and calls back to
 * ChessFrame which then configures ChessPanel for online mode.
 */
public class LobbyDialog extends JDialog
{
  private final ChessPanel  chessPanel;
  private final JTable      table;
  private final DefaultTableModel model;
  private final JLabel      lblInfo   = new JLabel(" ");

  // The room the player is currently waiting in (created but not started)
  private String  pendingRoomId = null;
  private Side    mySide        = null;

  public LobbyDialog(Frame owner, ChessPanel chessPanel)
  {
    super(owner, "在线大厅 — " + ChessClient.getInstance().getLoggedInUser(), false);
    this.chessPanel = chessPanel;
    EventBus.getDefault().register(this);

    model = new DefaultTableModel(
        new String[]{"房间名", "模式", "状态", "红方", "黑方", "观众", "ID"}, 0)
    {
      @Override
      public boolean isCellEditable(int r, int c)
      {
        return false;
      }
    };
    table = new JTable(model);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.getColumnModel().getColumn(6).setMinWidth(0);
    table.getColumnModel().getColumn(6).setMaxWidth(0); // hide ID column

    buildUI();
    setSize(720, 480);
    setLocationRelativeTo(owner);
    addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent e)
      {
        EventBus.getDefault().unregister(LobbyDialog.this);
      }
    });
    // Fetch rooms on open
    ChessClient.getInstance().send(NetMessage.getRooms());
  }

  private void buildUI()
  {
    JPanel root = new JPanel(new BorderLayout(6, 6));
    root.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

    // ── Title row ───────────────────────────────────────────────
    JLabel title = new JLabel("在线大厅  欢迎，" + ChessClient.getInstance().getLoggedInUser());
    title.setFont(new Font("微软雅黑", Font.BOLD, 16));
    root.add(title, BorderLayout.NORTH);

    // ── Table ───────────────────────────────────────────────────
    root.add(new JScrollPane(table), BorderLayout.CENTER);

    // ── Buttons ─────────────────────────────────────────────────
    JButton btnRefresh  = new JButton("刷新");
    JButton btnCreatePvP= new JButton("创建 PvP 房间");
    JButton btnCreateAI = new JButton("创建人机房间");
    JButton btnJoin     = new JButton("加入对战");
    JButton btnSpectate = new JButton("观战");
    JButton btnClose    = new JButton("关闭");

    JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    btnRow.add(btnRefresh);
    btnRow.add(btnCreatePvP);
    btnRow.add(btnCreateAI);
    btnRow.add(btnJoin);
    btnRow.add(btnSpectate);
    btnRow.add(btnClose);

    lblInfo.setForeground(new Color(0, 100, 180));
    JPanel south = new JPanel(new BorderLayout());
    south.add(btnRow,  BorderLayout.NORTH);
    south.add(lblInfo, BorderLayout.SOUTH);
    root.add(south, BorderLayout.SOUTH);

    setContentPane(root);

    btnRefresh.addActionListener(e ->
        ChessClient.getInstance().send(NetMessage.getRooms()));

    btnCreatePvP.addActionListener(e -> createRoom("pvp"));
    btnCreateAI.addActionListener(e  -> createRoom("ai"));

    btnJoin.addActionListener(e ->
    {
      String rid = selectedRoomId();
      if(rid == null)
      {
        setInfo("请先选择一个房间");
        return;
      }
      ChessClient.getInstance().send(NetMessage.joinRoom(rid));
    });

    btnSpectate.addActionListener(e ->
    {
      String rid = selectedRoomId();
      if(rid == null)
      {
        setInfo("请先选择一个房间");
        return;
      }
      ChessClient.getInstance().send(NetMessage.spectate(rid));
    });

    btnClose.addActionListener(e ->
    {
      EventBus.getDefault().unregister(this);
      dispose();
    });
  }

  private void createRoom(String mode)
  {
    String name = JOptionPane.showInputDialog(this,
        "请输入房间名称：", ChessClient.getInstance().getLoggedInUser() + "的房间");
    if(name == null)
    {
      return;
    }
    ChessClient.getInstance().send(NetMessage.createRoom(name.trim().isEmpty()
        ? ChessClient.getInstance().getLoggedInUser() + "的房间" : name.trim(), mode));
  }

  private String selectedRoomId()
  {
    int row = table.getSelectedRow();
    if(row < 0)
    {
      return null;
    }
    return (String)model.getValueAt(row, 6);
  }

  private void setInfo(String msg)
  {
    lblInfo.setText(msg);
  }

  // ── Room list helper ─────────────────────────────────────────────

  @SuppressWarnings("unchecked")
  private void updateTable(List<?> rooms)
  {
    model.setRowCount(0);
    if(rooms == null)
    {
      return;
    }
    for(Object obj : rooms)
    {
      if(!(obj instanceof Map))
      {
        continue;
      }
      Map<String, Object> r = (Map<String, Object>)obj;
      String modeRaw = str(r, "mode");
      String modeDisp = "ai".equals(modeRaw) ? "人机" : "PvP";
      String statusRaw = str(r, "status");
      String statusDisp = "playing".equals(statusRaw) ? "对战中" : "等待中";
      model.addRow(new Object[]{
          str(r, "name"),
          modeDisp,
          statusDisp,
          str(r, "redPlayer"),
          str(r, "blackPlayer"),
          (int)dbl(r, "spectatorCount"),
          str(r, "id")
      });
    }
  }

  private static String str(Map<String, Object> m, String k)
  {
    Object v = m.get(k);
    return v != null ? v.toString() : "";
  }

  private static double dbl(Map<String, Object> m, String k)
  {
    Object v = m.get(k);
    if(v == null)
    {
      return 0;
    }
    try
    {
      return Double.parseDouble(v.toString());
    }
    catch(NumberFormatException e)
    {
      return 0;
    }
  }

  // ── EventBus ─────────────────────────────────────────────────────

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onNetEvent(EventMessage evt)
  {
    if(!(evt.getObj() instanceof NetMessage))
    {
      return;
    }
    NetMessage msg = (NetMessage)evt.getObj();
    if(msg.getType() == null)
    {
      return;
    }
    switch(msg.getType())
    {
    case "room_list":
    {
      Object rooms = msg.get("rooms");
      if(rooms instanceof List)
      {
        updateTable((List<?>)rooms);
      }
      setInfo("已获取 " + model.getRowCount() + " 个房间");
      break;
    }
    case "room_created":
    {
      @SuppressWarnings("unchecked")
      Map<String, Object> room = (Map<String, Object>)msg.get("room");
      if(room != null)
      {
        pendingRoomId = str(room, "id");
        mySide = Side.Red;
        setInfo("房间「" + str(room, "name") + "」已创建，等待对手加入…");
        // Refresh room list
        ChessClient.getInstance().send(NetMessage.getRooms());
      }
      break;
    }
    case "room_joined":
    {
      @SuppressWarnings("unchecked")
      Map<String, Object> room = (Map<String, Object>)msg.get("room");
      String sideStr = msg.getString("side");
      if(room != null)
      {
        pendingRoomId = str(room, "id");
        mySide = "black".equals(sideStr) ? Side.Black : Side.Red;
        setInfo("已加入房间「" + str(room, "name") + "」，等待游戏开始…");
      }
      break;
    }
    case "game_start":
    {
      String rid = msg.getString("roomId");
      if(rid != null && mySide != null)
      {
        enterOnlineGame(rid, mySide, false);
      }
      break;
    }
    case "spectate_ok":
    {
      @SuppressWarnings("unchecked")
      Map<String, Object> room = (Map<String, Object>)msg.get("room");
      Object movesObj = msg.get("moves");
      String rid = room != null ? str(room, "id") : null;
      if(rid != null)
      {
        enterSpectateMode(rid, movesObj);
      }
      break;
    }
    case "room_update":
    {
      // Refresh list
      ChessClient.getInstance().send(NetMessage.getRooms());
      break;
    }
    case "room_err":
      setInfo("错误: " + msg.getString("msg"));
      break;
    default:
      break;
    }
  }

  // ── Launch game ──────────────────────────────────────────────────

  private void enterOnlineGame(String roomId, Side side, boolean asSpectator)
  {
    chessPanel.startOnlineGame(roomId, side, asSpectator, null);
    setInfo("游戏已开始！");
    EventBus.getDefault().unregister(this);
    dispose();
  }

  private void enterSpectateMode(String roomId, Object movesObj)
  {
    List<String> moves = null;
    if(movesObj instanceof List)
    {
      @SuppressWarnings("unchecked")
      List<String> ml = (List<String>)movesObj;
      moves = ml;
    }
    chessPanel.startOnlineGame(roomId, null, true, moves);
    setInfo("观战已开始！");
    EventBus.getDefault().unregister(this);
    dispose();
  }
}

