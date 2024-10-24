// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.event;

import java.util.EventObject;

/**
 * This event instructs the receiver(s) to reset the currently selected
 * interaction tool for the user.
 */
public class ResetInteractionToolCommand
    extends
      EventObject {

  /**
   * Creates a new instance.
   *
   * @param source The event's originator.
   */
  public ResetInteractionToolCommand(Object source) {
    super(source);
  }
}
