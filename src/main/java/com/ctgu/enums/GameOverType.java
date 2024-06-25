package com.ctgu.enums;

public enum GameOverType
{
	Win(1), Draw(0), Loss(-1);

	private int score;

	private GameOverType(int score)
	{
		this.score = score;
	}

	public int getScore()
	{
		return this.score;
	}
}