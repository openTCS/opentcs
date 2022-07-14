/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.drawing.course;

/**
 * An interface for drawing methods. Possible drawing methods are:
 * <p>
 * <ul> <li> symbolic: No relation between the real position and the position of the figure.
 * <li> coordinate based: The position of the figure is the exact real position. </ul>
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface DrawingMethod {

  /**
   * Returns the origin point.
   *
   * @return the origin point.
   */
  Origin getOrigin();
}
