package com.ctgu.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class Dialog
{
	public Dialog(String title, String content, Dimension preferredSize)
	{
		JTextPane textPane = new JTextPane();
		textPane.setPreferredSize(preferredSize);
		textPane.setFont(new Font("微软雅黑", Font.PLAIN, 16));

		SimpleAttributeSet attrset = new SimpleAttributeSet();
		StyleConstants.setForeground(attrset, Color.BLACK);
		StyleConstants.setFontSize(attrset, 16);
		StyleConstants.setAlignment(attrset, StyleConstants.ALIGN_LEFT);

		Document docs = textPane.getDocument();
		try
		{
			docs.insertString(docs.getLength(), content, attrset);
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
		textPane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(textPane);
		JOptionPane.showMessageDialog(null, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
	}
}
