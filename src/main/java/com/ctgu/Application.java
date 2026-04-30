package com.ctgu;

import com.ctgu.constant.ChessConstant;
import com.ctgu.view.ChessFrame;
import com.formdev.flatlaf.intellijthemes.FlatArcOrangeIJTheme;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Application
{
  private static final Logger logger = Logger.getLogger(Application.class.getName());

  public static void main(String[] args)
  {
    try
    {
      SLF4JBridgeHandler.removeHandlersForRootLogger();
      SLF4JBridgeHandler.install();
    }
    catch(Throwable t)
    {
      logger.log(Level.FINE, "Failed to install SLF4JBridgeHandler", t);
    }

    EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
        try
        {
          // FlatLaf must be set up on the EDT before any components are created.
          // Do NOT call UIManager.setLookAndFeel(system) afterwards – it would overwrite FlatLaf.
          FlatArcOrangeIJTheme.setup();
          UIManager.put("RootPane.setupButtonVisible", false);
          for(int i = 0; i < ChessConstant.DEFAULT_FONT.length; i++)
          {
            try
            {
              UIManager.put(ChessConstant.DEFAULT_FONT[i], new Font("微软雅黑", Font.PLAIN, 14));
            }
            catch(Throwable t)
            {
              // ignore font issues per-entry
            }
          }
        }
        catch(Exception e)
        {
          logger.log(Level.WARNING, "Failed to setup LookAndFeel or fonts", e);
        }
        new ChessFrame();
      }
    });
  }
}
