/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.components.properties.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.guing.base.model.ModelComponent;

/**
 * A property that holds a list of sets of {@link TCSResourceReference}s.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 */
public class ResourceProperty
    extends AbstractComplexProperty {

  /**
   * A list of of sets {@link TCSResourceReference}s.
   */
  private List<Set<TCSResourceReference<?>>> items = new ArrayList<>();

  /**
   * Creates a new ResourceProperty instance.
   *
   * @param model The model component this property belongs to.
   */
  public ResourceProperty(ModelComponent model) {
    super(model);
  }

  @Override
  public Object getComparableValue() {
    return items.stream()
        .flatMap(set -> set.stream())
        .map(r -> r.getName())
        .collect(Collectors.joining());
  }

  /**
   * Returns a list of all items of this property.
   *
   * @return a list of all items of this property.
   */
  public List<Set<TCSResourceReference<?>>> getItems() {
    return items;
  }

  /**
   * Sets the list.
   *
   * @param items the list of items for this property.
   */
  public void setItems(List<Set<TCSResourceReference<?>>> items) {
    this.items = items;
  }

  /**
   * Adds one item to the list.
   *
   * @param item the item to add.
   */
  public void addItem(Set<TCSResourceReference<?>> item) {
    items.add(item);
  }

  /**
   * Removes one item from the list.
   *
   * @param item the item to remove.
   */
  public void removeItem(Set<TCSResourceReference<?>> item) {
    items.remove(item);
  }

  @Override
  public void copyFrom(Property property) {
    ResourceProperty other = (ResourceProperty) property;
    List<Set<TCSResourceReference<?>>> items = new ArrayList<>(other.getItems());
    setItems(items);
  }

  @Override
  public String toString() {
    if (fValue != null) {
      return fValue.toString();
    }

    return items.stream()
        .flatMap(set -> set.stream())
        .map(r -> r.getName())
        .collect(Collectors.joining(", "));
  }

  @Override
  public Object clone() {
    ResourceProperty clone = (ResourceProperty) super.clone();
    List<Set<TCSResourceReference<?>>> items = new ArrayList<>(getItems());
    clone.setItems(items);

    return clone;
  }

}
