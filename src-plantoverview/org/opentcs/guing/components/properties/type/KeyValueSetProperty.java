/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.properties.type;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.opentcs.guing.model.ModelComponent;

/**
 * An attribute which contains a quantity of key-value pairs.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
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

    StringBuilder sb = new StringBuilder();
    Iterator<KeyValueProperty> iItems = getItems().iterator();

    while (iItems.hasNext()) {
      sb.append(iItems.next());

      if (iItems.hasNext()) {
        sb.append(", ");
      }
    }

    return sb.toString();
  }

  @Override
  public Object clone() {
    KeyValueSetProperty clone = (KeyValueSetProperty) super.clone();
    List<KeyValueProperty> items = new ArrayList<>(getItems());
    clone.setItems(items);

    return clone;
  }
}
