package com.ctgu.enums;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;

import javazoom.jl.player.Player;

public enum ChessAudio
{
	CLICK_FROM("300"), MAN_MOV_ERROR("301"), MAN_MOVE("302"), COM_MOVE("303"), MAN_EAT("304"), COM_EAT("305"), MAN_CHECK("306"), COM_CHECK("307"), WIN_BGM(
			"308"), LOSE_BGM("309"), BE_CHECKMATED_BY_COM("310"), OPEN_BOARD("311");

	private final String tag;

	ChessAudio(String tag)
	{
		this.tag = tag;
	}

	public void play()
	{
		Thread t = new Thread(() ->
		{
			Clip localClip = null;
			try
			{
				InputStream resourceStream = ChessAudio.class.getResourceAsStream("/config/sounds/" + tag + ".wav");
				if (resourceStream == null)
				{
					return;
				}
				localClip = AudioSystem.getClip();
				try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(resourceStream)))
				{
					localClip.open(audioInputStream);
				}
				final Clip clip = localClip;
				CountDownLatch latch = new CountDownLatch(1);
				clip.addLineListener(event ->
				{
					if (LineEvent.Type.STOP.equals(event.getType()))
					{
						latch.countDown();
					}
				});
				clip.start();
				latch.await();
			}
			catch (InterruptedException ie)
			{
				Thread.currentThread().interrupt();
			}
			catch (Exception e)
			{
				Logger.getLogger(ChessAudio.class.getName()).log(Level.FINE, "Audio play failed", e);
			}
			finally
			{
				if (localClip != null)
				{
					localClip.close();
				}
			}
		});
		t.setDaemon(true);
		t.start();
	}

	public static void playBackgroundMusic()
	{
		Thread t = new Thread(() ->
		{
			while (!Thread.currentThread().isInterrupted())
			{
				try (InputStream resourceStream = ChessAudio.class.getResourceAsStream("/config/sounds/backMusic.mp3"))
				{
					if (resourceStream == null)
					{
						return;
					}
					try (BufferedInputStream bis = new BufferedInputStream(resourceStream))
					{
						Player player = new Player(bis);
						player.play();
					}
				}
				catch (Exception e)
				{
					Logger.getLogger(ChessAudio.class.getName()).log(Level.WARNING, "Background music error", e);
					break;
				}
			}
		});
		t.setDaemon(true);
		t.start();
	}
}
