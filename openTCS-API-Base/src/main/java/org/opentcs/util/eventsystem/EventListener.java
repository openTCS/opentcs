/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.eventsystem;

import javax.annotation.Nonnull;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * This interfaces declares the method that is called to notify an object of an
 * event that has happened.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The actual event implementation.
 * @deprecated For event handling, use classes in <code>org.opentcs.util.event</code> instead.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public interface EventListener<E extends Event> {

  /**
   * Called when an event has happened.
   *
   * @param event The event that has happened.
   */
  void processEvent(@Nonnull E event);
}
