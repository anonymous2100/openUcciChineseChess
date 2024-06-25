package com.ctgu.pieces;

import java.util.ArrayList;
import java.util.List;

import com.ctgu.controller.ChessRules;
import com.ctgu.enums.Piece;
import com.ctgu.enums.Side;
import com.ctgu.model.Move;
import com.ctgu.model.Position;

public class Pawn
{
	public static boolean validatePawnMove(Position position, Move move)
	{
		int dy = move.toY - move.fromY;
		int adx = Math.abs(move.toX - move.fromX);
		int ady = Math.abs(move.toY - move.fromY);
		if (adx > 1 || ady > 1 || (adx + ady) > 1)
		{
			return false;
		}
		if (position.getSide() == Side.Red)
		{
			if (move.fromY > 4 && adx != 0)
			{
				return false;
			}
			if (dy > 0)
			{
				return false;
			}
		}
		else
		{
			if (move.fromY < 5 && adx != 0)
			{
				return false;
			}
			if (dy < 0)
			{
				return false;
			}
		}
		return true;
	}

	public static List<Move> enumPawnMoves(Position position, int y, int x, int from)
	{
		Side side = position.getSide();
		Piece[] posArray = position.getPosArray();
		List<Move> moves = new ArrayList<>();
		int dy = (side.isRed() ? -1 : 1);
		int to = (y + dy) * 9 + x;
		if (ChessRules.posOnBoard(to) && posArray[to].getSide() != side)
		{
			moves.add(new Move(from, to));
		}
		if ((side == Side.Red && y < 5) || (side == Side.Black && y > 4))
		{
			if (x > 0)
			{
				to = y * 9 + x - 1;
				if (ChessRules.posOnBoard(to) && posArray[to].getSide() != side)
				{
					moves.add(new Move(from, to));
				}
			}
			if (x < 8)
			{
				to = y * 9 + x + 1;
				if (ChessRules.posOnBoard(to) && posArray[to].getSide() != side)
				{
					moves.add(new Move(from, to));
				}
			}
		}
		return moves;
	}
}
