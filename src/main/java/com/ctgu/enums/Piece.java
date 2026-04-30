package com.ctgu.enums;

public enum Piece
{
	noPiece(0, ' '), redRook(0, 'R'), redKnight(1, 'N'), redBishop(2, 'B'), redAdvisor(3, 'A'), redKing(4, 'K'), redCanon(5, 'C'), redPawn(6, 'P'), blackRook(7,
			'r'), blackKnight(8, 'n'), blackBishop(9, 'b'), blackAdvisor(10, 'a'), blackKing(11, 'k'), blackCanon(12, 'c'), blackPawn(13, 'p');

	private char abbreviation;
	private int resIndex;

	private Piece(int resIndex, char abbreviation)
	{
		this.resIndex = resIndex;
		this.abbreviation = abbreviation;
	}

	public static Piece find(char abbreviation)
	{
		for (Piece id : values())
		{
			if (id.getAbbreviation() == abbreviation)
			{
				return id;
			}
		}
		return noPiece;
	}

	public static boolean sameSide(Piece id, Piece target)
	{
		return id.getSide() == target.getSide();
	}

	public boolean isEmpty()
	{
		return this == noPiece;
	}

	public boolean notEmpty()
	{
		return this != noPiece;
	}

	public boolean isKing()
	{
		return this.abbreviation == 'K' || this.abbreviation == 'k';
	}

	public boolean isPawn()
	{
		return this.abbreviation == 'P' || this.abbreviation == 'p';
	}

	public boolean isRook()
	{
		return this.abbreviation == 'R' || this.abbreviation == 'r';
	}

	public boolean isKnight()
	{
		return this.abbreviation == 'N' || this.abbreviation == 'n';
	}

	public boolean isAdvisor()
	{
		return this.abbreviation == 'A' || this.abbreviation == 'a';
	}

	public boolean isBishop()
	{
		return this.abbreviation == 'B' || this.abbreviation == 'b';
	}

	public boolean isCanon()
	{
		return this.abbreviation == 'C' || this.abbreviation == 'c';
	}

	public int getResIndex()
	{
		return this.resIndex;
	}

	public char getAbbreviation()
	{
		return this.abbreviation;
	}

	public Side getSide()
	{
		if (this.abbreviation == ' ')
		{
			return Side.Unknown;
		}
		return (this.abbreviation < 'A' || this.abbreviation > 'Z') ? Side.Black : Side.Red;
	}
}
