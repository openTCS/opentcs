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
import org.jhotdraw.draw.AbstractConstrainer;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.RotationDirection;
import org.jhotdraw.draw.TranslationDirection;

/**
 * Constrains a point such that it falls on a grid.
 *
 * @author Werner Randelshofer
 */
public class GridConstrainer
    extends AbstractConstrainer {

  private final static int MIN_GRID_SPACING = 4;
  /**
   * The width of a minor grid cell. The value 0 turns the constrainer off for
   * the horizontal axis.
   */
  private double width;
  /**
   * The height of a minor grid cell. The value 0 turns the constrainer off for
   * the vertical axis.
   */
  private double height;
  /**
   * The theta for constrained rotations on the grid. The value 0 turns the
   * constrainer off for rotations.
   */
  private double theta;
  /**
   * If this variable is true, the grid is drawn. Note: Grid cells are only
   * drawn, if they are at least two pixels apart on the view coordinate system.
   */
  private boolean isVisible;
  /**
   * The color for minor grid cells.
   */
  private static Color minorColor = new Color(0xd4d4d4);
  /**
   * The color for major grid cells.
   */
  private static Color majorColor = new Color(0xc0c0c0);
  /**
   * The color for medium grid cells.
   */
  private static Color mediumColor = new Color(0xd0d0d0);
  /**
   * The color for x- and y-axis.
   */
  private static Color axisColor = new Color(0xC040C0);
  /**
   * The spacing factor for a major grid cell.
   */
  private int majorGridSpacing = 10;
  /**
   * The spacing factor for a medium grid cell.
   */
  private final int mediumGridSpacing = 5;

  /**
   * Creates a new instance with a grid of 1x1.
   */
  public GridConstrainer() {
    this(1d, 1d, 0d, false);
  }

  /**
   * Creates a new instance with the specified grid size, and by 11.25Â° (in
   * degrees) for rotations. The grid is visible.
   *
   * @param width The width of a grid cell.
   * @param height The height of a grid cell.
   */
  public GridConstrainer(double width, double height) {
    this(width, height, Math.PI / 8d, true);
  }

  /**
   * Creates a new instance with the specified grid size. and by 11.25Â° (in
   * degrees) for rotations.
   *
   * @param width The width of a grid cell.
   * @param height The height of a grid cell.
   * @param visible Wether the grid is visible or not.
   */
  public GridConstrainer(double width, double height, boolean visible) {
    this(width, height, Math.PI / 8d, visible);
  }

  /**
   * Creates a new instance with the specified grid size.
   *
   * @param width The width of a grid cell.
   * @param height The height of a grid cell.
   * @param theta The theta for rotations in radians.
   * @param visible Wether the grid is visible or not.
   */
  public GridConstrainer(double width, double height, double theta, boolean visible) {
    if (width <= 0 || height <= 0) {
      throw new IllegalArgumentException("Width or height is <= 0");
    }

    this.width = width;
    this.height = height;
    this.theta = theta;
    this.isVisible = visible;
  }

  public double getWidth() {
    return width;
  }

  public double getHeight() {
    return height;
  }

  public double getTheta() {
    return theta;
  }

  public void setWidth(double newValue) {
    double oldValue = width;
    width = newValue;
    firePropertyChange("width", oldValue, newValue);
    fireStateChanged();
  }

  public void setHeight(double newValue) {
    double oldValue = height;
    height = newValue;
    firePropertyChange("height", oldValue, newValue);
    fireStateChanged();
  }

  public void setTheta(double newValue) {
    double oldValue = theta;
    theta = newValue;
    firePropertyChange("theta", oldValue, newValue);
    fireStateChanged();
  }

  @Override
  public Point2D.Double constrainPoint(Point2D.Double p) {
    p.x = Math.round(p.x / width) * width;
    p.y = Math.round(p.y / height) * height;

    return p;
  }

  /**
   * Constrains the placement of a point towards a direction.
   * <p>
   * This method
   * changes the point which is passed as a parameter.
   *
   * @param p A point on the drawing.
   * @param dir A direction.
   * @return Returns the constrained point.
   */
  protected Point2D.Double constrainPoint(Point2D.Double p, TranslationDirection dir) {
    Point2D.Double p0 = constrainPoint((Point2D.Double) p.clone());

    switch (dir) {
      case NORTH:
      case NORTH_WEST:
      case NORTH_EAST:
        if (p0.y < p.y) {
          p.y = p0.y;
        }
        else if (p0.y > p.y) {
          p.y = p0.y - height;
        }
        break;

      case SOUTH:
      case SOUTH_WEST:
      case SOUTH_EAST:
        if (p0.y < p.y) {
          p.y = p0.y + height;
        }
        else if (p0.y > p.y) {
          p.y = p0.y;
        }
        break;
      default:
    }

    switch (dir) {
      case WEST:
      case NORTH_WEST:
      case SOUTH_WEST:
        if (p0.x < p.x) {
          p.x = p0.x;
        }
        else if (p0.x > p.x) {
          p.x = p0.x - width;
        }
        break;

      case EAST:
      case NORTH_EAST:
      case SOUTH_EAST:
        if (p0.x < p.x) {
          p.x = p0.x + width;
        }
        else if (p0.x > p.x) {
          p.x = p0.x;
        }
        break;
      default:
    }

    return p;
  }

  @Override
  public Point2D.Double translatePoint(Point2D.Double p, TranslationDirection dir) {
    Point2D.Double p0 = constrainPoint((Point2D.Double) p.clone());

    switch (dir) {
      case NORTH:
      case NORTH_WEST:
      case NORTH_EAST:
        p.y = p0.y - height;
        break;

      case SOUTH:
      case SOUTH_WEST:
      case SOUTH_EAST:
        p.y = p0.y + height;
        break;
        
      default:
    }

    switch (dir) {
      case WEST:
      case NORTH_WEST:
      case SOUTH_WEST:
        p.x = p0.x - width;
        break;

      case EAST:
      case NORTH_EAST:
      case SOUTH_EAST:
        p.x = p0.x + width;
        break;
        
      default:
    }

    return p;
  }

  @Override
  public Rectangle2D.Double constrainRectangle(Rectangle2D.Double r) {
    Point2D.Double p0 = constrainPoint(new Point2D.Double(r.x, r.y));
    Point2D.Double p1 = constrainPoint(new Point2D.Double(r.x + r.width, r.y + r.height));

    if (Math.abs(p0.x - r.x) < Math.abs(p1.x - r.x - r.width)) {
      r.x = p0.x;
    }
    else {
      r.x = p1.x - r.width;
    }

    if (Math.abs(p0.y - r.y) < Math.abs(p1.y - r.y - r.height)) {
      r.y = p0.y;
    }
    else {
      r.y = p1.y - r.height;
    }

    return r;
  }

  /**
   * Constrains the placement of a rectangle towards a direction.
   * <p>
   * This
   * method changes the location of the rectangle which is passed as a
   * parameter. This method does not change the size of the rectangle.
   *
   * @param r A rectangle on the drawing.
   * @param dir A direction.
   * @return Returns the constrained rectangle.
   */
  protected Rectangle2D.Double constrainRectangle(Rectangle2D.Double r, TranslationDirection dir) {
    Point2D.Double p0 = new Point2D.Double(r.x, r.y);

    switch (dir) {
      case NORTH:
      case NORTH_WEST:
      case WEST:
        constrainPoint(p0, dir);
        break;

      case EAST:
      case NORTH_EAST:
        p0.x += r.width;
        constrainPoint(p0, dir);
        p0.x -= r.width;
        break;

      case SOUTH:
      case SOUTH_WEST:
        p0.y += r.height;
        constrainPoint(p0, dir);
        p0.y -= r.height;
        break;

      case SOUTH_EAST:
        p0.y += r.height;
        p0.x += r.width;
        constrainPoint(p0, dir);
        p0.y -= r.height;
        p0.x -= r.width;
        break;
        
      default:
    }

    r.x = p0.x;
    r.y = p0.y;

    return r;
  }

  @Override
  public Rectangle2D.Double translateRectangle(Rectangle2D.Double r, TranslationDirection dir) {
    double x = r.x;
    double y = r.y;

    constrainRectangle(r, dir);

    switch (dir) {
      case NORTH:
      case NORTH_WEST:
      case NORTH_EAST:
        if (y == r.y) {
          r.y -= height;
        }
        break;

      case SOUTH:
      case SOUTH_WEST:
      case SOUTH_EAST:
        if (y == r.y) {
          r.y += height;
        }
        break;
        
      default:
    }

    switch (dir) {
      case WEST:
      case NORTH_WEST:
      case SOUTH_WEST:
        if (x == r.x) {
          r.x -= width;
        }
        break;

      case EAST:
      case NORTH_EAST:
      case SOUTH_EAST:
        if (x == r.x) {
          r.x += width;
        }
        break;
        
      default:
    }

    return r;
  }

  @Override
  public String toString() {
    return super.toString() + "[" + width + "," + height + "]";
  }

  public boolean isVisible() {
    return isVisible;
  }

  public void setVisible(boolean newValue) {
    boolean oldValue = isVisible;
    isVisible = newValue;
    firePropertyChange("visible", oldValue, newValue);
    fireStateChanged();
  }

  /**
   * Spacing between major grid lines.
   */
  public int getMajorGridSpacing() {
    return majorGridSpacing;
  }

  /**
   * Spacing between major grid lines.
   */
  public void setMajorGridSpacing(int newValue) {
    int oldValue = majorGridSpacing;
    majorGridSpacing = newValue;
    firePropertyChange("majorGridSpacing", oldValue, newValue);
    fireStateChanged();
  }

  @Override
  public void draw(Graphics2D g, DrawingView view) {
    if (isVisible) {
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
            g.setColor(axisColor);
          }
          else if (i % majorGridSpacing == 0) {
            g.setColor(majorColor);
          }
          else if (i % mediumGridSpacing == 0) {
            g.setColor(mediumColor);
          }
          else {
            g.setColor(minorColor);
          }

          g.drawLine((int) viewPoint.x, viewBounds.y, (int) viewPoint.x, viewBounds.y + viewBounds.height);
        }
      }
      else if (width * majorGridSpacing * view.getScaleFactor() > 2) {
        g.setColor(majorColor);

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
            g.setColor(axisColor);
          }
          else if (i % majorGridSpacing == 0) {
            g.setColor(majorColor);
          }
          else if (i % mediumGridSpacing == 0) {
            g.setColor(mediumColor);
          }
          else {
            g.setColor(minorColor);
          }

          g.drawLine(viewBounds.x, (int) viewPoint.y, viewBounds.x + viewBounds.width, (int) viewPoint.y);
        }
      }
      else if (height * majorGridSpacing * view.getScaleFactor() > 2) {
        g.setColor(majorColor);

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

  @Override
  public double constrainAngle(double angle) {
    // No step specified then no constraining
    if (theta == 0) {
      return angle;
    }

    double factor = Math.round(angle / theta);
    return theta * factor;
  }

  @Override
  public double rotateAngle(double angle, RotationDirection dir) {
    // Check parameters
    if (dir == null) {
      throw new IllegalArgumentException("dir must not be null");
    }

    // Rotate into the specified direction by theta
    angle = constrainAngle(angle);

    switch (dir) {
      case CLOCKWISE:
        angle += theta;
        break;

      case COUNTER_CLOCKWISE:
      default:
        angle -= theta;
        break;
    }

    return angle;
  }
}
