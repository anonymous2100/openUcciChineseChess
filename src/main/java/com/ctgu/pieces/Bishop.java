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

public class Bishop
{
	public static boolean validateBishopMove(Position position, Move move)
	{
		int adx = Math.abs(move.toX - move.fromX);
		int ady = Math.abs(move.toY - move.fromY);
		if (adx != 2 || ady != 2)
		{
			return false;
		}
		int[] range = (position.getSide() == Side.Red) ? ChessConstant.BISHOP_RED_RANGE : ChessConstant.BISHOP_BLACK_RANGE;
		if (Arrays.binarySearch(range, move.to) < 0)
		{
			return false;
		}
		if (move.toX > move.fromX)
		{
			if (move.toY > move.fromY)
			{
				int heart = (move.fromY + 1) * 9 + move.fromX + 1;
				if (position.pieceAt(heart) != Piece.noPiece)
				{
					return false;
				}
			}
			else
			{
				int heart = (move.fromY - 1) * 9 + move.fromX + 1;
				if (position.pieceAt(heart) != Piece.noPiece)
				{
					return false;
				}
			}
		}
		else
		{
			if (move.toY > move.fromY)
			{
				int heart = (move.fromY + 1) * 9 + move.fromX - 1;
				if (position.pieceAt(heart) != Piece.noPiece)
				{
					return false;
				}
			}
			else
			{
				int heart = (move.fromY - 1) * 9 + move.fromX - 1;
				if (position.pieceAt(heart) != Piece.noPiece)
				{
					return false;
				}
			}
		}
		return true;
	}

	public static List<Move> enumBishopMoves(Position position, int y, int x, int from)
	{
		Side side = position.getSide();
		Piece[] posArray = position.getPosArray();
		int[][] heartOffsetList = { { -1, -1 }, { 1, -1 }, { -1, 1 }, { 1, 1 } };
		int[][] offsetList = new int[][] { { -2, -2 }, { 2, -2 }, { -2, 2 }, { 2, 2 } };
		int[] range = (side == Side.Red ? ChessConstant.BISHOP_RED_RANGE : ChessConstant.BISHOP_BLACK_RANGE);
		List<Move> moves = new ArrayList<>();
		for (int i = 0; i < 4; i++)
		{
			int[] heartOffset = heartOffsetList[i];
			int heart = (y + heartOffset[0]) * 9 + (x + heartOffset[1]);
			if (!ChessRules.posOnBoard(heart) || !posArray[heart].isEmpty())
			{
				continue;
			}
			final int[] offset = offsetList[i];
			final int to = (y + offset[0]) * 9 + (x + offset[1]);
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
