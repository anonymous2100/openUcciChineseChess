package com.ctgu.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import com.ctgu.util.ImageMerger;
import com.ctgu.util.StringUtil;

public class ListDialog extends JFrame
{
	private static final Logger logger = Logger.getLogger(ListDialog.class.getName());
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
			ImageIcon icon = null;
			if ("Board".equals(type))
			{
				java.net.URL url = getClass().getResource("/config/boards/" + ChessConstant.BOARD_NAME[Config.get().getBoard()]);
				if (url != null)
				{
					icon = new ImageIcon(url);
				}
			}
			else if ("Piece".equals(type))
			{
				icon = createPiecePreview(ChessConstant.PIECES_NAME[Config.get().getPieces()]);
			}
			if (icon != null)
			{
				lblBackground.setIcon(icon);
				lblBackground.setBounds(0, 0, icon.getIconWidth(), icon.getIconHeight());
			}
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "Failed to load preview image", e);
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
									java.net.URL url = getClass().getResource("/config/boards/" + jList.getSelectedValue());
									if (url != null)
									{
										ImageIcon icon = new ImageIcon(url);
										((JLabel) obj).setIcon(icon);
									}
									int board = jList.getSelectedIndex();
									Config.get().setBoard(board);
									BufferedImage bufImage = ImageIO.read(
											getClass().getResource("/config/boards/" + contentArray[Config.get().getBoard()]));
									chessPanel.setImgBoard(bufImage);
								}
								else if ("Piece".equals(type))
								{
									ImageIcon icon = createPiecePreview(jList.getSelectedValue());
									((JLabel) obj).setIcon(icon);
									int pieceImageArray = jList.getSelectedIndex();
									Config.get().setPieces(pieceImageArray);
									chessPanel.loadPieces();
								}
								chessPanel.repaint();
							}
							catch (IOException e1)
							{
								logger.log(Level.WARNING, "Failed to update preview image", e1);
							}
						}
					}
				}
			}
		});
	}

	/** 从类路径加载某套棋子的所有图片，合并为一张预览图 */
	private ImageIcon createPiecePreview(String styleName)
	{
		String dir = "/config/pieces/" + styleName + "/";
		BufferedImage[] images = new BufferedImage[ChessConstant.PIECE_ARRAY.length];
		int count = 0;
		for (String pieceName : ChessConstant.PIECE_ARRAY)
		{
			BufferedImage img = tryLoadImage(dir + pieceName + ".gif");
			if (img == null) img = tryLoadImage(dir + pieceName + ".png");
			if (img == null) img = tryLoadImage(dir + pieceName);
			if (img != null)
			{
				images[count++] = img;
			}
		}
		// 只保留实际加载到的图片
		BufferedImage[] loaded = new BufferedImage[count];
		System.arraycopy(images, 0, loaded, 0, count);
		return new ImageIcon(ImageMerger.mergeBufferedImages(loaded));
	}

	private BufferedImage tryLoadImage(String resourcePath)
	{
		try
		{
			java.net.URL url = getClass().getResource(resourcePath);
			if (url != null)
			{
				return ImageIO.read(url);
			}
		}
		catch (IOException e)
		{
			logger.log(Level.FINE, "Failed to load resource: " + resourcePath, e);
		}
		return null;
	}
}
