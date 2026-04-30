package com.ctgu.model;

/**
 * 引擎分析结果：保存分析后的最优走法、评分及变例信息
 */
public class AnalysisResult
{
  /**
   * 最优走法，nobestmove 时为 null
   */
  private final Move bestMove;
  /**
   * 分（厘兵，100 = 1个兵的优势），正值=当前行棋方优势
   */
  private final int scoreInCentipawns;
  /**
   * 是否为将杀
   */
  private final boolean mate;
  /**
   * 将杀步数（mate=true 时有效，正=当前方将杀，负=对方将杀）
   */
  private final int mateIn;
  /**
   * 引擎搜索深度
   */
  private final int depth;
  /**
   * 主变例（ICCS 走法序列，空格分隔）
   */
  private final String pv;
  /**
   * 是否为「走法提示」请求（true=提示，false=局势分析）
   */
  private final boolean hintMode;

  public AnalysisResult(Move bestMove, int scoreInCentipawns, boolean mate, int mateIn, int depth, String pv, boolean hintMode)
  {
    this.bestMove = bestMove;
    this.scoreInCentipawns = scoreInCentipawns;
    this.mate = mate;
    this.mateIn = mateIn;
    this.depth = depth;
    this.pv = pv;
    this.hintMode = hintMode;
  }

  public Move getBestMove()
  {
    return bestMove;
  }

  public int getScoreInCentipawns()
  {
    return scoreInCentipawns;
  }

  public boolean isMate()
  {
    return mate;
  }

  public int getMateIn()
  {
    return mateIn;
  }

  public int getDepth()
  {
    return depth;
  }

  public String getPv()
  {
    return pv;
  }

  public boolean isHintMode()
  {
    return hintMode;
  }

  /**
   * 生成人类可读的分析摘要。
   *
   * @param chineseNotation 传统记谱（如「炮二平五」），为 null 或空时 fallback 到 ICCS 坐标
   */
  public String toDisplayText(String chineseNotation)
  {
    StringBuilder sb = new StringBuilder();
    if(hintMode)
    {
      sb.append("【走法提示】\n");
    }
    else
    {
      sb.append("【局势分析】\n");
    }
    if(bestMove == null)
    {
      sb.append("暂无合法走法（绝杀或和棋）\n");
      return sb.toString();
    }
    // 走法（优先传统记谱，fallback ICCS）
    if(chineseNotation != null && !chineseNotation.isEmpty())
    {
      sb.append("推荐走法: ").append(chineseNotation).append("\n");
    }
    else
    {
      sb.append("推荐走法: ").append(formatMove(bestMove.name)).append("\n");
    }
    // 评分
    if(mate)
    {
      if(mateIn > 0)
      {
        sb.append("引擎评估: 当前方将在 ").append(mateIn).append(" 步内将死对方\n");
      }
      else if(mateIn < 0)
      {
        sb.append("引擎评估: 当前方将在 ").append(-mateIn).append(" 步内被将死\n");
      }
    }
    else
    {
      double score = scoreInCentipawns / 100.0;
      String sign = score >= 0 ? "+" : "";
      String advantage;
      if(scoreInCentipawns > 100)
      {
        advantage = "红方有利";
      }
      else if(scoreInCentipawns < -100)
      {
        advantage = "黑方有利";
      }
      else
      {
        advantage = "形势均等";
      }
      sb.append(String.format("局势评分: %s%.2f（%s）\n", sign, score, advantage));
    }
    // 搜索深度
    if(depth > 0)
    {
      sb.append("搜索深度: ").append(depth).append("\n");
    }
    // 主变例
    if(pv != null && !pv.isEmpty())
    {
      sb.append("后续变例: ").append(pv).append("\n");
    }
    return sb.toString();
  }

  /**
   * 将 ICCS 走法（如 "e2e4"）格式化为 "e2 → e4"
   */
  private static String formatMove(String iccs)
  {
    if(iccs == null || iccs.length() < 4)
    {
      return iccs;
    }
    return iccs.substring(0, 2) + " → " + iccs.substring(2, 4);
  }
}
