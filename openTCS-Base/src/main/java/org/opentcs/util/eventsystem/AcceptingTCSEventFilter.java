/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.eventsystem;

import java.io.Serializable;

/**
 * An event filter for <code>TCSEvent</code>s that accepts all events.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class AcceptingTCSEventFilter
    implements EventFilter<TCSEvent>, Serializable {

  /**
   * Creates a new instance.
   */
  public AcceptingTCSEventFilter() {
    // Do nada.
  }

  @Override
  public boolean accept(TCSEvent event) {
    return true;
  }
}
