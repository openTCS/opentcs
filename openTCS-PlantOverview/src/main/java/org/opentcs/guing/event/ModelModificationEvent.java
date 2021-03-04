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

/**
 * An event that informs listeners that changes to the system model have been
 * made.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ModelModificationEvent
    extends EventObject {

  /**
   * Creates a new instance.
   *
   * @param source The source of the event.
   */
  public ModelModificationEvent(Object source) {
    super(source);
  }
}
