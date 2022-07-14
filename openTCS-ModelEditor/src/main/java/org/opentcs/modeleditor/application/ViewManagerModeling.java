/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.application;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.ArrayList;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.jhotdraw.draw.DrawingView;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.guing.common.application.AbstractViewManager;
import org.opentcs.guing.common.components.dockable.CStackDockStation;
import org.opentcs.modeleditor.components.dockable.DockingManagerModeling;
import org.opentcs.util.event.EventSource;

/**
 * Manages the mapping of dockables to drawing views, transport order views and
 * order sequence views.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class ViewManagerModeling
    extends AbstractViewManager {

  /**
   * Manages the application's docking frames.
   */
  private final DockingManagerModeling dockingManager;
  /**
   * The default modelling dockable.
   */
  private DefaultSingleCDockable drawingViewModellingDockable;

  /**
   * Creates a new instance.
   *
   * @param dockingManager Manages the application's docking frames.
   * @param eventSource Where this instance registers event listeners.
   */
  @Inject
  public ViewManagerModeling(DockingManagerModeling dockingManager,
                             @ApplicationEventBus EventSource eventSource) {
    super(eventSource);
    this.dockingManager = requireNonNull(dockingManager, "dockingManager");
  }

  public void init() {
    setPlantOverviewStateModelling();
  }

  /**
   * Resets all components.
   */
  @Override
  public void reset() {
    super.reset();
    drawingViewModellingDockable = null;
  }

  /**
   * Initializes the unique modelling dockable.
   *
   * @param dockable The dockable that will be the modelling dockable.
   * @param title The title of this dockable.
   */
  public void initModellingDockable(DefaultSingleCDockable dockable, String title) {
    drawingViewModellingDockable = requireNonNull(dockable, "dockable");
    drawingViewModellingDockable.setTitleText(requireNonNull(title, "title"));
    drawingViewModellingDockable.setCloseable(false);
  }

  public void setBitmapToModellingView(File file) {
    getDrawingViewMap().get(drawingViewModellingDockable).getDrawingView().addBackgroundBitmap(file);
  }

  /**
   * Sets visibility states of all dockables to modelling.
   */
  private void setPlantOverviewStateModelling() {
    CStackDockStation station
        = dockingManager.getTabPane(DockingManagerModeling.COURSE_TAB_PANE_ID).getStation();

    for (DefaultSingleCDockable dock : new ArrayList<>(getDrawingViewMap().keySet())) {
      if (dock != drawingViewModellingDockable) {
        // Setting it to closeable = false, so the ClosingListener
        // doesn't remove the dockable when it's closed
        dock.setCloseable(false);
        dockingManager.setDockableVisibility(dock.getUniqueId(), false);
      }
    }

    dockingManager.showDockable(station, drawingViewModellingDockable, 0);
    DrawingView view = getDrawingViewMap().get(drawingViewModellingDockable).getDrawingView();
    view.getComponent().dispatchEvent(new FocusEvent(view.getComponent(), FocusEvent.FOCUS_GAINED));
  }
}
