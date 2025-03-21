// SPDX-FileCopyrightText: The original authors of JHotDraw and all its contributors
// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.thirdparty.modeleditor.jhotdraw.application.action.draw;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import org.jhotdraw.util.Images;

/**
 * ColorIcon.
 *
 * @author Werner Randelshofer
 */
public class ColorIcon
    implements
      javax.swing.Icon {

  private static BufferedImage noColorImage;
  private Color fillColor;
  private int width;
  private int height;
  private String name;

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
      noColorImage = Images.toBufferedImage(
          Images.createImage(
              ColorIcon.class, "/org/jhotdraw/draw/action/images/attribute.color.noColor.png"
          )
      );
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
        int[] xpoints = new int[]{x + 2,
            x + width - 5,
            x + width - 3,
            x + width - 3,
            x + 4,
            x + 2};
        int[] ypoints = new int[]{y + height - 5,
            y + 2,
            y + 2,
            y + 4,
            y + height - 3,
            y + height - 3};
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
