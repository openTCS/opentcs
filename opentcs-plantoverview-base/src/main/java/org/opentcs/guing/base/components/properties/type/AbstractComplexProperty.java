// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.components.properties.type;

import org.opentcs.guing.base.model.ModelComponent;

/**
 * Abstract super class for properties that should get their own details dialog
 * for editing because editing in simple text fields or combo boxes is hard to
 * realize or not comfortable for the user.
 */
public abstract class AbstractComplexProperty
    extends
      AbstractProperty {

  /**
   * Creates a new instance.
   *
   * @param model The model component this property belongs to.
   */
  public AbstractComplexProperty(ModelComponent model) {
    super(model);
  }
}
