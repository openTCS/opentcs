/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model.visualization;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import org.opentcs.data.TCSObjectReference;

/**
 * A layout element describing the way in which a model element is to be
 * displayed.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ModelLayoutElement
    extends LayoutElement
    implements Serializable {

  /**
   * The model object to be visualized.
   */
  private final TCSObjectReference<?> visualizedObject;

  /**
   * Creates a new ModelLayoutElement for the given model element.
   *
   * @param visualizedObject The model element this layout element corresponds
   * to.
   */
  public ModelLayoutElement(TCSObjectReference<?> visualizedObject) {
    this.visualizedObject = Objects.requireNonNull(visualizedObject,
                                                   "visualizedObject is null");
  }

  /**
   * Returns a reference to the object this layout element corresponds to.
   *
   * @return A reference to the object this layout element corresponds to.
   */
  public TCSObjectReference<?> getVisualizedObject() {
    return visualizedObject;
  }

  /**
   * Compares ModelLayoutElements by the names of their visualized objects.
   */
  public static final class ObjectNameComparator
      implements Comparator<ModelLayoutElement> {

    /**
     * Creates a new instance.
     */
    public ObjectNameComparator() {
      // Do nada.
    }

    @Override
    public int compare(ModelLayoutElement o1, ModelLayoutElement o2) {
      return o1.visualizedObject.getName().compareTo(
          o2.visualizedObject.getName());
    }
  }
}
