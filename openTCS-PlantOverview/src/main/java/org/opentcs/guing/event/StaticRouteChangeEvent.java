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

import java.util.EventObject;
import org.opentcs.guing.model.elements.StaticRouteModel;

/**
 * An event that informs listener about changes of a static route.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class StaticRouteChangeEvent
    extends EventObject {

  /**
   * Creates a new instance of StaticRouteChangeEvent.
   *
   * @param staticRoute The static route model that has changed.
   */
  public StaticRouteChangeEvent(StaticRouteModel staticRoute) {
    super(staticRoute);
  }
}
