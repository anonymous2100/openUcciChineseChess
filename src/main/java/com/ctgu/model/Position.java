package com.ctgu.model;

import com.ctgu.controller.ChessRules;
import com.ctgu.enums.Piece;
import com.ctgu.enums.Side;
import com.ctgu.util.ChessFenUtil;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Position
{
	private Piece[] posArray;
	private Side side;
	private MoveCounter moveCounter;

	public Position(boolean computerFirst)
	{
		side = Side.Red;
		posArray = new Piece[90];
		moveCounter = new MoveCounter();
		ChessRules.initBoard(posArray);
		side = computerFirst ? Side.Black : Side.Red;
	}

	public Position(Position otherPosition)
	{
		side = Side.Red;
		posArray = new Piece[90];
		moveCounter = new MoveCounter();
		side = otherPosition.side.swap();
		System.arraycopy(otherPosition.posArray, 0, posArray, 0, 90);
		moveCounter.copy(otherPosition.moveCounter);
	}

	public Position(String fen)
	{
		side = Side.Red;
		posArray = new Piece[90];
		moveCounter = new MoveCounter();
		if (!ChessFenUtil.fromFen(this, fen))
		{
			ChessRules.initBoard(posArray);
		}
	}

	public void moveTest(Move move, boolean turnSide)
	{
		posArray[move.to] = posArray[move.from];
		posArray[move.from] = Piece.noPiece;
		if (turnSide)
		{
			side = side.swap();
		}
	}

	public void turnSide()
	{
		side = side.swap();
	}

	public Piece pieceAt(int index)
	{
		return posArray[index];
	}

	public void update(Move move, Side side)
	{
		moveCounter.update(move, side);
	}

	public String toFen()
	{
		return ChessFenUtil.toFen(this);
	}

	public boolean fromFen(String fen)
	{
		return ChessFenUtil.fromFen(this, fen);
	}

	public static Position clone(Position otherPosition)
	{
		Position position = new Position(false);
		Piece[] pieces = new Piece[otherPosition.getPosArray().length];
		for (int i = 0; i < otherPosition.getPosArray().length; i++)
		{
			pieces[i] = otherPosition.getPosArray()[i];
		}
		position.setPosArray(pieces);
		position.setSide(otherPosition.getSide());
		position.setMoveCounter(new MoveCounter(otherPosition.getMoveCounter().getHalfMove(), otherPosition.getMoveCounter().getFullMove()));
		return position;
	}

	@Override
	public String toString()
	{
		return "Position [posArray=" + ChessRules.printChessArray(posArray) + ", side=" + side + ", moveCounter=" + moveCounter + "]";
	}
}
