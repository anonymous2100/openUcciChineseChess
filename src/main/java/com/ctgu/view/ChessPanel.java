package com.ctgu.view;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.ctgu.config.Config;
import com.ctgu.constant.ChessConstant;
import com.ctgu.controller.ChessController;
import com.ctgu.controller.ChessRules;
import com.ctgu.engine.UcciEngine;
import com.ctgu.enums.ChessAudio;
import com.ctgu.enums.GameOverType;
import com.ctgu.enums.Piece;
import com.ctgu.event.EventMessage;
import com.ctgu.model.Move;
import com.ctgu.model.Position;
import com.ctgu.util.ChessFenUtil;
import com.ctgu.util.ClipBoardUtil;
import com.ctgu.util.IniFileUtil;
import com.ctgu.util.StringUtil;

public class ChessPanel extends JPanel
{
	private static final long serialVersionUID = 6672024539300490002L;

	private JTabbedPane tabbedPane;
	private JScrollPane chessHistoryPane;
	private JTextArea stepTextArea;
	private JScrollPane engineInfoPane;
	private JTextArea engineTextArea;
	private JButton btnNewGame;
	private JButton btnSave;
	private JButton btnRetract;
	private JButton btnCopy;
	private JComboBox<String> btnChangeBoard;
	private JComboBox<String> btnChangePiece;
	private JComboBox<String> btnChangeCoordinate;
	private JComboBox<String> btnPaste;
	private BufferedImage imgBoard;
	private boolean diverted;
	private int from;
	private int to;
	private Piece[] posArray = new Piece[90];
	private BufferedImage[] pieceImageArray;
	private ChessController controller;
	private int fontWidth = 0;
	private int fontHeight = 0;

