/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.drawing;

/**
 * An item to show in a combo box.
 */
public class ZoomItem {

  private final double scaleFactor;

  public ZoomItem(double scaleFactor) {
    this.scaleFactor = scaleFactor;
  }

  public double getScaleFactor() {
    return scaleFactor;
  }

  @Override
  public String toString() {
    return String.format("%d %%", (int) (scaleFactor * 100));
  }
}
