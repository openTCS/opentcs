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
 * A TCS event.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class TCSEvent
    extends Event
    implements Serializable {

  /**
   * Creates a empty TCSEvent.
   */
  public TCSEvent() {
    // Do nada.
  }
}
