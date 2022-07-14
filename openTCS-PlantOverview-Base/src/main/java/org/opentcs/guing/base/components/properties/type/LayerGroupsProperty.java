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
import org.opentcs.data.model.visualization.LayerGroup;
import org.opentcs.guing.base.model.ModelComponent;

/**
 * A property that contains a list of layer groups.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LayerGroupsProperty
    extends AbstractComplexProperty {

  /**
   * Creates a new instance.
   *
   * @param model The model component this property belongs to.
   * @param layerGroups The layer groups.
   */
  public LayerGroupsProperty(ModelComponent model, Map<Integer, LayerGroup> layerGroups) {
    super(model);
    fValue = layerGroups;
  }

  @Override
  public Object getComparableValue() {
    return this.toString();
  }

  @Override
  public String toString() {
    return getValue().values().stream()
        .sorted(Comparator.comparing(group -> group.getId()))
        .map(group -> group.getName())
        .collect(Collectors.joining(", "));
  }

  @Override
  public void copyFrom(Property property) {
    LayerGroupsProperty other = (LayerGroupsProperty) property;
    Map<Integer, LayerGroup> items = new HashMap<>(other.getValue());
    setValue(items);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<Integer, LayerGroup> getValue() {
    return (Map) super.getValue();
  }
}
