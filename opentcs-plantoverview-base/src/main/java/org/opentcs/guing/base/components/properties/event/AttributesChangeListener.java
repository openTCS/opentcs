// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.components.properties.event;

/**
 * Interface that controllers/views implement.
 *
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
