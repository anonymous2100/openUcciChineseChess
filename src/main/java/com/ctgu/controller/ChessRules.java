package com.ctgu.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ctgu.enums.Piece;
import com.ctgu.enums.PieceColor;
import com.ctgu.enums.Side;
import com.ctgu.model.Move;
import com.ctgu.model.Position;
import com.ctgu.pieces.Advisor;
import com.ctgu.pieces.Bishop;
import com.ctgu.pieces.Cannon;
import com.ctgu.pieces.King;
import com.ctgu.pieces.Knight;
import com.ctgu.pieces.Pawn;
import com.ctgu.pieces.Rook;

public class ChessRules
{
	public static final Map<Piece, String> zhName = new HashMap<>();

	static
	{
		zhName.put(Piece.noPiece, "");
		zhName.put(Piece.redRook, "车");
		zhName.put(Piece.redKnight, "马");
		zhName.put(Piece.redBishop, "相");
		zhName.put(Piece.redAdvisor, "仕");
		zhName.put(Piece.redKing, "帅");
		zhName.put(Piece.redCanon, "炮");
		zhName.put(Piece.redPawn, "兵");
		zhName.put(Piece.blackRook, "车");
		zhName.put(Piece.blackKnight, "马");
		zhName.put(Piece.blackBishop, "象");
		zhName.put(Piece.blackAdvisor, "士");
		zhName.put(Piece.blackKing, "将");
		zhName.put(Piece.blackCanon, "炮");
		zhName.put(Piece.blackPawn, "卒");
	}

	public static boolean isRed(String c)
	{
		return "RNBAKCP".contains(c);
	}

	public static boolean isBlack(String c)
	{
		return "rnbakcp".contains(c);
	}

	public static void initBoard(Piece[] posArray)
	{
		for (int i = 0; i < 90; i++)
		{
			posArray[i] = Piece.noPiece;
		}
		posArray[0] = Piece.blackRook;
		posArray[1] = Piece.blackKnight;
		posArray[2] = Piece.blackBishop;
		posArray[3] = Piece.blackAdvisor;
		posArray[4] = Piece.blackKing;
		posArray[5] = Piece.blackAdvisor;
		posArray[6] = Piece.blackBishop;
		posArray[7] = Piece.blackKnight;
		posArray[8] = Piece.blackRook;
		posArray[19] = Piece.blackCanon;
		posArray[25] = Piece.blackCanon;
		posArray[27] = Piece.blackPawn;
		posArray[29] = Piece.blackPawn;
		posArray[31] = Piece.blackPawn;
		posArray[33] = Piece.blackPawn;
		posArray[35] = Piece.blackPawn;
		posArray[54] = Piece.redPawn;
		posArray[56] = Piece.redPawn;
		posArray[58] = Piece.redPawn;
		posArray[60] = Piece.redPawn;
		posArray[62] = Piece.redPawn;
		posArray[64] = Piece.redCanon;
		posArray[70] = Piece.redCanon;
		posArray[81] = Piece.redRook;
		posArray[82] = Piece.redKnight;
		posArray[83] = Piece.redBishop;
		posArray[84] = Piece.redAdvisor;
		posArray[85] = Piece.redKing;
		posArray[86] = Piece.redAdvisor;
		posArray[87] = Piece.redBishop;
		posArray[88] = Piece.redKnight;
		posArray[89] = Piece.redRook;
	}

	public static boolean posOnBoard(int pos)
	{
		return pos >= 0 && pos <= 89;
	}

