// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access;

import java.io.Serializable;

/**
 * Emitted when the kernel loads a model.
 */
public class ModelTransitionEvent
    implements
      Serializable {

  /**
   * The old model the kernel is leaving.
   */
  private final String oldModelName;
  /**
   * The new model the kernel is in transition to.
   */
  private final String newModelName;
  /**
   * Whether the transition to the entered state is finished or not.
   */
  private final boolean transitionFinished;

  /**
   * Creates a new instance.
   *
   * @param oldModelName The name of the previously loaded model.
   * @param newModelName The name of the new model.
   * @param transitionFinished Whether the transition to the new model is
   * finished, yet.
   */
  public ModelTransitionEvent(
      String oldModelName,
      String newModelName,
      boolean transitionFinished
  ) {
    this.oldModelName = oldModelName;
    this.newModelName = newModelName;
    this.transitionFinished = transitionFinished;
  }

  /**
   * Returns the model name the kernel is leaving.
   *
   * @return The model the kernel is leaving.
   */
  public String getOldModelName() {
    return oldModelName;
  }

  /**
   * Returns the model for which this event was generated.
   *
   * @return The model for which this event was generated.
   */
  public String getNewModelName() {
    return newModelName;
  }

  /**
   * Returns <code>true</code> if, and only if, the transition to the new kernel
   * state is finished.
   *
   * @return <code>true</code> if, and only if, the transition to the new kernel
   * state is finished.
   */
  public boolean isTransitionFinished() {
    return transitionFinished;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + '{'
        + "oldModelName=" + oldModelName
        + ", newModelName=" + newModelName
        + ", transitionFinished=" + transitionFinished
        + '}';
  }
}
