/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi;

import java.io.Serializable;
import org.opentcs.util.eventsystem.TCSEvent;

/**
 * Instances of this class represent events emitted by/for kernel proxy state
 * changes.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TCSProxyStateEvent
    extends TCSEvent
    implements Serializable {

  /**
   * The new state for which this event was created.
   */
  private final RemoteKernelConnection.State enteredState;

  /**
   * Creates a new TCSProxyStateEvent.
   *
   * @param enteredState The new state for which this event was created.
   */
  public TCSProxyStateEvent(RemoteKernelConnection.State enteredState) {
    this.enteredState = enteredState;
  }

  /**
   * Returns the state for which this event was created.
   *
   * @return The state for which this event was created.
   */
  public RemoteKernelConnection.State getEnteredState() {
    return enteredState;
  }
}
