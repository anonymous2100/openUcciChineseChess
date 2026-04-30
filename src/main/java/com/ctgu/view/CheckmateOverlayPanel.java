package com.ctgu.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;

/**
 * 绝杀/终局全屏覆盖特效面板。
 * <p>包含粒子爆发、扩散光圈、渐变文字光晕、旋转光芒效果，
 * 直到用户点击鼠标才消失。</p>
 */
public class CheckmateOverlayPanel extends JPanel
{
  private static final long serialVersionUID = 1L;

  // ── 颜色主题 ─────────────────────────────────────────────────────────
  /** 胜利主题色组 */
  private static final Color[] WIN_COLORS  = {
      new Color(255, 240,  60),
      new Color(255, 160,   0),
      new Color(255,  60,   0),
      new Color(220,   0, 100),
      new Color(255, 255, 180),
  };
  /** 失败主题色组 */
  private static final Color[] LOSS_COLORS = {
      new Color(120, 160, 255),
      new Color( 60,  80, 220),
      new Color( 30,  30, 180),
      new Color( 80,  30, 160),
      new Color(180, 200, 255),
  };

  // ── 粒子系统 ────────────────────────────────────────────────────────
  private static final int PARTICLE_COUNT = 80;
  private final float[] px, py, vx, vy, pa, psize;
  private final int[]   pci;   // 颜色索引
  private final Random  rnd = new Random(System.nanoTime());

  // ── 扩散光圈 ─────────────────────────────────────────────────────────
  private static final int RING_COUNT = 6;
  private final float[] ringR, ringA; // 半径、透明度

  // ── 旋转光芒参数 ─────────────────────────────────────────────────────
  private static final int RAY_COUNT = 12;

  // ── 动画状态 ─────────────────────────────────────────────────────────
  private float   phase      = 0f;
  private Timer   animTimer;

  // ── 内容 & 主题 ───────────────────────────────────────────────────────
  private final String  title;
  private final String  subtitle;
  private final boolean win;      // true=胜，false=负
  private final Color[] theme;

  // ── 回调 ─────────────────────────────────────────────────────────────
  private final Runnable onDismiss;

  // ── 构造 ─────────────────────────────────────────────────────────────

