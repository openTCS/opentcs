/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.layer;

/**
 * Listens for changes to/updates on layer data.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface LayerChangeListener {

  /**
   * Notifies the listener that the layer data has been initialized.
   */
  void layersInitialized();

  /**
   * Notifies the listener that some layer data has changed.
   */
  void layersChanged();

  /**
   * Notifies the listener that a layer has been added.
   */
  void layerAdded();

  /**
   * Notifies the listener that a layer has been removed.
   */
  void layerRemoved();
}
