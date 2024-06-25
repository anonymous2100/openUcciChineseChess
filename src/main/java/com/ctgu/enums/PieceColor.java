package com.ctgu.enums;

public class PieceColor
{
	public static final String unknown = "-";
	public static final String red = "w";
	public static final String black = "b";

	public static Side of(Piece piece)
	{
		if ("RNBAKCP".contains(String.valueOf(piece.getAbbreviation())))
		{
			return Side.Red;
		}
		if ("rnbakcp".contains(String.valueOf(piece.getAbbreviation())))
		{
			return Side.Black;
		}
		return Side.Unknown;
	}

	public static boolean sameColor(Piece p1, Piece p2)
	{
		return of(p1).equals(of(p2));
	}

	public static String opponent(String color)
	{
		if (color.equals(red))
		{
			return black;
		}
		if (color.equals(black))
		{
			return red;
		}
		return color;
	}
}