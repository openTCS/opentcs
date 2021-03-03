/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.properties.event;

/**
 * Ein PropertiesModelChangeListener, der benötigt wird, wenn ein
 * PropertiesModelChangeEvent erzeugt werden soll, jedoch kein direkter
 * PropertiesModelChangeListener die Änderung verursacht hat.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class NullAttributesChangeListener
    implements AttributesChangeListener {

  /**
   * Creates a new instance of NullPropertiesModelChangeListener
   */
  public NullAttributesChangeListener() {
  }

  @Override
  public void propertiesChanged(AttributesChangeEvent e) {
  }
}
