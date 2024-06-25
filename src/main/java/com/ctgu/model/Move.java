package com.ctgu.model;

import java.io.Serializable;

public class Move implements Serializable
{
	private static final long serialVersionUID = 5899758635073444310L;

	private static final String INVALID_STEP = "Parse Move fail!";
	public static final int NULL_INDEX = -1;
	protected boolean captured;
	public String name;

	public int from;
	public int fromX;
	public int fromY;

	public int to;
	public int toX;
	public int toY;

	public Move()
	{
		to = -1;
		from = -1;
		toY = -1;
		toX = -1;
		fromY = -1;
		fromX = -1;
	}

	public Move(String description)
	{
		name = description;
		if (description.length() != 4)
		{
			throw new RuntimeException(INVALID_STEP);
		}
		fromX = description.charAt(0) - 97;
		fromY = 9 - (description.charAt(1) - 48);
		toX = description.charAt(2) - 97;
		toY = 9 - (description.charAt(3) - 48);
		from = fromX + (fromY * 9);
		to = toX + (toY * 9);
		if (fromX < 0 || fromX > 8 || fromY < 0 || fromY > 9)
		{
			throw new RuntimeException(INVALID_STEP);
		}
	}

	public Move(int fromPos, int toPos)
	{
		from = fromPos;
		to = toPos;
		fromX = from % 9;
		fromY = from / 9;
		toX = to % 9;
		toY = to / 9;
		name = String.format("%c%c%c%c", new Object[] { Character.valueOf((char) (fromX + 97)), Character.valueOf((char) (57 - fromY)),
				Character.valueOf((char) (toX + 97)), Character.valueOf((char) (57 - toY)) });
		if (fromX < 0 || fromX > 8 || fromY < 0 || fromY > 9)
		{
			throw new RuntimeException(INVALID_STEP);
		}
	}

	public boolean isCaptured()
	{
		return captured;
	}

	public void setCaptured(boolean capturedFlag)
	{
		captured = capturedFlag;
	}

	public String toString()
	{
		return name;
	}
}
