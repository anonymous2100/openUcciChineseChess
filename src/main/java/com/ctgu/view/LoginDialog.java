package com.ctgu.view;

import com.ctgu.constant.ChessConstant;
import com.ctgu.event.EventMessage;
import com.ctgu.network.ChessClient;
import com.ctgu.network.ChessServer;
import com.ctgu.network.NetMessage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Modal dialog for server address input, user login and registration.
 * After successful login the dialog disposes and the caller can proceed.
 */
public class LoginDialog extends JDialog
{
  private static final int DEFAULT_PORT = ChessServer.DEFAULT_PORT;

  private final JTextField  tfServer   = new JTextField("127.0.0.1", 14);
  private final JTextField  tfPort     = new JTextField(String.valueOf(DEFAULT_PORT), 6);
  private final JTextField  tfUser     = new JTextField(12);
  private final JPasswordField tfPass  = new JPasswordField(12);
  private final JButton     btnLogin   = new JButton("登录");
  private final JButton     btnReg     = new JButton("注册");
  private final JButton     btnServer  = new JButton("启动本地服务器");
  private final JLabel      lblStatus  = new JLabel(" ");

  private boolean loggedIn = false;
  private ChessServer embeddedServer = null;

  public LoginDialog(Frame owner)
  {
    super(owner, "弈心象棋 — 联网登录", true);
    EventBus.getDefault().register(this);
    buildUI();
    pack();
    setResizable(false);
    setLocationRelativeTo(owner);
    addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent e)
      {
        EventBus.getDefault().unregister(LoginDialog.this);
      }
    });
  }

  private void buildUI()
  {
    JPanel root = new JPanel(new BorderLayout(8, 8));
    root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

    // ── Title ───────────────────────────────────────────────────
    JLabel title = new JLabel("弈心象棋", SwingConstants.CENTER);
    title.setFont(new Font("微软雅黑", Font.BOLD, 22));
    root.add(title, BorderLayout.NORTH);

    // ── Form ────────────────────────────────────────────────────
    JPanel form = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(5, 5, 5, 5);
    c.fill   = GridBagConstraints.HORIZONTAL;

    addRow(form, c, 0, "服务器地址:", tfServer);
    addRow(form, c, 1, "端口:", tfPort);
    addRow(form, c, 2, "用户名:", tfUser);
    addRow(form, c, 3, "密码:", tfPass);
    root.add(form, BorderLayout.CENTER);

    // ── Buttons ─────────────────────────────────────────────────
    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
    btnPanel.add(btnLogin);
    btnPanel.add(btnReg);
    btnPanel.add(btnServer);

    lblStatus.setForeground(new Color(180, 40, 40));
    lblStatus.setHorizontalAlignment(SwingConstants.CENTER);

    JPanel south = new JPanel(new BorderLayout());
    south.add(btnPanel, BorderLayout.NORTH);
    south.add(lblStatus, BorderLayout.SOUTH);
    root.add(south, BorderLayout.SOUTH);

    setContentPane(root);

    // ── Listeners ────────────────────────────────────────────────
    btnLogin.addActionListener(e -> doLogin());
    btnReg.addActionListener(e -> doRegister());
    btnServer.addActionListener(e -> toggleServer());
    getRootPane().setDefaultButton(btnLogin);
  }

  private static void addRow(JPanel p, GridBagConstraints c, int row, String label, JComponent field)
  {
    c.gridx = 0; c.gridy = row; c.weightx = 0;
    p.add(new JLabel(label), c);
    c.gridx = 1; c.weightx = 1;
    p.add(field, c);
  }

  // ── Actions ──────────────────────────────────────────────────────

  private void doLogin()
  {
    String user = tfUser.getText().trim();
    String pass = new String(tfPass.getPassword());
    if(user.isEmpty() || pass.isEmpty())
    {
      setStatus("用户名和密码不能为空", true);
      return;
    }
    ensureConnected(() -> ChessClient.getInstance().send(NetMessage.login(user, pass)));
  }

  private void doRegister()
  {
    String user = tfUser.getText().trim();
    String pass = new String(tfPass.getPassword());
    if(user.isEmpty() || pass.isEmpty())
    {
      setStatus("用户名和密码不能为空", true);
      return;
    }
    ensureConnected(() -> ChessClient.getInstance().send(NetMessage.register(user, pass)));
  }

  private void ensureConnected(Runnable afterConnect)
  {
    ChessClient client = ChessClient.getInstance();
    if(client.isConnected())
    {
      afterConnect.run();
      return;
    }
    String host = tfServer.getText().trim();
    int port;
    try
    {
      port = Integer.parseInt(tfPort.getText().trim());
    }
    catch(NumberFormatException ex)
    {
      setStatus("端口号格式错误", true);
      return;
    }
    setStatus("正在连接 " + host + ":" + port + " …", false);
    btnLogin.setEnabled(false);
    btnReg.setEnabled(false);
    final Runnable ar = afterConnect;
    // Connect asynchronously; listener will call afterConnect on success via MSG_NET_CONNECTED
    new Thread(() ->
    {
      try
      {
        client.connect(host, port);
        SwingUtilities.invokeLater(() ->
        {
          btnLogin.setEnabled(true);
          btnReg.setEnabled(true);
          setStatus("已连接，请登录", false);
          ar.run();
        });
      }
      catch(Exception ex)
      {
        SwingUtilities.invokeLater(() ->
        {
          btnLogin.setEnabled(true);
          btnReg.setEnabled(true);
          setStatus("连接失败: " + ex.getMessage(), true);
        });
      }
    }).start();
  }

  private void toggleServer()
  {
    if(embeddedServer != null && embeddedServer.isRunning())
    {
      embeddedServer.stop();
      embeddedServer = null;
      btnServer.setText("启动本地服务器");
      setStatus("本地服务器已停止", false);
    }
    else
    {
      int port;
      try
      {
        port = Integer.parseInt(tfPort.getText().trim());
      }
      catch(NumberFormatException ex)
      {
        setStatus("端口格式错误", true);
        return;
      }
      embeddedServer = new ChessServer();
      embeddedServer.startAsync(port);
      btnServer.setText("停止本地服务器");
      tfServer.setText("127.0.0.1");
      setStatus("本地服务器已启动，端口 " + port, false);
    }
  }

  private void setStatus(String msg, boolean error)
  {
    lblStatus.setForeground(error ? new Color(180, 40, 40) : new Color(0, 120, 0));
    lblStatus.setText(msg);
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
    String type = msg.getType();
    if(type == null)
    {
      return;
    }

    switch(type)
    {
    case "login_ok":
      setStatus("登录成功，欢迎 " + msg.getString("username") + "！", false);
      loggedIn = true;
      EventBus.getDefault().unregister(this);
      // Close after brief pause so user can see the message
      Timer t = new Timer(600, e -> dispose());
      t.setRepeats(false);
      t.start();
      break;
    case "login_err":
      setStatus("登录失败: " + msg.getString("msg"), true);
      break;
    case "reg_ok":
      setStatus("注册成功！请登录。", false);
      break;
    case "reg_err":
      setStatus("注册失败: " + msg.getString("msg"), true);
      break;
    case "error":
      setStatus("错误: " + msg.getString("msg"), true);
      break;
    default:
      break;
    }
  }

  // ── Accessor ─────────────────────────────────────────────────────

  public boolean isLoggedIn()
  {
    return loggedIn;
  }

  public ChessServer getEmbeddedServer()
  {
    return embeddedServer;
  }
}

