/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.components.layer;

import org.opentcs.data.model.visualization.Layer;
import org.opentcs.data.model.visualization.LayerGroup;

/**
 * A null object for a layer wrapper.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class NullLayerWrapper
    extends LayerWrapper {

  public NullLayerWrapper() {
    super(new Layer(0, 0, true, "null", 0), new LayerGroup(0, "null", true));
  }
}
