package com.ctgu.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	private static final Logger logger = Logger.getLogger(ToastFrame.class.getName());

	public ToastFrame(String str)
	{
		this.str = str;
		scheduleOnEdt(null);
	}

	public ToastFrame(String str, final Integer time)
	{
		this.str = str;
		scheduleOnEdt(time);
	}

	private void scheduleOnEdt(Integer time)
	{
		if (SwingUtilities.isEventDispatchThread())
		{
			initGUI(time);
		}
		else
		{
			SwingUtilities.invokeLater(() -> initGUI(time));
		}
	}

	private void initGUI(Integer time)
	{
		final int delay = (time == null) ? 2500 : time;
		try
		{
			java.net.URL canvasUrl = getClass().getResource("/boards/canvas.png");
			if (canvasUrl != null)
			{
				background = new ImageIcon(canvasUrl);
				background.setImage(background.getImage().getScaledInstance(background.getIconWidth(), background.getIconHeight(), Image.SCALE_DEFAULT));
			}

			setUndecorated(true);
			setLocationRelativeTo(null);
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
			setVisible(true);

			// Use a Swing Timer to auto-dismiss; avoids blocking the EDT with Thread.sleep
			Timer autoClose = new Timer(delay, e -> dispose());
			autoClose.setRepeats(false);
			autoClose.start();
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "ToastFrame initGUI failed", e);
		}
	}

	public static void main(String[] args)
	{
		new ToastFrame("绝杀！");
	}
}
