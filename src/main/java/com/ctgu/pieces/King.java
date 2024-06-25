package com.ctgu.pieces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ctgu.constant.ChessConstant;
import com.ctgu.controller.ChessRules;
import com.ctgu.enums.Piece;
import com.ctgu.enums.Side;
import com.ctgu.model.Move;
import com.ctgu.model.Position;

public class King
{
	public static boolean kingsFaceToFace(Position position, Move move)
	{
		Piece[] posArray = new Position(position).getPosArray();
		posArray[move.to] = posArray[move.from];
		posArray[move.from] = Piece.noPiece;
		for (int x = 3; x < 6; x++)
		{
			boolean foundKingAlready = false;
			for (int y = 0; y < 10; y++)
			{
				Piece id = posArray[(y * 9) + x];
				if (foundKingAlready)
				{
					if (!id.isKing())
					{
						if (id.notEmpty())
						{
							break;
						}
					}
					else
					{
						return true;
					}
				}
				if (id.isKing())
				{
					foundKingAlready = true;
				}
				if (y > 2)
				{
					break;
				}
			}
		}
		return false;
	}

	public static boolean validateKingMove(Position position, Move move)
	{
		int adx = Math.abs(move.toX - move.fromX);
		int ady = Math.abs(move.toY - move.fromY);

		boolean isUpDownMove = (adx == 0 && ady == 1);
		boolean isLeftRightMove = (adx == 1 && ady == 0);
		if (!isUpDownMove && !isLeftRightMove)
		{
			return false;
		}
		int[] range = (position.getSide() == Side.Red ? ChessConstant.RED_KING_RANGE : ChessConstant.BLACK_KING_RANGE);
		return Arrays.binarySearch(range, move.to) >= 0;
	}

	public static List<Move> enumKingMoves(Position position, int y, int x, int from)
	{
		Side side = position.getSide();
		Piece[] posArray = position.getPosArray();
		List<Move> moves = new ArrayList<>();
		int[][] array = new int[][] { { -1, 0 }, { 0, -1 }, { 1, 0 }, { 0, 1 } };
		int[] range = (side == Side.Red ? ChessConstant.RED_KING_RANGE : ChessConstant.BLACK_KING_RANGE);
		for (int[] offset : array)
		{
			int newX = x + offset[0];
			int newY = y + offset[1];
			int to = newY * 9 + newX;
			if (!ChessRules.posOnBoard(to) || posArray[to].getSide() == side)
			{
				continue;
			}
			if (Arrays.binarySearch(range, to) > -1)
			{
				moves.add(new Move(from, to));
			}
		}
		return moves;
	}
}
