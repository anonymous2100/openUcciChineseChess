package com.ctgu.engine;

import com.ctgu.controller.ChessRules;
import com.ctgu.enums.Piece;
import com.ctgu.enums.Side;
import com.ctgu.model.Move;
import com.ctgu.model.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * 纯 Java 象棋 AI：Minimax + Alpha-Beta 剪枝，深度 1–6 可调。
 */
public class LocalAiEngine
{
  private static final Logger logger = Logger.getLogger(LocalAiEngine.class.getName());

  private volatile int      depth;
  private volatile boolean  searching;
  private volatile boolean  stopRequested;
  /** 本次搜索节点计数（粗略值） */
  private volatile long     nodesSearched;

  private final ExecutorService executor = Executors.newSingleThreadExecutor(r ->
  {
    Thread t = new Thread(r, "LocalAI-Thread");
    t.setDaemon(true);
    return t;
  });

  private static final int VAL_KING    = 60000;
  private static final int VAL_ROOK    = 600;
  private static final int VAL_CANNON  = 300;
  private static final int VAL_KNIGHT  = 300;
  private static final int VAL_BISHOP  = 120;
  private static final int VAL_ADVISOR = 120;
  private static final int VAL_PAWN    = 60;

  // 位置加成表：红方视角，index 0 = 红方底线（boardRow 9）
  private static final int[] ROOK_POS = {
       0,  0,  0,  0,  0,  0,  0,  0,  0,
       2,  2,  2,  2,  2,  2,  2,  2,  2,
       4,  4,  4,  4,  4,  4,  4,  4,  4,
       6,  6,  6,  6,  6,  6,  6,  6,  6,
       8,  8,  8,  8,  8,  8,  8,  8,  8,
      10, 10, 10, 10, 10, 10, 10, 10, 10,
      12, 12, 12, 12, 12, 12, 12, 12, 12,
      14, 14, 14, 14, 14, 14, 14, 14, 14,
      14, 14, 14, 14, 14, 14, 14, 14, 14,
      14, 14, 14, 14, 14, 14, 14, 14, 14,
  };

  private static final int[] KNIGHT_POS = {
      -6, -4,  0,  2, -2,  2,  0, -4, -6,
      -4, -2,  2,  4,  0,  4,  2, -2, -4,
      -2,  0,  4,  6,  2,  6,  4,  0, -2,
       0,  2,  6,  8,  4,  8,  6,  2,  0,
       2,  4,  8, 10,  6, 10,  8,  4,  2,
       4,  6, 10, 12,  8, 12, 10,  6,  4,
       6,  8, 12, 14, 10, 14, 12,  8,  6,
       4,  6, 10, 14,  8, 14, 10,  6,  4,
       2,  4,  8, 10,  2, 10,  8,  4,  2,
       0, -2,  4,  4, -2,  4,  4, -2,  0,
  };

  private static final int[] CANNON_POS = {
       0,  2,  4,  6,  6,  6,  4,  2,  0,
       2,  6,  6,  8,  8,  8,  6,  6,  2,
       2,  6,  6,  8,  8,  8,  6,  6,  2,
       2,  6,  6,  8,  8,  8,  6,  6,  2,
       2,  6,  6,  8,  6,  8,  6,  6,  2,
       2,  6,  6,  8,  8,  8,  6,  6,  2,
       4,  8,  8,  8,  8,  8,  8,  8,  4,
       4,  2,  4,  6,  8,  6,  4,  2,  4,
       0,  2,  4,  6,  6,  6,  4,  2,  0,
       0,  0,  2,  6,  6,  6,  2,  0,  0,
  };

  private static final int[] PAWN_POS = {
      32, 26, 34, 28, 36, 28, 34, 26, 32,
      28, 22, 30, 24, 32, 24, 30, 22, 28,
      24, 18, 26, 20, 28, 20, 26, 18, 24,
      20, 10, 22, 14, 24, 14, 22, 10, 20,
      18,  0, 18,  0, 18,  0, 18,  0, 18,
       4,  0,  4,  0,  4,  0,  4,  0,  4,
       2,  0,  2,  0,  2,  0,  2,  0,  2,
       0,  0,  0,  0,  0,  0,  0,  0,  0,
       0,  0,  0,  0,  0,  0,  0,  0,  0,
       0,  0,  0,  0,  0,  0,  0,  0,  0,
  };

  public LocalAiEngine(int depth)
  {
    this.depth = clampDepth(depth);
  }

  public void setDepth(int d) { this.depth = clampDepth(d); }
  public int  getDepth()      { return depth; }
  public boolean isSearching(){ return searching; }
  public void stopSearch()    { stopRequested = true; }

  public void searchAsync(Position position, Consumer<SearchResult> callback)
  {
    searchAsync(position, callback, null);
  }

  /**
   * 异步搜索最佳走法。
   *
   * @param position     当前局面
   * @param callback     搜索完成后回调，在工作线程中执行
   * @param infoCallback 思考过程回调（每步信息），可为 null；在工作线程中执行
   */
  public void searchAsync(Position position, Consumer<SearchResult> callback, Consumer<String> infoCallback)
  {
    if(searching)
    {
      callback.accept(new SearchResult(null, 0, depth));
      return;
    }
    searching = true;
    stopRequested = false;
    nodesSearched = 0;
    Position posClone = Position.clone(position);
    executor.submit(() ->
    {
      try
      {
        SearchResult r = doSearch(posClone, depth, infoCallback);
        callback.accept(r);
      }
      catch(Exception e)
      {
        logger.warning("LocalAI error: " + e.getMessage());
        callback.accept(new SearchResult(null, 0, depth));
      }
      finally
      {
        searching = false;
      }
    });
  }

