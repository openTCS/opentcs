/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.drawing;

/**
 * An item to show in a combo box.
 * 
 * @author Heinz Huber (Fraunhofer IML)
 * @author Philipp Seifert (Fraunhofer IML)
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
    String s = String.format("%d %%", (int) (scaleFactor * 100));
    //      System.out.println(s);
    return s;
  }
}
