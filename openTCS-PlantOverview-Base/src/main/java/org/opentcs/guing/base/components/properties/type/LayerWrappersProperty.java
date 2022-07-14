/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.components.properties.type;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.opentcs.guing.base.components.layer.LayerWrapper;
import org.opentcs.guing.base.model.ModelComponent;

/**
 * A property that contains a list of layer wrappers.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LayerWrappersProperty
    extends AbstractComplexProperty {

  /**
   * Creates a new instance.
   *
   * @param model The model component this property belongs to.
   * @param layerWrappers The layer wrappers.
   */
  public LayerWrappersProperty(ModelComponent model, Map<Integer, LayerWrapper> layerWrappers) {
    super(model);
    fValue = layerWrappers;
  }

  @Override
  public Object getComparableValue() {
    return this.toString();
  }

  @Override
  public String toString() {
    return getValue().values().stream()
        .sorted(Comparator.comparing(wrapper -> wrapper.getLayer().getId()))
        .map(wrapper -> wrapper.getLayer().getName())
        .collect(Collectors.joining(", "));
  }

  @Override
  public void copyFrom(Property property) {
    LayerWrappersProperty other = (LayerWrappersProperty) property;
    Map<Integer, LayerWrapper> items = new HashMap<>(other.getValue());
    setValue(items);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<Integer, LayerWrapper> getValue() {
    return (Map) super.getValue();
  }
}
