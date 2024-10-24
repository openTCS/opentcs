// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.drawing.course;

import java.util.EventObject;

/**
 * Interface for classes that want to be notified about origin position changes
 * and origin scale changes.
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
