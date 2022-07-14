/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.dockable;

import bibliothek.gui.dock.common.SingleCDockable;
import java.beans.PropertyChangeListener;

/**
 * Utility class for working with dockables.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface DockingManager {

  /**
   * PropertyChangeEvent when a floating dockable closes.
   */
  String DOCKABLE_CLOSED = "DOCKABLE_CLOSED";

  /**
   * Adds a PropertyChangeListener.
   *
   * @param listener The new listener.
   */
  void addPropertyChangeListener(PropertyChangeListener listener);

  /**
   * Removes a dockable from the CControl.
   *
   * @param dockable The dockable that shall be removed.
   */
  void removeDockable(SingleCDockable dockable);

  /**
   * Removes a dockable with the given id.
   *
   * @param id The id of the dockable to remove.
   */
  void removeDockable(String id);
}
