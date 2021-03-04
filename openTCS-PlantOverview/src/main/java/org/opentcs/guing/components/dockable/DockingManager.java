/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.dockable;

import bibliothek.gui.dock.common.CContentArea;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.event.CVetoClosingEvent;
import bibliothek.gui.dock.common.event.CVetoClosingListener;
import bibliothek.gui.dock.common.group.CGroupBehavior;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import javax.swing.JComponent;
import javax.swing.JFrame;
import org.opentcs.guing.components.tree.TreeView;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Utility class for working with dockables.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class DockingManager {

  /**
   * ID for the tab pane, that contains the course, transport orders and
   * order sequences.
   */
  public static final String COURSE_TAB_PANE_ID = "course_tab_pane";
  /**
   * ID for the tab pane, that contains the components, blocks and groups.
   */
  public static final String TREE_TAB_PANE_ID = "tree_tab_pane";
  /**
   * ID for the dockable, that contains the VehiclePanel.
   */
  public static final String VEHICLES_DOCKABLE_ID = "vehicles_dock";
  public static final String COMPONENTS_ID = "comp_dock";
  public static final String BLOCKS_ID = "block_dock";
  public static final String GROUPS_ID = "groups_id";
  public static final String PROPERTIES_ID = "properties_id";
  public static final String STATUS_ID = "status_id";
  /**
   * PropertyChangeEvent when a floating dockable closes.
   */
  public static final String DOCKABLE_CLOSED = "DOCKABLE_CLOSED";
  /**
   * Tab pane that contains the components, blocks and groups.
   */
  private CStack treeTabPane;
  /**
   * Tab pane that contains the course, transport orders and
   * order sequences.
   */
  private CStack courseTabPane;
  /**
   * Map that contains all tab panes. They are stored by their id.
   */
  private final Map<String, CStack> tabPanes = new HashMap<>();
  /**
   * Control for the dockable panels.
   */
  private CControl control;
  /**
   * The listeners for closing events.
   */
  private final List<PropertyChangeListener> listeners = new ArrayList<>();

  /**
   * Creates a new instance.
   */
  @Inject
  public DockingManager() {

  }

  /**
   * Adds a PropertyChangeListener.
   *
   * @param listener The new listener.
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    if (!listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  /**
   * Creates a new dockable.
   *
   * @param id The unique id for this dockable.
   * @param title The title text of this new dockable.
   * @param comp The JComponent wrapped by the new dockable.
   * @param closeable If the dockable can be closeable or not.
   * @return The newly created dockable.
   */
  public DefaultSingleCDockable createDockable(String id,
                                               String title,
                                               JComponent comp,
                                               boolean closeable) {
    Objects.requireNonNull(id, "id is null");
    Objects.requireNonNull(title, "title is null");
    Objects.requireNonNull(comp, "comp is null");
    if (control == null) {
      return null;
    }
    DefaultSingleCDockable dockable = new DefaultSingleCDockable(id, title);
    dockable.setCloseable(closeable);
    dockable.add(comp);
    return dockable;
  }

  /**
   * Creates a new floating dockable.
   *
   * @param id The unique id for this dockable.
   * @param title The title text of this new dockable.
   * @param comp The JComponent wrapped by the new dockable.
   * @return The newly created dockable.
   */
  public DefaultSingleCDockable createFloatingDockable(String id,
                                                       String title,
                                                       JComponent comp) {
    if (control == null) {
      return null;
    }
    final DefaultSingleCDockable dockable = new DefaultSingleCDockable(id, title);
    dockable.setCloseable(true);
    dockable.setFocusComponent(comp);
    dockable.add(comp);
    dockable.addVetoClosingListener(new CVetoClosingListener() {

      @Override
      public void closing(CVetoClosingEvent event) {
      }

      @Override
      public void closed(CVetoClosingEvent event) {
        fireFloatingDockableClosed(dockable);
      }
    });
    control.addDockable(dockable);
    dockable.setExtendedMode(ExtendedMode.EXTERNALIZED);
    Rectangle centerRectangle = control.getContentArea().getCenter().getBounds();
    dockable.setLocation(CLocation.external((centerRectangle.width - comp.getWidth()) / 2,
                                            (centerRectangle.height - comp.getHeight()) / 2,
                                            comp.getWidth(),
                                            comp.getHeight()));
    return dockable;
  }

  /**
   * Adds a dockable as tab to the tab pane identified by the given id.
   *
   * @param newTab The new dockable that shall be added.
   * @param id The ID of the tab pane.
   * @param index Index where to insert the dockable in the tab pane.
   */
  public void addTabTo(DefaultSingleCDockable newTab, String id, int index) {
    Objects.requireNonNull(newTab, "newTab is null.");
    Objects.requireNonNull(id, "id is null");
    CStack tabPane = tabPanes.get(id);
    if (tabPane != null) {
      control.addDockable(newTab);
      newTab.setWorkingArea(tabPane);
      tabPane.getStation().add(newTab.intern(), index);
      tabPane.getStation().setFrontDockable(newTab.intern());
    }
  }

  /**
   * Removes a dockable from the CControl.
   *
   * @param dockable The dockable that shall be removed.
   */
  public void removeDockable(SingleCDockable dockable) {
    Objects.requireNonNull(dockable, "dockable is null");
    if (control != null) {
      control.removeDockable(dockable);
    }
  }

  /**
   * Removes a dockable with the given id.
   *
   * @param id The id of the dockable to remove.
   */
  public void removeDockable(String id) {
    Objects.requireNonNull(id);
    SingleCDockable dock = control.getSingleDockable(id);
    if (dock != null) {
      removeDockable(dock);
    }
  }

  /**
   * Returns the CControl.
   *
   * @return The CControl.
   */
  public CControl getCControl() {
    return control;
  }

  /**
   * Returns the whole component with all dockables, tab panes etc.
   *
   * @return The CContentArea of the CControl.
   */
  public CContentArea getContentArea() {
    if (control != null) {
      return control.getContentArea();
    }
    else {
      return null;
    }
  }

  /**
   * Returns the tab pane with the given id.
   *
   * @param id ID of the tab pane.
   * @return The tab pane or null if there is no tab pane with this id.
   */
  public CStack getTabPane(String id) {
    if (control != null) {
      return tabPanes.get(id);
    }
    else {
      return null;
    }
  }

  public void reset() {
    removeDockable(VEHICLES_DOCKABLE_ID);
    removeDockable(BLOCKS_ID);
    removeDockable(COMPONENTS_ID);
    removeDockable(GROUPS_ID);
    removeDockable(PROPERTIES_ID);
    removeDockable(STATUS_ID);
    control.removeStation(getTabPane(COURSE_TAB_PANE_ID));
    control.removeStation(getTabPane(TREE_TAB_PANE_ID));
  }

  /**
   * Wraps all given JComponents into a dockable and deploys them on the CControl.
   *
   * @param frame
   * @param vehiclesPanel
   * @param fTreeView
   * @param fBlocksView
   * @param fGroupsView
   * @param fPropertiesComponent
   * @param statusScrollPane
   */
  public void initializeDockables(JFrame frame,
                                  JComponent vehiclesPanel,
                                  TreeView fTreeView,
                                  TreeView fBlocksView,
                                  TreeView fGroupsView,
                                  JComponent fPropertiesComponent,
                                  JComponent statusScrollPane) {
    Objects.requireNonNull(frame, "frame is null");
    Objects.requireNonNull(vehiclesPanel, "vehiclesPane is null");
    Objects.requireNonNull(fTreeView, "fTreeView is null");
    Objects.requireNonNull(fBlocksView, "fBlocksView is null");
    Objects.requireNonNull(fGroupsView, "fGroupsView is null");
    Objects.requireNonNull(fPropertiesComponent, "fPropertiesComponent is null");
    Objects.requireNonNull(statusScrollPane, "statusScrollPane is null");

    control = new CControl(frame);
    control.setGroupBehavior(CGroupBehavior.TOPMOST);

    // Disable keyboard shortcuts to avoid collisions.
    control.putProperty(CControl.KEY_GOTO_NORMALIZED, null);
    control.putProperty(CControl.KEY_GOTO_EXTERNALIZED, null);
    control.putProperty(CControl.KEY_GOTO_MAXIMIZED, null);
    control.putProperty(CControl.KEY_MAXIMIZE_CHANGE, null);

    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    CGrid grid = new CGrid(control);
    courseTabPane = new CStack(COURSE_TAB_PANE_ID);
    tabPanes.put(COURSE_TAB_PANE_ID, courseTabPane);
    DefaultSingleCDockable vehiclesDockable
        = createDockable(VEHICLES_DOCKABLE_ID,
                         vehiclesPanel.getAccessibleContext().getAccessibleName(),
                         vehiclesPanel,
                         false);
    treeTabPane = new CStack(TREE_TAB_PANE_ID);
    tabPanes.put(TREE_TAB_PANE_ID, treeTabPane);
    DefaultSingleCDockable treeViewDock
        = createDockable(COMPONENTS_ID,
                         bundle.getString("dockable.treeView"),
                         (JComponent) fTreeView,
                         false);
    DefaultSingleCDockable treeBlocks
        = createDockable(BLOCKS_ID,
                         bundle.getString("tree.blocks.text"),
                         (JComponent) fBlocksView,
                         false);
    DefaultSingleCDockable treeGroups
        = createDockable(GROUPS_ID,
                         bundle.getString("tree.groups.text"),
                         (JComponent) fGroupsView,
                         false);
    grid.add(0, 0, 250, 400, treeTabPane);
    grid.add(0, 400, 250, 400, createDockable(PROPERTIES_ID,
                                              bundle.getString("dockable.properties"),
                                              fPropertiesComponent,
                                              false));
    grid.add(0, 800, 250, 200, createDockable(STATUS_ID,
                                              bundle.getString("dockable.status"),
                                              statusScrollPane,
                                              false));
    grid.add(250, 0, 150, 500, vehiclesDockable);
    grid.add(400, 0, 1000, 500, courseTabPane);

    control.getContentArea().deploy(grid);

    // init tab panes
    addTabTo(treeViewDock, TREE_TAB_PANE_ID, 0);
    addTabTo(treeBlocks, TREE_TAB_PANE_ID, 1);
    addTabTo(treeGroups, TREE_TAB_PANE_ID, 2);
    treeTabPane.getStation().setFrontDockable(treeViewDock.intern());
  }

  /**
   * Hides a dockable (by actually removing it from its station).
   *
   * @param station The CStackDockStation the dockable belongs to.
   * @param dockable The dockable to hide.
   */
  public void hideDockable(CStackDockStation station, DefaultSingleCDockable dockable) {
    int index = station.indexOf(dockable.intern());

    if (index <= -1) {
      station.add(dockable.intern(), station.getDockableCount());
      index = station.indexOf(dockable.intern());
    }
    station.remove(index);
  }

  /**
   * Shows a dockable (by actually adding it to its station).
   *
   * @param station The CStackDockStation the dockable belongs to.
   * @param dockable The dockable to show.
   * @param index Where to add the dockable.
   */
  public void showDockable(CStackDockStation station,
                           DefaultSingleCDockable dockable,
                           int index) {
    if (station.indexOf(dockable.intern()) <= -1) {
      station.add(dockable.intern(), index);
    }
  }

  /**
   * Sets the visibility status of a dockable with the given id.
   *
   * @param id The id of the dockable.
   * @param visible If it shall be visible or not.
   */
  public void setDockableVisibility(String id, boolean visible) {
    if (control != null) {
      SingleCDockable dockable = control.getSingleDockable(id);
      if (dockable != null) {
        dockable.setVisible(visible);
      }
    }
  }

  /**
   * Checks if the given dockable is docked to its CStackDockStation.
   *
   * @param station The station the dockable should be docked in.
   * @param dockable The dockable to check.
   * @return True if it is docked, false otherwise.
   */
  public boolean isDockableDocked(CStackDockStation station, DefaultSingleCDockable dockable) {
    return station.indexOf(dockable.intern()) <= -1;
  }

  /**
   * Fires a <code>PropertyChangeEvent</code> when a floatable dockable is closed
   * (eg a plugin panel).
   *
   * @param dockable The dockable that was closed.
   */
  private void fireFloatingDockableClosed(DefaultSingleCDockable dockable) {
    for (PropertyChangeListener listener : listeners) {
      listener.propertyChange(
          new PropertyChangeEvent(this, DOCKABLE_CLOSED, dockable, dockable));
    }
  }
}
