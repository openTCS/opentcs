/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.xmlhost.status;

import static java.util.Objects.requireNonNull;
import org.opentcs.data.TCSObjectEvent;

/**
 * A command for the message dispatcher's connection handler.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
abstract class ConnectionCommand
    implements Comparable<ConnectionCommand> {

  private final int priority;

  private final long creationTime;

  public ConnectionCommand(int priority) {
    this.priority = priority;
    this.creationTime = System.currentTimeMillis();
  }

  @Override
  public int compareTo(ConnectionCommand o) {
    // Natural ordering of commands by (1) priority and (2) age.
    if (priority < o.priority) {
      return -1;
    }
    else if (priority > o.priority) {
      return 1;
    }
    else if (this.creationTime < o.creationTime) {
      return -1;
    }
    else if (this.creationTime > o.creationTime) {
      return 1;
    }
    else {
      return 0;
    }
  }

  /**
   * Indicates the receiving task should be terminated.
   */
  public static class PoisonPill
      extends ConnectionCommand {

    /**
     * Creates a new instance.
     */
    public PoisonPill() {
      super(1);
    }
  }

  /**
   * Indicates the receiving task should process an event.
   */
  public static class ProcessObjectEvent
      extends ConnectionCommand {

    /**
     * The event to be processed.
     */
    private final TCSObjectEvent event;

    /**
     * Creates a new instance.
     *
     * @param event The event to be processed.
     */
    public ProcessObjectEvent(TCSObjectEvent event) {
      super(2);
      this.event = requireNonNull(event, "event");
    }

    /**
     * Returns the event to be processed.
     *
     * @return The event to be processed.
     */
    public TCSObjectEvent getEvent() {
      return event;
    }
  }

}
