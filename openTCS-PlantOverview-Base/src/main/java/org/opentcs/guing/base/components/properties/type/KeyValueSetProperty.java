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
import java.util.stream.Collectors;
import org.opentcs.guing.base.model.ModelComponent;

/**
 * An attribute which contains a quantity of key-value pairs.
 */
public class KeyValueSetProperty
    extends AbstractComplexProperty {

  /**
   * The quantity of key-value-pairs.
   */
  private List<KeyValueProperty> fItems = new ArrayList<>();

  /**
   * Creates a new instance.
   *
   * @param model The model component this property belongs to.
   */
  public KeyValueSetProperty(ModelComponent model) {
    super(model);
  }

  @Override
  public Object getComparableValue() {
    StringBuilder sb = new StringBuilder();

    for (KeyValueProperty property : fItems) {
      sb.append(property.getKey()).append(property.getValue());
    }

    return sb.toString();
  }

  /**
   * Adds a property.
   *
   * @param item The property to add.
   */
  public void addItem(KeyValueProperty item) {
    for (KeyValueProperty property : fItems) {
      if (item.getKey().equals(property.getKey())) {
        property.setKeyAndValue(property.getKey(), item.getValue());
        return;
      }
    }

    fItems.add(item);
  }

  /**
   * Removes a property.
   *
   * @param item The property to remove.
   */
  public void removeItem(KeyValueProperty item) {
    fItems.remove(item);
  }

  /**
   * Sets the list with key-value-pairs..
   *
   * @param items The values.
   */
  public void setItems(List<KeyValueProperty> items) {
    fItems = items;
  }

  /**
   * Returns all key-value-pairs.
   *
   * @return The properties.
   */
  public List<KeyValueProperty> getItems() {
    return fItems;
  }

  @Override
  public void copyFrom(Property property) {
    KeyValueSetProperty other = (KeyValueSetProperty) property;
    List<KeyValueProperty> items = new ArrayList<>(other.getItems());
    setItems(items);
  }

  @Override
  public String toString() {
    if (fValue != null) {
      return fValue.toString();
    }

    return getItems().stream()
        .sorted((i1, i2) -> i1.getKey().compareTo(i2.getKey()))
        .map(item -> item.toString())
        .collect(Collectors.joining(", "));
  }

  @Override
  public Object clone() {
    KeyValueSetProperty clone = (KeyValueSetProperty) super.clone();
    List<KeyValueProperty> items = new ArrayList<>(getItems());
    clone.setItems(items);

    return clone;
  }
}
