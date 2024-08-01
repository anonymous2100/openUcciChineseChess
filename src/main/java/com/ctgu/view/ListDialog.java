package com.ctgu.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.ctgu.config.Config;
import com.ctgu.constant.ChessConstant;
import com.ctgu.util.FileUtil;
import com.ctgu.util.ImageMerger;
import com.ctgu.util.IniFileUtil;
import com.ctgu.util.StringUtil;

public class ListDialog extends JFrame
{
	private static final long serialVersionUID = 2204210354091545385L;

	public ListDialog(String title, String[] contentArray, Dimension dimension, String type, ChessPanel chessPanel)
	{
		if (dimension == null)
		{
			dimension = new Dimension(1100, 900);
		}
		JPanel listPanel = new JPanel();
		listPanel.setLayout(null);

		JLabel titleLabel = new JLabel();
		titleLabel.setBounds(10, 10, 200, 50);
		titleLabel.setText(title);
		titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
		listPanel.add(titleLabel);

		JList<String> jList = new JList<>(contentArray);
		jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jList.setVisibleRowCount(10);
		jList.setFixedCellHeight(45);
		jList.setFixedCellWidth(100);
		jList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		jList.setCellRenderer(new DefaultListCellRenderer()
		{
			private static final long serialVersionUID = 1L;

			public void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				g.setColor(Color.BLACK);
				g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
			}
		});
		JScrollPane listScrollPane = new JScrollPane(jList);
		listScrollPane.setBounds(10, 60, 350, 750);
		listPanel.add(listScrollPane);

		JPanel imagePanel = new JPanel();
		imagePanel.setBounds(370, 50, 678, 750);
		JLabel lblBackground = new JLabel();
		// 设置默认选中状态
		try
		{
			File bgFile = null;
			ImageIcon icon = null;
			if ("Board".equals(type))
			{
				bgFile = new File(IniFileUtil.getBasePath() + "/boards/" + ChessConstant.BOARD_NAME[Config.get().getBoard()]);
				icon = new ImageIcon(bgFile.toURI().toURL());
			}
			else if ("Piece".equals(type))
			{
				String folderPath = IniFileUtil.getBasePath() + "/pieces/" + ChessConstant.PIECES_NAME[Config.get().getPieces()];
				List<File> fileList = new ArrayList<>();
				FileUtil.getFiles(folderPath, fileList);
				BufferedImage bufferedImage = ImageMerger.mergeImage(fileList);
				icon = new ImageIcon(bufferedImage);
			}
			lblBackground.setIcon(icon);
			lblBackground.setBounds(0, 0, icon.getIconWidth(), icon.getIconHeight());
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		imagePanel.add(lblBackground);
		listPanel.add(imagePanel);

		getContentPane().add(listPanel, BorderLayout.CENTER);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize((int) dimension.getWidth(), (int) dimension.getHeight());
		setResizable(true);
		setLocationRelativeTo(null);
		setTitle(StringUtil.isEmpty(title) ? "棋盘或棋子选择" : title);
		setVisible(true);

		jList.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				// 设置只有释放鼠标时才触发
				if (!jList.getValueIsAdjusting())
				{
					int count = imagePanel.getComponentCount();
					for (int i = 0; i < count; i++)
					{
						Object obj = imagePanel.getComponent(i);
						if (obj instanceof JLabel)
						{
							try
							{
								if ("Board".equals(type))
								{
									File bgFile = new File(IniFileUtil.getBasePath() + "/boards/" + jList.getSelectedValue());
									ImageIcon icon = new ImageIcon(bgFile.toURI().toURL());
									((JLabel) obj).setIcon(icon);

									int board = jList.getSelectedIndex();
									Config.get().setBoard(board);
									BufferedImage bufImage = ImageIO.read(
											new File(IniFileUtil.getBasePath() + "boards/" + contentArray[Config.get().getBoard()]));
									chessPanel.setImgBoard(bufImage);
								}
								else if ("Piece".equals(type))
								{
									String folderPath = IniFileUtil.getBasePath() + "/pieces/" + jList.getSelectedValue();
									List<File> fileList = new ArrayList<>();
									FileUtil.getFiles(folderPath, fileList);
									BufferedImage bufferedImage = ImageMerger.mergeImage(fileList);
									ImageIcon icon = new ImageIcon(bufferedImage);
									((JLabel) obj).setIcon(icon);
									int pieceImageArray = jList.getSelectedIndex();
									Config.get().setPieces(pieceImageArray);
									chessPanel.loadPieces();
								}
								chessPanel.repaint();
							}
							catch (IOException e1)
							{
								e1.printStackTrace();
							}
						}
					}
				}
			}
		});
	}
}
