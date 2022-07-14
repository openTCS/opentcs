/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.util;

import java.util.Comparator;
import org.opentcs.guing.base.model.ModelComponent;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Comparators {

  private static final Comparator<? super ModelComponent> MODEL_COMPONENTS_BY_NAME
      = (o1, o2) -> o1.getName().compareTo(o2.getName());

  private Comparators() {
  }

  public static Comparator<? super ModelComponent> modelComponentsByName() {
    return MODEL_COMPONENTS_BY_NAME;
  }
}
