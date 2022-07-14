/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.components.properties.event;

/**
 * A PropertiesModelChangeListener that does nothing.
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
