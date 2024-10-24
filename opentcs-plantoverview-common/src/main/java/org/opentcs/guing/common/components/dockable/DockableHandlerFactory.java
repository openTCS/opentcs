// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.dockable;

import bibliothek.gui.dock.common.DefaultSingleCDockable;

/**
 * A factory for handlers related to dockables.
 */
public interface DockableHandlerFactory {

  /**
   * Creates a new handler for closing the given dockable.
   *
   * @param dockable The dockable.
   * @return A new handler for closing the given dockable.
   */
  DockableClosingHandler createDockableClosingHandler(
      DefaultSingleCDockable dockable
  );
}
