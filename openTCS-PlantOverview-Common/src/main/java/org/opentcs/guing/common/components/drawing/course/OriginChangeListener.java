/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.drawing.course;

import java.util.EventObject;

/**
 * Interface for classes that want to be notified about origin position changes
 * and origin scale changes.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface OriginChangeListener {

  /**
   * Event that the position of the origin has changed.
   *
   * @param evt event that the position has changed.
   */
  void originLocationChanged(EventObject evt);

  /**
   * Event that the scale of the origin has changed.
   *
   * @param evt event that the scale of the origin has changed.
   */
  void originScaleChanged(EventObject evt);
}
