/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.event;

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
