package com.ctgu.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import com.ctgu.util.IniFileUtil;

public class ToastFrame extends JFrame
{
	private static final long serialVersionUID = -1072559780508199525L;

	private ImageIcon background;
	private JLabel text;
	Toolkit tk = Toolkit.getDefaultToolkit();
	Dimension screensize = tk.getScreenSize();
	int height = screensize.height;
	int width = screensize.width;
	private String str = null;

	public ToastFrame(String str)
	{
		this.str = str;
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				initGUI(null);
			}
		}).start();
	}

	public ToastFrame(String str, final Integer time)
	{
		this.str = str;
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				initGUI(time);
			}
		}).start();
	}

	private void initGUI(Integer time)
	{
		if (time == null)
		{
			time = 2500;
		}
		try
		{
			/// background = new ImageIcon(ToastFrame.class.getClassLoader().getResource("boards/canvas.png").getFile());
			background = new ImageIcon(IniFileUtil.getBasePath() + "boards/canvas.png");
			background.setImage(background.getImage().getScaledInstance(background.getIconWidth(), background.getIconHeight(), Image.SCALE_DEFAULT));

			// 让Frame窗口失去边框和标题栏的修饰
			setUndecorated(true);
			// 将此窗口置于屏幕的中央
			setLocationRelativeTo(null);
			setVisible(true);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			{
				text = new JLabel("<html>" + str + "</html>", JLabel.CENTER);
				text.setIcon(background);
				text.setFont(new Font("微软雅黑", Font.PLAIN, 24));
				text.setBackground(new java.awt.Color(255, 251, 240));
				text.setHorizontalTextPosition(JLabel.CENTER);
				text.setVerticalTextPosition(JLabel.CENTER);
				getContentPane().add(text, BorderLayout.CENTER);
			}
			pack();
			setBounds(width / 2 - 180, height / 2, 360, 120);
			try
			{
				Thread.sleep(time);
			}
			catch (InterruptedException e1)
			{
				e1.printStackTrace();
			}
			dispose();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		new ToastFrame("绝杀！");
	}
}