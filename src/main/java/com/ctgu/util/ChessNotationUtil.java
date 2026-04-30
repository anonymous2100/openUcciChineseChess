package com.ctgu.util;

import com.ctgu.enums.Piece;
import com.ctgu.enums.Side;
import com.ctgu.model.Move;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 将 ICCS 走法坐标转换为传统中国象棋记谱，例如「炮二平五」「马8进7」。
 * <p>
 * 坐标约定（与 Move 类一致）：
 * <pre>
 *   x = 列索引 0(a)…8(i)，棋盘左 → 右
 *   y = 行索引 0(顶，黑方底线) … 9(底，红方底线)
 *   红方列名：x=0→九, x=1→八, …, x=8→一
 *   黑方列名：x=0→1, x=1→2, …, x=8→9
 * </pre>
 */
public class ChessNotationUtil
{
	/** 红方列名（下标对应棋盘 x 坐标） */
	private static final String[] RED_COL   = {"九","八","七","六","五","四","三","二","一"};
	/** 黑方列名（下标对应棋盘 x 坐标） */
	private static final String[] BLACK_COL = {"1","2","3","4","5","6","7","8","9"};

	/**
	 * 将一步走法转换为传统记谱字符串。
	 *
	 * @param move  需要描述的走法（ICCS）
	 * @param board 走法执行**前**的棋盘状态（90 格 Piece 数组）
	 * @return 传统记谱，如「炮二平五」；无法转换时返回 ICCS 原串
	 */
	public static String toChineseNotation(Move move, Piece[] board)
	{
		if (move == null || board == null) return "";
		Piece piece = board[move.from];
		if (piece == null || piece.isEmpty()) return move.name;

		boolean isRed = piece.getSide() == Side.Red;
		String pieceName  = getPieceName(piece);
		String fromDesc   = getFromDescriptor(move.fromX, move.fromY, piece, board, isRed);
		String dirAndDest = getDirectionAndDest(piece, move.fromX, move.fromY, move.toX, move.toY, isRed);

		return pieceName + fromDesc + dirAndDest;
	}

	// ─────────────────────────── 内部辅助方法 ────────────────────────────

	private static String getPieceName(Piece p)
	{
		switch (p)
		{
			case redKing:      return "帅";
			case redAdvisor:   return "仕";
			case redBishop:    return "相";
			case redKnight:    return "马";
			case redRook:      return "车";
			case redCanon:     return "炮";
			case redPawn:      return "兵";
			case blackKing:    return "将";
			case blackAdvisor: return "士";
			case blackBishop:  return "象";
			case blackKnight:  return "马";
			case blackRook:    return "车";
			case blackCanon:   return "炮";
			case blackPawn:    return "卒";
			default:           return "?";
		}
	}

	/**
	 * 决定「从哪里」的列描述符：
	 * <ul>
	 *   <li>该列只有一枚同色同种棋子 → 列名（如「二」「8」）</li>
	 *   <li>同列有两枚同色同种棋子 → 前/后</li>
	 *   <li>同列有三枚（如三兵叠列）→ 前/中/后</li>
	 * </ul>
	 */
	private static String getFromDescriptor(int fromX, int fromY, Piece piece,
			Piece[] board, boolean isRed)
	{
		// 收集同列、同色、同种棋子的行号
		List<Integer> rows = new ArrayList<>();
		for (int pos = 0; pos < 90; pos++)
		{
			if (board[pos] == piece && (pos % 9) == fromX)
			{
				rows.add(pos / 9);   // 行号 0=顶, 9=底
			}
		}

		if (rows.size() == 1)
		{
			return colName(fromX, isRed);
		}

		Collections.sort(rows);               // 升序：0=顶, 9=底
		int rank = rows.indexOf(fromY);       // 该棋子在排序列表中的位置

		if (rows.size() == 2)
		{
			if (isRed)
			{
				// 红方：y 越小越靠近黑方阵营（前）
				return rank == 0 ? "前" : "后";
			}
			else
			{
				// 黑方：y 越大越靠近红方阵营（前）
				return rank == rows.size() - 1 ? "前" : "后";
			}
		}
		else
		{
			// 三枚及以上
			if (isRed)
			{
				if (rank == 0)               return "前";
				if (rank == rows.size() - 1) return "后";
				return "中";
			}
			else
			{
				if (rank == rows.size() - 1) return "前";
				if (rank == 0)               return "后";
				return "中";
			}
		}
	}

	/**
	 * 构建「方向+目标」部分：进/退/平 + 步数或目标列名。
	 */
	private static String getDirectionAndDest(Piece piece,
			int fromX, int fromY, int toX, int toY, boolean isRed)
	{
		// 横向平移
		if (toY == fromY)
		{
			return "平" + colName(toX, isRed);
		}

		// 进/退
		boolean advancing = isRed ? (toY < fromY) : (toY > fromY);
		String dir = advancing ? "进" : "退";

		// 车/炮：纵向步数
		if (piece.isRook() || piece.isCanon())
		{
			return dir + Math.abs(toY - fromY);
		}

		// 帅/将：步数（始终为1）
		if (piece.isKing())
		{
			return dir + Math.abs(toY - fromY);
		}

		// 兵/卒：向前一步
		if (piece.isPawn())
		{
			return dir + "1";
		}

		// 马/象/仕 → 目标列名
		return dir + colName(toX, isRed);
	}

	private static String colName(int x, boolean isRed)
	{
		return isRed ? RED_COL[x] : BLACK_COL[x];
	}
}

