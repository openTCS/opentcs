// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.model;

/**
 * The simplest form of a component in the system model that contains child elements.
 * SimpleFolder is used for plain folders.
 */
public class SimpleFolder
    extends
      CompositeModelComponent {

  /**
   * Creates a new instance.
   *
   * @param name The name of the folder.
   */
  public SimpleFolder(String name) {
    super(name);
  }
}
