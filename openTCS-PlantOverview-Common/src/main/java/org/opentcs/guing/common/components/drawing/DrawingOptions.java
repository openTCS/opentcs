/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.drawing;

/**
 * Allows the configuration of drawing options.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DrawingOptions {

  /**
   * Indicates whether blocks should be drawn or not.
   */
  private boolean blocksVisible = true;

  public DrawingOptions() {
  }

  /**
   * Returns whether blocks should be drawn or not.
   *
   * @return {@code true}, if blocks should be drawn, otherwise {@code false}.
   */
  public boolean isBlocksVisible() {
    return blocksVisible;
  }

  /**
   * Sets whether blocks should be drawn or not.
   *
   * @param blocksVisible The new value.
   */
  public void setBlocksVisible(boolean blocksVisible) {
    this.blocksVisible = blocksVisible;
  }
}
