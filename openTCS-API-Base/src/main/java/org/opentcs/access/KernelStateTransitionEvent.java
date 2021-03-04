/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access;

import java.io.Serializable;

/**
 * Emitted by/for kernel state changes.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class KernelStateTransitionEvent
    implements Serializable {

  /**
   * The old state the kernel is leaving.
   */
  private final Kernel.State leftState;
  /**
   * The new state the kernel is in transition to.
   */
  private final Kernel.State enteredState;
  /**
   * Whether the transition to the entered state is finished or not.
   */
  private final boolean transitionFinished;

  /**
   * Creates a new TCSKernelStateEvent.
   *
   * @param leftState The previous state of the kernel.
   * @param enteredState The new state of the kernel.
   * @param transitionFinished Whether the transistion is finished, yet.
   */
  public KernelStateTransitionEvent(Kernel.State leftState,
                                    Kernel.State enteredState,
                                    boolean transitionFinished) {
    this.leftState = leftState;
    this.enteredState = enteredState;
    this.transitionFinished = transitionFinished;
  }

  /**
   * Returns the state the kernel is leaving.
   *
   * @return The state the kernel is leaving.
   */
  public Kernel.State getLeftState() {
    return leftState;
  }

  /**
   * Returns the state for which this event was generated.
   *
   * @return The state for which this event was generated.
   */
  public Kernel.State getEnteredState() {
    return enteredState;
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
        + "leftState=" + leftState
        + ", enteredState=" + enteredState
        + ", transitionFinished=" + transitionFinished
        + '}';
  }
}
