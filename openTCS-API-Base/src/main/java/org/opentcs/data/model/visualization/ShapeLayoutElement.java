/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model.visualization;

import java.io.Serializable;

/**
 * A layout element describing a geometrical shape to be displayed.
 * <p>
 * This class is simply a marker class for identifying shape elements. The
 * actual description of the geometrical shape is contained in the element's
 * properties.
 * </p>
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ShapeLayoutElement
    extends LayoutElement
    implements Serializable {

  /**
   * Creates a new ShapeLayoutElement.
   */
  public ShapeLayoutElement() {
    // Do nada.
  }
}
