// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.components.properties.type;

import org.opentcs.guing.base.model.ModelComponent;

/**
 * A property for link actions.
 */
public class LinkActionsProperty
    extends
      StringSetProperty {

  /**
   * Creates a new instance.
   *
   * @param model The model component this property belongs to.
   */
  public LinkActionsProperty(ModelComponent model) {
    super(model);
  }
}
