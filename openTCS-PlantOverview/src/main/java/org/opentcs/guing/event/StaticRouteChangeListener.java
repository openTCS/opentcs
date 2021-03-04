/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.event;

/**
 * Interface for listeners that want to be informed about changes
 * on a static route.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface StaticRouteChangeListener {

  /**
   * Informs the listeners that the static route components have changed.
   *
   * @param e The fired event.
   */
  void pointsChanged(StaticRouteChangeEvent e);

  /**
   * Informs the listeners that the color of the route has changed.
   *
   * @param e The fired event.
   */
  void colorChanged(StaticRouteChangeEvent e);

  /**
   * Informs the listeners that a static route has been removed.
   *
   * @param e The fired event.
   */
  void staticRouteRemoved(StaticRouteChangeEvent e);
}
