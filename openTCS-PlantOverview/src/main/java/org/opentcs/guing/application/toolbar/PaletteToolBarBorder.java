/**
 * (c): IML, JHotDraw.
 *
 */
package org.opentcs.guing.application.toolbar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToolBar;

/**
 * A toolbar border.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class PaletteToolBarBorder
    extends org.jhotdraw.gui.plaf.palette.PaletteToolBarBorder {

  private final static float[] ENABLED_STOPS = new float[] {0f, 0.5f, 1f};
  private final static Color[] ENABLED_STOP_COLORS = new Color[] {
    new Color(0xf8f8f8), new Color(0xc8c8c8), new Color(0xf8f8f8)
  };

  @Override
  public void paintBorder(Component component, Graphics gr, int x, int y, int w, int h) {
    Graphics2D g = (Graphics2D) gr;

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    if ((component instanceof JToolBar) /* && ((((JToolBar) component).getUI()) instanceof PaletteToolBarUI) */) {
      JToolBar c = (JToolBar) component;

      if (c.isFloatable()) {
        int borderColor = 0x80ff0000;
        float[] stops = ENABLED_STOPS;
        Color[] stopColors = ENABLED_STOP_COLORS;

        g.setColor(new Color(borderColor, true));
        LinearGradientPaint lgp = new LinearGradientPaint(
            new Point2D.Float(1, 1), new Point2D.Float(19, 1),
            stops, stopColors,
            MultipleGradientPaint.CycleMethod.REPEAT);
        g.setPaint(lgp);
        g.fillRect(1, 1, 7 - 2, h - 2);
        ImageIcon icon = new ImageIcon(getClass().getResource("/org/opentcs/guing/res/symbols/toolbar/border.jpg"));

        if (c.getComponentCount() != 0 && !(c.getComponents()[0] instanceof JLabel)) {
          JLabel label = new JLabel(icon);
          label.setFocusable(false);
          c.add(label, 0);
          label.getParent().setBackground(label.getBackground());
          label.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        }
      }
    }
  }

  @Override
  public Insets getBorderInsets(Component component, Insets newInsets) {
    if (newInsets == null) {
      newInsets = new Insets(0, 0, 0, 0);
    }

    JComponent c = (JComponent) component;

    if (c.getClientProperty(org.jhotdraw.gui.plaf.palette.PaletteToolBarUI.TOOLBAR_INSETS_OVERRIDE_PROPERTY) instanceof Insets) {
      Insets override = (Insets) c.getClientProperty(org.jhotdraw.gui.plaf.palette.PaletteToolBarUI.TOOLBAR_INSETS_OVERRIDE_PROPERTY);
      newInsets.top = override.top;
      newInsets.left = override.left;
      newInsets.bottom = override.bottom;
      newInsets.right = override.right;

      return newInsets;
    }

    newInsets.top = 1;
    newInsets.left = 1;
    newInsets.bottom = 0;
    newInsets.right = 0;

    return newInsets;
  }
}
