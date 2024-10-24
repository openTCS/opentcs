// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.components.properties.type;

import org.opentcs.guing.base.model.ModelComponent;

/**
 * A property for location actions.
 */
public class LocationTypeActionsProperty
    extends
      StringSetProperty {

  /**
   * Creates a new instance.
   *
   * @param model The model component this property belongs to.
   */
  public LocationTypeActionsProperty(ModelComponent model) {
    super(model);
  }
}
