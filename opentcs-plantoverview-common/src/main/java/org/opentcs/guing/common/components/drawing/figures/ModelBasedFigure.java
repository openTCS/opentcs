// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.drawing.figures;

import org.jhotdraw.draw.Figure;
import org.opentcs.guing.base.model.DrawnModelComponent;
import org.opentcs.guing.base.model.ModelComponent;

/**
 * A figure that is based on/is a graphical representation for a {@link ModelComponent}.
 */
public interface ModelBasedFigure
    extends
      Figure {

  /**
   * Returns the model component for this figure.
   *
   * @return The model component for this figure.
   */
  DrawnModelComponent getModel();
}
