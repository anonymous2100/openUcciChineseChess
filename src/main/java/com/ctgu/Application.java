package com.ctgu;

import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.UIManager;

import com.ctgu.constant.ChessConstant;
import com.ctgu.view.ChessFrame;

public class Application
{
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					UIManager.put("RootPane.setupButtonVisible", false);
					String lookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
					UIManager.setLookAndFeel(lookAndFeel);
					for (int i = 0; i < ChessConstant.DEFAULT_FONT.length; i++)
					{
						UIManager.put(ChessConstant.DEFAULT_FONT[i], new Font("微软雅黑", Font.PLAIN, 14));
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				new ChessFrame();
			}
		});
	}
}
