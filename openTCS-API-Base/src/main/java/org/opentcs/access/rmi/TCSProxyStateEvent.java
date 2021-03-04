/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi;

import java.io.Serializable;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Instances of this class represent events emitted by/for kernel proxy state
 * changes.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Use {@code ClientConnectionMode} instead.
 */
@Deprecated
@ScheduledApiChange(when = "5.0")
public class TCSProxyStateEvent
    extends org.opentcs.util.eventsystem.TCSEvent
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
