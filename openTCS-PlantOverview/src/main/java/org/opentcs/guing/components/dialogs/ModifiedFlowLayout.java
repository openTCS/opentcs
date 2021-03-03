/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.dialogs;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

/**
 * A modified version of FlowLayout that allows containers using this
 * Layout to behave in a reasonable manner when placed inside a
 * JScrollPane.
 *
 * @author Preity Gupta (Fraunhofer IML)
 */
public class ModifiedFlowLayout
    extends FlowLayout {

  /**
   * Creates a new instance.
   */
  public ModifiedFlowLayout() {
    super();
  }

  /**
   * Creates a new instance.
   *
   * @param align The alignment value.
   */
  public ModifiedFlowLayout(int align) {
    super(align);
  }

  /**
   * Creates a new instance.
   *
   * @param align the alignment value
   * @param hgap the horizontal gap between components and between the
   * components and the borders of the container
   * @param vgap the vertical gap between components and between the components
   * and the borders of the container
   */
  public ModifiedFlowLayout(int align, int hgap, int vgap) {
    super(align, hgap, vgap);
  }

  @Override
  public Dimension minimumLayoutSize(Container target) {
    // Size of largest component, so we can resize it in
    // either direction with something like a split-pane.
    return computeMinSize(target);
  }

  @Override
  public Dimension preferredLayoutSize(Container target) {
    return computeSize(target);
  }

  private Dimension computeSize(Container target) {
    synchronized (target.getTreeLock()) {
      int hgap = getHgap();
      int vgap = getVgap();
      int w = target.getWidth();

      // Let this behave like a regular FlowLayout (single row)
      // if the container hasn't been assigned any size yet
      if (w == 0) {
        w = Integer.MAX_VALUE;
      }

      Insets insets = target.getInsets();

      if (insets == null) {
        insets = new Insets(0, 0, 0, 0);
      }

      int reqdWidth = 0;
      int maxwidth = w - (insets.left + insets.right + hgap * 2);
      int n = target.getComponentCount();
      int x = 0;
      int y = insets.top + vgap; // FlowLayout starts by adding vgap, so do that here too.
      int rowHeight = 0;

      for (int i = 0; i < n; i++) {
        Component c = target.getComponent(i);

        if (c.isVisible()) {
          Dimension d = c.getPreferredSize();

          if ((x == 0) || ((x + d.width) <= maxwidth)) {
            // fits in current row.
            if (x > 0) {
              x += hgap;
            }

            x += d.width;
            rowHeight = Math.max(rowHeight, d.height);
          }
          else {
            // Start of new row
            x = d.width;
            y += vgap + rowHeight;
            rowHeight = d.height;
          }

          reqdWidth = Math.max(reqdWidth, x);
        }
      }

      y += rowHeight;
      y += insets.bottom;

      return new Dimension(reqdWidth + insets.left + insets.right, y);
    }
  }

  private Dimension computeMinSize(Container target) {
    synchronized (target.getTreeLock()) {
      int minx = Integer.MAX_VALUE;
      int miny = Integer.MIN_VALUE;
      boolean foundOne = false;
      int n = target.getComponentCount();

      for (int i = 0; i < n; i++) {
        Component c = target.getComponent(i);

        if (c.isVisible()) {
          foundOne = true;
          Dimension d = c.getPreferredSize();
          minx = Math.min(minx, d.width);
          miny = Math.min(miny, d.height);
        }
      }

      if (foundOne) {
        return new Dimension(minx, miny);
      }

      return new Dimension(0, 0);
    }
  }
}
