/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.application;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import java.util.List;
import java.util.Map;
import org.opentcs.guing.common.components.drawing.DrawingViewScrollPane;

/**
 * Manages the mapping of dockables to drawing views, transport order views and
 * order sequence views.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface ViewManager {

  Map<DefaultSingleCDockable, DrawingViewScrollPane> getDrawingViewMap();

  /**
   * Returns the title texts of all drawing views.
   *
   * @return List of strings containing the names.
   */
  List<String> getDrawingViewNames();

  /**
   * Forgets the given dockable.
   *
   * @param dockable The dockable.
   */
  void removeDockable(DefaultSingleCDockable dockable);
}
