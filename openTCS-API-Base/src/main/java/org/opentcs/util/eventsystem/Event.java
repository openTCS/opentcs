/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.eventsystem;

import java.io.Serializable;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A generic event.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated For event handling, use classes in <code>org.opentcs.util.event</code> instead.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public abstract class Event
    implements Serializable {

  /**
   * Creates a new Event.
   */
  protected Event() {
    // Do nada.
  }
}
