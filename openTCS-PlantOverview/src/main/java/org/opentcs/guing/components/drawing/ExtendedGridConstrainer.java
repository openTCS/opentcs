/**
 * (c): IML, JHotDraw.
 *
 *
 * Extended by IML: 1. Different Constrainer lines (1/5/10 unit steps) 2.
 * Minimal grid spacing 3. Rulers
 *
 * @(#)GridConstrainer.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.opentcs.guing.components.drawing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.GridConstrainer;

/**
 * Constrains a point such that it falls on a grid.
 *
 * @author Werner Randelshofer
 */
public class ExtendedGridConstrainer
    extends GridConstrainer {

  private final static int MIN_GRID_SPACING = 4;
  /**
   * The spacing factor for a medium grid cell.
   */
  private static final int MEDIUM_GRID_SPACING = 5;
  /**
   * The color for minor grid cells.
   */
  private static final Color MINOR_COLOR = new Color(0xd4d4d4);
  /**
   * The color for major grid cells.
   */
  private static final Color MAJOR_COLOR = new Color(0xc0c0c0);
  /**
   * The color for medium grid cells.
   */
  private static final Color MEDIUM_COLOR = new Color(0xd0d0d0);
  /**
   * The color for x- and y-axis.
   */
  private static final Color AXIS_COLOR = new Color(0xC040C0);

  public ExtendedGridConstrainer() {
    super();
  }

  public ExtendedGridConstrainer(double width, double height) {
    super(width, height);
  }

  public ExtendedGridConstrainer(double width, double height, boolean visible) {
    super(width, height, visible);
  }

  public ExtendedGridConstrainer(double width, double height, double theta, boolean visible) {
    super(width, height, theta, visible);
  }

  @Override
  public void draw(Graphics2D g, DrawingView view) {
    if (isVisible()) {
      double width = getWidth();
      double height = getHeight();
      int majorGridSpacing = getMajorGridSpacing();

      AffineTransform t = view.getDrawingToViewTransform();
      Rectangle viewBounds = g.getClipBounds();
      Rectangle2D.Double bounds = view.viewToDrawing(viewBounds);

      Point2D.Double origin = constrainPoint(new Point2D.Double(bounds.x, bounds.y));
      Point2D.Double point = new Point2D.Double();
      Point2D.Double viewPoint = new Point2D.Double();

      // Vertical grid lines are only drawn, if they are at least [MIN_GRID_SPACING] 
      // pixels apart on the view coordinate system.
      if (width * view.getScaleFactor() > MIN_GRID_SPACING) {
        for (int i = (int) (origin.x / width), m = (int) ((origin.x + bounds.width) / width) + 1;
             i <= m; i++) {
          point.x = width * i;
          t.transform(point, viewPoint);

          if (i == 0) {
            g.setColor(AXIS_COLOR);
          }
          else if (i % majorGridSpacing == 0) {
            g.setColor(MAJOR_COLOR);
          }
          else if (i % MEDIUM_GRID_SPACING == 0) {
            g.setColor(MEDIUM_COLOR);
          }
          else {
            g.setColor(MINOR_COLOR);
          }

          g.drawLine((int) viewPoint.x, viewBounds.y, (int) viewPoint.x, viewBounds.y + viewBounds.height);
        }
      }
      else if (width * majorGridSpacing * view.getScaleFactor() > 2) {
        g.setColor(MAJOR_COLOR);

        for (int i = (int) (origin.x / width), m = (int) ((origin.x + bounds.width) / width) + 1;
             i <= m; i++) {
          if (i % majorGridSpacing == 0) {
            point.x = width * i;
            t.transform(point, viewPoint);
            g.drawLine((int) viewPoint.x, viewBounds.y, (int) viewPoint.x, viewBounds.y + viewBounds.height);
          }
        }
      }

      // Horizontal grid lines are only drawn, if they are at least [MIN_GRID_SPACING] 
      // pixels apart on the view coordinate system.
      if (height * view.getScaleFactor() > MIN_GRID_SPACING) {
        for (int i = (int) (origin.y / height), m = (int) ((origin.y + bounds.height) / height) + 1;
             i <= m; i++) {
          point.y = height * i;
          t.transform(point, viewPoint);

          if (i == 0) {
            g.setColor(AXIS_COLOR);
          }
          else if (i % majorGridSpacing == 0) {
            g.setColor(MAJOR_COLOR);
          }
          else if (i % MEDIUM_GRID_SPACING == 0) {
            g.setColor(MEDIUM_COLOR);
          }
          else {
            g.setColor(MINOR_COLOR);
          }

          g.drawLine(viewBounds.x, (int) viewPoint.y, viewBounds.x + viewBounds.width, (int) viewPoint.y);
        }
      }
      else if (height * majorGridSpacing * view.getScaleFactor() > 2) {
        g.setColor(MAJOR_COLOR);

        for (int i = (int) (origin.y / height), m = (int) ((origin.y + bounds.height) / height) + 1;
             i <= m; i++) {
          if (i % majorGridSpacing == 0) {
            point.y = height * i;
            t.transform(point, viewPoint);
            g.drawLine(viewBounds.x, (int) viewPoint.y, viewBounds.x + viewBounds.width, (int) viewPoint.y);
          }
        }
      }
    }
  }
}
