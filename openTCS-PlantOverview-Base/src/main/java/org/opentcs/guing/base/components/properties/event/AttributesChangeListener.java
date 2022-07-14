/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.components.properties.event;

/**
 * Interface that controllers/views implement.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @see AttributesChangeEvent
 */
public interface AttributesChangeListener {

  /**
   * Event received when the model has been changed.
   *
   * @param e The event.
   */
  void propertiesChanged(AttributesChangeEvent e);
}
