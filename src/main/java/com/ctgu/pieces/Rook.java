package com.ctgu.pieces;

import java.util.ArrayList;
import java.util.List;

import com.ctgu.enums.Piece;
import com.ctgu.enums.Side;
import com.ctgu.model.Move;
import com.ctgu.model.Position;

public class Rook
{
	public static boolean validateRookMove(Position position, Move move)
	{
		int dx = move.toX - move.fromX;
		int dy = move.toY - move.fromY;
		if (dx != 0 && dy != 0)
		{
			return false;
		}
		if (dy == 0)
		{
			if (dx < 0)
			{
				for (int i = move.fromX - 1; i > move.toX; i--)
				{
					if (position.pieceAt(move.fromY * 9 + i) != Piece.noPiece)
					{
						return false;
					}
				}
			}
			else
			{
				for (int i = move.fromX + 1; i < move.toX; i++)
				{
					if (position.pieceAt(move.fromY * 9 + i) != Piece.noPiece)
					{
						return false;
					}
				}
			}
		}
		else
		{
			if (dy < 0)
			{
				for (int i = move.fromY - 1; i > move.toY; i--)
				{
					if (position.pieceAt(i * 9 + move.fromX) != Piece.noPiece)
					{
						return false;
					}
				}
			}
			else
			{
				for (int i = move.fromY + 1; i < move.toY; i++)
				{
					if (position.pieceAt(i * 9 + move.fromX) != Piece.noPiece)
					{
						return false;
					}
				}
			}
		}
		return true;

	}

	public static List<Move> enumRookMoves(Position position, int y, int x, int from)
	{
		Side side = position.getSide();
		Piece[] posArray = position.getPosArray();
		List<Move> moves = new ArrayList<>();
		for (int dx = x - 1; dx >= 0; dx--)
		{
			int to = y * 9 + dx;
			Piece target = posArray[to];
			if (target == Piece.noPiece)
			{
				moves.add(new Move(from, to));
			}
			else
			{
				if (posArray[to].getSide() != side)
				{
					moves.add(new Move(from, to));
				}
				break;
			}
		}
		for (int dy = y - 1; dy >= 0; dy--)
		{
			int to = dy * 9 + x;
			Piece target = posArray[to];
			if (target == Piece.noPiece)
			{
				moves.add(new Move(from, to));
			}
			else
			{
				if (posArray[to].getSide() != side)
				{
					moves.add(new Move(from, to));
				}
				break;
			}
		}
		for (int dx = x + 1; dx < 9; dx++)
		{
			int to = y * 9 + dx;
			Piece target = posArray[to];
			if (target == Piece.noPiece)
			{
				moves.add(new Move(from, to));
			}
			else
			{
				if (posArray[to].getSide() != side)
				{
					moves.add(new Move(from, to));
				}
				break;
			}
		}
		for (int dy = y + 1; dy < 10; dy++)
		{
			int to = dy * 9 + x;
			Piece target = posArray[to];
			if (target == Piece.noPiece)
			{
				moves.add(new Move(from, to));
			}
			else
			{
				if (posArray[to].getSide() != side)
				{
					moves.add(new Move(from, to));
				}
				break;
			}
		}
		return moves;
	}
}
