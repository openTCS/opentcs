/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.application;

import bibliothek.gui.Dockable;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.intern.DefaultCommonDockable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.guing.common.application.AbstractViewManager;
import org.opentcs.guing.common.components.dockable.CStackDockStation;
import org.opentcs.guing.common.components.dockable.DockableTitleComparator;
import org.opentcs.guing.common.components.drawing.OpenTCSDrawingView;
import org.opentcs.operationsdesk.components.dockable.DockingManagerOperating;
import org.opentcs.operationsdesk.peripherals.jobs.PeripheralJobsContainerPanel;
import org.opentcs.operationsdesk.transport.orders.TransportOrdersContainerPanel;
import org.opentcs.operationsdesk.transport.sequences.OrderSequencesContainerPanel;
import org.opentcs.util.event.EventSource;

/**
 * Manages the mapping of dockables to drawing views, transport order views and
 * order sequence views.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class ViewManagerOperating
    extends AbstractViewManager {

  /**
   * Manages the application's docking frames.
   */
  private final DockingManagerOperating dockingManager;
  /**
   * Where we register event listeners.
   */
  private final EventSource eventSource;
  /**
   * Map for transport order dockable -> transport order container panel.
   */
  private final Map<DefaultSingleCDockable, TransportOrdersContainerPanel> transportOrderViews;
  /**
   * Map for order sequences dockable -> order sequences container panel.
   */
  private final Map<DefaultSingleCDockable, OrderSequencesContainerPanel> orderSequenceViews;
  /**
   * Map for peripheral job dockable -> peripheral job container panel.
   */
  private final Map<DefaultSingleCDockable, PeripheralJobsContainerPanel> peripheralJobViews;

  /**
   * Creates a new instance.
   *
   * @param dockingManager Manages the application's docking frames.
   * @param eventSource Where this instance registers event listeners.
   */
  @Inject
  public ViewManagerOperating(DockingManagerOperating dockingManager,
                              @ApplicationEventBus EventSource eventSource) {
    super(eventSource);
    this.dockingManager = requireNonNull(dockingManager, "dockingManager");
    this.eventSource = requireNonNull(eventSource, "eventSource");
    transportOrderViews = new TreeMap<>(new DockableTitleComparator());
    orderSequenceViews = new TreeMap<>(new DockableTitleComparator());
    peripheralJobViews = new TreeMap<>(new DockableTitleComparator());
  }

  public void init() {
    setPlantOverviewStateOperating();
  }

  /**
   * Resets all components.
   */
  public void reset() {
    super.reset();
    transportOrderViews.clear();
    orderSequenceViews.clear();
    peripheralJobViews.clear();
  }

  public Map<DefaultSingleCDockable, TransportOrdersContainerPanel> getTransportOrderMap() {
    return transportOrderViews;
  }

  public Map<DefaultSingleCDockable, OrderSequencesContainerPanel> getOrderSequenceMap() {
    return orderSequenceViews;
  }

  public Map<DefaultSingleCDockable, PeripheralJobsContainerPanel> getPeripheralJobMap() {
    return peripheralJobViews;
  }

  /**
   * Returns all drawing views (excluding the modelling view)
   *
   * @return List with all known <code>OpenTCSDrawingViews</code>, but not
   * the modelling view.
   */
  public List<OpenTCSDrawingView> getOperatingDrawingViews() {
    return getDrawingViewMap().entrySet().stream()
        .map(entry -> entry.getValue().getDrawingView())
        .collect(Collectors.toList());
  }

  public int getNextTransportOrderViewIndex() {
    return nextAvailableIndex(transportOrderViews.keySet());
  }

  public int getNextOrderSequenceViewIndex() {
    return nextAvailableIndex(orderSequenceViews.keySet());
  }

  public int getNextPeripheralJobViewIndex() {
    return nextAvailableIndex(peripheralJobViews.keySet());
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

  public DefaultSingleCDockable getLastPeripheralJobView() {
    int biggestIndex = getNextPeripheralJobViewIndex();
    DefaultSingleCDockable lastView = null;
    Iterator<DefaultSingleCDockable> peripheralJobViewIterator
        = peripheralJobViews.keySet().iterator();
    for (int i = 0; i < biggestIndex; i++) {
      if (peripheralJobViewIterator.hasNext()) {
        lastView = peripheralJobViewIterator.next();
      }
    }

    return lastView;
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

    orderSequenceViews.put(dockable, panel);
  }

  /**
   * Puts a <code>OrderSequencesContainerPanel</code> with a key dockable
   * into the order sequence view map.
   *
   * @param dockable The dockable the panel is wrapped into. Used as the key.
   * @param panel The panel.
   */
  public void addPeripheralJobView(DefaultSingleCDockable dockable,
                                   PeripheralJobsContainerPanel panel) {
    requireNonNull(dockable, "dockable");
    requireNonNull(panel, "panel");

    peripheralJobViews.put(dockable, panel);
  }

  /**
   * Forgets the given dockable.
   *
   * @param dockable The dockable.
   */
  @Override
  public void removeDockable(DefaultSingleCDockable dockable) {
    super.removeDockable(dockable);

    transportOrderViews.remove(dockable);
    orderSequenceViews.remove(dockable);
    peripheralJobViews.remove(dockable);
  }

  /**
   * Evaluates which dockable should be the front dockable.
   *
   * @return The dockable that should be the front dockable. <code>null</code>
   * if no dockables exist.
   */
  @Override
  public DefaultCommonDockable evaluateFrontDockable() {
    if (!getDrawingViewMap().isEmpty()) {
      return getDrawingViewMap().keySet().iterator().next().intern();
    }
    if (!transportOrderViews.isEmpty()) {
      return transportOrderViews.keySet().iterator().next().intern();
    }
    if (!orderSequenceViews.isEmpty()) {
      return orderSequenceViews.keySet().iterator().next().intern();
    }
    if (!peripheralJobViews.isEmpty()) {
      return peripheralJobViews.keySet().iterator().next().intern();
    }
    return null;
  }

  /**
   * Sets visibility states of all dockables to operating.
   */
  private void setPlantOverviewStateOperating() {
    CStackDockStation station
        = dockingManager.getTabPane(DockingManagerOperating.COURSE_TAB_PANE_ID).getStation();
    Dockable frontDock = station.getFrontDockable();
    int i = 0;
    for (DefaultSingleCDockable dock : new ArrayList<>(getDrawingViewMap().keySet())) {
      // Restore to default
      dock.setCloseable(true);
      dockingManager.showDockable(station, dock, i);
      i++;
    }
    i = getDrawingViewMap().size();
    for (DefaultSingleCDockable dock : new ArrayList<>(getDrawingViewMap().keySet())) {
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

    for (DefaultSingleCDockable dock : new ArrayList<>(peripheralJobViews.keySet())) {
      dockingManager.showDockable(station, dock, i);
      i++;
    }

    if (frontDock != null && frontDock.isDockableShowing()) {
      station.setFrontDockable(frontDock);
    }
    else {
      station.setFrontDockable(station.getDockable(0));
    }
    dockingManager.setDockableVisibility(DockingManagerOperating.VEHICLES_DOCKABLE_ID, true);
  }
}
