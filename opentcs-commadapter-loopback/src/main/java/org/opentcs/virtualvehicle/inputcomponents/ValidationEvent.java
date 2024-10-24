// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.virtualvehicle.inputcomponents;

import java.util.EventObject;

/**
 * An event holding a single boolean variable indicating if something is valid.
 */
public class ValidationEvent
    extends
      EventObject {

  /**
   * Validation state.
   */
  private final boolean valid;

  /**
   * Create a new <code>ValidationEvent</code>.
   *
   * @param source The source of the event.
   * @param valid The state of validation that should be reported by this event.
   */
  public ValidationEvent(Object source, boolean valid) {
    super(source);
    this.valid = valid;
  }

  /**
   * Return the state of validation.
   *
   * @return valid
   */
  public boolean valid() {
    return valid;
  }
}
