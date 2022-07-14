/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.application;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.intern.DefaultCommonDockable;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.opentcs.guing.common.components.dockable.DockableTitleComparator;
import org.opentcs.guing.common.components.drawing.DrawingViewScrollPane;
import org.opentcs.util.event.EventSource;

/**
 * Manages the mapping of dockables to drawing views, transport order views and
 * order sequence views.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public abstract class AbstractViewManager
    implements ViewManager {

  /**
   * Where we register event listeners.
   */
  private final EventSource eventSource;
  /**
   * Map for Dockable -> DrawingView + Rulers.
   */
  private final Map<DefaultSingleCDockable, DrawingViewScrollPane> drawingViewMap;

  /**
   * Creates a new instance.
   *
   * @param eventSource Where this instance registers event listeners.
   */
  public AbstractViewManager(EventSource eventSource) {
    this.eventSource = requireNonNull(eventSource, "eventSource");
    drawingViewMap = new TreeMap<>(new DockableTitleComparator());
  }

  @Override
  public Map<DefaultSingleCDockable, DrawingViewScrollPane> getDrawingViewMap() {
    return drawingViewMap;
  }

  /**
   * Returns the title texts of all drawing views.
   *
   * @return List of strings containing the names.
   */
  @Override
  public List<String> getDrawingViewNames() {
    return drawingViewMap.keySet().stream()
        .map(dock -> dock.getTitleText())
        .sorted()
        .collect(Collectors.toList());
  }

  /**
   * Forgets the given dockable.
   *
   * @param dockable The dockable.
   */
  @Override
  public void removeDockable(DefaultSingleCDockable dockable) {
    DrawingViewScrollPane scrollPane = drawingViewMap.remove(dockable);
    if (scrollPane != null) {
      eventSource.unsubscribe(scrollPane.getDrawingView());
    }
  }

  /**
   * Resets all components.
   */
  public void reset() {
    drawingViewMap.clear();
  }

  public int getNextDrawingViewIndex() {
    return nextAvailableIndex(drawingViewMap.keySet());
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
   * Evaluates which dockable should be the front dockable.
   *
   * @return The dockable that should be the front dockable. <code>null</code>
   * if no dockables exist.
   */
  public DefaultCommonDockable evaluateFrontDockable() {
    if (!drawingViewMap.isEmpty()) {
      return drawingViewMap.keySet().iterator().next().intern();
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
  protected int nextAvailableIndex(Set<DefaultSingleCDockable> setToIterate) {
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
}