	public static boolean hasAttackAbility(Position position, Side side)
	{
		Piece[] posArray = position.getPosArray();
		for (int y = 0; y < 10; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				Piece id = posArray[(y * 9) + x];
				if (id.getSide() == side && (id.isRook() || id.isKnight() || id.isCanon() || id.isPawn()))
				{
					return true;
				}
			}
		}
		return false;
	}

	public static boolean legalMove(Position position, Move move)
	{
		if (move.from < 0 || move.from > 89)
		{
			return false;
		}
		if (move.to < 0 || move.to > 89)
		{
			return false;
		}
		Side side = position.getSide();
		Piece[] posArray = position.getPosArray();
		if (posArray[move.to].getSide() == side)
		{
			return false;
		}
		Piece piece = posArray[move.from];
		boolean valid = false;
		if (piece == Piece.redKing || piece == Piece.blackKing)
		{
			valid = King.validateKingMove(position, move);
		}
		else if (piece == Piece.redAdvisor || piece == Piece.blackAdvisor)
		{
			valid = Advisor.validateAdvisorMove(position, move);
		}
		else if (piece == Piece.redBishop || piece == Piece.blackBishop)
		{
			valid = Bishop.validateBishopMove(position, move);
		}
		else if (piece == Piece.redKnight || piece == Piece.blackKnight)
		{
			valid = Knight.validateKnightMove(position, move);
		}
		else if (piece == Piece.redRook || piece == Piece.blackRook)
		{
			valid = Rook.validateRookMove(position, move);
		}
		else if (piece == Piece.redCanon || piece == Piece.blackCanon)
		{
			valid = Cannon.validateCanonMove(position, move);
		}
		else if (piece == Piece.redPawn || piece == Piece.blackPawn)
		{
			valid = Pawn.validatePawnMove(position, move);
		}
		if (!valid)
		{
			return false;
		}
		if (willBeChecked(position, move))
		{
			return false;
		}
		if (willKingsMeet(position, move))
		{
			return false;
		}
		return true;
	}

	public static boolean willKingsMeet(Position position, Move move)
	{
		Position tempPosition = Position.clone(position);
		tempPosition.moveTest(move, false);
		for (int x = 3; x < 6; x++)
		{
			boolean foundKingAlready = false;
			for (int y = 0; y < 10; y++)
			{
				Piece piece = tempPosition.pieceAt(y * 9 + x);
				if (!foundKingAlready)
				{
					if (piece == Piece.redKing || piece == Piece.blackKing)
					{
						foundKingAlready = true;
					}
					if (y > 2)
					{
						break;
					}
				}
				else
				{
					if (piece == Piece.redKing || piece == Piece.blackKing)
					{
						return true;
					}
					if (piece != Piece.noPiece)
					{
						break;
					}
				}
			}
		}
		return false;
	}

	public static boolean willBeChecked(Position position, Move move)
	{
		Position tempPosition = Position.clone(position);
		tempPosition.moveTest(move, false);
		return beChecked(tempPosition);
	}

	public static boolean beChecked(Position position)
	{
		int myKingPos = findKingPos(position);
		Position opponentPosition = Position.clone(position);
		opponentPosition.turnSide();
		List<Move> opponentMoves = enumMoves(opponentPosition);
		for (Move move : opponentMoves)
		{
			if (move.to == myKingPos)
			{
				return true;
			}
		}
		return false;
	}

	public static boolean check(Position position)
	{
		int kingPos = findKingPos(position);
		List<Move> stepList = enumMoves(position);
		for (Move move : stepList)
		{
			int stepFrom = move.from;
			Side fromSide = position.getPosArray()[stepFrom].getSide();
			if (move.to == kingPos && position.getSide() != fromSide)
			{
				return true;
			}
		}
		return false;
	}

	public static List<Move> enumMoves(Position position)
	{
		List<Move> moves = new ArrayList<>();
		for (int y = 0; y < 10; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				int from = y * 9 + x;
				Piece piece = position.pieceAt(from);
				if (PieceColor.of(piece) != position.getSide())
				{
					continue;
				}
				List<Move> pieceMoves;
				if (piece == Piece.redKing || piece == Piece.blackKing)
				{
					pieceMoves = King.enumKingMoves(position, y, x, from);
				}
				else if (piece == Piece.redAdvisor || piece == Piece.blackAdvisor)
				{
					pieceMoves = Advisor.enumAdvisorMoves(position, y, x, from);
				}
				else if (piece == Piece.redBishop || piece == Piece.blackBishop)
				{
					pieceMoves = Bishop.enumBishopMoves(position, y, x, from);
				}
				else if (piece == Piece.redKnight || piece == Piece.blackKnight)
				{
					pieceMoves = Knight.enumKnightMoves(position, y, x, from);
				}
				else if (piece == Piece.redRook || piece == Piece.blackRook)
				{
					pieceMoves = Rook.enumRookMoves(position, y, x, from);
				}
				else if (piece == Piece.redCanon || piece == Piece.blackCanon)
				{
					pieceMoves = Cannon.enumCanonMoves(position, y, x, from);
				}
				else if (piece == Piece.redPawn || piece == Piece.blackPawn)
				{
					pieceMoves = Pawn.enumPawnMoves(position, y, x, from);
				}
				else
				{
					continue;
				}
				moves.addAll(pieceMoves);
			}
		}
		return moves;
	}

	public static int findKingPos(Position position)
	{
		Piece[] posArray = position.getPosArray();
		Side side = position.getSide();
		for (int i = 0; i < posArray.length; i++)
		{
			Piece id = posArray[i];
			if (id.isKing() && side == id.getSide())
			{
				return i;
			}
		}
		return -1;
	}

	public static String printChessArray(Piece[] posArray)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("\n");
		for (int pos = 0; pos < 90; pos++)
		{
			Piece chessId = posArray[pos];
			if (chessId.getAbbreviation() == Piece.noPiece.getAbbreviation())
			{
				sb.append("+\t");
			}
			else
			{
				sb.append(getChineseChess(chessId.getAbbreviation())).append("\t");
			}
			if ((pos + 1) % 9 == 0)
			{
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	public static String getChineseChess(char chessChar)
	{
		String chessValue = String.valueOf(chessChar);
		if (chessValue.startsWith("r") || chessValue.startsWith("R"))
		{
			return "车";
		}
		else if (chessValue.startsWith("n") || chessValue.startsWith("N"))
		{
			return "马";
		}
		else if (chessValue.startsWith("b") || chessValue.startsWith("B"))
		{
			if (Character.isUpperCase(chessChar))
			{
				return "相";
			}
			else
			{
				return "象";
			}
		}
		else if (chessValue.startsWith("a") || chessValue.startsWith("A"))
		{
			if (Character.isUpperCase(chessChar))
			{
				return "仕";
			}
			else
			{
				return "士";
			}
		}
		else if (chessValue.startsWith("k") || chessValue.startsWith("K"))
		{
			if (Character.isUpperCase(chessChar))
			{
				return "帅";
			}
			else
			{
				return "将";
			}
		}
		else if (chessValue.startsWith("c") || chessValue.startsWith("C"))
		{
			return "炮";
		}
		else if (chessValue.startsWith("p") || chessValue.startsWith("P"))
		{
			if (Character.isUpperCase(chessChar))
			{
				return "兵";
			}
			else
			{
				return "卒";
			}
		}
		return " ";
	}
}
