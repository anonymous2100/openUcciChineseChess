package com.ctgu.enums;

public enum Side
{
	Unknown('-'), Red('w'), Black('b');

	private char abbreviation;

	private Side(char abbreviation)
	{
		this.abbreviation = abbreviation;
	}

	public static Side find(char abbreviation)
	{
		for (Side side : values())
		{
			if (side.abbreviation == abbreviation)
			{
				return side;
			}
		}
		return Unknown;
	}

	public Side swap()
	{
		return isRed() ? Black : Red;
	}

	public boolean isRed()
	{
		return this == Red;
	}

	public boolean isBlack()
	{
		return this == Black;
	}

	public char getAbbreviation()
	{
		return this.abbreviation;
	}
}
