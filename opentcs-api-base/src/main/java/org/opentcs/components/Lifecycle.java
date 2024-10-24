// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components;

/**
 * Defines methods for controlling a generic component's lifecycle.
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
