/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.drawing.figures;

import org.jhotdraw.draw.Figure;
import org.opentcs.guing.base.model.DrawnModelComponent;
import org.opentcs.guing.base.model.ModelComponent;

/**
 * A figure that is based on/is a graphical representation for a {@link ModelComponent}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface ModelBasedFigure
    extends Figure {

  /**
   * Returns the model component for this figure.
   *
   * @return The model component for this figure.
   */
  public DrawnModelComponent getModel();
}
