// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.drawing.course;

/**
 * An interface for drawing methods. Possible drawing methods are:
 * <p>
 * <ul> <li> symbolic: No relation between the real position and the position of the figure.
 * <li> coordinate based: The position of the figure is the exact real position. </ul>
 */
public interface DrawingMethod {

  /**
   * Returns the origin point.
   *
   * @return the origin point.
   */
  Origin getOrigin();
}