	public ChessPanel()
	{
		controller = new ChessController(ChessConstant.ENGINE_NAME[Config.get().getEngine()]);
		controller.start(ChessConstant.ENGINE_NAME[Config.get().getEngine()]);
		if (Config.get().isComputerFirst())
		{
			setThinkingStatus();
			controller.engineThink();
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
		System.out.println("【EventBus】onReceiveMsg: " + msg);
		if (msg.getType() == ChessConstant.MSG_BEST_MOVE)
		{
			Move move = (Move) msg.getObj();
			System.out.println("【EventBus】move.from=" + move.from + ",move.to=" + move.to + ",move=" + move.toString());
			update(controller.currentPosArray(), move.from, move.to);
			if (!controller.isPaused())
			{
				if (controller.checked())
				{
					ChessAudio.COM_CHECK.play();
					new ToastFrame("将军！");
				}
				else if (controller.captured())
				{
					ChessAudio.COM_EAT.play();
				}
				else
				{
					ChessAudio.COM_MOVE.play();
				}
				updateInfoArea(controller.getGameContext().getManualText(), "------------------------------------------------------------\n");
				setReadyStatus();
				scanResult(false);
			}
		}
		else if (msg.getType() == ChessConstant.MSG_NO_BEST_MOVE)
		{
			if (controller.hasAttackAbility())
			{
				showGameOver(GameOverType.Win);
			}
			else
			{
				showGameOver(GameOverType.Loss);
				int opt = JOptionPane.showConfirmDialog(null, "很不幸，你输了~~~\n是否重新开始游戏？", "重新开始游戏", JOptionPane.YES_NO_OPTION);
				if (JOptionPane.YES_OPTION == opt)
				{
					initGame();
				}
			}
			setReadyStatus();
		}
		else if (msg.getType() == ChessConstant.MSG_ENGINE_THINKING)
		{
			updateEngineInfo(msg, "<<<");
		}
		else if (msg.getType() == ChessConstant.MSG_HUMAN_INPUT)
		{
			updateEngineInfo(msg, ">>>");
		}
	}

	private void updateEngineInfo(EventMessage msg, String promptString)
	{
		String engineInfo = String.valueOf(msg.getObj());
		StringBuffer sb = new StringBuffer((engineTextArea == null || StringUtil.isBlank(engineTextArea.getText())) ? "" : engineTextArea.getText());
		sb.append(promptString).append(engineInfo).append("\n");
		if (engineTextArea != null)
		{
			engineTextArea.setText(sb.toString());
		}
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
				if ((y >= 0 && y <= 9) && (x >= 0 && x <= 8))
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
		if (!controller.isEngineReady())
		{
			new ToastFrame("引擎未准备！");
			return;
		}
		if (controller.sameSide(pos))
		{
			update(controller.currentPosArray(), pos, -1);
			ChessAudio.CLICK_FROM.play();
		}
		else if (controller.sameSide(getFrom()) && !controller.isThinking())
		{
			Move move = new Move(getFrom(), pos);
			if (controller.legalMove(move))
			{
				controller.recordMove(move);
				update(controller.currentPosition().getPosArray(), move.from, move.to);
				if (controller.checked())
				{
					ChessAudio.MAN_CHECK.play();
				}
				else if (controller.captured())
				{
					ChessAudio.MAN_EAT.play();
				}
				else
				{
					ChessAudio.MAN_MOVE.play();
				}
				updateInfoArea(controller.getGameContext().getManualText(), null);
				if (!scanResult(true))
				{
					setThinkingStatus();
					controller.engineThink();
				}
				return;
			}
			else
			{
				if (ChessRules.willBeChecked(controller.currentPosition(), move))
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
				if (JOptionPane.YES_OPTION == opt)
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
				fileChooser.setCurrentDirectory(new File("D:/"));
				fileChooser.setDialogTitle("保存棋谱文件");
				fileChooser.setSelectedFile(new File(new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()) + ".pgn"));
				fileChooser.setFileFilter(new FileNameExtensionFilter("PGN FILE", "pgn"));
				int result = fileChooser.showDialog(null, "保存文件");
				if (result == JFileChooser.APPROVE_OPTION)
				{
					File file = fileChooser.getSelectedFile();
					System.out.println("棋谱文件保存地址为：" + file.getAbsolutePath());
					boolean flag = controller.saveManual(file.getAbsolutePath());
					System.out.println(flag);
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
				if (controller.currentPosition() != null)
				{
					String fenString = controller.currentPosition().toFen();
					System.out.println("复制局面: " + fenString);
					ClipBoardUtil.setSysClipboardText("fen " + fenString);
				}
			}
		});
		btnChangeBoard.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				if (ItemEvent.SELECTED == e.getStateChange())
				{
					if (btnChangeBoard.getSelectedIndex() > 0)
					{
						int board = btnChangeBoard.getSelectedIndex() - 1;
						System.out.println("更换棋盘: " + ChessConstant.BOARD_NAME[board]);
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
				if (ItemEvent.SELECTED == e.getStateChange())
				{
					if (btnChangePiece.getSelectedIndex() > 0)
					{
						int pieceImageArray = btnChangePiece.getSelectedIndex() - 1;
						System.out.println("更换棋子: " + ChessConstant.PIECES_NAME[pieceImageArray]);
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
				if (ItemEvent.SELECTED == e.getStateChange())
				{
					if (btnChangeCoordinate.getSelectedIndex() > 0)
					{
						int coordinate = btnChangeCoordinate.getSelectedIndex() - 1;
						System.out.println("更换坐标: {}" + ChessConstant.COORDINATE_NAME[coordinate]);
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
				if (ItemEvent.SELECTED == e.getStateChange())
				{
					if (btnPaste.getSelectedIndex() > 0)
					{
						UcciEngine.getInstance().shutdown();
						int selectedEngine = btnPaste.getSelectedIndex() - 1;
						System.out.println("更换引擎: {}" + ChessConstant.ENGINE_NAME[selectedEngine]);
						Config.get().setEngine(selectedEngine);
						UcciEngine.getInstance().restart(ChessConstant.ENGINE_NAME[selectedEngine]);
					}
				}
			}
		});
	}

	public void initGame()
	{
		ChessAudio.OPEN_BOARD.play();
		new ToastFrame("重新开始游戏...");
		restart();
		repaint();
		engineTextArea.setText("");
		stepTextArea.setText("");
	}

	public void retract(ChessController controller)
	{
		if (!controller.isThinking())
		{
			try
			{
				controller.retractTurn();
				update(controller.currentPosArray(), -1, -1);
				String manualText = controller.getGameContext().getManualText();
				if (StringUtil.isEmpty(manualText))
				{
					stepTextArea.setText("");
					engineTextArea.setText("");
				}
				else
				{
					updateInfoArea(manualText, null);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void update(Piece[] newPosArray, int fromPos, int toPos)
	{
		System.arraycopy(newPosArray, 0, posArray, 0, 90);
		from = fromPos;
		to = toPos;
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
		if (!controller.isThinking())
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
		if (StringUtil.isNotBlank(manualText))
		{
			StringBuffer sb = new StringBuffer(stepTextArea == null ? "" : manualText);
			stepTextArea.setText(sb.toString());
		}
		if (StringUtil.isNotBlank(engineText))
		{
			StringBuffer sb = new StringBuffer(engineTextArea == null ? "" : engineTextArea.getText());
			sb.append(engineText);
			engineTextArea.setText(sb.toString());
		}
	}

	private boolean scanResult(boolean byPerson)
	{
		if (scanLongCatch(controller, byPerson))
		{
			return true;
		}
		if (controller.killed())
		{
			if (byPerson)
			{
				showGameOver(byPerson ? GameOverType.Win : GameOverType.Loss);
			}
			else
			{
				showGameOver(byPerson ? GameOverType.Win : GameOverType.Loss);
				int opt = JOptionPane.showConfirmDialog(null, "很不幸，你输了~~~\n是否重新开始游戏？", "重新开始游戏", JOptionPane.YES_NO_OPTION);
				if (JOptionPane.YES_OPTION == opt)
				{
					initGame();
				}
			}
			return true;
		}
		else if (controller.historySize() < 240)
		{
			return false;
		}
		else
		{
			showGameOver(GameOverType.Draw);
			new ToastFrame("平局！");
			return true;
		}
	}

	private boolean scanLongCatch(ChessController controller, boolean byPerson)
	{
		LinkedList<Position> positionArray = controller.getGameContext().getPositionArray();
		int count = positionArray.size();
		if (count > 7)
		{
			String[] fens = new String[7];
			for (int i = 0; i < fens.length; i++)
			{
				fens[i] = ChessFenUtil.getPositionFen(positionArray.get((count - i) - 1).toFen());
			}
			if (fens[0].equals(fens[4]) && fens[1].equals(fens[5]) && fens[2].equals(fens[6]))
			{
				if (byPerson)
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
		if (dialogType == GameOverType.Win)
		{
			new ToastFrame("你赢了！");
			ChessAudio.WIN_BGM.play();
		}
		else if (dialogType == GameOverType.Draw)
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
		imgBoard = getImage(getCodeBase(), "boards/" + ChessConstant.BOARD_NAME[Config.get().getBoard()]);
	}

	public void loadPieces()
	{
		BufferedImage bufImage = null;
		pieceImageArray = new BufferedImage[16];
		try
		{
			for (int i = 0; i < pieceImageArray.length; i++)
			{
				File imageFolder = new File(getCodeBase() + "pieces/" + ChessConstant.PIECES_NAME[Config.get().getPieces()]);
				File[] listFile = imageFolder.listFiles();
				String suffix = listFile[0].getName().substring(listFile[0].getName().lastIndexOf("."));
				String filePath = getCodeBase() + "pieces/" + ChessConstant.PIECES_NAME[Config.get().getPieces()] + "/" + ChessConstant.PIECE_ARRAY[i] + suffix;
				bufImage = ImageIO.read(new File(filePath));
				pieceImageArray[i] = bufImage;
			}
		}
		catch (IOException e)
		{
			System.out.println("初始化棋子资源出错：" + e.getMessage());
			e.printStackTrace();
		}
	}

	private String getCodeBase()
	{
		return IniFileUtil.getBasePath();
	}

	private BufferedImage getImage(String basePath, String string)
	{
		BufferedImage bufImage = null;
		try
		{
			File file = new File(basePath + string);
			bufImage = ImageIO.read(file);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return bufImage;
	}

	public BufferedImage getPiece(int index)
	{
		return pieceImageArray[index];
	}

	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(imgBoard, 0, 0, this);
		String boardName = ChessConstant.BOARD_NAME[Config.get().getBoard()];
		if (boardName != null && boardName.startsWith("bg_"))
		{
			drawGrid(g2);
		}
		drawIndicators(g2);
		if (Config.get().getCoordinate() == 0)
		{
			drawNumbers(g2);
		}
		else if (Config.get().getCoordinate() == 1)
		{
			drawICCSNumbers(g2);
		}
		else
		{
			drawICoordinates(g2);
		}
		drawPieces(g2);
	}

	/**
	 * 绘制所有象棋棋子
	 * 
	 * @param g2
	 */
	private void drawPieces(Graphics2D g2)
	{
		for (int pos = 0; pos < posArray.length; pos++)
		{
			Piece chessId = posArray[pos];
			if (chessId != null && !chessId.isEmpty())
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
		int x = pos % 9;
		int y = pos / 9;
		if (diverted)
		{
			x = 8 - x;
			y = 9 - y;
		}
		int dx = (ChessConstant.GRID_WIDTH / 2 - ChessConstant.PIECE_WIDTH / 2);
		int dy = (ChessConstant.GRID_WIDTH / 2 - ChessConstant.PIECE_WIDTH / 2);
		g2.drawImage(bitmap, //
				ChessConstant.CHESSBOARD_MARGIN + x * ChessConstant.GRID_WIDTH - ChessConstant.GRID_WIDTH / 2 + dx,//
				ChessConstant.CHESSBOARD_MARGIN + y * ChessConstant.GRID_WIDTH - ChessConstant.GRID_WIDTH / 2 + dy, //
				this);
	}

	private void drawIndicators(Graphics2D g2)
	{
		if (from != -1)
		{
			BufferedImage bitmap;
			if (to != -1)
			{
				bitmap = getPiece(15);
			}
			else
			{
				bitmap = getPiece(14);
			}
			drawPiece(g2, bitmap, from);
		}
		if (to != -1)
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
		for (int i = 0; i < 9; i++)
		{
			fontWidth = fm.stringWidth(ChessConstant.blackMarkNumbers[i]);
			fontHeight = fm.getHeight();
			int width = fontWidth;
			g2.drawString(ChessConstant.blackMarkNumbers[i], ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH - width / 2,
					ChessConstant.CHESSBOARD_MARGIN / 4 + ChessConstant.GRID_WIDTH / 4);
		}
		for (int i = 0; i < 9; i++)
		{
			fontWidth = fm.stringWidth(ChessConstant.redMarkNumbers[i]);
			fontHeight = fm.getHeight();
			int width = fontWidth;
			g2.drawString(ChessConstant.redMarkNumbers[i], ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH - width / 2,
					ChessConstant.CHESSBOARD_MARGIN + ChessConstant.GRID_WIDTH * 9 + ChessConstant.GRID_WIDTH / 4 + ChessConstant.CHESSBOARD_MARGIN / 2);
		}
	}

	private void drawICCSNumbers(Graphics2D g2)
	{
		g2.setColor(ChessConstant.WORD_COLOR);
		Font font = new Font(ChessConstant.FONT_NAME[0], Font.PLAIN, 25);
		g2.setFont(font);
		FontMetrics fm = g2.getFontMetrics(font);
		for (int i = 0; i < 9; i++)
		{
			fontWidth = fm.stringWidth(ChessConstant.iccsHorizontalNumbers[i]);
			fontHeight = fm.getHeight();
			int width = fontWidth;
			g2.drawString(ChessConstant.iccsHorizontalNumbers[i], ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH - width / 2,
					ChessConstant.CHESSBOARD_MARGIN / 4 + ChessConstant.GRID_WIDTH / 4);
		}
		for (int i = 0; i < 9; i++)
		{
			fontWidth = fm.stringWidth(ChessConstant.iccsHorizontalNumbers[i]);
			fontHeight = fm.getHeight();
			int width = fontWidth;
			g2.drawString(ChessConstant.iccsHorizontalNumbers[i], ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH - width / 2,
					ChessConstant.CHESSBOARD_MARGIN + ChessConstant.GRID_WIDTH * 9 + ChessConstant.GRID_WIDTH / 4 + ChessConstant.CHESSBOARD_MARGIN / 2);
		}
		for (int i = 0; i <= 9; i++)
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
		for (int i = 0; i < 9; i++)
		{
			fontWidth = fm.stringWidth(ChessConstant.xIndex[i]);
			fontHeight = fm.getHeight();
			int width = fontWidth;
			g2.drawString(ChessConstant.xIndex[i], ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH - width / 2,
					ChessConstant.CHESSBOARD_MARGIN / 4 + ChessConstant.GRID_WIDTH / 4);
		}
		for (int i = 0; i < 9; i++)
		{
			fontWidth = fm.stringWidth(ChessConstant.xIndex[i]);
			fontHeight = fm.getHeight();
			int width = fontWidth;
			g2.drawString(ChessConstant.xIndex[i], ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH - width / 2,
					ChessConstant.CHESSBOARD_MARGIN + ChessConstant.GRID_WIDTH * 9 + ChessConstant.GRID_WIDTH / 4 + ChessConstant.CHESSBOARD_MARGIN / 2);
		}
		for (int i = 0; i <= 9; i++)
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
		g2.drawRect(ChessConstant.CHESSBOARD_MARGIN, ChessConstant.CHESSBOARD_MARGIN, ChessConstant.GRID_WIDTH * 8, ChessConstant.GRID_WIDTH * 9);
		for (int i = 0; i <= 9; i++)
		{
			g2.drawLine(ChessConstant.CHESSBOARD_MARGIN, ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH,
					ChessConstant.CHESSBOARD_MARGIN + ChessConstant.GRID_WIDTH * 8, ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH);
		}
		Font f2 = new Font(ChessConstant.FONT_NAME[2], Font.PLAIN, 32);
		g2.setFont(f2);
		g2.drawString("楚河", ChessConstant.CHESSBOARD_MARGIN + ChessConstant.GRID_WIDTH,
				ChessConstant.CHESSBOARD_MARGIN + 4 * ChessConstant.GRID_WIDTH + (int) (ChessConstant.GRID_WIDTH * 2 / 3));
		g2.drawString("汉界", ChessConstant.CHESSBOARD_MARGIN + ChessConstant.GRID_WIDTH * 6,
				ChessConstant.CHESSBOARD_MARGIN + 4 * ChessConstant.GRID_WIDTH + (int) (ChessConstant.GRID_WIDTH * 2 / 3));
		for (int i = 0; i < 9; i++)
		{
			g2.drawLine(ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH, ChessConstant.CHESSBOARD_MARGIN,
					ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH, ChessConstant.CHESSBOARD_MARGIN + 4 * ChessConstant.GRID_WIDTH);
		}
		for (int i = 0; i < 9; i++)
		{
			g2.drawLine(ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH, ChessConstant.CHESSBOARD_MARGIN + 5 * ChessConstant.GRID_WIDTH,
					ChessConstant.CHESSBOARD_MARGIN + i * ChessConstant.GRID_WIDTH, ChessConstant.CHESSBOARD_MARGIN + 9 * ChessConstant.GRID_WIDTH);
		}
		g2.drawLine(ChessConstant.CHESSBOARD_MARGIN + 3 * ChessConstant.GRID_WIDTH, ChessConstant.CHESSBOARD_MARGIN,
				ChessConstant.CHESSBOARD_MARGIN + 5 * ChessConstant.GRID_WIDTH, ChessConstant.CHESSBOARD_MARGIN + 2 * ChessConstant.GRID_WIDTH);
		g2.drawLine(ChessConstant.CHESSBOARD_MARGIN + 5 * ChessConstant.GRID_WIDTH, ChessConstant.CHESSBOARD_MARGIN,
				ChessConstant.CHESSBOARD_MARGIN + 3 * ChessConstant.GRID_WIDTH, ChessConstant.CHESSBOARD_MARGIN + 2 * ChessConstant.GRID_WIDTH);
		g2.drawLine(ChessConstant.CHESSBOARD_MARGIN + 3 * ChessConstant.GRID_WIDTH, ChessConstant.CHESSBOARD_MARGIN + 7 * ChessConstant.GRID_WIDTH,
				ChessConstant.CHESSBOARD_MARGIN + 5 * ChessConstant.GRID_WIDTH, ChessConstant.CHESSBOARD_MARGIN + 9 * ChessConstant.GRID_WIDTH);
		g2.drawLine(ChessConstant.CHESSBOARD_MARGIN + 5 * ChessConstant.GRID_WIDTH, ChessConstant.CHESSBOARD_MARGIN + 7 * ChessConstant.GRID_WIDTH,
				ChessConstant.CHESSBOARD_MARGIN + 3 * ChessConstant.GRID_WIDTH, ChessConstant.CHESSBOARD_MARGIN + 9 * ChessConstant.GRID_WIDTH);
	}

	private void initButtons()
	{
		setLayout(null);
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);

		tabbedPane.setBounds(ChessConstant.CHESSBOARD_MARGIN + ChessConstant.GRID_WIDTH * 9, ChessConstant.CHESSBOARD_MARGIN / 8 - fontHeight, 550, 820);

		chessHistoryPane = new JScrollPane();
		tabbedPane.addTab("棋谱信息", null, chessHistoryPane, null);
		stepTextArea = new JTextArea();
		stepTextArea.setFont(new Font(ChessConstant.FONT_NAME[2], 0, 20));
		stepTextArea.setEditable(false);
		DefaultCaret stepTextCaret = (DefaultCaret) stepTextArea.getCaret();
		stepTextCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		chessHistoryPane.setViewportView(stepTextArea);

		engineInfoPane = new JScrollPane();
		tabbedPane.addTab("引擎思考信息", null, engineInfoPane, null);
		engineTextArea = new JTextArea();
		engineTextArea.setFont(new Font(ChessConstant.FONT_NAME[2], 0, 20));
		engineTextArea.setEditable(false);
		DefaultCaret engineTextCaret = (DefaultCaret) engineTextArea.getCaret();
		engineTextCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		engineInfoPane.setViewportView(engineTextArea);
		tabbedPane.setSelectedIndex(0);
		add(tabbedPane);

		int yCoordinateUp = 850;
		btnNewGame = new JButton("新棋局");
		btnNewGame.setBounds(90, yCoordinateUp, 95, 25);
		add(btnNewGame);

		btnSave = new JButton("存棋谱");
		btnSave.setBounds(220, yCoordinateUp, 95, 25);
		add(btnSave);

		btnRetract = new JButton("悔棋");
		btnRetract.setBounds(340, yCoordinateUp, 95, 25);
		add(btnRetract);

		btnCopy = new JButton("复制局面");
		btnCopy.setBounds(455, yCoordinateUp, 95, 25);
		add(btnCopy);

		int yCoordinateDown = 900;
		btnChangeBoard = new JComboBox<String>();
		btnChangeBoard.setBounds(90, yCoordinateDown, 95, 25);
		btnChangeBoard.addItem("更换棋盘");
		for (int i = 0; i < ChessConstant.BOARD_NAME.length; i++)
		{
			btnChangeBoard.addItem(ChessConstant.BOARD_NAME[i]);
		}
		btnChangeBoard.setSelectedIndex(Config.get().getBoard() + 1);
		add(btnChangeBoard);

		btnChangePiece = new JComboBox<String>();
		btnChangePiece.setBounds(220, yCoordinateDown, 95, 25);
		btnChangePiece.addItem("更换棋子");
		for (int i = 0; i < ChessConstant.PIECES_NAME.length; i++)
		{
			btnChangePiece.addItem(ChessConstant.PIECES_NAME[i]);
		}
		btnChangePiece.setSelectedIndex(Config.get().getPieces() + 1);
		add(btnChangePiece);

		btnChangeCoordinate = new JComboBox<String>();
		btnChangeCoordinate.setBounds(340, yCoordinateDown, 95, 25);
		btnChangeCoordinate.addItem("更换坐标");
		for (int i = 0; i < ChessConstant.COORDINATE_NAME.length; i++)
		{
			btnChangeCoordinate.addItem(ChessConstant.COORDINATE_NAME[i]);
		}
		btnChangeCoordinate.setSelectedIndex(Config.get().getCoordinate() + 1);
		add(btnChangeCoordinate);

		btnPaste = new JComboBox<String>();
		btnPaste.setBounds(455, yCoordinateDown, 95, 25);
		btnPaste.addItem("更换引擎");
		for (int i = 0; i < ChessConstant.ENGINE_NAME.length; i++)
		{
			btnPaste.addItem(ChessConstant.ENGINE_NAME[i]);
		}
		btnPaste.setSelectedIndex(Config.get().getEngine() + 1);
		add(btnPaste);
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
