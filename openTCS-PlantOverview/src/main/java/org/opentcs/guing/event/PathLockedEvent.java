/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.event;

import java.util.EventObject;
import org.opentcs.guing.components.drawing.figures.PathConnection;

/**
 * An event that a path has been locked.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
public class PathLockedEvent
    extends EventObject {

  /**
   * Creates a new instance of PathLockedEvent.
   *
   * @param source The <code>PathConnection</code> that has been locked.
   */
  public PathLockedEvent(PathConnection source) {
    super(source);
  }
}
