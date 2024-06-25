package com.ctgu.controller;

import org.greenrobot.eventbus.EventBus;

import com.ctgu.config.Config;
import com.ctgu.constant.ChessConstant;
import com.ctgu.engine.EngineMonitor;
import com.ctgu.engine.UcciEngine;
import com.ctgu.enums.ChessAudio;
import com.ctgu.enums.Piece;
import com.ctgu.enums.Side;
import com.ctgu.event.EventMessage;
import com.ctgu.model.GameContext;
import com.ctgu.model.Move;
import com.ctgu.model.Position;
import com.ctgu.view.ToastFrame;

public class ChessController implements EngineMonitor
{
	public static final int MAX_STEPS_FOR_DRAW = 120;
	private UcciEngine engine;
	private GameContext gameContext;
	private boolean paused;
	private long thinkStart;

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
		long timeUsed = System.currentTimeMillis() - thinkStart;
		if (timeUsed < 1000)
		{
			try
			{
				Thread.sleep(1000 - timeUsed);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
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
			Move move = new Move(line.substring(moveStartPos, moveEndPos));
			recordMove(move);
			EventBus.getDefault().post(new EventMessage(ChessConstant.MSG_BEST_MOVE, move));
		}
		else if (line.startsWith(UcciEngine.RSP_NO_BEST_MOVE))
		{
			EventBus.getDefault().post(new EventMessage(ChessConstant.MSG_NO_BEST_MOVE, null));
			if (hasAttackAbility())
			{
				ChessAudio.WIN_BGM.play();
				new ToastFrame("恭喜你赢了！");
			}
			else
			{
				ChessAudio.WIN_BGM.play();
				new ToastFrame("和棋！");
			}
		}
	}

	public void onError(String line)
	{
		System.out.println("Error occurs: " + line);
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
		boolean checked = ChessRules.check(currentPosition());
		return checked;
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
