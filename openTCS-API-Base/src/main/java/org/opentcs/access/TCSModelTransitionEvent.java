/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access;

import java.io.Serializable;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Instances of this class represent events emitted when the kernel loads a
 * model.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Use {@link ModelTransitionEvent} instead, which does not extend TCSEvent.
 */
@Deprecated
@ScheduledApiChange(when = "5.0")
public class TCSModelTransitionEvent
    extends org.opentcs.util.eventsystem.TCSEvent
    implements Serializable {

  /**
   * The old model the kernel is leaving.
   */
  private final String oldModelName;
  /**
   * The new model the kernel is in transition to.
   */
  private final String newModelName;
  /**
   * Whether the content of the model actually changed with the transition.
   */
  private final boolean modelContentChanged;
  /**
   * Whether the transition to the entered state is finished or not.
   */
  private final boolean transitionFinished;

  /**
   * Creates a new TCSModelTransitionEvent.
   *
   * @param oldModelName The name of the previously loaded model.
   * @param newModelName The name of the new model.
   * @param modelContentChanged Whether the content of the model actually
   * changed with the transition.
   * @param transitionFinished Whether the transition to the new model is
   * finished, yet.
   */
  public TCSModelTransitionEvent(String oldModelName,
                                 String newModelName,
                                 boolean modelContentChanged,
                                 boolean transitionFinished) {
    this.oldModelName = oldModelName;
    this.newModelName = newModelName;
    this.modelContentChanged = modelContentChanged;
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
   * Returns <code>true</code> if, and only if, the content of the model
   * actually changed with the transition.
   *
   * @return <code>true</code> if, and only if, the content of the model
   * actually changed with the transition.
   */
  public boolean hasModelContentChanged() {
    return modelContentChanged;
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
    return "TCSModelTransitionEvent{"
        + "oldModelName=" + oldModelName
        + ", newModelName=" + newModelName
        + ", modelContentChanged=" + modelContentChanged
        + ", transitionFinished=" + transitionFinished
        + '}';
  }
}
