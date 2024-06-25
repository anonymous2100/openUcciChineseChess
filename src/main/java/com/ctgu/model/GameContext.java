package com.ctgu.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.ctgu.config.Config;
import com.ctgu.constant.ChessConstant;
import com.ctgu.enums.Piece;
import com.ctgu.enums.Side;

public class GameContext
{
	private String initFen;
	private LinkedList<String> fenList;
	private LinkedList<Position> positionList;
	private LinkedList<Move> moveList;
	private LinkedList<String> moveStringList;

	public GameContext()
	{
		moveList = new LinkedList<>();
		positionList = new LinkedList<>();
		fenList = new LinkedList<>();
		moveStringList = new LinkedList<>();
		prepare();
	}

	public GameContext(Position prevPosition, Move prevMove)
	{
		this();
		moveList.add(prevMove);
		positionList.add(prevPosition);
	}

	public String getManualText()
	{
		StringBuffer sb = new StringBuffer();
		if (moveStringList.size() == 0)
		{
			return sb.toString();
		}
		for (int i = 0; i < moveStringList.size(); i++)
		{
			sb.append(moveStringList.get(i));
			sb.append(" , ");
			if (i % 2 == 1)
			{
				sb.deleteCharAt(sb.length() - 1);
				sb.deleteCharAt(sb.length() - 1);
				sb.append(";").append("\n");
			}
		}
		return sb.toString();
	}

	private String translate(Position position, Move move)
	{
		int side;
		if (position.getSide().isRed())
		{
			side = 0;
		}
		else
		{
			side = 1;
		}
		StringBuffer sb = new StringBuffer(transChessName(position, move));
		if (move.toY == move.fromY)
		{
			sb.append(ChessConstant.DIRECTION_NAMES[5]);
			sb.append(ChessConstant.DIGITS[side].charAt(move.toX));
		}
		else
		{
			int dir;
			if (side == 0)
			{
				dir = -1;
			}
			else
			{
				dir = 1;
			}
			sb.append((move.toY - move.fromY) * dir > 0 ? ChessConstant.DIRECTION_NAMES[3] : ChessConstant.DIRECTION_NAMES[4]);
			sb.append(move.toX == move.fromX ? ChessConstant.DIGITS[2 - side].charAt(Math.abs(move.toY - move.fromY) - 1)
					: ChessConstant.DIGITS[side].charAt(move.toX));
		}
		return sb.toString();
	}

	private String transChessName(Position position, Move move)
	{
		int side;
		if (position.getSide().isRed())
		{
			side = 0;
		}
		else
		{
			side = 1;
		}
		Piece id = position.getPosArray()[move.from];
		char chessName = getChessName(id, ChessConstant.CHESS_NAMES[side]);
		StringBuffer sb = new StringBuffer();
		if (id.isKing())
		{
			sb.append(chessName);
			sb.append(ChessConstant.DIGITS[side].charAt(move.fromX));
			return sb.toString();
		}
		Map<Integer, List<Integer>> lines = listInLine(position.getPosArray(), id);
		List<Integer> cols = multiPieceCols(lines);
		int colCount = cols.size();
		if (!id.isPawn())
		{
			switch (colCount)
			{
				case 0:
					sb.append(chessName);
					sb.append(ChessConstant.DIGITS[side].charAt(move.fromX));
					return sb.toString();
				case 1:
					sb.append(move.from == ((Integer) ((List<Integer>) lines.get(cols.get(0))).get(side)).intValue() ? ChessConstant.DIRECTION_NAMES[0]
							: ChessConstant.DIRECTION_NAMES[2]).append(chessName);
					return sb.toString();
				default:
					break;
			}
		}
		int index;
		switch (colCount)
		{
			case 0:
				sb.append(chessName);
				sb.append(ChessConstant.DIGITS[side].charAt(move.fromX));
				return sb.toString();
			case 1:
				List<Integer> posArray = (List<Integer>) lines.get(cols.get(0));
				int count = posArray.size();
				int p1 = ((Integer) posArray.get(side)).intValue();
				int p2 = ((Integer) posArray.get(1 - side)).intValue();
				char posDir = ' ';
				if (count == 2)
				{
					if (move.from == p1)
					{
						posDir = ChessConstant.DIRECTION_NAMES[0];
					}
					else
					{
						posDir = ChessConstant.DIRECTION_NAMES[2];
					}
				}
				else if (count == 3)
				{
					posDir = (move.from == p1 ? ChessConstant.DIRECTION_NAMES[0]
							: move.from == p2 ? ChessConstant.DIRECTION_NAMES[1] : ChessConstant.DIRECTION_NAMES[2]);
				}
				else
				{
					index = 0;
					while (index < posArray.size())
					{
						if (move.from == ((Integer) posArray.get(index)).intValue())
						{
							posDir = ChessConstant.DIGITS[2 - side].charAt(index);
						}
						else
						{
							index++;
						}
					}
				}
				sb.append(posDir).append(chessName);
				return sb.toString();
			case 2:
				Collections.sort(cols);
				List<Integer> leftCol = (List<Integer>) lines.get(cols.get(side));
				List<Integer> rightCol = (List<Integer>) lines.get(cols.get(1 - side));
				int rightColSize = rightCol.size();
				int leftColSize = leftCol.size();
				for (index = 0; index < rightColSize; index++)
				{
					if (move.from == ((Integer) rightCol.get(index)).intValue())
					{
						sb.append(ChessConstant.DIGITS[2 - side].charAt(side == 0 ? index : (rightColSize - index) - 1)).append(chessName);
						return sb.toString();
					}
				}
				for (index = 0; index < leftColSize; index++)
				{
					if (move.from == ((Integer) leftCol.get(index)).intValue())
					{
						sb.append(ChessConstant.DIGITS[2 - side].charAt((side == 0 ? index : (leftColSize - index) - 1) + rightColSize)).append(chessName);
						return sb.toString();
					}
				}
				throw new RuntimeException("Logic Error");
		}
		return "";
	}

