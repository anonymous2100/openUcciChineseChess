package com.ctgu.view;

import com.ctgu.config.Config;
import com.ctgu.constant.ChessConstant;
import com.ctgu.controller.ChessController;
import com.ctgu.controller.ChessRules;
import com.ctgu.engine.LocalAiEngine;
import com.ctgu.engine.UcciEngine;
import com.ctgu.enums.*;
import com.ctgu.event.EventMessage;
import com.ctgu.model.AnalysisResult;
import com.ctgu.model.Move;
import com.ctgu.model.Position;
import com.ctgu.network.ChessClient;
import com.ctgu.network.NetMessage;
import com.ctgu.util.*;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ChessPanel extends JPanel
{
  private static final long serialVersionUID = 6672024539300490002L;

  // ── 右侧面板宽度 ────────────────────────────────────────────────
  private static final int RIGHT_PANEL_WIDTH = 560;

  private JTabbedPane tabbedPane;
  private JScrollPane chessHistoryPane;
  private JTextArea stepTextArea;
  private JScrollPane engineInfoPane;
  private JTextArea engineTextArea;
  // 操作按钮
  private JButton btnNewGame;
  private JButton btnSave;
  private JButton btnRetract;
  private JButton btnCopy;
  private JButton btnAnalyze;   // 局势分析
  private JButton btnHint;      // 走法提示
  // 设置下拉
  private JComboBox<String> btnChangeBoard;
  private JComboBox<String> btnChangePiece;
  private JComboBox<String> btnChangeCoordinate;
  private JComboBox<String> btnPaste;
  // 分析结果面板
  private JTextArea analysisTextArea;

  private BufferedImage imgBoard;
  private boolean diverted;
  private int from;
  private int to;
  // 走法提示高亮
  private int hintFrom = -1;
  private int hintTo   = -1;

  private Piece[] posArray = new Piece[90];
  private BufferedImage[] pieceImageArray;
  private ChessController controller;
  private int fontWidth = 0;
  private int fontHeight = 0;

  // ── 对战模式 / 本地 AI ──────────────────────────────────────────────
  private GameMode gameMode = GameMode.HUMAN_VS_AI;
  // 默认与 UI 中 btnAiType.setSelectedIndex(1)（"本地 AI"）保持一致
  private boolean useLocalAi = true;
  private LocalAiEngine localAiEngine = new LocalAiEngine(3);
  private JComboBox<String> btnGameMode;
  private JComboBox<String> btnAiDepth;
  private JComboBox<String> btnAiType;

  // ── AI 讲解面板 ──────────────────────────────────────────────────────
  private JTextArea aiCommentaryArea;
  private JTextArea opponentCommentaryArea;

  // ── 棋子动画 ─────────────────────────────────────────────────────────
  private volatile boolean animating = false;
  private int animToPos = -1;
  private BufferedImage animPieceImg = null;
  private float animProgress = 0f;
  private int animFromPx, animFromPy;
  private int animToPx, animToPy;
  private Timer animTimer = null;

  // ── 将军高亮（被将的将/帅所在格子，-1=无将） ────────────────────────
  private int checkedKingPos = -1;
  /** 将军脉冲动画相位 0..1 循环 */
  private float checkAnimPhase = 0f;
  /** 将军动画定时器（30ms/帧） */
  private Timer checkAnimTimer = null;

  // ── 用于讲解的前一步评分（红方视角）────────────────────────────────
  private int evalBeforeHuman = 0;

  // ── 联网对战状态 ─────────────────────────────────────────────────
  private String onlineRoomId = null;
  private Side mySide = null;
  private boolean isSpectator = false;
  private DanmuPanel danmuPanel = null;
  private JTextField danmakuInputField = null;

  // ── 绝杀/终局覆盖特效 ────────────────────────────────────────────
  private CheckmateOverlayPanel checkmateOverlay = null;

  public ChessPanel()
  {
    controller = new ChessController(ChessConstant.ENGINE_NAME[Config.get().getEngine()]);
    controller.start(ChessConstant.ENGINE_NAME[Config.get().getEngine()]);
    if(Config.get().isComputerFirst())
    {
      setThinkingStatus();
      // 仅在使用 UCCI 引擎时让引擎思考；本地 AI 模式在 initGame/triggerAiMove 中触发
      if(!useLocalAi)
      {
        controller.engineThink();
      }
    }
    initButtons();
    initBoardListener();
    initComponentListener();
    loadBoard();
    loadPieces();
    clear();
    EventBus.getDefault().register(this);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onReceiveMsg(EventMessage msg)
  {
    java.util.logging.Logger.getLogger(ChessPanel.class.getName()).fine("【EventBus】onReceiveMsg: " + msg);
    if(msg.getType() == ChessConstant.MSG_BEST_MOVE)
    {
      Move move = (Move)msg.getObj();
      java.util.logging.Logger.getLogger(ChessPanel.class.getName()).fine("【EventBus】move.from=" + move.from + ",move.to=" + move.to);
      // 清除提示高亮（引擎已落子）
      hintFrom = -1;
      hintTo = -1;
      update(controller.currentPosArray(), move.from, move.to);
      if(!controller.isPaused())
      {
        if(controller.checked())
        {
          ChessAudio.COM_CHECK.play();
          new ToastFrame("将军！");
        }
        else if(controller.captured())
        {
          ChessAudio.COM_EAT.play();
        }
        else
        {
          ChessAudio.COM_MOVE.play();
        }
        updateInfoArea(controller.getGameContext().getManualText(), "------------------------------------------------------------\n");
        // AI 讲解（UCCI 引擎落子后）
        Piece movedPiece = controller.currentPosArray()[move.to];
        boolean givesCheck = controller.checked();
        int currentEval = quickEval(controller.currentPosArray());
        appendAiCommentary(buildAiCommentaryUcci(move, movedPiece, controller.captured(), givesCheck, currentEval));

        setReadyStatus();
        boolean gameOver = scanResult(false);
        // AI 对战：UCCI 引擎双方自动续走（短暂延迟后触发）
        if(!gameOver && gameMode == GameMode.AI_VS_AI && !useLocalAi)
        {
          Timer delay = new Timer(600, e -> triggerAiMove());
          delay.setRepeats(false);
          delay.start();
        }
      }
    }
    else if(msg.getType() == ChessConstant.MSG_NO_BEST_MOVE)
    {
      if(controller.hasAttackAbility())
      {
        showGameOver(GameOverType.Win);
        showCheckmateOverlay(true, "绝杀！", "对手无路可走，您大获全胜！");
      }
      else
      {
        showGameOver(GameOverType.Loss);
        showCheckmateOverlay(false, "被将死！", "本局结束，再接再厉！");
        Timer t = new Timer(2500, ev ->
        {
          int opt = JOptionPane.showConfirmDialog(null, "很不幸，你输了~~~\n是否重新开始游戏？", "重新开始游戏", JOptionPane.YES_NO_OPTION);
          if(JOptionPane.YES_OPTION == opt)
          {
            initGame();
          }
        });
        t.setRepeats(false);
        t.start();
      }
      setReadyStatus();
    }
    else if(msg.getType() == ChessConstant.MSG_ANALYSIS_RESULT)
    {
      // try-finally 保证无论中间是否异常，按钮一定恢复可用
      try
      {
        AnalysisResult result = (AnalysisResult)msg.getObj();

        // 转换传统记谱：在棋盘未改变前获取快照
        String chineseNotation = "";
        if(result.getBestMove() != null)
        {
          // clone 防止 recordMove 修改原数组后影响转换结果
          Piece[] boardSnapshot = controller.currentPosArray().clone();
          chineseNotation = ChessNotationUtil.toChineseNotation(
              result.getBestMove(), boardSnapshot);
        }

        if(result.isHintMode() && result.getBestMove() != null)
        {
          // ── 走法提示：自动代替用户落子，并让引擎继续对弈 ──────────
          Move bestMove = result.getBestMove();
          if(controller.legalMove(bestMove))
          {
            controller.recordMove(bestMove);
            hintFrom = -1;
            hintTo   = -1;
            update(controller.currentPosition().getPosArray(), bestMove.from, bestMove.to);
            if(controller.checked())
            {
              ChessAudio.MAN_CHECK.play();
            }
            else if(controller.captured())
            {
              ChessAudio.MAN_EAT.play();
            }
            else
            {
              ChessAudio.MAN_MOVE.play();
            }
            updateInfoArea(controller.getGameContext().getManualText(), null);
            // 显示分析结果
            if(analysisTextArea != null)
            {
              analysisTextArea.setText(result.toDisplayText(chineseNotation));
              analysisTextArea.setCaretPosition(0);
            }
            // 检查局势，未结束则让引擎走下一步
            if(!scanResult(true))
            {
              setThinkingStatus();
              controller.engineThink();
            }
          }
          else
          {
            // 走法不合法（罕见），退化为仅高亮提示
            hintFrom = result.getBestMove().from;
            hintTo   = result.getBestMove().to;
            repaint();
            if(analysisTextArea != null)
            {
              analysisTextArea.setText(result.toDisplayText(chineseNotation));
              analysisTextArea.setCaretPosition(0);
            }
          }
        }
        else
        {
          // ── 局势分析：只显示结果，不落子 ──────────────────────────
          if(analysisTextArea != null)
          {
            analysisTextArea.setText(result.toDisplayText(chineseNotation));
            analysisTextArea.setCaretPosition(0);
          }
        }
      }
      catch(Exception ex)
      {
        java.util.logging.Logger.getLogger(ChessPanel.class.getName())
            .log(java.util.logging.Level.WARNING, "处理分析结果时出错", ex);
      }
      finally
      {
        // 无论是否异常，始终恢复按钮可用状态
        btnAnalyze.setEnabled(true);
        btnHint.setEnabled(true);
      }
    }
    else if(msg.getType() == ChessConstant.MSG_ENGINE_THINKING)
    {
      // UCCI 引擎输出：仅在使用 UCCI 模式时显示
      if(!useLocalAi)
      {
        updateEngineInfo(msg, "<<<");
      }
    }
    else if(msg.getType() == ChessConstant.MSG_HUMAN_INPUT)
    {
      // UCCI 引擎输入回显：仅在使用 UCCI 模式时显示
      if(!useLocalAi)
      {
        updateEngineInfo(msg, ">>>");
      }
    }
    else if(msg.getType() == ChessConstant.MSG_LOCAL_AI_THINKING)
    {
      // 本地 AI 思考过程：始终显示
      updateEngineInfo(msg, "");
    }
    // ── 联网事件 ─────────────────────────────────────────────────────
    else if(msg.getType() == ChessConstant.MSG_NET_OPP_MOVE)
    {
      handleOnlineMove(msg);
    }
    else if(msg.getType() == ChessConstant.MSG_NET_CHAT)
    {
      handleOnlineChat(msg);
    }
    else if(msg.getType() == ChessConstant.MSG_NET_GAME_OVER)
    {
      handleOnlineGameOver(msg);
    }
    else if(msg.getType() == ChessConstant.MSG_NET_PLAYER_LEFT)
    {
      handlePlayerLeft(msg);
    }
  }

  private static final int MAX_ENGINE_INFO_LINES = 500;

  private void updateEngineInfo(EventMessage msg, String promptString)
  {
    if(engineTextArea == null)
    {
      return;
    }
    engineTextArea.append(promptString + msg.getObj() + "\n");

    // Prevent unbounded growth: trim the oldest lines when cap is exceeded
    String text = engineTextArea.getText();
    int lineCount = 0;
    for(int i = 0; i < text.length(); i++)
    {
      if(text.charAt(i) == '\n')
      {
        lineCount++;
      }
    }
    if(lineCount > MAX_ENGINE_INFO_LINES)
    {
      int toRemove = lineCount - MAX_ENGINE_INFO_LINES;
      int cutAt = 0;
      int found = 0;
      for(int i = 0; i < text.length(); i++)
      {
        if(text.charAt(i) == '\n')
        {
          found++;
          if(found >= toRemove)
          {
            cutAt = i + 1;
            break;
          }
        }
      }
      engineTextArea.setText(text.substring(cutAt));
    }
    // 始终滚动到最新引擎输出
    engineTextArea.setCaretPosition(engineTextArea.getDocument().getLength());
  }

  private void initBoardListener()
  {
    addMouseListener(new MouseListener()
    {
      public void mouseClicked(MouseEvent e)
      {
      }

      public void mouseEntered(MouseEvent e)
      {
      }

      public void mouseExited(MouseEvent e)
      {
      }

      public void mousePressed(MouseEvent e)
      {
        int y = Math.round(1.0f * (e.getY() - ChessConstant.CHESSBOARD_MARGIN - ChessConstant.Y_INIT) / ChessConstant.GRID_WIDTH);
        int x = Math.round(1.0f * (e.getX() - ChessConstant.CHESSBOARD_MARGIN - ChessConstant.X_INIT) / ChessConstant.GRID_WIDTH);
        if((y >= 0 && y <= 9) && (x >= 0 && x <= 8))
        {
          onBoardCrossClicked((y * 9) + x);
        }
      }

      public void mouseReleased(MouseEvent e)
      {
      }
    });
  }

  private void onBoardCrossClicked(int pos)
  {
    if(gameMode == GameMode.AI_VS_AI)
    {
      return; // AI 对战时不允许人工操作
    }
    // ── 观战模式：禁止落子 ─────────────────────────────────────────
    if(gameMode == GameMode.ONLINE_SPECTATE)
    {
      return;
    }
    // ── 在线对战模式 ───────────────────────────────────────────────
    if(gameMode == GameMode.ONLINE_PVP)
    {
      Side currentSide = controller.currentPosition().getSide();
      if(mySide != currentSide)
      {
        new ToastFrame("请等待对手落子！", 1000);
        return;
      }
      if(controller.sameSide(pos))
      {
        update(controller.currentPosArray(), pos, -1);
        ChessAudio.CLICK_FROM.play();
        evalBeforeHuman = quickEval(controller.currentPosArray());
      }
      else if(controller.sameSide(getFrom()))
      {
        Move move = new Move(getFrom(), pos);
        if(controller.legalMove(move))
        {
          Piece[] boardBefore = controller.currentPosArray().clone();
          controller.recordMove(move);
          hintFrom = -1;
          hintTo = -1;
          update(controller.currentPosition().getPosArray(), move.from, move.to);
          boolean gaveCheck = controller.checked();
          if(gaveCheck)
          {
            ChessAudio.MAN_CHECK.play();
            new ToastFrame("将军！", 1500);
          }
          else if(controller.captured())
          {
            ChessAudio.MAN_EAT.play();
          }
          else
          {
            ChessAudio.MAN_MOVE.play();
          }
          updateInfoArea(controller.getGameContext().getManualText(), null);
          // 发送走法到服务器
          ChessClient.getInstance().send(NetMessage.move(onlineRoomId, move.name));
          scanResult(true);
        }
        else
        {
          if(ChessRules.willBeChecked(controller.currentPosition(), move))
          {
            new ToastFrame("不能送将，请重新选择！", 2500);
          }
          ChessAudio.MAN_MOV_ERROR.play();
        }
      }
      return;
    }
    if(!controller.isEngineReady() && !useLocalAi)
    {
      new ToastFrame("引擎未准备！");
      return;
    }
    if(controller.sameSide(pos))
    {
      update(controller.currentPosArray(), pos, -1);
      ChessAudio.CLICK_FROM.play();
      // 记录人类走棋前的局面评分（用于讲解）
      evalBeforeHuman = quickEval(controller.currentPosArray());
    }
    else if(controller.sameSide(getFrom()) && !controller.isThinking())
    {
      Move move = new Move(getFrom(), pos);
      if(controller.legalMove(move))
      {
        // 记录走棋前棋盘（用于传统记谱与走法评价）
        Piece[] boardBefore = controller.currentPosArray().clone();

        controller.recordMove(move);
        hintFrom = -1;
        hintTo = -1;
        update(controller.currentPosition().getPosArray(), move.from, move.to);
        boolean gaveCheck = controller.checked();
        if(gaveCheck)
        {
          ChessAudio.MAN_CHECK.play();
          new ToastFrame("将军！", 1500);
        }
        else if(controller.captured())
        {
          ChessAudio.MAN_EAT.play();
        }
        else
        {
          ChessAudio.MAN_MOVE.play();
        }
        updateInfoArea(controller.getGameContext().getManualText(), null);

        // 对手点评：对比走棋前后局面变化（一致性评估，避免误判）
        int moveDelta = computeMoveDelta(boardBefore, controller.currentPosArray());
        appendOpponentCommentary(buildHumanMoveComment(move, boardBefore, moveDelta, gaveCheck));

        if(!scanResult(true))
        {
          if(gameMode != GameMode.HUMAN_VS_HUMAN)
          {
            triggerAiMove();
          }
        }
        return;
      }
      else
      {
        if(ChessRules.willBeChecked(controller.currentPosition(), move))
        {
          new ToastFrame("不能送将，请重新选择！", 2500);
        }
        ChessAudio.MAN_MOV_ERROR.play();
      }
    }
  }

  private void initComponentListener()
  {
    btnNewGame.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        int opt = JOptionPane.showConfirmDialog(null, "确认重新开始？", "重新开始游戏", JOptionPane.YES_NO_OPTION);
        if(JOptionPane.YES_OPTION == opt)
        {
          initGame();
        }
      }
    });
    btnSave.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileChooser.setDialogTitle("保存棋谱文件");
        fileChooser.setSelectedFile(new File(new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()) + ".pgn"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("PGN FILE", "pgn"));
        int result = fileChooser.showDialog(null, "保存文件");
        if(result == JFileChooser.APPROVE_OPTION)
        {
          File file = fileChooser.getSelectedFile();
          java.util.logging.Logger.getLogger(ChessPanel.class.getName()).info("棋谱文件保存地址为：" + file.getAbsolutePath());
          boolean flag = controller.saveManual(file.getAbsolutePath());
          java.util.logging.Logger.getLogger(ChessPanel.class.getName()).fine(String.valueOf(flag));
        }
      }
    });
    btnRetract.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        retract(controller);
      }
    });
    btnCopy.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        if(controller.currentPosition() != null)
        {
          String fenString = controller.currentPosition().toFen();
          java.util.logging.Logger.getLogger(ChessPanel.class.getName()).info("复制局面: " + fenString);
          ClipBoardUtil.setSysClipboardText("fen " + fenString);
        }
      }
    });
    btnChangeBoard.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        if(ItemEvent.SELECTED == e.getStateChange())
        {
          if(btnChangeBoard.getSelectedIndex() > 0)
          {
            int board = btnChangeBoard.getSelectedIndex() - 1;
            java.util.logging.Logger.getLogger(ChessPanel.class.getName()).info("更换棋盘: " + ChessConstant.BOARD_NAME[board]);
            Config.get().setBoard(board);
            loadBoard();
            repaint();
          }
        }
      }
    });
    btnChangePiece.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        if(ItemEvent.SELECTED == e.getStateChange())
        {
          if(btnChangePiece.getSelectedIndex() > 0)
          {
            int pieceImageArray = btnChangePiece.getSelectedIndex() - 1;
            java.util.logging.Logger.getLogger(ChessPanel.class.getName()).info("更换棋子: " + ChessConstant.PIECES_NAME[pieceImageArray]);
            Config.get().setPieces(pieceImageArray);
            loadPieces();
            repaint();
          }
        }
      }
    });
    btnChangeCoordinate.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        if(ItemEvent.SELECTED == e.getStateChange())
        {
          if(btnChangeCoordinate.getSelectedIndex() > 0)
          {
            int coordinate = btnChangeCoordinate.getSelectedIndex() - 1;
            java.util.logging.Logger.getLogger(ChessPanel.class.getName()).info("更换坐标: " + ChessConstant.COORDINATE_NAME[coordinate]);
            Config.get().setCoordinate(coordinate);
            repaint();
          }
        }
      }
    });
    btnPaste.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        if(ItemEvent.SELECTED == e.getStateChange())
        {
          if(btnPaste.getSelectedIndex() > 0)
          {
            UcciEngine.getInstance().shutdown();
            int selectedEngine = btnPaste.getSelectedIndex() - 1;
            java.util.logging.Logger.getLogger(ChessPanel.class.getName()).info("更换引擎: " + ChessConstant.ENGINE_NAME[selectedEngine]);
            Config.get().setEngine(selectedEngine);
            UcciEngine.getInstance().restart(ChessConstant.ENGINE_NAME[selectedEngine]);
          }
        }
      }
    });

    // ── 对战模式 ──────────────────────────────────────────────────
    btnGameMode.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        if(ItemEvent.SELECTED == e.getStateChange())
        {
          int idx = btnGameMode.getSelectedIndex();
          gameMode = (idx == 1) ? GameMode.AI_VS_AI : (idx == 2) ? GameMode.HUMAN_VS_HUMAN : GameMode.HUMAN_VS_AI;
        }
      }
    });

    // ── AI 难度（UCCI 引擎用时 / 本地 AI 搜索深度） ────────────────
    btnAiDepth.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        if(ItemEvent.SELECTED == e.getStateChange())
        {
          int depth = btnAiDepth.getSelectedIndex() + 1; // 1–6
          localAiEngine.setDepth(depth);
          // UCCI 引擎难度通过时间限制控制（每级 500ms）
          // Config.get().setTimeLimit(depth * 500); // 如果 Config 支持
        }
      }
    });

    // ── AI 类型（UCCI / 本地 Minimax）────────────────────────────
    btnAiType.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        if(ItemEvent.SELECTED == e.getStateChange())
        {
          useLocalAi = (btnAiType.getSelectedIndex() == 1);
        }
      }
    });

    btnAnalyze.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        // 先检查引擎状态，避免禁用按钮后无法恢复
        if(!controller.isEngineReady())
        {
          new ToastFrame("引擎未就绪，请稍候！", 2000);
          return;
        }
        if(controller.isThinking())
        {
          new ToastFrame("引擎正在思考，请等待！", 2000);
          return;
        }
        btnAnalyze.setEnabled(false);
        btnHint.setEnabled(false);
        if(analysisTextArea != null)
        {
          analysisTextArea.setText("🔍 正在分析局势，请稍候（约 2 秒）…");
        }
        controller.requestAnalysis(false);
      }
    });

    btnHint.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        // 先检查引擎状态，避免禁用按钮后无法恢复
        if(!controller.isEngineReady())
        {
          new ToastFrame("引擎未就绪，请稍候！", 2000);
          return;
        }
        if(controller.isThinking())
        {
          new ToastFrame("引擎正在思考，请等待！", 2000);
          return;
        }
        btnAnalyze.setEnabled(false);
        btnHint.setEnabled(false);
        if(analysisTextArea != null)
        {
          analysisTextArea.setText("💡 正在计算最优走法，请稍候（约 2 秒）…");
        }
        controller.requestAnalysis(true);
      }
    });
  }

  // ═══════════════════════════════════════════════════════════════════
  //  AI 走法调度
  // ═══════════════════════════════════════════════════════════════════

  /**
   * 统一触发 AI 走棋：根据 useLocalAi 选择引擎
   */
  private void triggerAiMove()
  {
    if(gameMode == GameMode.HUMAN_VS_HUMAN)
      return;
    setThinkingStatus();
    if(useLocalAi)
    {
      startLocalAiSearch();
    }
    else
    {
      controller.engineThink();
    }
  }

  /**
   * 使用本地 Minimax AI 搜索并落子
   */
  private void startLocalAiSearch()
  {
    if(localAiEngine.isSearching())
      return;
    Position posForSearch = Position.clone(controller.currentPosition());
    // 思考过程回调：在搜索线程中触发，通过 EventBus 转发到 UI 线程展示
    java.util.function.Consumer<String> infoCallback =
        line -> EventBus.getDefault().post(new EventMessage(ChessConstant.MSG_LOCAL_AI_THINKING, line));

    localAiEngine.searchAsync(posForSearch, result -> javax.swing.SwingUtilities.invokeLater(() -> {
      if(result == null || result.bestMove == null)
      {
        EventBus.getDefault().post(new EventMessage(ChessConstant.MSG_NO_BEST_MOVE, null));
        return;
      }
      Move move = result.bestMove;
      if(controller.legalMove(move))
      {
        Piece capturedPiece = controller.currentPosArray()[move.to];
        Piece movedPiece = controller.currentPosArray()[move.from];
        String notation = ChessNotationUtil.toChineseNotation(move, controller.currentPosArray().clone());

        controller.recordMove(move);
        hintFrom = -1;
        hintTo = -1;
        update(controller.currentPosition().getPosArray(), move.from, move.to);

        if(controller.checked())
        {
          ChessAudio.COM_CHECK.play();
          new ToastFrame("将军！");
        }
        else if(controller.captured())
        {
          ChessAudio.COM_EAT.play();
        }
        else
        {
          ChessAudio.COM_MOVE.play();
        }
        updateInfoArea(controller.getGameContext().getManualText(), "------------------------------------------------------------\n");

        // AI 讲解
        boolean givesCheck = controller.checked();
        appendAiCommentary(buildAiCommentaryLocal(move, movedPiece, capturedPiece, givesCheck, result.score, notation));

        setReadyStatus();
        boolean gameOver = scanResult(false);
        if(!gameOver && gameMode == GameMode.AI_VS_AI)
        {
          Timer t = new Timer(600, ev -> triggerAiMove());
          t.setRepeats(false);
          t.start();
        }
      }
    }), infoCallback);
  }

  // ═══════════════════════════════════════════════════════════════════
  //  棋子动画
  // ═══════════════════════════════════════════════════════════════════
  private void startAnimation(int fromPos, int toPos, Piece piece)
  {
    if(animTimer != null && animTimer.isRunning())
      animTimer.stop();

    animToPos = toPos;
    animPieceImg = (pieceImageArray != null && piece != null) ? getPiece(piece.getResIndex()) : null;
    animProgress = 0f;

    int dx = ChessConstant.GRID_WIDTH / 2 - ChessConstant.PIECE_WIDTH / 2;

    int fx = fromPos % 9, fy = fromPos / 9;
    if(diverted)
    {
      fx = 8 - fx;
      fy = 9 - fy;
    }
    animFromPx = ChessConstant.CHESSBOARD_MARGIN + fx * ChessConstant.GRID_WIDTH - ChessConstant.GRID_WIDTH / 2 + dx;
    animFromPy = ChessConstant.CHESSBOARD_MARGIN + fy * ChessConstant.GRID_WIDTH - ChessConstant.GRID_WIDTH / 2 + dx;

    int tx = toPos % 9, ty = toPos / 9;
    if(diverted)
    {
      tx = 8 - tx;
      ty = 9 - ty;
    }
    animToPx = ChessConstant.CHESSBOARD_MARGIN + tx * ChessConstant.GRID_WIDTH - ChessConstant.GRID_WIDTH / 2 + dx;
    animToPy = ChessConstant.CHESSBOARD_MARGIN + ty * ChessConstant.GRID_WIDTH - ChessConstant.GRID_WIDTH / 2 + dx;

    animating = true;
    animTimer = new Timer(16, e -> {
      animProgress += 0.15f; // ~7 帧完成，约 112ms（更流畅快速）
      if(animProgress >= 1f)
      {
        animProgress = 1f;
        animating = false;
        ((Timer)e.getSource()).stop();
      }
      repaint();
    });
    animTimer.start();
  }

  // ═══════════════════════════════════════════════════════════════════
  //  讲解文本生成
  // ═══════════════════════════════════════════════════════════════════

  /**
   * 在 AI 讲解框末尾追加一条记录
   */
  private void appendAiCommentary(String text)
  {
    if(aiCommentaryArea == null)
      return;
    aiCommentaryArea.append(text);
    aiCommentaryArea.setCaretPosition(aiCommentaryArea.getDocument().getLength());
  }

  /**
   * 在对手点评框末尾追加一条记录
   */
  private void appendOpponentCommentary(String text)
  {
    if(opponentCommentaryArea == null)
      return;
    opponentCommentaryArea.append(text);
    opponentCommentaryArea.setCaretPosition(opponentCommentaryArea.getDocument().getLength());
  }

  /**
   * UCCI 引擎落子后的讲解（落子前棋盘已不可得，基于落子后信息）
   */
  private String buildAiCommentaryUcci(Move move, Piece movedPiece, boolean captured, boolean givesCheck, int scoreRedPerspective)
  {
    StringBuilder sb = new StringBuilder();
    String side = (movedPiece != null && movedPiece.getSide() == Side.Red) ? "红方" : "黑方";
    String pName = movedPiece != null ? ChessRules.zhName.getOrDefault(movedPiece, "棋子") : "棋子";
    sb.append("\n【").append(side).append("】").append(pName);
    if(captured)
    {
      sb.append("吃子");
    }
    if(givesCheck)
    {
      sb.append("，将军！");
    }
    sb.append("\n");
    sb.append(buildPieceStrategyHint(movedPiece, move));
    sb.append(buildScoreComment(movedPiece, scoreRedPerspective));
    return sb.toString();
  }

  /**
   * 本地 AI 落子后的讲解（有完整的落子前信息）
   */
  private String buildAiCommentaryLocal(Move move, Piece movedPiece, Piece capturedPiece, boolean givesCheck, int scoreFromAiPerspective,
      String notation)
  {
    StringBuilder sb = new StringBuilder();
    String side = (movedPiece != null && movedPiece.getSide() == Side.Red) ? "红方" : "黑方";
    String pName = movedPiece != null ? ChessRules.zhName.getOrDefault(movedPiece, "棋子") : "棋子";
    sb.append("\n【").append(side).append("】");
    if(notation != null && !notation.isEmpty())
    {
      sb.append(notation);
    }
    else
    {
      sb.append(pName).append("移动");
    }
    if(capturedPiece != null && !capturedPiece.isEmpty())
    {
      sb.append("，吃").append(ChessRules.zhName.getOrDefault(capturedPiece, "子"));
    }
    if(givesCheck)
    {
      sb.append("，将军！");
    }
    sb.append("\n");
    sb.append(buildPieceStrategyHint(movedPiece, move));
    // scoreFromAiPerspective 为当前行棋方视角，转换到红方视角
    int redScore = (movedPiece != null && movedPiece.getSide() == Side.Red) ? scoreFromAiPerspective : -scoreFromAiPerspective;
    sb.append(buildScoreComment(movedPiece, redScore));
    return sb.toString();
  }

  /**
   * 对人类走法的评价（对手点评）——细化版
   *
   * @param move        本手走法
   * @param boardBefore 走棋前棋盘快照
   * @param moveDelta   走棋质量评分（正 = 红方受益，负 = 红方受损）
   * @param givesCheck  本步是否将军
   */
  private String buildHumanMoveComment(Move move, Piece[] boardBefore, int moveDelta, boolean givesCheck)
  {
    StringBuilder sb = new StringBuilder();
    String notation = ChessNotationUtil.toChineseNotation(move, boardBefore);
    sb.append("\n【您走了「").append(notation.isEmpty() ? move.name : notation).append("」】\n");

    Piece movedPiece    = (move.from >= 0 && move.from < 90) ? boardBefore[move.from] : null;
    Piece capturedPiece = (move.to   >= 0 && move.to   < 90) ? boardBefore[move.to]   : null;
    boolean isCapture   = capturedPiece != null && !capturedPiece.isEmpty();
    int delta = moveDelta;

    // ── 综合质量评价 ─────────────────────────────────────────────────
    if(givesCheck && isCapture)
    {
      String capName = ChessRules.zhName.getOrDefault(capturedPiece, "子");
      sb.append("  [妙] 将军 + 吃").append(capName).append("! 双重威胁，出色的组合拳!\n");
    }
    else if(givesCheck)
    {
      if(delta >= 0)
      {
        sb.append("  [优] 将军! 积极进攻，迫使对方应将，争得主动权。\n");
      }
      else
      {
        sb.append("  [攻] 将军，但请检查将后自身防守是否稳固。\n");
      }
    }
    else if(isCapture)
    {
      String capName = ChessRules.zhName.getOrDefault(capturedPiece, "子");
      if(delta > 200)
      {
        sb.append("  [妙] 精彩! 净吃").append(capName).append("，局面大幅改善，继续保持攻势!\n");
      }
      else if(delta > 60)
      {
        sb.append("  [优] 吃").append(capName).append("有利，此换子对己方有利，保持优势。\n");
      }
      else if(delta > -60)
      {
        sb.append("  [平] 等价换").append(capName).append("，双方交换后局面基本均衡，需看后续发展。\n");
      }
      else if(delta > -200)
      {
        sb.append("  [疑] 吃").append(capName).append("代价较大，此换子请仔细权衡，对方可能有更强后续。\n");
      }
      else
      {
        sb.append("  [劣] 失误! 吃").append(capName).append("得不偿失，己方损失更大——建议考虑悔棋。\n");
      }
    }
    else
    {
      // 纯走子（无吃无将）
      if(delta > 300)
      {
        sb.append("  [妙] 绝妙好棋! 此步让局面大幅改善，对手极难应对!\n");
      }
      else if(delta > 120)
      {
        sb.append("  [优] 好棋! 占据要点，局面明显有利，对手承压。\n");
      }
      else if(delta > 40)
      {
        sb.append("  [优] 稳健好棋，局面略有改善，继续保持节奏。\n");
      }
      else if(delta > -40)
      {
        sb.append("  [平] 平稳走法，局面基本均衡，双方继续周旋。\n");
      }
      else if(delta > -120)
      {
        sb.append("  [疑] 走法欠佳，对方获得了更强的反击机会，请注意防守。\n");
      }
      else if(delta > -300)
      {
        sb.append("  [劣] 失误! 此步令己方陷入明显劣势，可考虑悔棋重新选择。\n");
      }
      else
      {
        sb.append("  [劣] 严重失误! 此步大幅丧失优势，局面岌岌可危，请仔细审视!\n");
      }
    }

    // ── 棋子专项策略提示 ─────────────────────────────────────────────
    sb.append(buildPieceStrategyHint(movedPiece, move));

    return sb.toString();
  }

  /**
   * 棋子策略提示——结合棋子类型与实际落点给出具体建议。
   *
   * @param p    移动的棋子
   * @param move 本手走法（可为 null，退化为通用提示）
   */
  private String buildPieceStrategyHint(Piece p, Move move)
  {
    if(p == null)
    {
      return "";
    }
    boolean isRed = (p.getSide() == Side.Red);
    int toX   = (move != null) ? move.toX   : -1;
    int toY   = (move != null) ? move.toY   : -1;
    int fromX = (move != null) ? move.fromX : -1;

    if(p.isRook())
    {
      if(toX >= 3 && toX <= 5)
      {
        return "  > 车占中路要道，控制纵横关键通道，是积极进取的好位置。\n";
      }
      if((isRed && toY == 9) || (!isRed && toY == 0))
      {
        return "  > 车宜早出，困于底线难以发挥最大威力，宜向河界推进。\n";
      }
      return "  > 车为最强子，宜占开放路线，与另一车或炮配合，形成强力双重攻势。\n";
    }

    if(p.isKnight())
    {
      if(toX == 0 || toX == 8)
      {
        return "  > [注意] \"马跳边，老将边哭边\"——边路马威力大减，尽量将马移回中路发挥作用。\n";
      }
      boolean crossedRiver = isRed ? (toY < 5) : (toY >= 5);
      if(crossedRiver)
      {
        return "  > 过河马威力倍增，可与炮配合形成\"马后炮\"等连杀手段，继续积极进攻！\n";
      }
      if(toX >= 3 && toX <= 5)
      {
        return "  > 马占中路，八方皆可出击，是优秀的控制位置，保持这个势头。\n";
      }
      return "  > 马踏八方，注意行进路线畅通（勿蹩脚），尽量让马向中路靠拢。\n";
    }

    if(p.isCanon())
    {
      boolean crossedRiver = isRed ? (toY < 5) : (toY >= 5);
      if(crossedRiver)
      {
        return "  > 炮已过河，攻击威力大增！保留好炮架，配合车马发动凌厉进攻。\n";
      }
      if(toX == 4)
      {
        return "  > 中炮开局，攻势凶猛！结合车马形成\"当头炮\"战术，是经典有力的开局选择。\n";
      }
      return "  > 炮须炮架方能发威，勿轻易换炮；善用\"空心炮\"\"重炮\"等杀法配合车马进攻。\n";
    }

    if(p.isPawn())
    {
      boolean crossedRiver = isRed ? (toY < 5) : (toY >= 5);
      boolean movedSideways = (move != null) && (toX != fromX);
      if(crossedRiver)
      {
        if(movedSideways)
        {
          return "  > 过河兵（卒）横向骚扰，牵制对方阵地，是残局中的重要战力，继续压制！\n";
        }
        return "  > 过河兵（卒）前进压迫，威力倍增，继续向前逼迫对方将帅！\n";
      }
      return "  > 兵（卒）过河前只能前进，宜积极推进过河，以发挥更大的进攻威力。\n";
    }

    if(p.isBishop())
    {
      return "  > 象（相）守护本宫，双象配合可形成牢固防线；注意象眼不可被棋子堵住。\n";
    }

    if(p.isAdvisor())
    {
      return "  > 仕（士）是将帅的贴身护卫，宜保持在将帅周围；双仕护将可大幅增强安全性。\n";
    }

    if(p.isKing())
    {
      return "  > 将帅宜深藏稳守，避免不必要的移动；残局时可积极参与，但须防止被将死。\n";
    }

    return "";
  }

  /**
   * 兼容旧调用（无 Move 参数）
   */
  private String buildPieceStrategyHint(Piece p)
  {
    return buildPieceStrategyHint(p, null);
  }

  /**
   * 局势评分描述——6 档细化，含战术建议。
   *
   * @param mover    行棋方棋子（用于上下文，当前未使用）
   * @param redScore 红方视角评分（正 = 红方有利）
   */
  private String buildScoreComment(Piece mover, int redScore)
  {
    boolean redAdv = redScore > 0;
    int abs = Math.abs(redScore);
    double display = redScore / 100.0;

    String grade;
    String advice;
    if(abs > 900)
    {
      grade = redAdv ? "[胜] 红方胜势" : "[胜] 黑方胜势";
      advice = redAdv ? "局面已定，红方处于必胜态势，精准收官即可。" : "局面已定，黑方处于必胜态势，精准收官即可。";
    }
    else if(abs > 500)
    {
      grade = redAdv ? "红方大优" : "黑方大优";
      advice = redAdv ? "红方子力绝对优势，积极兑子以锁定胜局。" : "黑方子力绝对优势，积极兑子以锁定胜局。";
    }
    else if(abs > 250)
    {
      grade = redAdv ? "红方明显占优" : "黑方明显占优";
      advice = redAdv ? "红方占有较大优势，可主动进攻扩大优势。" : "黑方占有较大优势，可主动进攻扩大优势。";
    }
    else if(abs > 100)
    {
      grade = redAdv ? "红方略占优势" : "黑方略占优势";
      advice = redAdv ? "红方稍占上风，宜稳步推进，勿轻易丢失优势。" : "黑方稍占上风，宜稳步推进，勿轻易丢失优势。";
    }
    else if(abs > 40)
    {
      grade = "局面微差";
      advice = redAdv ? "形势接近均等，红方略微有利，需耐心经营。" : "形势接近均等，黑方略微有利，需耐心经营。";
    }
    else
    {
      grade = "势均力敌";
      advice = "双方子力基本均衡，局面胶着，需寻找战机突破。";
    }
    return String.format("  > 局势：%s（%+.1f 分）\n  > %s\n", grade, display, advice);
  }

  // ═══════════════════════════════════════════════════════════════════
  //  走法质量评估（红方视角）
  // ═══════════════════════════════════════════════════════════════════

  /**
   * 评估一步棋的质量（红方视角）。
   * 对比走棋前后两个局面，同时考虑材料变化和新增挂子风险。
   * 返回值：正 = 红方受益，负 = 红方受损。
   *
   * @param boardBefore 走棋前棋盘
   * @param boardAfter  走棋后棋盘
   */
  private int computeMoveDelta(Piece[] boardBefore, Piece[] boardAfter)
  {
    // 1. 材料变化（吃子时体现）
    int materialDelta = quickEval(boardAfter) - quickEval(boardBefore);
    // 2. 新增挂子风险：走棋后黑方可获利的吃子利润增量
    int riskBefore = computeMaxThreat(boardBefore, Side.Black);
    int riskAfter  = computeMaxThreat(boardAfter,  Side.Black);
    int newRisk = Math.max(0, riskAfter - riskBefore);
    return materialDelta - newRisk;
  }

  /**
   * 计算 attackerSide 从当前局面中能获得的最大吃子利润。
   * 利润 = max(0, 被吃子价值 - 攻击者价值)。
   * 只统计"有利可图"的吃子（低价子吃高价子），避免把正常对峙误判为威胁。
   *
   * @param board        棋盘数组
   * @param attackerSide 进攻方
   */
  private int computeMaxThreat(Piece[] board, Side attackerSide)
  {
    try
    {
      Position tempPos = new Position(false);
      tempPos.setPosArray(board.clone());
      tempPos.setSide(attackerSide);
      List<Move> moves = ChessRules.enumMoves(tempPos);
      int maxProfit = 0;
      for(Move m : moves)
      {
        Piece target = board[m.to];
        if(target != null && !target.isEmpty() && target.getSide() != attackerSide && !target.isKing())
        {
          Piece attacker = board[m.from];
          int profit = evalPieceValue(target) - evalPieceValue(attacker);
          if(profit > maxProfit)
          {
            maxProfit = profit;
          }
        }
      }
      return maxProfit;
    }
    catch(Exception ignored)
    {
      return 0;
    }
  }

  // ═══════════════════════════════════════════════════════════════════
  //  局面快速评分（红方视角，用于讲解）
  // ═══════════════════════════════════════════════════════════════════

  private int quickEval(Piece[] board)
  {
    int score = 0;
    for(Piece p : board)
    {
      if(p == null || p.isEmpty())
      {
        continue;
      }
      int v = evalPieceValue(p);
      score += (p.getSide() == Side.Red) ? v : -v;
    }
    return score;
  }

  private static int evalPieceValue(Piece p)
  {
    if(p.isKing())
    {
      return 60000;
    }
    if(p.isRook())
    {
      return 600;
    }
    if(p.isCanon())
    {
      return 300;
    }
    if(p.isKnight())
    {
      return 300;
    }
    if(p.isBishop())
    {
      return 120;
    }
    if(p.isAdvisor())
    {
      return 120;
    }
    if(p.isPawn())
    {
      return 60;
    }
    return 0;
  }

  // ═══════════════════════════════════════════════════════════════════
  //  联网对战 — 公共入口
  // ═══════════════════════════════════════════════════════════════════

  /**
   * 由 LobbyDialog 调用，进入在线对战 / 观战模式。
   *
   * @param roomId       服务器房间 ID
   * @param side         本客户端执的颜色（观战时为 null）
   * @param spectator    true = 观战，false = 对战
   * @param historyMoves 观战时服务器发来的历史棋步（可为 null）
   */
  public void startOnlineGame(String roomId, Side side, boolean spectator, java.util.List<String> historyMoves)
  {
    onlineRoomId = roomId;
    mySide = side;
    isSpectator = spectator;
    gameMode = spectator ? GameMode.ONLINE_SPECTATE : GameMode.ONLINE_PVP;

    // 重置棋盘
    hintFrom = -1;
    hintTo = -1;
    if(!controller.isThinking())
    {
      controller.restart();
    }
    update(controller.currentPosArray(), -1, -1);
    stepTextArea.setText("");
    engineTextArea.setText("");
    if(analysisTextArea != null)
    {
      analysisTextArea.setText("在线对局中…");
    }
    if(aiCommentaryArea != null)
    {
      aiCommentaryArea.setText("在线对局：等待走棋…");
    }
    if(opponentCommentaryArea != null)
    {
      opponentCommentaryArea.setText("在线对局：等待走棋…");
    }

    // 观战时回放历史棋步
    if(historyMoves != null && !historyMoves.isEmpty())
    {
      for(String moveName : historyMoves)
      {
        try
        {
          Move m = new Move(moveName);
          if(controller.legalMove(m))
          {
            controller.recordMove(m);
          }
        }
        catch(Exception ignored)
        {
        }
      }
      update(controller.currentPosArray(), -1, -1);
      updateInfoArea(controller.getGameContext().getManualText(), null);
    }

    // 显示弹幕浮层
    if(danmuPanel != null)
    {
      danmuPanel.setVisible(true);
    }

    String tip = spectator ? "观战已开始！" : "在线对战已开始！您执" + (side == Side.Red ? "红先行 ♟" : "黑后行 ♟");
    new ToastFrame(tip, 2500);
  }

  // ── 联网事件处理 ───────────────────────────────────────────────────

  /**
   * 收到对手走法，应用到本地棋盘并播放音效
   */
  private void handleOnlineMove(EventMessage evt)
  {
    if(gameMode != GameMode.ONLINE_PVP && gameMode != GameMode.ONLINE_SPECTATE)
    {
      return;
    }
    if(!(evt.getObj() instanceof NetMessage))
    {
      return;
    }
    NetMessage msg = (NetMessage)evt.getObj();
    String moveName = msg.getString("move");
    if(moveName == null || moveName.length() != 4)
    {
      return;
    }
    try
    {
      Move m = new Move(moveName);
      if(controller.legalMove(m))
      {
        controller.recordMove(m);
        hintFrom = -1;
        hintTo = -1;
        update(controller.currentPosition().getPosArray(), m.from, m.to);
        if(controller.checked())
        {
          ChessAudio.COM_CHECK.play();
          new ToastFrame("将军！");
        }
        else if(controller.captured())
        {
          ChessAudio.COM_EAT.play();
        }
        else
        {
          ChessAudio.COM_MOVE.play();
        }
        updateInfoArea(controller.getGameContext().getManualText(), null);
        scanResult(false);
      }
    }
    catch(Exception e)
    {
      java.util.logging.Logger.getLogger(ChessPanel.class.getName()).warning("无效的在线走法: " + moveName);
    }
  }

  /**
   * 收到聊天/弹幕消息，转发给 DanmakuPanel
   */
  private void handleOnlineChat(EventMessage evt)
  {
    if(!(evt.getObj() instanceof NetMessage))
    {
      return;
    }
    NetMessage msg = (NetMessage)evt.getObj();
    String username = msg.getString("username");
    String text = msg.getString("text");
    if(danmuPanel != null && username != null && text != null)
    {
      danmuPanel.addBullet(username, text);
    }
  }

  /**
   * 收到游戏结束通知
   */
  private void handleOnlineGameOver(EventMessage evt)
  {
    if(!(evt.getObj() instanceof NetMessage))
    {
      return;
    }
    NetMessage msg = (NetMessage)evt.getObj();
    String winner = msg.getString("winner");
    String reason = msg.getString("reason");
    String me = ChessClient.getInstance().getLoggedInUser();
    if(winner != null && !winner.isEmpty())
    {
      if(winner.equals(me))
      {
        ChessAudio.WIN_BGM.play();
        new ToastFrame("🎉 恭喜！您赢得了本局比赛！", 3000);
      }
      else
      {
        ChessAudio.LOSE_BGM.play();
        new ToastFrame("很遗憾，您输了本局比赛。", 3000);
      }
    }
    else
    {
      new ToastFrame("对局已结束：" + (reason != null ? reason : "未知原因"), 2500);
    }
    // 允许重置游戏模式
    gameMode = GameMode.HUMAN_VS_AI;
    onlineRoomId = null;
    if(danmuPanel != null)
    {
      danmuPanel.setVisible(false);
    }
  }

  /**
   * 收到对方离开房间的通知
   */
  private void handlePlayerLeft(EventMessage evt)
  {
    if(!(evt.getObj() instanceof NetMessage))
    {
      return;
    }
    NetMessage msg = (NetMessage)evt.getObj();
    String username = msg.getString("username");
    new ToastFrame((username != null ? username : "对手") + " 已离开，对局中止。", 3000);
    gameMode = GameMode.HUMAN_VS_AI;
    onlineRoomId = null;
    if(danmuPanel != null)
    {
      danmuPanel.setVisible(false);
    }
  }

  // ═══════════════════════════════════════════════════════════════════
  //  本地游戏
  // ═══════════════════════════════════════════════════════════════════

  public void initGame()
  {
    ChessAudio.OPEN_BOARD.play();
    new ToastFrame("重新开始游戏...");
    hintFrom = -1;
    hintTo = -1;
    // 关闭绝杀覆盖层（如有）
    if(checkmateOverlay != null)
    {
      checkmateOverlay.dismiss();
      checkmateOverlay = null;
    }
    // 退出联网模式
    if(gameMode == GameMode.ONLINE_PVP || gameMode == GameMode.ONLINE_SPECTATE)
    {
      if(onlineRoomId != null)
      {
        ChessClient.getInstance().send(NetMessage.leaveRoom(onlineRoomId));
      }
      onlineRoomId = null;
      mySide = null;
      isSpectator = false;
      gameMode = GameMode.HUMAN_VS_AI;
      if(danmuPanel != null)
      {
        danmuPanel.setVisible(false);
      }
    }
    restart();
    repaint();
    engineTextArea.setText("");
    stepTextArea.setText("");
    if(analysisTextArea != null)
    {
      analysisTextArea.setText("点击「分析棋局」或「走法提示」查看分析结果。");
    }
    if(aiCommentaryArea != null)
    {
      aiCommentaryArea.setText("等待走棋…");
    }
    if(opponentCommentaryArea != null)
    {
      opponentCommentaryArea.setText("等待走棋…");
    }
    btnAnalyze.setEnabled(true);
    btnHint.setEnabled(true);
    // AI 对战模式自动开始
    if(gameMode == GameMode.AI_VS_AI)
    {
      Timer t = new Timer(500, e -> triggerAiMove());
      t.setRepeats(false);
      t.start();
    }
  }

  public void retract(ChessController controller)
  {
    if(!controller.isThinking())
    {
      try
      {
        controller.retractTurn();
        update(controller.currentPosArray(), -1, -1);
        String manualText = controller.getGameContext().getManualText();
        if(StringUtil.isEmpty(manualText))
        {
          stepTextArea.setText("");
          engineTextArea.setText("");
        }
        else
        {
          updateInfoArea(manualText, null);
        }
      }
      catch(Exception e)
      {
        java.util.logging.Logger.getLogger(ChessPanel.class.getName()).log(java.util.logging.Level.WARNING, "retract failed", e);
      }
    }
  }

  public void update(Piece[] newPosArray, int fromPos, int toPos)
  {
    System.arraycopy(newPosArray, 0, posArray, 0, 90);
    from = fromPos;
    to = toPos;
    // 更新将军高亮状态，并驱动脉冲动画
    checkedKingPos = computeCheckedKingPos();
    if(checkedKingPos >= 0)
    {
      startCheckAnimation();
    }
    else
    {
      stopCheckAnimation();
    }
    // 启动棋子移动动画
    if(fromPos >= 0 && toPos >= 0)
    {
      Piece p = newPosArray[toPos];
      if(p != null && !p.isEmpty())
      {
        startAnimation(fromPos, toPos, p);
        return; // 由 animTimer 触发 repaint
      }
    }
    repaint();
  }

  public void clear()
  {
    to = -1;
    from = -1;
    diverted = false;
    ChessRules.initBoard(posArray);
  }

  public void select(int pos)
  {
    from = pos;
    to = -1;
    repaint();
  }

  private void restart()
  {
    if(!controller.isThinking())
    {
      controller.restart();
      update(controller.currentPosArray(), -1, -1);
      updateInfoArea(null, null);
      repaint();
      UcciEngine.getInstance().restart(ChessConstant.ENGINE_NAME[Config.get().getEngine()]);
    }
  }

  private void updateInfoArea(String manualText, String engineText)
  {
    if(StringUtil.isNotBlank(manualText))
    {
      stepTextArea.setText(manualText);
      // 走棋后自动滚动到棋谱末尾，方便查看最新一步
      stepTextArea.setCaretPosition(stepTextArea.getDocument().getLength());
    }
    if(StringUtil.isNotBlank(engineText))
    {
      engineTextArea.append(engineText);
      // 滚动到最新引擎信息
      engineTextArea.setCaretPosition(engineTextArea.getDocument().getLength());
    }
  }

  // ═══════════════════════════════════════════════════════════════════
  //  绝杀/终局覆盖特效
  // ═══════════════════════════════════════════════════════════════════

  /**
   * 显示绝杀/将死全屏覆盖特效，直到用户点击才消失。
   *
   * @param win      true=我方胜，false=我方负
   * @param title    主标题（如 "绝杀！"）
   * @param subtitle 副标题（如 "您赢得了本局！"）
   */
  private void showCheckmateOverlay(boolean win, String title, String subtitle)
  {
    // 若已存在则先移除旧的
    if(checkmateOverlay != null)
    {
      checkmateOverlay.dismiss();
      checkmateOverlay = null;
    }
    // 覆盖整个 ChessPanel 区域
    int totalW = ChessConstant.CHESSBOARD_MARGIN * 2 + ChessConstant.GRID_WIDTH * 8 + 700;
    int totalH = ChessConstant.CHESSBOARD_MARGIN * 2 + ChessConstant.GRID_WIDTH * 9 + 250;
    checkmateOverlay = new CheckmateOverlayPanel(title, subtitle, win, () -> checkmateOverlay = null);
    checkmateOverlay.setBounds(0, 0, totalW, totalH);
    add(checkmateOverlay);
    setComponentZOrder(checkmateOverlay, 0); // 最上层
    revalidate();
    repaint();
  }

  private boolean scanResult(boolean byPerson)
  {
    if(scanLongCatch(controller, byPerson))
    {
      return true;
    }
    if(controller.killed())
    {
      if(byPerson)
      {
        showGameOver(GameOverType.Win);
        showCheckmateOverlay(true, "绝杀！", "您将死对手，旗开得胜！");
      }
      else
      {
        showGameOver(GameOverType.Loss);
        showCheckmateOverlay(false, "被将死！", "本局结束，再接再厉！");
        // 延迟弹确认框，让特效先显示
        Timer t = new Timer(2500, e ->
        {
          int opt = JOptionPane.showConfirmDialog(null, "很不幸，你输了~~~\n是否重新开始游戏？", "重新开始游戏", JOptionPane.YES_NO_OPTION);
          if(JOptionPane.YES_OPTION == opt)
          {
            initGame();
          }
        });
        t.setRepeats(false);
        t.start();
      }
      return true;
    }
    else if(controller.historySize() < ChessController.MAX_STEPS_FOR_DRAW * 2)
    {
      return false;
    }
    else
    {
      showGameOver(GameOverType.Draw);
      return true;
    }
  }

  private boolean scanLongCatch(ChessController controller, boolean byPerson)
  {
    LinkedList<Position> positionArray = controller.getGameContext().getPositionArray();
    int count = positionArray.size();
    if(count > 7)
    {
      String[] fens = new String[7];
      for(int i = 0; i < fens.length; i++)
      {
        fens[i] = ChessFenUtil.getPositionFen(positionArray.get((count - i) - 1).toFen());
      }
      if(fens[0].equals(fens[4]) && fens[1].equals(fens[5]) && fens[2].equals(fens[6]))
      {
        if(byPerson)
        {
          ChessAudio.BE_CHECKMATED_BY_COM.play();
        }
        else
        {
          ChessAudio.WIN_BGM.play();
        }
        showGameOver(byPerson ? GameOverType.Loss : GameOverType.Win);
        return true;
      }
    }
    return false;
  }

  private static void showGameOver(GameOverType dialogType)
  {
    if(dialogType == GameOverType.Win)
    {
      new ToastFrame("你赢了！");
      ChessAudio.WIN_BGM.play();
    }
    else if(dialogType == GameOverType.Draw)
    {
      new ToastFrame("平局！");
      ChessAudio.LOSE_BGM.play();
    }
    else
    {
      new ToastFrame("很不幸，你输了~~~");
      ChessAudio.LOSE_BGM.play();
    }
  }

  public void setThinkingStatus()
  {
  }

  public static void setReadyStatus()
  {
  }

  public void loadBoard()
  {
    BufferedImage raw = loadImageFromResource("/config/boards/" + ChessConstant.BOARD_NAME[Config.get().getBoard()]);
    // 预缩放棋盘图到设备像素，防止 HiDPI 拉伸模糊
    int boardLogW = ChessConstant.CHESSBOARD_MARGIN * 2 + ChessConstant.GRID_WIDTH * 8;
    int boardLogH = ChessConstant.CHESSBOARD_MARGIN * 2 + ChessConstant.GRID_WIDTH * 9;
    imgBoard = scaleToDeviceRes(raw, boardLogW, boardLogH, getDeviceScale());
  }

  public void loadPieces()
  {
    pieceImageArray = new BufferedImage[16];
    String piecesDir = "/config/pieces/" + ChessConstant.PIECES_NAME[Config.get().getPieces()] + "/";
    for(int i = 0; i < pieceImageArray.length; i++)
    {
      pieceImageArray[i] = loadPieceImage(piecesDir, ChessConstant.PIECE_ARRAY[i]);
    }
  }

  /** 尝试以 .gif → .png → 裸名 顺序加载棋子图片 */
  private BufferedImage loadPieceImage(String dir, String name)
  {
    BufferedImage img = tryLoadResource(dir + name + ".gif");
    if(img != null) return scaleToDeviceRes(img, ChessConstant.PIECE_WIDTH, ChessConstant.PIECE_HEIGHT, getDeviceScale());
    img = tryLoadResource(dir + name + ".png");
    if(img != null) return scaleToDeviceRes(img, ChessConstant.PIECE_WIDTH, ChessConstant.PIECE_HEIGHT, getDeviceScale());
    img = tryLoadResource(dir + name);
    if(img != null) return scaleToDeviceRes(img, ChessConstant.PIECE_WIDTH, ChessConstant.PIECE_HEIGHT, getDeviceScale());
    return null;
  }

  private BufferedImage tryLoadResource(String resourcePath)
  {
    try
    {
      java.net.URL url = getClass().getResource(resourcePath);
      if(url != null)
      {
        return ImageIO.read(url);
      }
    }
    catch(IOException e)
    {
      java.util.logging.Logger.getLogger(ChessPanel.class.getName())
          .log(java.util.logging.Level.FINE, "加载资源失败: " + resourcePath, e);
    }
    return null;
  }

  private BufferedImage loadImageFromResource(String resourcePath)
  {
    try
    {
      java.net.URL url = getClass().getResource(resourcePath);
      if(url != null)
      {
        return ImageIO.read(url);
      }
      java.util.logging.Logger.getLogger(ChessPanel.class.getName())
          .warning("未找到资源: " + resourcePath);
    }
    catch(IOException e)
    {
      java.util.logging.Logger.getLogger(ChessPanel.class.getName())
          .log(java.util.logging.Level.WARNING, "读取资源失败: " + resourcePath, e);
    }
    return null;
  }

  public BufferedImage getPiece(int index)
  {
    return pieceImageArray[index];
  }

  /**
   * 获取当前屏幕的 HiDPI 设备像素倍率（例如 1.0 / 1.25 / 1.5 / 2.0）
   */
  private static double getDeviceScale()
  {
    GraphicsConfiguration gc = GraphicsEnvironment
        .getLocalGraphicsEnvironment()
        .getDefaultScreenDevice()
        .getDefaultConfiguration();
    AffineTransform tx = gc.getDefaultTransform();
    return tx.getScaleX();
  }

  /**
   * 将图片预缩放到目标逻辑尺寸 × 设备倍率的设备像素，避免 HiDPI 运行时拉伸模糊。
   */
  private static BufferedImage scaleToDeviceRes(BufferedImage src, int logicalW, int logicalH, double scale)
  {
    if(src == null)
    {
      return null;
    }
    int targetW = (int)Math.round(logicalW * scale);
    int targetH = (int)Math.round(logicalH * scale);
    if(targetW == src.getWidth() && targetH == src.getHeight())
    {
      return src;
    }
    BufferedImage dst = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = dst.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.drawImage(src, 0, 0, targetW, targetH, null);
    g.dispose();
    return dst;
  }

  @Override
  public void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D)g;
    // 高质量渲染提示：图片已预缩放至设备像素，绘制时使用 BILINEAR 保持清晰
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    if(imgBoard != null)
    {
      // 以棋盘逻辑尺寸绘制，防止拉伸
      int boardLogW = ChessConstant.CHESSBOARD_MARGIN * 2 + ChessConstant.GRID_WIDTH * 8;
      int boardLogH = ChessConstant.CHESSBOARD_MARGIN * 2 + ChessConstant.GRID_WIDTH * 9;
      g2.drawImage(imgBoard, 0, 0, boardLogW, boardLogH, this);
    }
    String boardName = ChessConstant.BOARD_NAME[Config.get().getBoard()];
    if(boardName != null && boardName.startsWith("bg_"))
    {
      drawGrid(g2);
    }
    // 走法提示高亮（绘制在棋子下方）
    if(hintFrom != -1)
    {
      drawHintHighlight(g2, hintFrom, new Color(50, 200, 50));
    }
    if(hintTo != -1)
    {
      drawHintHighlight(g2, hintTo, new Color(255, 165, 0));
    }
    drawIndicators(g2);
    if(Config.get().getCoordinate() == 0)
    {
      drawNumbers(g2);
    }
    else if(Config.get().getCoordinate() == 1)
    {
      drawICCSNumbers(g2);
    }
    else
    {
      drawICoordinates(g2);
    }
    // 将军高亮（在棋子下方绘制红色光圈）
    drawCheckKingHighlight(g2);
    drawPieces(g2);

    // ── 动画中的棋子：绘制在插值像素坐标处 ───────────────────────────
    if(animating && animPieceImg != null)
    {
      float t = animProgress;
      float easedT = t * t * (3 - 2 * t); // smoothstep 缓动
      int ax = (int)(animFromPx + (animToPx - animFromPx) * easedT);
      int ay = (int)(animFromPy + (animToPy - animFromPy) * easedT);
      drawPieceAt(g2, animPieceImg, ax, ay);
    }
  }

  /**
   * 绘制走法提示高亮圆圈（半透明）
   */
  private void drawHintHighlight(Graphics2D g2, int pos, Color color)
  {
    int col = pos % 9;
    int row = pos / 9;
    if(diverted)
    {
      col = 8 - col;
      row = 9 - row;
    }
    int cx = ChessConstant.CHESSBOARD_MARGIN + col * ChessConstant.GRID_WIDTH;
    int cy = ChessConstant.CHESSBOARD_MARGIN + row * ChessConstant.GRID_WIDTH;
    int r = ChessConstant.PIECE_WIDTH / 2 + 5;
    Composite oldComposite = g2.getComposite();
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
    g2.setColor(color);
    g2.fillOval(cx - r, cy - r, r * 2, r * 2);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
    g2.setColor(color.darker());
    g2.setStroke(new BasicStroke(2.5f));
    g2.drawOval(cx - r, cy - r, r * 2, r * 2);
    g2.setComposite(oldComposite);
  }

  /**
   * 绘制将军高亮（多层脉冲+旋转虚线+火焰渐变+四向尖刺）
   */
  private void drawCheckKingHighlight(Graphics2D g2)
  {
    if(checkedKingPos < 0)
      return;

    int col = checkedKingPos % 9;
    int row = checkedKingPos / 9;
    if(diverted)
    {
      col = 8 - col;
      row = 9 - row;
    }
    int cx = ChessConstant.CHESSBOARD_MARGIN + col * ChessConstant.GRID_WIDTH;
    int cy = ChessConstant.CHESSBOARD_MARGIN + row * ChessConstant.GRID_WIDTH;
    int r  = ChessConstant.PIECE_WIDTH / 2 + 6;

    float phase = checkAnimPhase;
    // 0→1 平滑脉冲（smoothstep 更舒适）
    float sinVal = (float)(0.5 + 0.5 * Math.sin(phase * 2 * Math.PI));
    float pulse  = sinVal * sinVal * (3 - 2 * sinVal);

    Composite oldComp   = g2.getComposite();
    Stroke    oldStroke = g2.getStroke();
    Paint     oldPaint  = g2.getPaint();

    // ── 层1：外扩光晕（随脉冲膨胀，向外淡出） ──────────────────────────
    int   haloR     = r + 10 + (int)(pulse * 14);
    float haloAlpha = 0.45f - pulse * 0.30f;
    Color haloInner = new Color(255, 50, 0, Math.max(0, (int)(haloAlpha * 255)));
    RadialGradientPaint haloGrad = new RadialGradientPaint(
        cx, cy, haloR,
        new float[]{ 0f, 1f },
        new Color[]{ haloInner, new Color(255, 20, 0, 0) });
    g2.setPaint(haloGrad);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    g2.fillOval(cx - haloR, cy - haloR, haloR * 2, haloR * 2);

    // ── 层2：内部火焰径向渐变（黄→橙→红→透明） ────────────────────────
    float fireAlpha = 0.50f + pulse * 0.25f;
    RadialGradientPaint fireGrad = new RadialGradientPaint(
        cx, cy, r,
        new float[]{ 0f, 0.30f, 0.65f, 1f },
        new Color[]{
            new Color(255, 240, 100, 220),
            new Color(255, 130,  10, 180),
            new Color(200,  10,  10, 100),
            new Color(120,   0,   0,   0)
        });
    g2.setPaint(fireGrad);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fireAlpha));
    g2.fillOval(cx - r, cy - r, r * 2, r * 2);

    // ── 层3：鲜红实心内圈（随脉冲闪烁） ──────────────────────────────────
    g2.setPaint(null);
    g2.setColor(new Color(255, 30, 30));
    g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f + pulse * 0.25f));
    g2.drawOval(cx - r, cy - r, r * 2, r * 2);

    // ── 层4：橙红中圈（轻微随脉冲缩放） ────────────────────────────────
    int r2 = r + 5 + (int)(pulse * 4);
    g2.setColor(new Color(255, 90, 0));
    g2.setStroke(new BasicStroke(2.0f));
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
    g2.drawOval(cx - r2, cy - r2, r2 * 2, r2 * 2);

    // ── 层5：旋转虚线外圈（360°/s） ────────────────────────────────────
    int r3 = r + 13;
    Graphics2D g2r = (Graphics2D)g2.create();
    g2r.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2r.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.80f));
    g2r.setColor(new Color(255, 70, 70));
    g2r.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
        1f, new float[]{ 7f, 5f }, 0f));
    g2r.rotate(Math.toRadians(phase * 360.0), cx, cy);
    g2r.drawOval(cx - r3, cy - r3, r3 * 2, r3 * 2);
    // 反向第二圈（节奏感）
    g2r.setColor(new Color(255, 200, 60));
    g2r.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
        1f, new float[]{ 4f, 8f }, 0f));
    g2r.rotate(Math.toRadians(180.0), cx, cy);
    g2r.drawOval(cx - r3, cy - r3, r3 * 2, r3 * 2);
    g2r.dispose();

    // ── 层6：四向尖刺（随脉冲伸缩，目标感） ────────────────────────────
    int spikeLen = r + 5 + (int)(pulse * 8);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f + pulse * 0.20f));
    g2.setColor(new Color(255, 70, 0));
    g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    int gap = r - 4;
    g2.drawLine(cx,       cy - gap, cx,       cy - spikeLen); // 上
    g2.drawLine(cx,       cy + gap, cx,       cy + spikeLen); // 下
    g2.drawLine(cx - gap, cy,       cx - spikeLen, cy);       // 左
    g2.drawLine(cx + gap, cy,       cx + spikeLen, cy);       // 右

    // ── 还原 ──────────────────────────────────────────────────────────
    g2.setComposite(oldComp);
    g2.setStroke(oldStroke);
    g2.setPaint(oldPaint);
  }

  // ── 将军脉冲动画控制 ─────────────────────────────────────────────────

  private void startCheckAnimation()
  {
    if(checkAnimTimer != null && checkAnimTimer.isRunning())
      return;
    checkAnimPhase = 0f;
    checkAnimTimer = new Timer(30, e -> {
      checkAnimPhase = (checkAnimPhase + 0.04f) % 1.0f;
      repaint();
    });
    checkAnimTimer.start();
  }

  private void stopCheckAnimation()
  {
    if(checkAnimTimer != null)
    {
      checkAnimTimer.stop();
      checkAnimTimer = null;
    }
    checkAnimPhase = 0f;
  }

  /**
   * 计算当前局面中被将的王/帅位置（-1 = 无将军）。
   * 在每次 update() 后调用，缓存结果用于绘制高亮。
   */
  private int computeCheckedKingPos()
  {
    try
    {
      Position pos = controller.currentPosition();
      if(pos != null && ChessRules.beChecked(pos))
      {
        return ChessRules.findKingPos(pos);
      }
    }
    catch(Exception ignored)
    {
    }
    return -1;
  }

  /**
   * 快速局面评分（红方视角）+ 挂子惩罚。
   * 在人类走棋后调用（此时轮到对方/黑方），检测是否有红方棋子被吃，
   * 避免纯材料计算把"让子送吃"误判为"好棋"。
   */
  private int evalWithHangingPenalty(Position currentPos)
  {
    if(currentPos == null)
      return 0;
    Piece[] board = currentPos.getPosArray();
    int baseScore = quickEval(board);
    // currentPos.getSide() 是对方（黑方），找出对方能立刻吃掉的最大红方子价值
    try
    {
      List<Move> opponentMoves = ChessRules.enumMoves(currentPos);
      int maxHanging = 0;
      for(Move m : opponentMoves)
      {
        Piece t = board[m.to];
        if(t != null && !t.isEmpty() && t.getSide() == Side.Red && !t.isKing())
        {
          int v = evalPieceValue(t);
          if(v > maxHanging)
            maxHanging = v;
        }
      }
      // 从红方角度扣分：对方能免费（或低价）吃到的最大子价值
      baseScore -= maxHanging;
    }
    catch(Exception ignored)
    {
    }
    return baseScore;
  }

  /**
   * 绘制所有象棋棋子
   *
   * @param g2
   */
  private void drawPieces(Graphics2D g2)
  {
    for(int pos = 0; pos < posArray.length; pos++)
    {
      // 正在动画的棋子在目标格跳过（在 paintComponent 末尾单独绘制插值位置）
      if(animating && pos == animToPos) continue;
      Piece chessId = posArray[pos];
      if(chessId != null && !chessId.isEmpty())
      {
        drawPiece(g2, getPiece(chessId.getResIndex()), pos);
      }
    }
  }

  public void drawChessIndex(Graphics2D g2, int pos)
  {
    String indexString = String.valueOf(pos);
    FontMetrics fm1 = g2.getFontMetrics(new Font(ChessConstant.FONT_NAME[0], Font.PLAIN, 25));
    int textX = fm1.stringWidth(indexString) / 2;
    g2.setColor(ChessConstant.WORD_COLOR);
    int x = pos % 9;
    int y = pos / 9;
    g2.drawString(indexString, ChessConstant.CHESSBOARD_MARGIN + x * ChessConstant.GRID_WIDTH - textX,
        ChessConstant.CHESSBOARD_MARGIN + y * ChessConstant.GRID_WIDTH + textX);
  }

  private void drawPiece(Graphics2D g2, BufferedImage bitmap, int pos)
  {
    if(bitmap == null)
    {
      return;
    }
    int x = pos % 9;
    int y = pos / 9;
    if(diverted)
    {
      x = 8 - x;
      y = 9 - y;
    }
    int dx = (ChessConstant.GRID_WIDTH / 2 - ChessConstant.PIECE_WIDTH / 2);
    int dy = (ChessConstant.GRID_WIDTH / 2 - ChessConstant.PIECE_WIDTH / 2);
    int px = ChessConstant.CHESSBOARD_MARGIN + x * ChessConstant.GRID_WIDTH - ChessConstant.GRID_WIDTH / 2 + dx;
    int py = ChessConstant.CHESSBOARD_MARGIN + y * ChessConstant.GRID_WIDTH - ChessConstant.GRID_WIDTH / 2 + dy;
    drawPieceAt(g2, bitmap, px, py);
  }

  /**
   * 在像素坐标 (px, py) 处绘制棋子，包含阴影 + 光照效果
   */
  private void drawPieceAt(Graphics2D g2, BufferedImage bitmap, int px, int py)
  {
    if(bitmap == null)
      return;
    int pw = ChessConstant.PIECE_WIDTH;
    int ph = ChessConstant.PIECE_HEIGHT;

    // ── 阴影：棋子正下方半透明椭圆 ─────────────────────────────────
    Composite oldComp = g2.getComposite();
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.32f));
    g2.setColor(Color.BLACK);
    g2.fillOval(px + 5, py + 7, pw - 6, ph - 8);
    g2.setComposite(oldComp);

    // ── 棋子图片（逻辑宽高 PIECE_WIDTH × PIECE_HEIGHT，配合预缩放） ──
    g2.drawImage(bitmap, px, py, pw, ph, this);

    // ── 光照：径向渐变高光，模拟顶部打光 ──────────────────────────
    float cx = px + pw * 0.38f;
    float cy = py + ph * 0.30f;
    float r = pw * 0.48f;
    RadialGradientPaint rgp = new RadialGradientPaint(cx, cy, r, new float[] { 0f, 1f },
        new Color[] { new Color(255, 255, 255, 72), new Color(255, 255, 255, 0) });
    Composite lightComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
    g2.setComposite(lightComp);
    g2.setPaint(rgp);
    g2.fillOval(px, py, pw, ph);
    g2.setComposite(oldComp);
    g2.setPaint(null);
  }

  private void drawIndicators(Graphics2D g2)
  {
    if(from != -1)
    {
      BufferedImage bitmap;
      if(to != -1)
      {
        bitmap = getPiece(15);
      }
      else
      {
        bitmap = getPiece(14);
      }
      drawPiece(g2, bitmap, from);
    }
    if(to != -1)
    {
      drawPiece(g2, getPiece(14), to);
    }
  }

  private void drawNumbers(Graphics2D g2)
  {
    g2.setColor(ChessConstant.WORD_COLOR);
    Font font = new Font(ChessConstant.FONT_NAME[0], Font.PLAIN, 25);
    g2.setFont(font);
    FontMetrics fm = g2.getFontMetrics(font);
    for(int i = 0; i < 9; i++)
    {
      fontWidth = fm.stringWidth(ChessConstant.blackMarkNumbers[i]);
      fontHeight = fm.getHeight();
      int width = fontWidth;
      g2.drawString(ChessConstant.blackMarkNumbers[i], ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH - width / 2,
          ChessConstant.CHESSBOARD_MARGIN / 4 + ChessConstant.GRID_WIDTH / 4);
    }
    for(int i = 0; i < 9; i++)
    {
      fontWidth = fm.stringWidth(ChessConstant.redMarkNumbers[i]);
      fontHeight = fm.getHeight();
      int width = fontWidth;
      g2.drawString(ChessConstant.redMarkNumbers[i], ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH - width / 2,
          ChessConstant.CHESSBOARD_MARGIN + ChessConstant.GRID_WIDTH * 9 + ChessConstant.GRID_WIDTH / 4
              + ChessConstant.CHESSBOARD_MARGIN / 2);
    }
  }

  private void drawICCSNumbers(Graphics2D g2)
  {
    g2.setColor(ChessConstant.WORD_COLOR);
    Font font = new Font(ChessConstant.FONT_NAME[0], Font.PLAIN, 25);
    g2.setFont(font);
    FontMetrics fm = g2.getFontMetrics(font);
    for(int i = 0; i < 9; i++)
    {
      fontWidth = fm.stringWidth(ChessConstant.iccsHorizontalNumbers[i]);
      fontHeight = fm.getHeight();
      int width = fontWidth;
      g2.drawString(ChessConstant.iccsHorizontalNumbers[i], ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH - width / 2,
          ChessConstant.CHESSBOARD_MARGIN / 4 + ChessConstant.GRID_WIDTH / 4);
    }
    for(int i = 0; i < 9; i++)
    {
      fontWidth = fm.stringWidth(ChessConstant.iccsHorizontalNumbers[i]);
      fontHeight = fm.getHeight();
      int width = fontWidth;
      g2.drawString(ChessConstant.iccsHorizontalNumbers[i], ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH - width / 2,
          ChessConstant.CHESSBOARD_MARGIN + ChessConstant.GRID_WIDTH * 9 + ChessConstant.GRID_WIDTH / 4
              + ChessConstant.CHESSBOARD_MARGIN / 2);
    }
    for(int i = 0; i <= 9; i++)
    {
      fontWidth = fm.stringWidth(ChessConstant.iccsVerticalNumbers[i]);
      fontHeight = fm.getHeight();
      int width = fontWidth;
      g2.drawString(ChessConstant.iccsVerticalNumbers[9 - i], ChessConstant.CHESSBOARD_MARGIN / 4,
          ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH + width / 2);
    }
  }

  private void drawICoordinates(Graphics2D g2)
  {
    g2.setColor(ChessConstant.WORD_COLOR);
    Font font = new Font(ChessConstant.FONT_NAME[0], Font.PLAIN, 25);
    g2.setFont(font);
    FontMetrics fm = g2.getFontMetrics(font);
    for(int i = 0; i < 9; i++)
    {
      fontWidth = fm.stringWidth(ChessConstant.xIndex[i]);
      fontHeight = fm.getHeight();
      int width = fontWidth;
      g2.drawString(ChessConstant.xIndex[i], ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH - width / 2,
          ChessConstant.CHESSBOARD_MARGIN / 4 + ChessConstant.GRID_WIDTH / 4);
    }
    for(int i = 0; i < 9; i++)
    {
      fontWidth = fm.stringWidth(ChessConstant.xIndex[i]);
      fontHeight = fm.getHeight();
      int width = fontWidth;
      g2.drawString(ChessConstant.xIndex[i], ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH - width / 2,
          ChessConstant.CHESSBOARD_MARGIN + ChessConstant.GRID_WIDTH * 9 + ChessConstant.GRID_WIDTH / 4
              + ChessConstant.CHESSBOARD_MARGIN / 2);
    }
    for(int i = 0; i <= 9; i++)
    {
      fontWidth = fm.stringWidth(ChessConstant.yIndex[i]);
      fontHeight = fm.getHeight();
      int width = fontWidth;
      g2.drawString(ChessConstant.yIndex[i], ChessConstant.CHESSBOARD_MARGIN / 4,
          ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH + width / 2);
    }
  }

  private void drawGrid(Graphics2D g2)
  {
    g2.setColor(ChessConstant.LINE_COLOR);
    g2.setStroke(new BasicStroke(2.0f));
    Font f = new Font(ChessConstant.FONT_NAME[2], Font.BOLD, 30);
    g2.setFont(f);
    g2.drawRect(ChessConstant.CHESSBOARD_MARGIN, ChessConstant.CHESSBOARD_MARGIN, ChessConstant.GRID_WIDTH * 8,
        ChessConstant.GRID_WIDTH * 9);
    for(int i = 0; i <= 9; i++)
    {
      g2.drawLine(ChessConstant.CHESSBOARD_MARGIN, ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH,
          ChessConstant.CHESSBOARD_MARGIN + ChessConstant.GRID_WIDTH * 8, ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH);
    }
    Font f2 = new Font(ChessConstant.FONT_NAME[2], Font.PLAIN, 32);
    g2.setFont(f2);
    g2.drawString("楚河", ChessConstant.CHESSBOARD_MARGIN + ChessConstant.GRID_WIDTH,
        ChessConstant.CHESSBOARD_MARGIN + 4 * ChessConstant.GRID_WIDTH + (int)(ChessConstant.GRID_WIDTH * 2 / 3));
    g2.drawString("汉界", ChessConstant.CHESSBOARD_MARGIN + ChessConstant.GRID_WIDTH * 6,
        ChessConstant.CHESSBOARD_MARGIN + 4 * ChessConstant.GRID_WIDTH + (int)(ChessConstant.GRID_WIDTH * 2 / 3));
    for(int i = 0; i < 9; i++)
    {
      g2.drawLine(ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH, ChessConstant.CHESSBOARD_MARGIN,
          ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH, ChessConstant.CHESSBOARD_MARGIN + 4 * ChessConstant.GRID_WIDTH);
    }
    for(int i = 0; i < 9; i++)
    {
      g2.drawLine(ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH,
          ChessConstant.CHESSBOARD_MARGIN + 5 * ChessConstant.GRID_WIDTH, ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH,
          ChessConstant.CHESSBOARD_MARGIN + 9 * ChessConstant.GRID_WIDTH);
    }
    g2.drawLine(ChessConstant.CHESSBOARD_MARGIN + 3 * ChessConstant.GRID_WIDTH, ChessConstant.CHESSBOARD_MARGIN,
        ChessConstant.CHESSBOARD_MARGIN + 5 * ChessConstant.GRID_WIDTH, ChessConstant.CHESSBOARD_MARGIN + 2 * ChessConstant.GRID_WIDTH);
    g2.drawLine(ChessConstant.CHESSBOARD_MARGIN + 5 * ChessConstant.GRID_WIDTH, ChessConstant.CHESSBOARD_MARGIN,
        ChessConstant.CHESSBOARD_MARGIN + 3 * ChessConstant.GRID_WIDTH, ChessConstant.CHESSBOARD_MARGIN + 2 * ChessConstant.GRID_WIDTH);
    g2.drawLine(ChessConstant.CHESSBOARD_MARGIN + 3 * ChessConstant.GRID_WIDTH,
        ChessConstant.CHESSBOARD_MARGIN + 7 * ChessConstant.GRID_WIDTH, ChessConstant.CHESSBOARD_MARGIN + 5 * ChessConstant.GRID_WIDTH,
        ChessConstant.CHESSBOARD_MARGIN + 9 * ChessConstant.GRID_WIDTH);
    g2.drawLine(ChessConstant.CHESSBOARD_MARGIN + 5 * ChessConstant.GRID_WIDTH,
        ChessConstant.CHESSBOARD_MARGIN + 7 * ChessConstant.GRID_WIDTH, ChessConstant.CHESSBOARD_MARGIN + 3 * ChessConstant.GRID_WIDTH,
        ChessConstant.CHESSBOARD_MARGIN + 9 * ChessConstant.GRID_WIDTH);
  }

  private void initButtons()
  {
    setLayout(null);

    int rightX = ChessConstant.CHESSBOARD_MARGIN + ChessConstant.GRID_WIDTH * 9;

    // ── 右侧整体容器（垂直 BoxLayout） ──────────────────────────────
    JPanel rightPanel = new JPanel();
    rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
    // 右侧面板高度与左侧棋盘保持一致：
    //   棋盘高度 = CHESSBOARD_MARGIN(68)*2 + GRID_WIDTH(68)*9 = 748px
    //   右侧 y 偏移 = 8，故容器高度取 748 - 8 = 740px
    rightPanel.setBounds(rightX, 8, RIGHT_PANEL_WIDTH, 740);

    // ── 1. 棋谱 / 引擎思考信息 TabbedPane ────────────────────────
    // 高度从 570 压缩至 370，为下方操作/设置/分析面板腾出空间
    tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    tabbedPane.setMaximumSize(new Dimension(RIGHT_PANEL_WIDTH, 370));
    tabbedPane.setPreferredSize(new Dimension(RIGHT_PANEL_WIDTH, 370));

    chessHistoryPane = new JScrollPane();
    chessHistoryPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    chessHistoryPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    tabbedPane.addTab("棋谱信息", null, chessHistoryPane, null);
    stepTextArea = new JTextArea();
    stepTextArea.setFont(new Font(ChessConstant.FONT_NAME[2], Font.PLAIN, 16));
    stepTextArea.setEditable(false);
    // NEVER_UPDATE：允许用户自由上下滚动查看历史；走棋时手动滚到底部
    DefaultCaret stepTextCaret = (DefaultCaret)stepTextArea.getCaret();
    stepTextCaret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
    chessHistoryPane.setViewportView(stepTextArea);

    engineInfoPane = new JScrollPane();
    engineInfoPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    engineInfoPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    tabbedPane.addTab("引擎思考信息", null, engineInfoPane, null);
    engineTextArea = new JTextArea();
    engineTextArea.setFont(new Font(ChessConstant.FONT_NAME[2], Font.PLAIN, 14));
    engineTextArea.setEditable(false);
    // NEVER_UPDATE：允许用户查看历史；引擎信息通过 append 自动追加在底部
    DefaultCaret engineTextCaret = (DefaultCaret)engineTextArea.getCaret();
    engineTextCaret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
    engineInfoPane.setViewportView(engineTextArea);
    tabbedPane.setSelectedIndex(0);
    rightPanel.add(tabbedPane);
    rightPanel.add(Box.createVerticalStrut(6));

    // ── 2. 操作按钮面板 ───────────────────────────────────────────
    // GridLayout(1, 0) = 1 行、列数随按钮数自动确定，保证始终同行
    JPanel toolPanel = new JPanel(new GridLayout(1, 0, 4, 0));
    toolPanel.setBorder(BorderFactory.createTitledBorder("操作"));
    toolPanel.setMaximumSize(new Dimension(RIGHT_PANEL_WIDTH, 55));
    toolPanel.setPreferredSize(new Dimension(RIGHT_PANEL_WIDTH, 55));

    btnNewGame  = new JButton("新棋局");
    btnSave     = new JButton("存棋谱");
    btnRetract  = new JButton("悔棋");
    btnCopy     = new JButton("复制局面");
    btnAnalyze  = new JButton("分析棋局");
    btnHint     = new JButton("走法提示");

    toolPanel.add(btnNewGame);
    toolPanel.add(btnSave);
    toolPanel.add(btnRetract);
    toolPanel.add(btnCopy);
    toolPanel.add(btnAnalyze);
    toolPanel.add(btnHint);
    rightPanel.add(toolPanel);
    rightPanel.add(Box.createVerticalStrut(6));

    // ── 3. 设置面板 ───────────────────────────────────────────────
    JPanel settingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
    settingPanel.setBorder(BorderFactory.createTitledBorder("设置"));
    settingPanel.setMaximumSize(new Dimension(RIGHT_PANEL_WIDTH, 90));
    settingPanel.setPreferredSize(new Dimension(RIGHT_PANEL_WIDTH, 90));

    btnChangeBoard = new JComboBox<>();
    btnChangeBoard.setPreferredSize(new Dimension(120, 25));
    btnChangeBoard.addItem("更换棋盘");
    for(int i = 0; i < ChessConstant.BOARD_NAME.length; i++)
    {
      btnChangeBoard.addItem(ChessConstant.BOARD_NAME[i]);
    }
    btnChangeBoard.setSelectedIndex(Config.get().getBoard() + 1);

    btnChangePiece = new JComboBox<>();
    btnChangePiece.setPreferredSize(new Dimension(120, 25));
    btnChangePiece.addItem("更换棋子");
    for(int i = 0; i < ChessConstant.PIECES_NAME.length; i++)
    {
      btnChangePiece.addItem(ChessConstant.PIECES_NAME[i]);
    }
    btnChangePiece.setSelectedIndex(Config.get().getPieces() + 1);

    btnChangeCoordinate = new JComboBox<>();
    btnChangeCoordinate.setPreferredSize(new Dimension(110, 25));
    btnChangeCoordinate.addItem("更换坐标");
    for(int i = 0; i < ChessConstant.COORDINATE_NAME.length; i++)
    {
      btnChangeCoordinate.addItem(ChessConstant.COORDINATE_NAME[i]);
    }
    btnChangeCoordinate.setSelectedIndex(Config.get().getCoordinate() + 1);

    btnPaste = new JComboBox<>();
    btnPaste.setPreferredSize(new Dimension(120, 25));
    btnPaste.addItem("更换引擎");
    for(int i = 0; i < ChessConstant.ENGINE_NAME.length; i++)
    {
      btnPaste.addItem(ChessConstant.ENGINE_NAME[i]);
    }
    btnPaste.setSelectedIndex(Config.get().getEngine() + 1);

    // ── 对战模式 / AI 难度 / AI 类型 ──────────────────────────────
    btnGameMode = new JComboBox<>();
    btnGameMode.setPreferredSize(new Dimension(108, 25));
    btnGameMode.addItem("人机对战");
    btnGameMode.addItem("AI 对战");
    btnGameMode.addItem("双人对战");
    btnGameMode.setSelectedIndex(0);

    btnAiDepth = new JComboBox<>();
    btnAiDepth.setPreferredSize(new Dimension(108, 25));
    btnAiDepth.addItem("难度1（最快）");
    btnAiDepth.addItem("难度2");
    btnAiDepth.addItem("难度3");
    btnAiDepth.addItem("难度4");
    btnAiDepth.addItem("难度5");
    btnAiDepth.addItem("难度6（最强）");
    btnAiDepth.setSelectedIndex(2); // 默认深度3

    btnAiType = new JComboBox<>();
    btnAiType.setPreferredSize(new Dimension(108, 25));
    btnAiType.addItem("UCCI引擎");
    btnAiType.addItem("本地 AI");
    btnAiType.setSelectedIndex(1);

    settingPanel.add(btnChangeBoard);
    settingPanel.add(btnChangePiece);
    settingPanel.add(btnChangeCoordinate);
    settingPanel.add(btnPaste);
    settingPanel.add(btnGameMode);
    settingPanel.add(btnAiDepth);
    settingPanel.add(btnAiType);
    rightPanel.add(settingPanel);
    rightPanel.add(Box.createVerticalStrut(6));

    // ── 4. 局势分析结果面板 ───────────────────────────────────────
    JPanel analysisContainer = new JPanel(new BorderLayout(0, 4));
    analysisContainer.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(), "局势分析",
        TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
        new Font(ChessConstant.FONT_NAME[2], Font.BOLD, 14)));
    analysisContainer.setMaximumSize(new Dimension(RIGHT_PANEL_WIDTH, 215));
    analysisContainer.setPreferredSize(new Dimension(RIGHT_PANEL_WIDTH, 215));

    analysisTextArea = new JTextArea("点击「分析棋局」或「走法提示」查看分析结果。");
    analysisTextArea.setFont(new Font(ChessConstant.FONT_NAME[2], Font.PLAIN, 15));
    analysisTextArea.setEditable(false);
    analysisTextArea.setLineWrap(true);
    analysisTextArea.setWrapStyleWord(true);
    analysisTextArea.setBackground(new Color(250, 248, 240));
    JScrollPane analysisScrollPane = new JScrollPane(analysisTextArea);
    analysisScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    analysisContainer.add(analysisScrollPane, BorderLayout.CENTER);
    rightPanel.add(analysisContainer);

    add(rightPanel);

    // ── 5. AI 讲解面板（棋盘正下方） ─────────────────────────────────
    int boardBottom = ChessConstant.CHESSBOARD_MARGIN * 2 + ChessConstant.GRID_WIDTH * 9 + 14;
    int commentaryW = ChessConstant.CHESSBOARD_MARGIN * 2 + ChessConstant.GRID_WIDTH * 8 - 8;

    JTabbedPane commentaryPane = new JTabbedPane(JTabbedPane.TOP);
    commentaryPane.setBounds(4, boardBottom, commentaryW, 186);

    aiCommentaryArea = new JTextArea("AI讲解：等待走棋…");
    aiCommentaryArea.setFont(new Font(ChessConstant.FONT_NAME[2], Font.PLAIN, 13));
    aiCommentaryArea.setEditable(false);
    aiCommentaryArea.setLineWrap(true);
    aiCommentaryArea.setWrapStyleWord(true);
    JScrollPane aiScroll = new JScrollPane(aiCommentaryArea);
    aiScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    commentaryPane.addTab("AI 讲解", aiScroll);

    opponentCommentaryArea = new JTextArea("对手点评：等待走棋…");
    opponentCommentaryArea.setFont(new Font(ChessConstant.FONT_NAME[2], Font.PLAIN, 13));
    opponentCommentaryArea.setEditable(false);
    opponentCommentaryArea.setLineWrap(true);
    opponentCommentaryArea.setWrapStyleWord(true);
    JScrollPane oppScroll = new JScrollPane(opponentCommentaryArea);
    oppScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    commentaryPane.addTab("对手点评", oppScroll);

    // ── 弹幕/聊天输入 tab ───────────────────────────────────────────
    JPanel danmakuChatPanel = new JPanel(new BorderLayout(4, 4));
    danmakuChatPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
    JLabel danmakuTip = new JLabel("在线对局时可向房间内所有人发送弹幕：");
    danmakuTip.setFont(new Font(ChessConstant.FONT_NAME[2], Font.PLAIN, 13));
    danmakuInputField = new JTextField();
    danmakuInputField.setFont(new Font(ChessConstant.FONT_NAME[2], Font.PLAIN, 14));
    danmakuInputField.setToolTipText("输入弹幕内容后按 Enter 或点击「发送」");
    JButton btnSendDanmaku = new JButton("发送弹幕");
    btnSendDanmaku.setFont(new Font(ChessConstant.FONT_NAME[2], Font.BOLD, 13));
    JPanel danmakuInputRow = new JPanel(new BorderLayout(4, 0));
    danmakuInputRow.add(danmakuInputField, BorderLayout.CENTER);
    danmakuInputRow.add(btnSendDanmaku, BorderLayout.EAST);
    danmakuChatPanel.add(danmakuTip, BorderLayout.NORTH);
    danmakuChatPanel.add(danmakuInputRow, BorderLayout.CENTER);

    ActionListener sendDanmaku = e -> {
      String text = danmakuInputField.getText().trim();
      if(text.isEmpty())
      {
        return;
      }
      if(onlineRoomId == null || !ChessClient.getInstance().isConnected())
      {
        new ToastFrame("请先加入在线房间！", 1500);
        return;
      }
      ChessClient.getInstance().send(NetMessage.chat(onlineRoomId, text));
      danmakuInputField.setText("");
    };
    btnSendDanmaku.addActionListener(sendDanmaku);
    danmakuInputField.addActionListener(sendDanmaku); // Enter 键也发送

    commentaryPane.addTab("弹幕聊天 💬", danmakuChatPanel);

    add(commentaryPane);

    // ── 6. 弹幕浮层（透明，覆盖棋盘区域） ───────────────────────────
    int boardLogW = ChessConstant.CHESSBOARD_MARGIN * 2 + ChessConstant.GRID_WIDTH * 8;
    int boardLogH = ChessConstant.CHESSBOARD_MARGIN * 2 + ChessConstant.GRID_WIDTH * 9;
    danmuPanel = new DanmuPanel();
    danmuPanel.setBounds(0, 0, boardLogW, boardLogH);
    danmuPanel.setVisible(false); // 只在联网模式下显示
    add(danmuPanel); // 后加入 = Z 轴最高（浮于棋盘之上）
  }

  public int getFrom()
  {
    return from;
  }

  public void setFrom(int from)
  {
    this.from = from;
  }

  public int getTo()
  {
    return to;
  }

  public void setTo(int to)
  {
    this.to = to;
  }

  public BufferedImage getImgBoard()
  {
    return imgBoard;
  }

  public void setImgBoard(BufferedImage imgBoard)
  {
    this.imgBoard = imgBoard;
  }
}
