/*
 * openTCS copyright information:
 * Copyright (c) 2017 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.util;

import java.util.Comparator;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.persistence.CourseObjectProperty;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Comparators {

  private static final Comparator<? super ModelComponent> MODEL_COMPONENTS_BY_NAME
      = (o1, o2) -> o1.getName().compareTo(o2.getName());

  private static final Comparator<? super CourseObjectProperty> PROPERTIES_BY_NAME
      = (o1, o2) -> o1.getKey().compareTo(o2.getKey());

  private Comparators() {
  }

  public static Comparator<? super ModelComponent> modelComponentsByName() {
    return MODEL_COMPONENTS_BY_NAME;
  }

  public static Comparator<? super CourseObjectProperty> courseObjectPropertiesByName() {
    return PROPERTIES_BY_NAME;
  }
}
