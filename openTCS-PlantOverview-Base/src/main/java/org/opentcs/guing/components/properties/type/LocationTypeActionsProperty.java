/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.properties.type;

import org.opentcs.guing.model.ModelComponent;

/**
 * A property for location actions.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LocationTypeActionsProperty
    extends StringSetProperty {

  /**
   * Creates a new instance.
   *
   * @param model The model component this property belongs to.
   */
  public LocationTypeActionsProperty(ModelComponent model) {
    super(model);
  }
}