	private char getChessName(Piece id, String chessNameString)
	{
		switch (id.getAbbreviation())
		{
			case 'A':
				return chessNameString.charAt(1);
			case 'B':
				return chessNameString.charAt(2);
			case 'C':
			case 'c':
				return chessNameString.charAt(5);
			case 'K':
			case 'k':
				return chessNameString.charAt(0);
			case 'N':
			case 'n':
				return chessNameString.charAt(3);
			case 'P':
			case 'p':
				return chessNameString.charAt(6);
			case 'R':
			case 'r':
				return chessNameString.charAt(4);
			default:
				return '-';
		}
	}

	private Map<Integer, List<Integer>> listInLine(Piece[] posArray, Piece id)
	{
		Map<Integer, List<Integer>> lines = new HashMap<>();
		for (int pos = 0; pos < posArray.length; pos++)
		{
			if (posArray[pos] == id)
			{
				int col = pos % 9;
				List<Integer> line = (List<Integer>) lines.get(Integer.valueOf(col));
				if (line == null)
				{
					line = new ArrayList<>();
					lines.put(Integer.valueOf(col), line);
				}
				line.add(Integer.valueOf(pos));
			}
		}
		return lines;
	}

	private List<Integer> multiPieceCols(Map<Integer, List<Integer>> lines)
	{
		List<Integer> containMultiPieceCols = new ArrayList<>();
		for (Integer col : lines.keySet())
		{
			if (lines.get(col).size() > 1)
			{
				containMultiPieceCols.add(col);
			}
		}
		return containMultiPieceCols;
	}

