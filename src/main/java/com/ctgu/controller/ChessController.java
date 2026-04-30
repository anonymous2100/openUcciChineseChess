package com.ctgu.controller;

import com.ctgu.config.Config;
import com.ctgu.constant.ChessConstant;
import com.ctgu.engine.EngineMonitor;
import com.ctgu.engine.UcciEngine;
import com.ctgu.enums.Piece;
import com.ctgu.enums.Side;
import com.ctgu.event.EventMessage;
import com.ctgu.model.AnalysisResult;
import com.ctgu.model.GameContext;
import com.ctgu.model.Move;
import com.ctgu.model.Position;
import com.ctgu.view.ToastFrame;
import org.greenrobot.eventbus.EventBus;

import java.util.logging.Logger;

public class ChessController implements EngineMonitor
{
	public static final int MAX_STEPS_FOR_DRAW = 120;
  /**
   * 分析请求的思考时间（毫秒）
   */
  private static final int ANALYSIS_THINK_TIME_MS = 3000;

	private UcciEngine engine;
	private GameContext gameContext;
	private boolean paused;
	private long thinkStart;

  // ── 分析模式状态 ────────────────────────────────────────────────
  private volatile boolean analysisMode = false;
  private volatile boolean hintMode = false;
  private int lastAnalysisScore = 0;
  private boolean lastAnalysisIsMate = false;
  private int lastAnalysisMateIn = 0;
  private int lastAnalysisDepth = 0;
  private String lastAnalysisPv = "";

	public ChessController(String engineFileName)
	{
		if (!UcciEngine.installed(engineFileName))
		{
			new ToastFrame("引擎文件不存在！");
		}
		engine = UcciEngine.getInstance();
		gameContext = new GameContext();
	}

	public void onResponse(String line)
  {
    // 分析模式下解析 info 行（无延迟，直接返回）
    if(analysisMode && line.startsWith("info "))
    {
      parseInfoLine(line);
      EventBus.getDefault().post(new EventMessage(ChessConstant.MSG_ENGINE_THINKING, line));
      return;
    }

    // 正常对弈模式：保证引擎至少思考 1 秒，避免极速落子体验差
    // 分析模式的 bestmove/nobestmove 不应再额外等待
    if(!analysisMode)
    {
      long timeUsed = System.currentTimeMillis() - thinkStart;
      if(timeUsed < 1000)
      {
        try
        {
          Thread.sleep(1000 - timeUsed);
        }
        catch(InterruptedException e)
        {
          java.util.logging.Logger.getLogger(ChessController.class.getName()).log(java.util.logging.Level.FINE, "Sleep interrupted", e);
          Thread.currentThread().interrupt();
        }
			}
		}
		EventBus.getDefault().post(new EventMessage(ChessConstant.MSG_ENGINE_THINKING, line));

		if (line.startsWith(UcciEngine.RSP_BEST_MOVE_HEADER))
		{
			int moveStartPos = UcciEngine.RSP_BEST_MOVE_HEADER.length() + 1;
			int moveEndPos = line.indexOf(32, moveStartPos);
			if (moveEndPos < 0)
			{
				moveEndPos = line.length() <= (UcciEngine.RSP_BEST_MOVE_HEADER.length() + 5) + 1 ? line.length() : moveStartPos + 4;
      }
      String moveStr = line.substring(moveStartPos, moveEndPos);

      if(analysisMode)
      {
        // 分析模式：不落子，仅汇报结果
        analysisMode = false;
        Move bestMove = null;
        try
        {
          bestMove = new Move(moveStr);
        }
        catch(Exception ignored)
        {
        }
        AnalysisResult result =
            new AnalysisResult(bestMove, lastAnalysisScore, lastAnalysisIsMate, lastAnalysisMateIn, lastAnalysisDepth, lastAnalysisPv,
                hintMode);
        EventBus.getDefault().post(new EventMessage(ChessConstant.MSG_ANALYSIS_RESULT, result));
      }
      else
      {
        Move move = new Move(moveStr);
        recordMove(move);
        EventBus.getDefault().post(new EventMessage(ChessConstant.MSG_BEST_MOVE, move));
      }
		}
		else if (line.startsWith(UcciEngine.RSP_NO_BEST_MOVE))
    {
      if(analysisMode)
      {
        analysisMode = false;
        AnalysisResult result =
            new AnalysisResult(null, lastAnalysisScore, lastAnalysisIsMate, lastAnalysisMateIn, lastAnalysisDepth, lastAnalysisPv,
                hintMode);
        EventBus.getDefault().post(new EventMessage(ChessConstant.MSG_ANALYSIS_RESULT, result));
      }
      else
      {
        EventBus.getDefault().post(new EventMessage(ChessConstant.MSG_NO_BEST_MOVE, null));
      }
    }
  }

  /**
   * 向引擎请求局势分析；不落子。
   *
   * @param hint true = 走法提示（高亮最优步），false = 完整局势分析
   */
  public void requestAnalysis(boolean hint)
  {
    if(engine.isThinking())
    {
      // 先停止当前思考，等待 bestmove 响应后再分析
      new ToastFrame("请等待引擎当前思考完成！", 3000);
      return;
    }
    analysisMode = true;
    hintMode = hint;
    lastAnalysisScore = 0;
    lastAnalysisIsMate = false;
    lastAnalysisMateIn = 0;
    lastAnalysisDepth = 0;
    lastAnalysisPv = "";
    thinkStart = System.currentTimeMillis();
    engine.sendCommand(gameContext.getPositionCommand());
    // UCCI 协议使用 "go time <ms>"，不是 UCI 的 "go movetime <ms>"
    engine.sendCommand("go time " + ANALYSIS_THINK_TIME_MS);
  }

