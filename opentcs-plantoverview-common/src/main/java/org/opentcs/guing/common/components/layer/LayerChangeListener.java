// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.layer;

/**
 * Listens for changes to/updates on layer data.
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
