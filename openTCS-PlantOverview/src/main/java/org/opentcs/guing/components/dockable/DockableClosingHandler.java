/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.dockable;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.event.CVetoClosingEvent;
import bibliothek.gui.dock.common.event.CVetoClosingListener;
import com.google.inject.assistedinject.Assisted;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.guing.application.ViewManager;

/**
 * Handles closing of a dockable.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DockableClosingHandler
    implements CVetoClosingListener {

  /**
   * The dockable.
   */
  private final DefaultSingleCDockable dockable;
  /**
   * Manages the application's dockables.
   */
  private final DockingManager dockingManager;
  /**
   * Manages the application's views.
   */
  private final ViewManager viewManager;

  /**
   * Creates a new instance.
   *
   * @param dockable The dockable.
   * @param viewManager Manages the application's views.
   * @param dockingManager Manages the application's dockables.
   */
  @Inject
  public DockableClosingHandler(@Assisted DefaultSingleCDockable dockable,
                                ViewManager viewManager,
                                DockingManager dockingManager) {
    this.dockable = requireNonNull(dockable, "dockable");
    this.viewManager = requireNonNull(viewManager, "viewManager");
    this.dockingManager = requireNonNull(dockingManager, "dockingManager");
  }

  @Override
  public void closing(CVetoClosingEvent event) {
  }

  @Override
  public void closed(CVetoClosingEvent event) {
    if (event.isExpected()) {
      dockingManager.getCControl().removeDockable(dockable);
      viewManager.removeDockable(dockable);
    }
  }
}
