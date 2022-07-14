/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.model;

/**
 * The simplest form of a component in the system model that contains child elements.
 * SimpleFolder is used for plain folders.
 * 
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class SimpleFolder
    extends CompositeModelComponent {

  /**
   * Creates a new instance.
   *
   * @param name The name of the folder.
   */
  public SimpleFolder(String name) {
    super(name);
  }
}
