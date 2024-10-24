// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.drawing.figures;

import org.jhotdraw.draw.AttributeKey;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.common.components.drawing.course.Origin;

/**
 * Constants that are relevant to figures.
 */
public interface FigureConstants {

  /**
   * Key for figures to access their models.
   */
  AttributeKey<ModelComponent> MODEL = new AttributeKey<>("Model", ModelComponent.class);
  /**
   * Key for figures to access the origin.
   */
  AttributeKey<Origin> ORIGIN = new AttributeKey<>("Origin", Origin.class);
}
