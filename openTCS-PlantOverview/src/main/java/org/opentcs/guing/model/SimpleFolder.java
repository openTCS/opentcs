/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model;

/**
 * Ist die einfachste Form einer konkreten Komponente im Systemmodell, die
 * Kindelemente enthält. SimpleFolder wird für schlichte Ordner verwendet.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class SimpleFolder
    extends CompositeModelComponent {

  /**
   * Erzeugt von SimpleFolder einen neues Exemplar mit dem übergebenen Namen.
   *
   * @param name
   */
  public SimpleFolder(String name) {
    super(name);
  }
}