  private SearchResult doSearch(Position pos, int d, Consumer<String> info)
  {
    List<Move> moves = getLegalMoves(pos);
    if(moves.isEmpty()) return new SearchResult(null, -VAL_KING, d);

    sortMoves(pos, moves);

    int alpha = Integer.MIN_VALUE / 2;
    int beta  = Integer.MAX_VALUE / 2;
    Move best = moves.get(0);
    long startMs = System.currentTimeMillis();

    if(info != null)
    {
      info.accept(String.format("本地AI 开始搜索  深度=%d  候选走法=%d", d, moves.size()));
    }

    int moveIdx = 0;
    for(Move m : moves)
    {
      moveIdx++;
      if(stopRequested) break;
      int score = -negamax(applyMove(pos, m), d - 1, -beta, -alpha);
      boolean improved = score > alpha;
      if(improved)
      {
        alpha = score;
        best  = m;
      }
      if(info != null)
      {
        info.accept(String.format("  [%d/%d] 走法=%-5s  评分=%+6d  节点=%d%s",
            moveIdx, moves.size(), m.name, score, nodesSearched, improved ? "  ★最优" : ""));
      }
    }

    long elapsed = System.currentTimeMillis() - startMs;
    if(info != null)
    {
      info.accept(String.format("本地AI 搜索完毕  最佳走法=%s  评分=%+d  节点=%d  耗时=%dms",
          best != null ? best.name : "无", alpha, nodesSearched, elapsed));
    }
    return new SearchResult(best, alpha, d);
  }

  private int negamax(Position pos, int d, int alpha, int beta)
  {
    nodesSearched++;
    if(stopRequested) return 0;
    if(d == 0) return evaluate(pos);

    List<Move> moves = getLegalMoves(pos);
    if(moves.isEmpty())
      return ChessRules.check(pos) ? -VAL_KING + (depth - d) : 0;

    sortMoves(pos, moves);

    for(Move m : moves)
    {
      if(stopRequested) return alpha;
      int score = -negamax(applyMove(pos, m), d - 1, -beta, -alpha);
      if(score >= beta) return beta;
      if(score > alpha) alpha = score;
    }
    return alpha;
  }

  private List<Move> getLegalMoves(Position pos)
  {
    List<Move> all   = ChessRules.enumMoves(pos);
    List<Move> legal = new ArrayList<>(all.size());
    for(Move m : all)
      if(ChessRules.legalMove(pos, m)) legal.add(m);
    return legal;
  }

  private void sortMoves(Position pos, List<Move> moves)
  {
    Piece[] b = pos.getPosArray();
    moves.sort((a, mv) -> getPieceValue(b[mv.to]) - getPieceValue(b[a.to]));
  }

  private Position applyMove(Position pos, Move m)
  {
    Position next = Position.clone(pos);
    next.moveTest(m, true);
    return next;
  }

  private int evaluate(Position pos)
  {
    Piece[] board = pos.getPosArray();
    int red = 0, black = 0;
    for(int i = 0; i < 90; i++)
    {
      Piece p = board[i];
      if(p == null || p.isEmpty()) continue;
      int v = getPieceValue(p) + posBonus(p, i);
      if(p.getSide() == Side.Red) red += v;
      else black += v;
    }
    int total = red - black;
    return pos.getSide() == Side.Red ? total : -total;
  }

  public static int getPieceValue(Piece p)
  {
    if(p == null || p.isEmpty()) return 0;
    if(p.isKing())    return VAL_KING;
    if(p.isRook())    return VAL_ROOK;
    if(p.isCanon())   return VAL_CANNON;
    if(p.isKnight())  return VAL_KNIGHT;
    if(p.isBishop())  return VAL_BISHOP;
    if(p.isAdvisor()) return VAL_ADVISOR;
    if(p.isPawn())    return VAL_PAWN;
    return 0;
  }

  private int posBonus(Piece p, int pos)
  {
    int boardRow = pos / 9;
    int col      = pos % 9;
    int tableRow = (p.getSide() == Side.Red) ? (9 - boardRow) : boardRow;
    int idx      = tableRow * 9 + col;
    if(idx < 0 || idx >= 90) return 0;
    if(p.isRook())   return ROOK_POS[idx];
    if(p.isKnight()) return KNIGHT_POS[idx];
    if(p.isCanon())  return CANNON_POS[idx];
    if(p.isPawn())   return PAWN_POS[idx];
    return 0;
  }

  private static int clampDepth(int d) { return Math.max(1, Math.min(6, d)); }

  public void shutdown()
  {
    stopRequested = true;
    executor.shutdownNow();
  }

  // ── SearchResult ──────────────────────────────────────────────────────

  public static class SearchResult
  {
    public final Move bestMove;
    public final int  score;
    public final int  depth;

    public SearchResult(Move bestMove, int score, int depth)
    {
      this.bestMove = bestMove;
      this.score    = score;
      this.depth    = depth;
    }
  }
}

