package com.ctgu.enums;

import java.applet.AudioClip;
import java.io.File;
import java.net.MalformedURLException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.JApplet;

import com.ctgu.util.IniFileUtil;

public enum ChessAudio
{
	CLICK_FROM("300"), MAN_MOV_ERROR("301"), MAN_MOVE("302"), COM_MOVE("303"), MAN_EAT("304"), COM_EAT("305"), MAN_CHECK("306"), COM_CHECK("307"), WIN_BGM(
			"308"), LOSE_BGM("309"), BE_CHECKMATED_BY_COM("310"), OPEN_BOARD("311");

	private final String tag;
	private Clip clip;

	ChessAudio(String tag)
	{
		this.tag = tag;
		try
		{
			clip = AudioSystem.getClip();
		}
		catch (LineUnavailableException e)
		{
			e.printStackTrace();
		}
	}

	public void play()
	{
		String name = "/sounds/" + tag + ".wav";
		String musicFilePath = IniFileUtil.getBasePath() + name;
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					clip.close();
					AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(musicFilePath));
					clip.open(audioInputStream);
					clip.start();
					Thread.sleep(5000);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}).start();
	}

	public static void playBackgroundMusic()
	{
		File musicFile = new File(IniFileUtil.getBasePath() + "/sounds/backMusic.mp3");
		if (musicFile.exists())
		{
			AudioClip bgMusic;
			try
			{
				bgMusic = JApplet.newAudioClip(musicFile.toURI().toURL());
				bgMusic.loop();
			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
			}
		}
	}
}
