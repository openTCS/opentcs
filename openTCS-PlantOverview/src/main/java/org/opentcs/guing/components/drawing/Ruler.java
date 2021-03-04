/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.drawing;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventObject;
import static java.util.Objects.requireNonNull;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.opentcs.guing.components.drawing.course.Origin;
import org.opentcs.guing.components.drawing.course.OriginChangeListener;

/**
 * A ruler.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class Ruler
    extends JComponent
    implements PropertyChangeListener,
               OriginChangeListener {

  /**
   * Size of the rulers (height of the horizontal ruler, width of the vertical).
   */
  private static final int SIZE = 25;
  /**
   * The standard translation of the drawing view. Not sure though why
   * it is -12.
   */
  private static final int STANDARD_TRANSLATION = -12;
  /**
   * The DrawingView.
   */
  protected final OpenTCSDrawingView drawingView;
  /**
   * The current scale factor.
   */
  protected double scaleFactor = 1.0;
  /**
   * The scale factor for the horizontal ruler.
   */
  protected double horizontalRulerScale = Origin.DEFAULT_SCALE;
  /**
   * The scale factor for the vertical ruler.
   */
  protected double verticalRulerScale = Origin.DEFAULT_SCALE;

  /**
   * Creates a new instance.
   *
   * @param drawingView The drawing view.
   */
  private Ruler(OpenTCSDrawingView drawingView) {
    this.drawingView = requireNonNull(drawingView, "drawingView");

  }

  /**
   * A horizontal ruler.
   */
  public static class Horizontal
      extends Ruler {

    /**
     * Creates a new instance.
     *
     * @param drawingView The drawing view.
     */
    public Horizontal(OpenTCSDrawingView drawingView) {
      super(drawingView);
    }

    /**
     * Sets a new width of the ruler and repaints it.
     *
     * @param preferredWidth The new width.
     */
    public void setPreferredWidth(int preferredWidth) {
      setPreferredSize(new Dimension(preferredWidth, SIZE));
      repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);

      Rectangle drawHere = g.getClipBounds();
      Point translation = (Point) drawingView.getTranslation().clone();
      // if we scroll right the translation isn't incremented by default
      // we use the translation of the visible rect instead
      int visibleRectX = drawingView.getVisibleRect().x + STANDARD_TRANSLATION;
      if (STANDARD_TRANSLATION == translation.x) {
        translation.x = visibleRectX;
      }

      Graphics2D g2d = (Graphics2D) g;
      g2d.setFont(new Font("Arial", Font.PLAIN, 10));
      // i translated
      int translated;
      // i normalized to decimal
      int draw;
      int drawOld = 0;
      // draw translated
      int drawTranslated;
      String translatedAsString;
      String lastIndex;

      // base line
      g2d.drawLine(0, SIZE - 1,
                   getWidth(), SIZE - 1);

      for (int i = drawHere.x; i < getWidth(); i += 10) {
        translated = translateValue(i, translation);
        translatedAsString = Integer.toString(translated);
        lastIndex = translatedAsString.substring(translatedAsString.length() - 1);

        int decimal = Integer.parseInt(lastIndex);
        {
          // These steps are neccessary to guarantee lines are drawn
          // at every pixel. It always rounds i to a decimal, so the modulo
          // operators work
          draw = i;

          if (translated < 0) {
            draw += decimal;
          }
          else {
            draw -= decimal;
          }

          drawTranslated = translateValue(draw, translation);
          // draw has to be incremented by 1, otherwise the drawn lines
          // are wrong by 1 pixel
          draw++;
        }

        if (drawTranslated % (10 * scaleFactor) == 0) {
          g2d.drawLine(draw, SIZE - 1, draw, SIZE - 4);
        }

        if (drawTranslated % (50 * scaleFactor) == 0) {
          g2d.drawLine(draw, SIZE - 1, draw, SIZE - 7);
        }

        if (drawTranslated % (100 * scaleFactor) == 0) {
          g2d.drawLine(draw, SIZE - 1, draw, SIZE - 11);
          int value = (int) (drawTranslated / scaleFactor) * (int) horizontalRulerScale;
          String textValue = Integer.toString(value);
          if (scaleFactor < 0.06) {
            if (value % 5000 == 0) {
              g2d.drawString(textValue, value == 0 ? draw - 2 : draw - 8, 9);
            }
          }
          else if ((draw - drawOld) < 31) {
            if (value % 500 == 0) {
              g2d.drawString(textValue, value == 0 ? draw - 2 : draw - 8, 9);
            }
          }
          else {
            g2d.drawString(textValue, value == 0 ? draw - 2 : draw - 8, 9);
          }

          drawOld = draw;
        }
      }

    }

    /**
     * Returns a translated value, considering current translation of the view.
     *
     * @param i The value.
     * @return The translated value.
     */
    private int translateValue(int i, Point translation) {
      if (translation.x < 0) {
        return i + translation.x;
      }
      else {
        return i;
      }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals("scaleFactor")) {
        scaleFactor = (double) evt.getNewValue();
        SwingUtilities.invokeLater(new Runnable() {

          @Override
          public void run() {
            setPreferredWidth(drawingView.getWidth());
          }
        });

      }
    }

    @Override
    public void originLocationChanged(EventObject evt) {
    }

    @Override
    public void originScaleChanged(EventObject evt) {
      if (evt.getSource() instanceof Origin) {
        Origin origin = (Origin) evt.getSource();
        SwingUtilities.invokeLater(() -> {
          horizontalRulerScale = origin.getScaleX();
          repaint();
        });
      }
    }
  }

  /**
   * A vertical ruler.
   */
  public static class Vertical
      extends Ruler {

    /**
     * Creates a new instance.
     *
     * @param drawingView The drawing view.
     */
    public Vertical(OpenTCSDrawingView drawingView) {
      super(drawingView);
    }

    /**
     * Sets a new height of the ruler and repaints it.
     *
     * @param preferredHeight The new height.
     */
    public void setPreferredHeight(int preferredHeight) {
      setPreferredSize(new Dimension(SIZE, preferredHeight));
      repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);

      Rectangle drawHere = g.getClipBounds();
      Point translation = (Point) drawingView.getTranslation().clone();
      // if we scroll downwards the translation isn't incremented by default
      // we use the translation of the visible rect instead
      if (translation.y == STANDARD_TRANSLATION) {
        translation.y = drawingView.getVisibleRect().y + STANDARD_TRANSLATION;
      }

      Graphics2D g2d = (Graphics2D) g;
      g2d.setFont(new Font("Arial", Font.PLAIN, 10));
      // i translated
      int translated;
      // i normalized to decimal
      int draw;
      int drawOld = 0;
      // draw translated
      int drawTranslated;
      String translatedAsString;
      String lastIndex;

      // base line
      g2d.drawLine(
          SIZE - 1, 0,
          SIZE - 1, getHeight());

      // Rotate the font for vertical axis
      AffineTransform fontAT = new AffineTransform();
      fontAT.rotate(270 * java.lang.Math.PI / 180);
      Font font = g2d.getFont().deriveFont(fontAT);
      g2d.setFont(font);

      for (int i = drawHere.y; i < getHeight(); i += 10) {
        translated = translateValue(i, translation);
        translatedAsString = Integer.toString(translated);
        lastIndex = translatedAsString.substring(translatedAsString.length() - 1);
        int decimal = Integer.parseInt(lastIndex);

        {
          // These steps are neccessary to guarantee lines are drawn
          // at every pixel. It always rounds i to a decimal, so the modulo
          // operators work
          draw = i;

          if (translated < 0) {
            draw += decimal;
          }
          else {
            draw -= decimal;
          }

          drawTranslated = translateValue(draw, translation);
          draw++;
        }

        if (drawTranslated % (10 * scaleFactor) == 0) {
          g2d.drawLine(SIZE - 1, draw, SIZE - 4, draw);
        }

        if (drawTranslated % (50 * scaleFactor) == 0) {
          g2d.drawLine(SIZE - 1, draw, SIZE - 7, draw);
        }

        if (drawTranslated % (100 * scaleFactor) == 0) {
          g2d.drawLine(SIZE - 1, draw, SIZE - 11, draw);
          int value = -(int) (drawTranslated / scaleFactor) * (int) verticalRulerScale;
          String textValue = Integer.toString(value);

          if (scaleFactor < 0.06) {
            if (value % 5000 == 0) {
              g2d.drawString(textValue, 9, value == 0 ? draw + 2 : draw + 8);
            }
          }
          else if ((draw - drawOld) < 31) {
            if (value % 500 == 0) {
              g2d.drawString(textValue, 9, value == 0 ? draw + 2 : draw + 8);
            }
          }
          else {
            g2d.drawString(textValue, 9, value == 0 ? draw + 2 : draw + 8);
          }

          drawOld = draw;
        }
      }
    }

    /**
     * Returns a translated value, considering current translation of the view.
     *
     * @param i The value.
     * @return The translated value.
     */
    private int translateValue(int i, Point translation) {
      if (translation.y < 0) {
        return i + translation.y;
      }
      else {
        return i;
      }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals("scaleFactor")) {
        scaleFactor = (double) evt.getNewValue();
        SwingUtilities.invokeLater(new Runnable() {

          @Override
          public void run() {
            setPreferredHeight(drawingView.getHeight());
          }
        });

      }
    }

    @Override
    public void originLocationChanged(EventObject evt) {
    }

    @Override
    public void originScaleChanged(EventObject evt) {
      if (evt.getSource() instanceof Origin) {
        Origin origin = (Origin) evt.getSource();
        SwingUtilities.invokeLater(() -> {
          verticalRulerScale = origin.getScaleY();
          repaint();
        });
      }
    }
  }
}
