/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;

/**
 * Keeps and provides information about the current state of the application as
 * a whole.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ApplicationState {

  /**
   * The application's current mode of operation.
   */
  private OperationMode operationMode = OperationMode.UNDEFINED;

  /**
   * Creates a new instance.
   */
  @Inject
  public ApplicationState() {
  }

  /**
   * Returns the application's current mode of operation.
   *
   * @return The application's current mode of operation.
   */
  public OperationMode getOperationMode() {
    return operationMode;
  }

  /**
   * Checks whether the application is currently in the given mode of operation.
   *
   * @param mode The mode to check for.
   * @return <code>true</code> if, and only if, the application is currently in
   * the given mode.
   */
  public boolean hasOperationMode(OperationMode mode) {
    return operationMode == mode;
  }

  /**
   * Sets the application's current mode of operation.
   *
   * @param operationMode The application's new mode of operation.
   */
  public void setOperationMode(OperationMode operationMode) {
    this.operationMode = requireNonNull(operationMode, "operationMode");
  }

}
