/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application;

import bibliothek.gui.Dockable;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.intern.DefaultCommonDockable;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.guing.components.dockable.CStackDockStation;
import org.opentcs.guing.components.dockable.DockableTitleComparator;
import org.opentcs.guing.components.dockable.DockingManager;
import org.opentcs.guing.components.drawing.DrawingViewScrollPane;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.event.OperationModeChangeEvent;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.transport.OrderSequencesContainerPanel;
import org.opentcs.guing.transport.TransportOrdersContainerPanel;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.event.EventSource;

/**
 * Manages the mapping of dockables to drawing views, transport order views and
 * order sequence views.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class ViewManager
    implements EventHandler {

  /**
   * Manages the application's docking frames.
   */
  private final DockingManager dockingManager;
  /**
   * Where we register event listeners.
   */
  private final EventSource eventSource;
  /**
   * Map for Dockable -> DrawingView + Rulers.
   */
  private final Map<DefaultSingleCDockable, DrawingViewScrollPane> drawingViewMap;
  /**
   * Map for transport order dockable -> transport order container panel.
   */
  private final Map<DefaultSingleCDockable, TransportOrdersContainerPanel> transportOrderViews;
  /**
   * Map for order sequences dockable -> order sequences container panel.
   */
  private final Map<DefaultSingleCDockable, OrderSequencesContainerPanel> orderSequenceViews;
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
  public ViewManager(DockingManager dockingManager,
                     @ApplicationEventBus EventSource eventSource) {
    this.dockingManager = requireNonNull(dockingManager, "dockingManager");
    this.eventSource = requireNonNull(eventSource, "eventSource");
    drawingViewMap = new TreeMap<>(new DockableTitleComparator());
    transportOrderViews = new TreeMap<>(new DockableTitleComparator());
    orderSequenceViews = new TreeMap<>(new DockableTitleComparator());
  }

  @Override
  public void onEvent(Object event) {
    if (event instanceof OperationModeChangeEvent) {
      handleModeChange((OperationModeChangeEvent) event);
    }
  }

  /**
   * Resets all components.
   */
  public void reset() {
    drawingViewModellingDockable = null;
    drawingViewMap.clear();
    transportOrderViews.clear();
    orderSequenceViews.clear();
  }

  /**
   * Toggles the visibility of the group members for a specific
   * <code>OpenTCSDrawingView</code>.
   *
   * @param title The title of the drawing view.
   * @param sf The group folder containing the elements to hide.
   * @param visible Visible or not.
   */
  public void setGroupVisibilityInDrawingView(String title, GroupModel sf, boolean visible) {
    for (DefaultSingleCDockable dock : drawingViewMap.keySet()) {
      if (dock.getTitleText().equals(title)) {
        drawingViewMap.get(dock).getDrawingView().setGroupVisible(sf.getChildComponents(), visible);
      }
    }
  }

  public Map<DefaultSingleCDockable, DrawingViewScrollPane> getDrawingViewMap() {
    return drawingViewMap;
  }

  public Map<DefaultSingleCDockable, TransportOrdersContainerPanel> getTransportOrderMap() {
    return transportOrderViews;
  }

  public Map<DefaultSingleCDockable, OrderSequencesContainerPanel> getOrderSequenceMap() {
    return orderSequenceViews;
  }

  private void handleModeChange(OperationModeChangeEvent evt) {
    switch (evt.getNewMode()) {
      case MODELLING:
        setPlantOverviewStateModelling();
        break;
      case OPERATING:
        setPlantOverviewStateOperating();
        break;
      default:
      // XXX Unhandled mode. Do anything?
    }
  }

  /**
   * Returns all drawing views (excluding the modelling view)
   *
   * @return List with all known <code>OpenTCSDrawingViews</code>, but not
   * the modelling view.
   */
  public List<OpenTCSDrawingView> getOperatingDrawingViews() {
    List<OpenTCSDrawingView> views = new ArrayList<>();

    for (DrawingViewScrollPane scrollPane : drawingViewMap.values()) {
      if (drawingViewMap.get(drawingViewModellingDockable) != scrollPane) {
        views.add(scrollPane.getDrawingView());
      }
    }

    return views;
  }

  /**
   * Returns the title texts of all drawing views.
   *
   * @return List of strings containing the names.
   */
  public List<String> getDrawingViewNames() {
    List<String> names = new ArrayList<>();
    for (DefaultSingleCDockable dock : drawingViewMap.keySet()) {
      if (dock != drawingViewModellingDockable) {
        names.add(dock.getTitleText());
      }
    }
    Collections.sort(names);

    return names;
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
    drawingViewMap.get(drawingViewModellingDockable).getDrawingView().addBackgroundBitmap(file);
  }

  public int getNextDrawingViewIndex() {
    return (drawingViewMap.isEmpty() && drawingViewModellingDockable == null)
        ? 0 : nextAvailableIndex(drawingViewMap.keySet());
  }

  public int getNextTransportOrderViewIndex() {
    return nextAvailableIndex(transportOrderViews.keySet());
  }

  public int getNextOrderSequenceViewIndex() {
    return nextAvailableIndex(orderSequenceViews.keySet());
  }

  public DefaultSingleCDockable getLastTransportOrderView() {
    int biggestIndex = getNextTransportOrderViewIndex();
    DefaultSingleCDockable lastTOView = null;
    Iterator<DefaultSingleCDockable> tranportOrderViewIterator
        = transportOrderViews.keySet().iterator();
    for (int i = 0; i < biggestIndex; i++) {
      if (tranportOrderViewIterator.hasNext()) {
        lastTOView = tranportOrderViewIterator.next();
      }
    }

    return lastTOView;
  }

  public DefaultSingleCDockable getLastOrderSequenceView() {
    int biggestIndex = getNextOrderSequenceViewIndex();
    DefaultSingleCDockable lastOSView = null;
    Iterator<DefaultSingleCDockable> orderSequencesViewIterator
        = orderSequenceViews.keySet().iterator();
    for (int i = 0; i < biggestIndex; i++) {
      if (orderSequencesViewIterator.hasNext()) {
        lastOSView = orderSequencesViewIterator.next();
      }
    }

    return lastOSView;
  }

  /**
   * Puts a scroll pane with a key dockable into the drawing view map.
   * The scroll pane has to contain the drawing view and both rulers.
   *
   * @param dockable The dockable the scrollPane is wrapped into. Used as the key.
   * @param scrollPane The scroll pane containing the drawing view and rulers.
   */
  public void addDrawingView(DefaultSingleCDockable dockable,
                             DrawingViewScrollPane scrollPane) {
    requireNonNull(dockable, "dockable");
    requireNonNull(scrollPane, "scrollPane");

    eventSource.subscribe(scrollPane.getDrawingView());
    drawingViewMap.put(dockable, scrollPane);
  }

  /**
   * Puts a <code>TransportOrdersContainerPanel</code> with a key dockable
   * into the transport order view map.
   *
   * @param dockable The dockable the panel is wrapped into. Used as the key.
   * @param panel The panel.
   */
  public void addTransportOrderView(DefaultSingleCDockable dockable,
                                    TransportOrdersContainerPanel panel) {
    requireNonNull(dockable, "dockable");
    requireNonNull(panel, "panel");

    eventSource.subscribe(panel);
    transportOrderViews.put(dockable, panel);
  }

  /**
   * Puts a <code>OrderSequencesContainerPanel</code> with a key dockable
   * into the order sequence view map.
   *
   * @param dockable The dockable the panel is wrapped into. Used as the key.
   * @param panel The panel.
   */
  public void addOrderSequenceView(DefaultSingleCDockable dockable,
                                   OrderSequencesContainerPanel panel) {
    requireNonNull(dockable, "dockable");
    requireNonNull(panel, "panel");

    eventSource.subscribe(panel);
    orderSequenceViews.put(dockable, panel);
  }

  /**
   * Forgets the given dockable.
   *
   * @param dockable The dockable.
   */
  public void removeDockable(DefaultSingleCDockable dockable) {
    DrawingViewScrollPane scrollPane = drawingViewMap.remove(dockable);
    if (scrollPane != null) {
      eventSource.unsubscribe(scrollPane.getDrawingView());
    }

    TransportOrdersContainerPanel ordersPanel = transportOrderViews.remove(dockable);
    if (ordersPanel != null) {
      eventSource.unsubscribe(ordersPanel);
    }

    OrderSequencesContainerPanel sequencePanel = orderSequenceViews.remove(dockable);
    if (sequencePanel != null) {
      eventSource.unsubscribe(sequencePanel);
    }
  }

  /**
   * Evaluates which dockable should be the front dockable.
   *
   * @return The dockable that should be the front dockable. <code>null</code>
   * if no dockables exist.
   */
  public DefaultCommonDockable evaluateFrontDockable() {
    if (!drawingViewMap.isEmpty()) {
      return drawingViewMap.keySet().iterator().next().intern();
    }
    if (!transportOrderViews.isEmpty()) {
      return transportOrderViews.keySet().iterator().next().intern();
    }
    if (!orderSequenceViews.isEmpty()) {
      return orderSequenceViews.keySet().iterator().next().intern();
    }
    return null;
  }

  /**
   * Returns the next available index of a set of dockables.
   * E.g. if "Dock 0" and "Dock 2" are being used, 1 would be returned.
   *
   * @param setToIterate The set to iterate.
   * @return The next available index.
   */
  private int nextAvailableIndex(Set<DefaultSingleCDockable> setToIterate) {
    // Name
    Pattern p = Pattern.compile("\\d");
    Matcher m;
    int biggestIndex = 0;

    for (DefaultSingleCDockable dock : setToIterate) {
      m = p.matcher(dock.getTitleText());

      if (m.find()) {
        int index = Integer.parseInt(m.group(0));

        if (index > biggestIndex) {
          biggestIndex = index;
        }
      }
    }

    return biggestIndex + 1;
  }

  /**
   * Sets visibility states of all dockables to modelling.
   */
  private void setPlantOverviewStateModelling() {
    CStackDockStation station
        = dockingManager.getTabPane(DockingManager.COURSE_TAB_PANE_ID).getStation();
    List<DefaultSingleCDockable> drawingViews = new ArrayList<>(drawingViewMap.keySet());
    for (int i = 0; i < drawingViews.size(); i++) {
      DefaultSingleCDockable dock = drawingViews.get(i);
      if (dock != drawingViewModellingDockable) {
        // Setting it to closeable = false, so the ClosingListener
        // doesn't remove the dockable when it's closed
        dock.setCloseable(false);
        dockingManager.setDockableVisibility(dock.getUniqueId(), false);
      }
    }

    for (DefaultSingleCDockable dock : new ArrayList<>(transportOrderViews.keySet())) {
      dockingManager.hideDockable(station, dock);
      transportOrderViews.get(dock).clearTransportOrders();
    }

    for (DefaultSingleCDockable dock : new ArrayList<>(orderSequenceViews.keySet())) {
      dockingManager.hideDockable(station, dock);
      orderSequenceViews.get(dock).clearOrderSequences();
    }

    dockingManager.setDockableVisibility(DockingManager.VEHICLES_DOCKABLE_ID, false);
    dockingManager.showDockable(station, drawingViewModellingDockable, 0);
    OpenTCSDrawingView view = drawingViewMap.get(drawingViewModellingDockable).getDrawingView();
    view.dispatchEvent(new FocusEvent(view, FocusEvent.FOCUS_GAINED));
  }

  /**
   * Sets visibility states of all dockables to operating.
   */
  private void setPlantOverviewStateOperating() {
    CStackDockStation station
        = dockingManager.getTabPane(DockingManager.COURSE_TAB_PANE_ID).getStation();
    Dockable frontDock = station.getFrontDockable();
    dockingManager.hideDockable(station, drawingViewModellingDockable);
    int i = 0;
    for (DefaultSingleCDockable dock : new ArrayList<>(drawingViewMap.keySet())) {
      if (dock != drawingViewModellingDockable) {
        // Restore to default
        dock.setCloseable(true);
        dockingManager.showDockable(station, dock, i);
        i++;
      }
    }
    i = drawingViewMap.size();
    for (DefaultSingleCDockable dock : new ArrayList<>(drawingViewMap.keySet())) {
      // OpenTCSDrawingViews can be undocked when switching states, so
      // we make sure they aren't counted as docked
      if (dockingManager.isDockableDocked(station, dock)) {
        i--;
      }
    }

    int dockedDrawingViews = i;

    for (DefaultSingleCDockable dock : new ArrayList<>(transportOrderViews.keySet())) {
      dockingManager.showDockable(station, dock, i);
      i++;
    }

    i = dockedDrawingViews + transportOrderViews.size();

    for (DefaultSingleCDockable dock : new ArrayList<>(orderSequenceViews.keySet())) {
      dockingManager.showDockable(station, dock, i);
      i++;
    }

    if (frontDock != null && frontDock.isDockableShowing()) {
      station.setFrontDockable(frontDock);
    }
    else {
      station.setFrontDockable(station.getDockable(0));
    }
    dockingManager.setDockableVisibility(DockingManager.VEHICLES_DOCKABLE_ID, true);
  }

}
