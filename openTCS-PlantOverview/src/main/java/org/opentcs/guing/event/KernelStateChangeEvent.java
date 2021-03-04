/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.event;

import java.util.EventObject;
import static java.util.Objects.requireNonNull;
import org.opentcs.access.Kernel;

/**
 * Informs listeners about a change of the kernel's state.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class KernelStateChangeEvent
    extends EventObject {

  /**
   * The new/current kernel state.
   */
  private final State newState;

  /**
   * Creates a new instance.
   *
   * @param source The source of this event.
   * @param newState The new/current kernel state.
   */
  public KernelStateChangeEvent(Object source, State newState) {
    super(source);
    this.newState = requireNonNull(newState, "newState");
  }

  /**
   * Returns the new/current kernel state.
   *
   * @return The new/current kernel state.
   */
  public State getNewState() {
    return newState;
  }

  @Override
  public String toString() {
    return "KernelStateChangeEvent{"
        + "newState=" + newState
        + ", source=" + getSource()
        + '}';
  }

  public static State convertKernelState(Kernel.State kernelState) {
    switch (kernelState) {
      case MODELLING:
        return State.MODELLING;
      case OPERATING:
        return State.OPERATING;
      case SHUTDOWN:
        return State.SHUTDOWN;
      default:
        throw new IllegalArgumentException("Unhandled state: " + kernelState);
    }
  }

  public static enum State {
    MODELLING,
    OPERATING,
    SHUTDOWN,
    LOGGED_IN,
    DISCONNECTED;
  }
}
