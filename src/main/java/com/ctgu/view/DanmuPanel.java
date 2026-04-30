package com.ctgu.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Transparent danmaku (bullet-comment) overlay.
 * Attach it to the same JLayeredPane as the chess board at PALETTE_LAYER
 * so it floats above the board without intercepting mouse events.
 * Also provides a bottom input bar for sending danmaku.
 */
public class DanmuPanel extends JPanel
{
  // ── Danmaku entry ────────────────────────────────────────────────
  private static class Bullet
  {
    String text;
    float  x, y;
    float  speed;
    Color  color;
    float  alpha;
    int    w;   // pre-measured text width

    Bullet(String text, float startX, float y, float speed, Color color, int w)
    {
      this.text  = text;
      this.x     = startX;
      this.y     = y;
      this.speed = speed;
      this.color = color;
      this.alpha = 1.0f;
      this.w     = w;
    }
  }

  private final List<Bullet>   bullets  = new ArrayList<>();
  private final Random          rng      = new Random();
  private final Timer           animTick;
  private final Font            font     = new Font("微软雅黑", Font.BOLD, 18);

  // Track occupied vertical lanes to avoid complete overlap
  private final float[] laneCooldown;
  private static final int LANES = 12;

  // ── Constructor ─────────────────────────────────────────────────

  public DanmuPanel()
  {
    setOpaque(false);
    setLayout(null);
    // Don't intercept mouse events; let them fall through to the chess board
    setEnabled(false);
    laneCooldown = new float[LANES];

    animTick = new Timer(33, new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        tick();
      }
    });
    animTick.start();
  }

  // ── Public API ───────────────────────────────────────────────────

  public void addBullet(String user, String text)
  {
    String full = user + ": " + text;
    SwingUtilities.invokeLater(() ->
    {
      FontMetrics fm = getFontMetrics(font);
      int w = fm.stringWidth(full);
      int lane = pickLane();
      float y = 30 + lane * (getHeight() / (float)LANES);
      float speed = 2.5f + rng.nextFloat() * 1.5f;
      Color c = pickColor();
      Bullet b = new Bullet(full, getWidth() + 10, y, speed, c, w);
      synchronized(bullets)
      {
        bullets.add(b);
      }
      laneCooldown[lane] = 60; // 60 ticks before this lane is reused
    });
  }

  public void stop()
  {
    animTick.stop();
  }

  // ── Animation ────────────────────────────────────────────────────

  private void tick()
  {
    // Advance bullets
    synchronized(bullets)
    {
      Iterator<Bullet> it = bullets.iterator();
      while(it.hasNext())
      {
        Bullet b = it.next();
        b.x -= b.speed;
        if(b.x + b.w < 0)
        {
          it.remove();
        }
      }
    }
    // Cool down lanes
    for(int i = 0; i < LANES; i++)
    {
      if(laneCooldown[i] > 0)
      {
        laneCooldown[i]--;
      }
    }
    repaint();
  }

  private int pickLane()
  {
    // Find the lane with the lowest cooldown
    int best = 0;
    for(int i = 1; i < LANES; i++)
    {
      if(laneCooldown[i] < laneCooldown[best])
      {
        best = i;
      }
    }
    return best;
  }

  private static final Color[] PALETTE = {
      new Color(255, 80, 80),
      new Color(80, 200, 80),
      new Color(80, 160, 255),
      new Color(255, 200, 50),
      new Color(220, 80, 220),
      new Color(50, 220, 200),
      Color.WHITE
  };

  private Color pickColor()
  {
    return PALETTE[rng.nextInt(PALETTE.length)];
  }

  // ── Rendering ────────────────────────────────────────────────────

  @Override
  protected void paintComponent(Graphics g)
  {
    // Do NOT call super (transparent)
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setFont(font);
    synchronized(bullets)
    {
      for(Bullet b : bullets)
      {
        // Shadow
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        g2.setColor(Color.BLACK);
        g2.drawString(b.text, b.x + 2, b.y + 2);
        // Text
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.92f));
        g2.setColor(b.color);
        g2.drawString(b.text, b.x, b.y);
      }
    }
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
  }

  /**
   * Pass mouse events through to the underlying chess board.
   * This component is a pure rendering overlay; it must not capture input.
   */
  @Override
  public boolean contains(int x, int y)
  {
    return false;
  }
}

