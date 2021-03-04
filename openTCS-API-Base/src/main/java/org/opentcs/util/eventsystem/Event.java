/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.eventsystem;

import java.io.Serializable;

/**
 * A generic event.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class Event
    implements Serializable {

  /**
   * Creates a new Event.
   */
  protected Event() {
    // Do nada.
  }
}
