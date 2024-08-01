package com.ctgu.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.ctgu.constant.ChessConstant;
import com.ctgu.engine.UcciEngine;
import com.ctgu.util.FileUtil;
import com.ctgu.util.IniFileUtil;

public class ChessFrame extends JFrame implements ActionListener
{
	private static final long serialVersionUID = -3588934942461159633L;
	private JMenu gameMenu;
	private JMenuItem newItem;
	private JMenuItem exitItem;

	private JMenu boardPieceMenu;
	private JMenuItem boardItem;
	private JMenuItem pieceItem;

	private JMenu helpMenu;
	private JMenuItem helpItem;
	private JMenuItem aboutItem;
	private static ChessPanel chessPanel;

	public ChessFrame()
	{
		chessPanel = new ChessPanel();
		initWindowViews(chessPanel);
		initMenus(chessPanel);
	}

	private void initMenus(ChessPanel chessPanel)
	{
		JMenuBar menuBar = new JMenuBar();
		gameMenu = new JMenu("游戏");
		newItem = new JMenuItem("新棋局");
		exitItem = new JMenuItem("退出");
		gameMenu.add(newItem);
		gameMenu.add(exitItem);
		menuBar.add(gameMenu);
		newItem.addActionListener(this);
		exitItem.addActionListener(this);

		boardPieceMenu = new JMenu("设置");
		boardItem = new JMenuItem("棋盘设置");
		pieceItem = new JMenuItem("棋子设置");
		boardPieceMenu.add(boardItem);
		boardPieceMenu.add(pieceItem);
		menuBar.add(boardPieceMenu);
		boardItem.addActionListener(this);
		pieceItem.addActionListener(this);

		helpMenu = new JMenu("帮助");
		helpItem = new JMenuItem("帮助");
		aboutItem = new JMenuItem("关于");
		helpMenu.add(helpItem);
		helpMenu.add(aboutItem);
		menuBar.add(helpMenu);
		helpItem.addActionListener(this);
		aboutItem.addActionListener(this);
		this.getContentPane().add(menuBar, BorderLayout.NORTH);
	}

	private void initWindowViews(ChessPanel chessPanel)
	{
		this.getContentPane().add(chessPanel, BorderLayout.CENTER);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(ChessConstant.CHESSBOARD_MARGIN + ChessConstant.GRID_WIDTH * 9, 1050);
		this.setResizable(true);
		this.setLocationRelativeTo(null);
		this.setTitle("弈心象棋");
		this.setVisible(true);
		this.addWindowClickOperation();
	}

	private void addWindowClickOperation()
	{
		this.addWindowListener(new WindowListener()
		{
			@Override
			public void windowOpened(WindowEvent e)
			{
			}

			@Override
			public void windowClosing(WindowEvent e)
			{
				UcciEngine.getInstance().shutdown();
			}

			@Override
			public void windowClosed(WindowEvent e)
			{

			}

			@Override
			public void windowIconified(WindowEvent e)
			{
			}

			@Override
			public void windowDeiconified(WindowEvent e)
			{

			}

			@Override
			public void windowActivated(WindowEvent e)
			{

			}

			@Override
			public void windowDeactivated(WindowEvent e)
			{

			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == helpItem)
		{
			String filePath = IniFileUtil.getBasePath() + "about.txt";
			String content = FileUtil.readFromFile(filePath);
			new Dialog("帮助", content, new Dimension(600, 540));
		}
		else if (e.getSource() == aboutItem)
		{
			new Dialog("关于", "弈心象棋\n开发者：anonymous2100\n联系方式：2110931055@qq.com", new Dimension(200, 150));
		}
		else if (e.getSource() == boardItem)
		{
			new ListDialog("请选择棋盘", ChessConstant.BOARD_NAME, null, "Board", chessPanel);
		}
		else if (e.getSource() == pieceItem)
		{
			new ListDialog("请选择棋子", ChessConstant.PIECES_NAME, null, "Piece", chessPanel);
		}
		else if (e.getSource() == newItem)
		{
			int opt = JOptionPane.showConfirmDialog(null, "确认重新开始？", "重新开始游戏", JOptionPane.YES_NO_OPTION);
			if (JOptionPane.YES_OPTION == opt)
			{
				chessPanel.initGame();
			}
		}
		else if (e.getSource() == exitItem)
		{
			int opt = JOptionPane.showConfirmDialog(null, "确认退出？", "退出", JOptionPane.YES_NO_OPTION);
			if (JOptionPane.YES_OPTION == opt)
			{
				UcciEngine.getInstance().shutdown();
				System.exit(0);
			}
		}
	}
}
