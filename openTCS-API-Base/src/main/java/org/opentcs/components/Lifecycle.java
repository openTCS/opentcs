/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components;

/**
 * Defines methods for controlling a generic component's lifecycle.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface Lifecycle {

  /**
   * (Re-)Initializes this component before it is being used.
   */
  void initialize();
  
  /**
   * Checks whether this component is initialized.
   *
   * @return <code>true</code> if, and only if, this component is initialized.
   */
  boolean isInitialized();

  /**
   * Terminates the instance and frees resources.
   */
  void terminate();
}
