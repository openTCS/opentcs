// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.dialogs;

/**
 * A Listener Interface, instances can handle validation of the user input.
 */
public interface InputValidationListener {

  /**
   * Notifies about the validity of an input.
   *
   * @param success true if input is valid, false otherwise.
   */
  void inputValidationSuccessful(boolean success);
}
