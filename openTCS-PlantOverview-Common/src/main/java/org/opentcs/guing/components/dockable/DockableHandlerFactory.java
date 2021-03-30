/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.dockable;

import bibliothek.gui.dock.common.DefaultSingleCDockable;

/**
 * A factory for handlers related to dockables.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface DockableHandlerFactory {

  /**
   * Creates a new handler for closing the given dockable.
   *
   * @param dockable The dockable.
   * @return A new handler for closing the given dockable.
   */
  DockableClosingHandler createDockableClosingHandler(
      DefaultSingleCDockable dockable);
}