  /**
   * @param title     主标题（如"绝杀！"）
   * @param subtitle  副标题（如"您赢得了本局！"）
   * @param win       胜利=true，失败=false（影响配色）
   * @param onDismiss 用户点击后执行的回调（可为 null）
   */
  public CheckmateOverlayPanel(String title, String subtitle, boolean win, Runnable onDismiss)
  {
    this.title     = title;
    this.subtitle  = subtitle;
    this.win       = win;
    this.theme     = win ? WIN_COLORS : LOSS_COLORS;
    this.onDismiss = onDismiss;

    setOpaque(false);
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    // 粒子数组
    px    = new float[PARTICLE_COUNT];
    py    = new float[PARTICLE_COUNT];
    vx    = new float[PARTICLE_COUNT];
    vy    = new float[PARTICLE_COUNT];
    pa    = new float[PARTICLE_COUNT];
    psize = new float[PARTICLE_COUNT];
    pci   = new int[PARTICLE_COUNT];

    // 光圈数组
    ringR = new float[RING_COUNT];
    ringA = new float[RING_COUNT];
    for(int i = 0; i < RING_COUNT; i++)
    {
      ringR[i] = i * 40f;
      ringA[i] = 1f - (float)i / RING_COUNT;
    }

    initParticles(0.5f, 0.45f);

    addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        dismiss();
      }
    });

    animTimer = new Timer(16, e -> tick());
    animTimer.start();
  }

  // ── 私有工具 ─────────────────────────────────────────────────────────

  private void initParticles(float cx, float cy)
  {
    for(int i = 0; i < PARTICLE_COUNT; i++)
    {
      spawnParticle(i, cx, cy);
    }
  }

  private void spawnParticle(int i, float cx, float cy)
  {
    px[i]    = cx;
    py[i]    = cy;
    double a = rnd.nextDouble() * 2 * Math.PI;
    float  s = 1.8f + rnd.nextFloat() * 4.5f;
    vx[i]    = (float)(Math.cos(a) * s);
    vy[i]    = (float)(Math.sin(a) * s) - 2.0f; // 略向上偏
    pa[i]    = 0.75f + rnd.nextFloat() * 0.25f;
    psize[i] = 5f + rnd.nextFloat() * 9f;
    pci[i]   = rnd.nextInt(theme.length);
  }

  private void tick()
  {
    phase = (phase + 0.033f) % 1.0f;

    // 更新粒子（归一化坐标，0~1）
    int w = getWidth(), h = getHeight();
    if(w == 0 || h == 0) { repaint(); return; }
    float cx = 0.5f, cy = 0.45f;
    float scaleX = 1f / w, scaleY = 1f / h;

    for(int i = 0; i < PARTICLE_COUNT; i++)
    {
      px[i]    += vx[i] * scaleX;
      py[i]    += vy[i] * scaleY;
      vy[i]    += 0.06f * scaleY; // 重力
      pa[i]    -= 0.007f;
      psize[i] -= 0.06f;
      if(pa[i] <= 0 || psize[i] <= 0)
      {
        spawnParticle(i, cx, cy);
      }
    }

    // 更新光圈
    for(int i = 0; i < RING_COUNT; i++)
    {
      ringR[i] += 2.8f;
      ringA[i] -= 0.013f;
      if(ringA[i] <= 0)
      {
        ringR[i] = 0;
        ringA[i] = 1f;
      }
    }

    repaint();
  }

  /** 关闭覆盖层 */
  public void dismiss()
  {
    if(animTimer != null)
    {
      animTimer.stop();
      animTimer = null;
    }
    setVisible(false);
    Container parent = getParent();
    if(parent != null)
    {
      parent.remove(this);
      parent.revalidate();
      parent.repaint();
    }
    if(onDismiss != null)
    {
      onDismiss.run();
    }
  }

  // ── 绘制 ─────────────────────────────────────────────────────────────

  @Override
  protected void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    int w = getWidth(), h = getHeight();
    if(w == 0 || h == 0)
      return;

    Graphics2D g2 = (Graphics2D)g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
    g2.setRenderingHint(RenderingHints.KEY_RENDERING,       RenderingHints.VALUE_RENDER_QUALITY);

    int cx = w / 2, cy = (int)(h * 0.45);

    // ── 1. 背景：径向渐变暗场，中心略亮 ──────────────────────────────
    Color bgCenter = win
        ? new Color(60, 10,  0, 200)
        : new Color( 5, 10, 40, 200);
    Color bgEdge   = new Color(0, 0, 0, 235);
    RadialGradientPaint bg = new RadialGradientPaint(
        cx, cy, Math.max(w, h) * 0.72f,
        new float[]{ 0f, 1f },
        new Color[]{ bgCenter, bgEdge });
    g2.setPaint(bg);
    g2.fillRect(0, 0, w, h);

    // ── 2. 旋转光芒（细条射线） ────────────────────────────────────────
    float rayPhase = phase * 2 * (float)Math.PI;
    Color rayColor = win ? new Color(255, 180, 0, 45) : new Color(80, 120, 255, 45);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    g2.setColor(rayColor);
    int rayLen = (int)(Math.max(w, h) * 0.72);
    for(int i = 0; i < RAY_COUNT; i++)
    {
      double angle = rayPhase + i * 2 * Math.PI / RAY_COUNT;
      int ex = cx + (int)(Math.cos(angle) * rayLen);
      int ey = cy + (int)(Math.sin(angle) * rayLen);
      g2.setStroke(new BasicStroke(14f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
      g2.drawLine(cx, cy, ex, ey);
    }

    // ── 3. 扩散光圈 ───────────────────────────────────────────────────
    Color ringColor = win ? new Color(255, 100, 0) : new Color(60, 100, 255);
    for(int i = 0; i < RING_COUNT; i++)
    {
      float ra = Math.max(0, ringA[i]);
      if(ra <= 0) continue;
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ra * 0.75f));
      g2.setPaint(null);
      g2.setColor(ringColor);
      g2.setStroke(new BasicStroke(2.5f));
      int rr = (int)ringR[i];
      g2.drawOval(cx - rr, cy - rr, rr * 2, rr * 2);
    }

    // ── 4. 粒子 ───────────────────────────────────────────────────────
    for(int i = 0; i < PARTICLE_COUNT; i++)
    {
      if(pa[i] <= 0 || psize[i] <= 0) continue;
      float x = px[i] * w;
      float y = py[i] * h;
      Color c = theme[pci[i]];
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, pa[i])));
      g2.setColor(c);
      float s = psize[i];
      // 小菱形粒子更炫
      double angle = phase * Math.PI * 2 + i;
      AffineTransform old = g2.getTransform();
      g2.translate(x, y);
      g2.rotate(angle);
      g2.fill(new Rectangle2D.Float(-s / 2, -s / 4, s, s / 2));
      g2.setTransform(old);
    }

    // ── 5. 主文字光晕（多层偏移模拟高斯模糊） ────────────────────────
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    float pulse     = (float)(0.5 + 0.5 * Math.sin(phase * 2 * Math.PI));
    float pulseSmth = pulse * pulse * (3 - 2 * pulse); // smoothstep

    Font mainFont = new Font("微软雅黑", Font.BOLD, 96);
    g2.setFont(mainFont);
    FontMetrics fm  = g2.getFontMetrics(mainFont);
    int tw = fm.stringWidth(title);
    int tx = cx - tw / 2;
    int ty = cy + fm.getAscent() / 3;

    // 光晕层（从大到小，逐渐不透明）
    for(int glow = 24; glow >= 0; glow -= 3)
    {
      float ratio = 1f - (float)glow / 24f;
      float ga    = ratio * (0.55f + pulseSmth * 0.35f);
      Color gc    = win
          ? new Color(255, glow > 12 ? 80 : 220, 0,  (int)(ga * 255))
          : new Color( 80, 130, 255, (int)(ga * 255));
      g2.setColor(gc);
      if(glow > 0)
      {
        int half = glow / 2;
        g2.drawString(title, tx - half, ty - half);
        g2.drawString(title, tx + half, ty - half);
        g2.drawString(title, tx - half, ty + half);
        g2.drawString(title, tx + half, ty + half);
      }
      else
      {
        // 最后一次绘制本体（渐变色）
        GradientPaint textGrad = win
            ? new GradientPaint(tx, ty - fm.getAscent(), new Color(255, 255, 120),
                tx, ty, new Color(255, 80, 0))
            : new GradientPaint(tx, ty - fm.getAscent(), new Color(180, 220, 255),
                tx, ty, new Color(40, 80, 220));
        g2.setPaint(textGrad);
        g2.drawString(title, tx, ty);
        g2.setPaint(null);
      }
    }

    // ── 6. 副标题 ─────────────────────────────────────────────────────
    Font subFont = new Font("微软雅黑", Font.PLAIN, 26);
    g2.setFont(subFont);
    FontMetrics sfm = g2.getFontMetrics(subFont);
    int sw = sfm.stringWidth(subtitle);
    Color subColor = win ? new Color(255, 210, 80) : new Color(150, 180, 255);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f + pulseSmth * 0.3f));
    g2.setColor(subColor);
    g2.drawString(subtitle, cx - sw / 2, ty + 55);

    // ── 7. "点击继续" 提示（快速闪烁） ───────────────────────────────
    String hint = "点击任意处继续";
    Font hintFont = new Font("微软雅黑", Font.PLAIN, 18);
    g2.setFont(hintFont);
    FontMetrics hfm = g2.getFontMetrics(hintFont);
    int hw = hfm.stringWidth(hint);
    float hintBlink = (float)(0.3 + 0.7 * Math.abs(Math.sin(phase * 4 * Math.PI)));
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, hintBlink));
    g2.setColor(new Color(200, 200, 200));
    g2.drawString(hint, cx - hw / 2, h - 50);

    g2.dispose();
  }
}

