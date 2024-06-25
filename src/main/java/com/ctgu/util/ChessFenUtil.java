package com.ctgu.util;

import com.ctgu.enums.Piece;
import com.ctgu.enums.Side;
import com.ctgu.model.Position;
import com.ctgu.model.MoveCounter;

public class ChessFenUtil
{
	public static String getPositionFen(String fen)
	{
		int pos = fen.indexOf(45);
		if (pos < 0)
		{
			return fen;
		}
		return fen.substring(0, pos);
	}

	public static String toFen(Position position)
	{
		return toFen(position.getPosArray(), position.getSide(), position.getMoveCounter());
	}

	public static String toFen(Piece[] posArray, Side side, MoveCounter stepCounter)
	{
		if (posArray == null || posArray.length != 90)
		{
			throw new RuntimeException("Illegal position input!");
		}
		StringBuffer buffer = new StringBuffer();
		for (int row = 0; row < 10; row++)
		{
			int emptyCounter = 0;
			for (int col = 0; col < 9; col++)
			{
				Piece id = posArray[(row * 9) + col];
				if (id.isEmpty())
				{
					emptyCounter++;
				}
				else
				{
					if (emptyCounter > 0)
					{
						buffer.append((char) (emptyCounter + 48));
						emptyCounter = 0;
					}
					buffer.append(id.getAbbreviation());
				}
			}
			if (emptyCounter > 0)
			{
				buffer.append((char) (emptyCounter + 48));
			}
			if (row < 9)
			{
				buffer.append('/');
			}
		}
		buffer.append(' ').append(side.getAbbreviation());
		buffer.append(" - - ");
		buffer.append(stepCounter.toString());
		return buffer.toString();
	}

	public static boolean fromFen(Position position, String fen)
	{
		int pos = fen.indexOf(32);
		if (pos < 0)
		{
			return false;
		}
		pos++;
		position.setSide(Side.find(fen.charAt(pos)));
		String ignoreMark = " - - ";
		pos = fen.indexOf(ignoreMark, pos + 1);
		if (pos < 0)
		{
			return false;
		}
		return position.getMoveCounter().load(fen.substring(pos + " - - ".length()));
	}

	public static boolean isFenChar(char c)
	{
		return "RNBAKCPrnbakcp".indexOf(c) > -1;
	}
}
