/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.components.properties.type;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.opentcs.guing.base.model.ModelComponent;

/**
 * A property that contains a quantity of strings.
 */
public class StringSetProperty
    extends AbstractComplexProperty {

  /**
   * The strings.
   */
  private List<String> fItems = new ArrayList<>();

  /**
   * Creates a new instance.
   *
   * @param model The model component this property belongs to.
   */
  public StringSetProperty(ModelComponent model) {
    super(model);
  }

  @Override
  public Object getComparableValue() {
    StringBuilder sb = new StringBuilder();

    for (String s : fItems) {
      sb.append(s);
    }

    return sb.toString();
  }

  /**
   * Adds a string.
   *
   * @param item The string to add.
   */
  public void addItem(String item) {
    fItems.add(item);
  }

  /**
   * Sets the list of strings.
   *
   * @param items The list.
   */
  public void setItems(List<String> items) {
    fItems = items;
  }

  /**
   * Returns the list of string.
   *
   * @return The list.
   */
  public List<String> getItems() {
    return fItems;
  }

  @Override
  public void copyFrom(Property property) {
    StringSetProperty other = (StringSetProperty) property;
    setItems(new ArrayList<>(other.getItems()));
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    Iterator<String> e = getItems().iterator();

    while (e.hasNext()) {
      b.append(e.next());

      if (e.hasNext()) {
        b.append(", ");
      }
    }

    return b.toString();
  }

  @Override
  public Object clone() {
    StringSetProperty clone = (StringSetProperty) super.clone();
    clone.setItems(new ArrayList<>(getItems()));

    return clone;
  }
}