	public boolean saveManual(String filePath)
	{
		PrintWriter writer = null;
		try
		{
			FileOutputStream fos = new FileOutputStream(new File(filePath));
			OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
			writer = new PrintWriter(osw);
			try
			{
				writeManual(writer);
				if (writer != null)
				{
					writer.close();
				}
				return true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	private void writeManual(PrintWriter writer)
	{
		DateFormat fmt = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
		writer.println("[Game \"Chinese Chess\"] ");
		writer.println("[Event \"Person vs Machine\"] ");
		writer.format("[Date \"%s\"] \n", new Object[] { fmt.format(new Date()) });
		writer.println("[Red \"Person\"] ");
		writer.println("[Black \"Machine\"] ");
		if (initFen != null && initFen.length() > 10)
		{
			writer.format("[FEN \"%s\"] \n", new Object[] { initFen });
		}
		writer.println("[Result \"*\"] ");
		int moveCount = moveStringList.size();

		for (int i = 0; i < moveCount; i++)
		{
			if (i % 2 == 0)
			{
				writer.format(" %d. %s ", new Object[] { Integer.valueOf((i / 2) + 1), moveStringList.get(i) });
			}
			else
			{
				writer.format(" %s \n", new Object[] { moveStringList.get(i) });
			}
		}
		writer.println(" *");
	}

	public int length()
	{
		return fenList.size();
	}

	public LinkedList<Position> getPositionArray()
	{
		return positionList;
	}

	public void reset()
	{
		moveStringList.clear();
		fenList.clear();
		moveList.clear();
		positionList.clear();
		moveList.add(new Move());
		positionList.add(new Position(Config.get().isComputerFirst()));
		prepare();
	}

	private void prepare()
	{
		moveList.add(new Move());
		positionList.add(new Position(Config.get().isComputerFirst()));
	}

	public boolean retract()
	{
		if (moveList.size() < 2)
		{
			return false;
		}
		if (moveStringList.size() > 0)
		{
			moveStringList.removeLast();
		}
		if (fenList.size() > 0)
		{
			fenList.removeLast();
		}
		if (moveList.size() > 0)
		{
			moveList.removeLast();
		}
		if (positionList.size() > 0)
		{
			positionList.removeLast();
		}
		return true;
	}

	public Position popup()
	{
		if (positionList.size() < 2)
		{
			return null;
		}
		moveList.removeLast();
		return positionList.removeLast();
	}

	public void recordStep(Position position, Move move)
	{
		fenList.add(position.toFen());
		moveStringList.add(translate(position, move));
	}

	public void applyMove(Move move)
	{
		Position lastPosition = currentPosition();
		Side side = lastPosition.getSide();
		Position currentPosition = new Position(lastPosition);
		moveList.add(move);
		positionList.add(currentPosition);
		Piece[] posArray = currentPosition.getPosArray();
		if (posArray[move.to].notEmpty())
		{
			move.setCaptured(true);
		}
		posArray[move.to] = posArray[move.from];
		posArray[move.from] = Piece.noPiece;
		currentPosition().update(move, side);
	}

	public String getPositionCommand()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("position ");
		buffer.append("fen ").append(lastPosition().toFen());
		String moves = lastMoves();
		if (moves.length() > 0)
		{
			buffer.append(" moves").append(moves);
		}
		return buffer.toString();
	}

	private Position lastPosition()
	{
		for (int i = moveList.size() - 1; i > -1; i--)
		{
			if (moveList.get(i).isCaptured())
			{
				return positionList.get(i);
			}
		}
		return positionList.get(positionList.size() - 1);
	}

	private String lastMoves()
	{
		StringBuffer buffer = new StringBuffer();
		int count = moveList.size();
		int index = count - 1;
		while (index > 0 && !(moveList.get(index)).isCaptured())
		{
			index--;
		}
		for (int i = index + 1; i < count; i++)
		{
			buffer.append(' ');
			if (moveList.get(i).name != null)
			{
				buffer.append(moveList.get(i).name);
			}
		}
		return buffer.toString();
	}

	public String toFen()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(currentPosition().toFen());
		buffer.append(" - - ");
		buffer.append(' ').append(currentPosition().getMoveCounter().getFenCounterMarks());
		return buffer.toString();
	}

	public boolean fromFen(String fen)
	{
		Position position = new Position(fen);
		moveList.clear();
		positionList.clear();
		moveList.add(new Move());
		positionList.add(position);
		return true;
	}

	public Move lastMove()
	{
		return moveList.getLast();
	}

	public Move prevMove()
	{
		int count = moveCount();
		if (count < 1)
		{
			return null;
		}
		return moveList.get(count);
	}

	public Position currentPosition()
	{
		return positionList.getLast();
	}

	public int moveCount()
	{
		return moveList.size() - 1;
	}

	public Move getMove(int index)
	{
		return moveList.get(index);
	}

	public Position getPosition(int index)
	{
		return positionList.get(index);
	}

	@Override
	public String toString()
	{
		return "GameContext [initFen=" + initFen + ", fenList=" + fenList + ", positionList=" + positionList + ", moveList=" + moveList + ", moveStringList="
				+ moveStringList + "]";
	}
}
