package com.ctgu.pieces;

import java.util.ArrayList;
import java.util.List;

import com.ctgu.controller.ChessRules;
import com.ctgu.enums.Piece;
import com.ctgu.enums.Side;
import com.ctgu.model.Move;
import com.ctgu.model.Position;

public class Knight
{
	public static boolean validateKnightMove(Position position, Move move)
	{
		int dx = move.toX - move.fromX;
		int dy = move.toY - move.fromY;
		int adx = Math.abs(dx);
		int ady = Math.abs(dy);
		if (!(adx == 1 && ady == 2) && !(adx == 2 && ady == 1))
		{
			return false;
		}
		if (adx > ady)
		{
			if (dx > 0)
			{
				int foot = move.fromY * 9 + move.fromX + 1;
				if (position.pieceAt(foot) != Piece.noPiece)
				{
					return false;
				}
			}
			else
			{
				int foot = move.fromY * 9 + move.fromX - 1;
				if (position.pieceAt(foot) != Piece.noPiece)
				{
					return false;
				}
			}
		}
		else
		{
			if (dy > 0)
			{
				int foot = (move.fromY + 1) * 9 + move.fromX;
				if (position.pieceAt(foot) != Piece.noPiece)
				{
					return false;
				}
			}
			else
			{
				int foot = (move.fromY - 1) * 9 + move.fromX;
				if (position.pieceAt(foot) != Piece.noPiece)
				{
					return false;
				}
			}
		}
		return true;
	}

	public static List<Move> enumKnightMoves(Position position, int y, int x, int from)
	{
		Side side = position.getSide();
		Piece[] posArray = position.getPosArray();
		List<Move> moves = new ArrayList<>();
		int[][] offsetList = new int[][] { { -1, 2 }, { -2, 1 }, { -2, -1 }, { -1, -2 }, { 1, -2 }, { 2, -1 }, { 2, 1 }, { 1, 2 } };
		int[][] footOffsetList = new int[][] { { 0, 1 }, { -1, 0 }, { -1, 0 }, { 0, -1 }, { 0, -1 }, { 1, 0 }, { 1, 0 }, { 0, 1 } };
		for (int i = 0; i < offsetList.length; i++)
		{
			int[] temp = offsetList[i];
			int toX = x + temp[0];
			int toY = y + temp[1];
			if (toY < 0 || toY > 9 || toX < 0 || toX > 9)
			{
				continue;
			}
			int to = toY * 9 + toX;
			if (!ChessRules.posOnBoard(to) || posArray[to].getSide() == side)
			{
				continue;
			}
			int[] tempFoot = footOffsetList[i];
			int footToX = x + tempFoot[0];
			int footToY = y + tempFoot[1];
			int footTo = footToY * 9 + footToX;
			if (!ChessRules.posOnBoard(footTo) || !posArray[footTo].isEmpty())
			{
				continue;
			}
			moves.add(new Move(from, to));
		}
		return moves;
	}
}
