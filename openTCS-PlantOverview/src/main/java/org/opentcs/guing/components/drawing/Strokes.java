/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.drawing;

import java.awt.BasicStroke;
import java.awt.Stroke;

/**
 * Strokes used in the drawing.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Strokes {

  /**
   * Prevents instantiation of this utility class.
   */
  private Strokes() {
  }

  /**
   * Decoration of paths, points and locations that are part of a block.
   */
  public static final Stroke BLOCK_ELEMENT = new BasicStroke(4.0f);
  /**
   * Decoration of paths that are part of a transport order.
   */
  public static final Stroke PATH_ON_ROUTE
      = new BasicStroke(6.0f,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        10.0f,
                        new float[] {10.0f, 5.0f},
                        0.0f);
  /**
   * Decoration of paths that are part of a withdrawn transport order.
   */
  public static final Stroke PATH_ON_WITHDRAWN_ROUTE
      = new BasicStroke(6.0f,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        10.0f,
                        new float[] {8.0f, 4.0f, 2.0f, 4.0f},
                        0.0f);
}
