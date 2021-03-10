/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.properties.type;

import java.util.List;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.BlockModel.Type;

/**
 * Subclass for a {@link Type} selection property.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class BlockTypeProperty
    extends SelectionProperty<Type> {

  public BlockTypeProperty(ModelComponent model,
                           List<Type> possibleValues,
                           Object value) {
    super(model, possibleValues, value);
  }
}
