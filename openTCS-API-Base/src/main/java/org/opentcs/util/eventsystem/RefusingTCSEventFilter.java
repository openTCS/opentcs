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
 * An event filter for {@link TCSEvent}s that does not accept any events.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated {@link org.opentcs.util.eventsystem.EventFilter} is deprecated.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public class RefusingTCSEventFilter
    implements EventFilter<TCSEvent>,
               Serializable {

  /**
   * Creates a new instance.
   */
  public RefusingTCSEventFilter() {
  }

  @Override
  public boolean accept(TCSEvent event) {
    return false;
  }
}
