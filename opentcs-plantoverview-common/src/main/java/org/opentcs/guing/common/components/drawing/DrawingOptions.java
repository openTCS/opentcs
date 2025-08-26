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
  /**
   * Indicates whether envelopes should be drawn at claimed and allocated resources.
   */
  private boolean envelopesVisible = false;

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

  /**
   * Returns whether envelopes should be drawn at allocated and claimed resources.
   *
   * @return {@code true}, if envelopes should be drawn, otherwise {@code false}.
   */
  public boolean isEnvelopesVisible() {
    return envelopesVisible;
  }

  /**
   * Sets whether envelopes should be drawn at allocated and claimed resources.
   *
   * @param envelopesVisible The new value.
   */
  public void setEnvelopesVisible(boolean envelopesVisible) {
    this.envelopesVisible = envelopesVisible;
  }
}
