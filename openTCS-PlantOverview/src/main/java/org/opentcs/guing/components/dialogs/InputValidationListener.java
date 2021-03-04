/*
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.dialogs;

/**
 * A Listener Interface, instances can handle validation of the user input.
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public interface InputValidationListener {

  /**
   * Notifies about the validity of an input.
   *
   * @param success true if input is valid, false otherwise.
   */
  void inputValidationSuccessful(boolean success);
}
