/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.util;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

/**
 * Provides cursors for various situations.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class Cursors {

  /**
   * A cursor suitable for dragging a vehicle to a destination point.
   */
  private static final Cursor dragVehicleCursor;

  static {
    // Load an image for the vehicle drag cursor.
    BufferedImage bi = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
    bi.createGraphics().drawImage(new ImageIcon(Cursors.class.getClassLoader().
        getResource("org/opentcs/guing/res/symbols/toolbar/create-order.22.png")).
        getImage(), 0, 0, null);
    dragVehicleCursor = Toolkit.getDefaultToolkit().createCustomCursor(
        bi, new Point(0, 0), "toCursor");
  }

  /**
   * Prevents instantiation.
   */
  private Cursors() {
    // Do nada.
  }

  /**
   * Returns a cursor suitable for dragging a vehicle to a destination point.
   *
   * @return A cursor suitable for dragging a vehicle to a destination point.
   */
  public static Cursor getDragVehicleCursor() {
    return dragVehicleCursor;
  }
}
