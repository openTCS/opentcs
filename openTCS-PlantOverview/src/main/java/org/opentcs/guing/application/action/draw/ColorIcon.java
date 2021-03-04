/**
 * (c): IML, JHotDraw.
 *
 * Changed by IML to allow access to ResourceBundle.
 *
 *
 * @(#)ColorIcon.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.opentcs.guing.application.action.draw;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import org.jhotdraw.util.Images;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * ColorIcon.
 *
 * @author Werner Randelshofer
 */
public class ColorIcon
    implements javax.swing.Icon {

  private Color fillColor;
  private static BufferedImage noColorImage;
  private int width;
  private int height;
  private String name;

  /**
   * Creates a new instance.
   *
   * @param rgb The combined RGB components
   */
  public ColorIcon(int rgb) {
    this(new Color(rgb));
  }

  public ColorIcon(Color color) {
    this(color, (color == null)
         ? ResourceBundleUtil.getBundle().getToolTipTextProperty("attribute.color.noColor")
         : color.getRed() + "," + color.getGreen() + "," + color.getBlue(), 14, 14);
  }

  public ColorIcon(int rgb, String name) {
    this(new Color(rgb), name, 14, 14);
  }

  public ColorIcon(Color color, String name) {
    this(color, name, 14, 14);
  }

  public ColorIcon(Color color, String name, int width, int height) {
    this.fillColor = color;
    this.name = name;
    this.width = width;
    this.height = height;

    if (noColorImage == null) {
      noColorImage = Images.toBufferedImage(Images.createImage(
          ColorIcon.class, "/org/jhotdraw/draw/action/images/attribute.color.noColor.png"));
    }
  }

  public Color getColor() {
    return fillColor;
  }

  public String getName() {
    return name;
  }

  @Override
  public int getIconWidth() {
    return width;
  }

  @Override
  public int getIconHeight() {
    return height;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    //Graphics2D g = (Graphics2D) gr;
    if (fillColor == null || fillColor.getAlpha() == 0) {
      if (width == noColorImage.getWidth() && height == noColorImage.getHeight()) {
        g.drawImage(noColorImage, x, y, c);
      }
      else {
        g.setColor(Color.WHITE);
        g.fillRect(x + 1, y + 1, width - 2, height - 2);
        g.setColor(Color.red);
        int[] xpoints = new int[] {x + 2, x + width - 5, x + width - 3, x + width - 3, x + 4, x + 2};
        int[] ypoints = new int[] {y + height - 5, y + 2, y + 2, y + 4, y + height - 3, y + height - 3};
        g.fillPolygon(xpoints, ypoints, xpoints.length);
      }
    }
    else {
      //  g.setColor(Color.WHITE);
      //  g.fillRect(x + 1, y + 1, width - 2, height - 2);
      g.setColor(fillColor);
      //  g.fillRect(x + 2, y + 2, width - 4, height - 4);
      g.fillRect(x + 1, y + 1, width - 2, height - 2);
    }

    g.setColor(new Color(0x666666));

    // Draw the rectangle using drawLine to work around a drawing bug in
    // Apples MRJ for Java 1.5
//  g.drawRect(x, y, getIconWidth() - 1, getIconHeight() - 1);
    g.drawLine(x, y, x + width - 1, y);
    g.drawLine(x + width - 1, y, x + width - 1, y + width - 1);
    g.drawLine(x + width - 1, y + height - 1, x, y + height - 1);
    g.drawLine(x, y + height - 1, x, y);
  }
}
