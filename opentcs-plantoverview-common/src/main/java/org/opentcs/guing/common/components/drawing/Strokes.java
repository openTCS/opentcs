// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.drawing;

import java.awt.BasicStroke;
import java.awt.Stroke;

/**
 * Strokes used in the drawing.
 */
public class Strokes {

  /**
   * Decoration of paths, points and locations that are part of a block.
   */
  public static final Stroke BLOCK_ELEMENT = new BasicStroke(4.0f);
  /**
   * Decoration of envelopes .
   */
  public static final Stroke ENVELOPES
      = new BasicStroke(
          2.0f,
          BasicStroke.CAP_BUTT,
          BasicStroke.JOIN_MITER,
          10.0f,
          new float[]{2.0f, 2.0f},
          0.0f
      );
  /**
   * Decoration of paths that are part of a transport order.
   */
  public static final Stroke PATH_ON_ROUTE
      = new BasicStroke(
          6.0f,
          BasicStroke.CAP_BUTT,
          BasicStroke.JOIN_MITER,
          10.0f,
          new float[]{10.0f, 5.0f},
          0.0f
      );
  /**
   * Decoration of paths that are part of a withdrawn transport order.
   */
  public static final Stroke PATH_ON_WITHDRAWN_ROUTE
      = new BasicStroke(
          6.0f,
          BasicStroke.CAP_BUTT,
          BasicStroke.JOIN_MITER,
          10.0f,
          new float[]{8.0f, 4.0f, 2.0f, 4.0f},
          0.0f
      );

  /**
   * Prevents instantiation of this utility class.
   */
  private Strokes() {
  }

}
