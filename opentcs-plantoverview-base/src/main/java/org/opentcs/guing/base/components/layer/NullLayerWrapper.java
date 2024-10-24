// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.components.layer;

import org.opentcs.data.model.visualization.Layer;
import org.opentcs.data.model.visualization.LayerGroup;

/**
 * A null object for a layer wrapper.
 */
public class NullLayerWrapper
    extends
      LayerWrapper {

  public NullLayerWrapper() {
    super(new Layer(0, 0, true, "null", 0), new LayerGroup(0, "null", true));
  }
}
