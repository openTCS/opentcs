// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.drawing;

/**
 * Allows the configuration of drawing options.
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
