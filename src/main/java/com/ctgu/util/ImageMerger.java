package com.ctgu.util;

import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

public class ImageMerger
{
	private static final Logger logger = Logger.getLogger(ImageMerger.class.getName());
	public static BufferedImage mergeImage(List<File> fileList)
	{
		BufferedImage[] images = new BufferedImage[fileList.size()];
		for (int i = 0; i < fileList.size(); i++)
		{
			try
			{
				images[i] = ImageIO.read(fileList.get(i));
			}
			catch (IOException e)
			{
				logger.log(Level.WARNING, "Failed to read image for merging: " + fileList.get(i).getAbsolutePath(), e);
			}
		}
		return mergeBufferedImages(images);
	}

	public static BufferedImage mergeBufferedImages(BufferedImage[] images)
	{
		if (images.length == 0 || images[0] == null)
		{
			logger.log(Level.WARNING, "No images available for merge");
			return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		}
		int pictureWidth = images[0].getWidth();
		int pictureHeight = images[0].getHeight();
		logger.fine("pictureWidth=" + pictureWidth + ", pictureHeight=" + pictureHeight);
		// 合并后图片的宽度
		int mergedWidth = pictureWidth * (images.length / 2) + pictureWidth;
		// 合并后图片的高度
		int mergedHeight = pictureHeight * 3;
		logger.fine("mergedWidth=" + mergedWidth + ", mergedHeight=" + mergedHeight);
		BufferedImage mergedImage = new BufferedImage(mergedWidth, mergedHeight, BufferedImage.TYPE_INT_RGB);

		Graphics2D g2d = mergedImage.createGraphics();
		// 增加下面代码使得背景透明
		mergedImage = g2d.getDeviceConfiguration().createCompatibleImage(mergedWidth, mergedHeight, Transparency.TRANSLUCENT);
		g2d.dispose();
		g2d = mergedImage.createGraphics();

		for (int i = 0; i < images.length; i++)
		{
			// 设置绘制的位置
			// 图片间的横向间隔
			int x = i * images[i].getWidth();
			// 图片的纵向位置为0
			int y = 0;
			if (i >= 8)
			{
				x = i * images[i].getWidth() - images[i].getWidth() * 8;
				y = images[i].getHeight() + 10;
			}
			// 绘制图片
			g2d.drawImage(images[i], x, y, null);
		}
		// 释放绘图资源
		g2d.dispose();
		return mergedImage;
	}

	public static void main(String[] args)
	{
		// 图片文件路径列表
		String folderPath = "D:\\3_MyWorkspace\\ucci-chinese-chess\\src\\main\\resources\\pieces\\2D金边";
		List<File> fileList = new ArrayList<>();
		FileUtil.getFiles(folderPath, fileList);

		BufferedImage bufferedImage = mergeImage(fileList);

		String mergedImagePath = folderPath + "/mergedImage.png"; // 合并后图片的文件路径
		try
		{
			File mergedImageFile = new File(mergedImagePath);
			ImageIO.write(bufferedImage, "png", mergedImageFile);
		}
		catch (IOException e)
		{
			logger.log(Level.WARNING, "Failed to write merged image: " + mergedImagePath, e);
		}
	}
}
