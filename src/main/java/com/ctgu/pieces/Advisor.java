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

public class Advisor
{
	public static boolean validateAdvisorMove(Position position, Move move)
	{
		int adx = Math.abs(move.toX - move.fromX);
		int ady = Math.abs(move.toY - move.fromY);
		if (adx != 1 || ady != 1)
		{
			return false;
		}
		int[] range = (position.getSide() == Side.Red) ? ChessConstant.ADVISOR_RED_RANGE : ChessConstant.ADVISOR_BLACK_RANGE;
		return Arrays.binarySearch(range, move.to) >= 0;
	}

	public static List<Move> enumAdvisorMoves(Position position, int y, int x, int from)
	{
		Side side = position.getSide();
		Piece[] posArray = position.getPosArray();
		List<Move> moves = new ArrayList<>();
		int[][] offsetList = { { -1, -1 }, { 1, -1 }, { -1, 1 }, { 1, 1 } };
		int[] range = (side == Side.Red ? ChessConstant.ADVISOR_RED_RANGE : ChessConstant.ADVISOR_BLACK_RANGE);
		for (int i = 0; i < 4; i++)
		{
			int[] offset = offsetList[i];
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
