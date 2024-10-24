// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.components.properties.type;

import java.util.List;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.PointModel.Type;

/**
 * Subclass for a {@link Type} selection property.
 */
public class PointTypeProperty
    extends
      SelectionProperty<Type> {

  public PointTypeProperty(ModelComponent model) {
    super(model);
  }

  public PointTypeProperty(
      ModelComponent model,
      List<Type> possibleValues,
      Object value
  ) {
    super(model, possibleValues, value);
  }
}
