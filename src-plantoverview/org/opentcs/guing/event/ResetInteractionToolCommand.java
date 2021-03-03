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
 * This event instructs the receiver(s) to reset the currently selected
 * interaction tool for the user.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ResetInteractionToolCommand
    extends EventObject {

  /**
   * Creates a new instance.
   *
   * @param source The event's originator.
   */
  public ResetInteractionToolCommand(Object source) {
    super(source);
  }
}
