/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.properties.type;

import org.opentcs.guing.model.ModelComponent;

/**
 * Abstract super class for properties that should get their own details dialog
 * for editing because editing in simple text fields or combo boxes is hard to
 * realize or not comfortable for the user.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public abstract class AbstractComplexProperty
    extends AbstractProperty {

  /**
   * Creates a new instance.
   *
   * @param model The model component this property belongs to.
   */
  public AbstractComplexProperty(ModelComponent model) {
    super(model);
  }
}