  /**
   * 解析引擎 info 行，提取分数/深度/变例
   */
  private void parseInfoLine(String line)
  {
    String[] parts = line.split("\\s+");
    for(int i = 0; i < parts.length - 1; i++)
    {
      switch(parts[i])
      {
      case "depth":
        try
        {
          lastAnalysisDepth = Integer.parseInt(parts[i + 1]);
        }
        catch(Exception ignored)
        {
        }
        break;
      case "score":
        if(i + 2 < parts.length)
        {
          if("cp".equals(parts[i + 1]))
          {
            try
            {
              lastAnalysisScore = Integer.parseInt(parts[i + 2]);
            }
            catch(Exception ignored)
            {
            }
            lastAnalysisIsMate = false;
          }
          else if("mate".equals(parts[i + 1]))
          {
            lastAnalysisIsMate = true;
            try
            {
              lastAnalysisMateIn = Integer.parseInt(parts[i + 2]);
            }
            catch(Exception ignored)
            {
            }
          }
        }
        break;
      case "pv":
        // 取前 6 步变例
        StringBuilder pvBuilder = new StringBuilder();
        for(int j = i + 1; j < Math.min(i + 7, parts.length); j++)
        {
          if(pvBuilder.length() > 0)
            pvBuilder.append(' ');
          pvBuilder.append(parts[j]);
        }
        lastAnalysisPv = pvBuilder.toString();
        break;
      default:
        break;
      }
		}
	}

	public void onError(String line)
	{
		Logger.getLogger(ChessController.class.getName()).warning("Error occurs: " + line);
	}

	public void start(String engineFileName)
	{
		engine.setMonitor(this);
		engine.startup(engineFileName);
		paused = false;
	}

	public boolean isPaused()
	{
		return paused;
	}

	public void restart()
	{
		gameContext.reset();
		paused = false;
	}

	public boolean saveManual(String filePath)
	{
		return gameContext.saveManual(filePath);
	}

	public Position currentPosition()
	{
		return gameContext.currentPosition();
	}

	public Piece[] currentPosArray()
	{
		return currentPosition().getPosArray();
	}

	public void retractTurn()
	{
		gameContext.retract();
		gameContext.retract();
	}

	public boolean sameSide(int pos)
	{
		if (pos < 0)
		{
			return false;
		}
		return currentPosArray()[pos].getSide() == gameContext.currentPosition().getSide();
	}

	public boolean legalMove(Move move)
	{
		if (!ChessRules.legalMove(currentPosition(), move))
		{
			return false;
		}
		boolean legal;
		gameContext.applyMove(move);
		if (ChessRules.check(new Position(currentPosition())))
		{
			legal = false;
		}
		else
		{
			legal = true;
		}
		gameContext.popup();
		return legal;
	}

	public boolean checked()
	{
		// Use beChecked() which correctly checks if the CURRENT side's king is attacked
		// by the opponent (check() was always returning false due to a logic bug).
		return ChessRules.beChecked(currentPosition());
	}

	public boolean captured()
	{
		try
		{
			return gameContext.prevMove().isCaptured();
		}
		catch (Exception e)
		{
			return false;
		}
	}

	public void recordMove(Move move)
	{
		gameContext.recordStep(currentPosition(), move);
		gameContext.applyMove(move);
	}

	public void engineThink()
	{
		thinkStart = System.currentTimeMillis();
		String positionCommand = gameContext.getPositionCommand();
		engine.sendCommand(positionCommand);
		EventBus.getDefault().post(new EventMessage(ChessConstant.MSG_HUMAN_INPUT, positionCommand));

		String goCommand = String.format(UcciEngine.CMD_GO_TIME_PATTERN, new Object[] { Integer.valueOf(Config.get().getTimeLimit()) });
		engine.sendCommand(goCommand);
		EventBus.getDefault().post(new EventMessage(ChessConstant.MSG_HUMAN_INPUT, goCommand));
	}

	public boolean killed()
	{
		boolean checkLift = false;
		boolean hasValidStep = false;
		for (Move move : ChessRules.enumMoves(currentPosition()))
		{
			if (ChessRules.legalMove(currentPosition(), move))
			{
				gameContext.applyMove(move);
				if (!ChessRules.check(new Position(currentPosition())))
				{
					checkLift = true;
				}
				gameContext.popup();
				if (checkLift)
				{
					hasValidStep = true;
					break;
				}
			}
		}
		return !hasValidStep;
	}

	public boolean hasAttackAbility()
	{
		return ChessRules.hasAttackAbility(currentPosition(), Side.Red);
	}

	public boolean isEngineReady()
	{
		return engine.isReady();
	}

	public boolean isThinking()
	{
		return engine.isThinking();
	}

	public int historySize()
	{
		return gameContext.length();
	}

	public int getTimeLimit()
	{
		return Config.get().getTimeLimit();
	}

	public GameContext getGameContext()
	{
		return gameContext;
	}
}
