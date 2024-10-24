// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
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
