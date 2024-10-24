// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.layer;

/**
 * Listens for changes to/updates on layer group data.
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
