/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model.visualization;

import java.io.Serializable;
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
}
