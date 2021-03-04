/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2017 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.properties.type;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import org.opentcs.guing.model.ModelComponent;

/**
 * A property that contains a set of transport order categories represented by strings.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class OrderCategoriesProperty
    extends AbstractComplexProperty {

  /**
   * The set of transport order categories.
   */
  private Set<String> fItems = new TreeSet<>();

  /**
   * Creates a new instance.
   * 
   * @param model The model component this property belongs to.
   */
  public OrderCategoriesProperty(ModelComponent model) {
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
  public void setItems(Set<String> items) {
    fItems = items;
  }

  /**
   * Returns the list of string.
   *
   * @return The list.
   */
  public Set<String> getItems() {
    return fItems;
  }
  
  @Override
  public void copyFrom(Property property) {
    OrderCategoriesProperty other = (OrderCategoriesProperty) property;
    Set<String> items = new TreeSet<>(other.getItems());
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
    OrderCategoriesProperty clone = (OrderCategoriesProperty) super.clone();
    Set<String> items = new TreeSet<>(getItems());
    clone.setItems(items);
    return clone;
  }

  @Override
  public boolean isPersistent() {
    return false;
  }
}
