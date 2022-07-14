/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.layer;

/**
 * Listens for changes to/updates on layer group data.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface LayerGroupChangeListener {

  /**
   * Notifies the listener that the layer group data has been initialized.
   */
  void groupsInitialized();

  /**
   * Notifies the listener that some layer group data has changed.
   */
  void groupsChanged();

  /**
   * Notifies the listener that a layer group has been added.
   */
  void groupAdded();

  /**
   * Notifies the listener that a layer group has been removed.
   */
  void groupRemoved();
}
