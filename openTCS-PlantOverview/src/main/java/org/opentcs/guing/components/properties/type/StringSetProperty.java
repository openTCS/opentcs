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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.opentcs.guing.model.ModelComponent;

/**
 * A property that contains a quantity of strings.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class StringSetProperty
    extends AbstractComplexProperty {

  /**
   * The strings.
   */
  private List<String> fItems = new LinkedList<>();

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
    List<String> items = new LinkedList<>(other.getItems());
    setItems(items);
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
    List<String> items = new LinkedList<>(getItems());
    clone.setItems(items);

    return clone;
  }
}
