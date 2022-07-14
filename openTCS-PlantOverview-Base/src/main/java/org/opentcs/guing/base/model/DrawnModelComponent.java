/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.model;

import org.opentcs.guing.base.components.properties.type.LayerWrapperProperty;

/**
 * A model component that is drawn and represented by a corresponding figure.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface DrawnModelComponent
    extends ModelComponent {

  /**
   * The property key for the layer wrapper that contains the layer on which a model component 
   * (respectively its figure) is to be drawn.
   */
  String LAYER_WRAPPER = "LAYER_WRAPPER";

  /**
   * Returns this component's layer wrapper property.
   *
   * @return This component's layer wrapper property.
   */
  default LayerWrapperProperty getPropertyLayerWrapper() {
    return (LayerWrapperProperty) getProperty(LAYER_WRAPPER);
  }
}
