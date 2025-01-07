// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.components.properties.type;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.opentcs.guing.base.model.AcceptableOrderTypeModel;
import org.opentcs.guing.base.model.ModelComponent;

/**
 * A property that contains a set of acceptable order types with their priority.
 */
public class OrderTypesProperty
    extends
      AbstractComplexProperty {

  /**
   * The set of order types.
   */
  private Set<AcceptableOrderTypeModel> fItems = new TreeSet<>(
      Comparator.comparingInt(AcceptableOrderTypeModel::getPriority)
          .thenComparing(AcceptableOrderTypeModel::getName)
  );

  /**
   * Creates a new instance.
   *
   * @param model The model component this property belongs to.
   */
  public OrderTypesProperty(ModelComponent model) {
    super(model);
  }

  @Override
  public Object getComparableValue() {
    StringBuilder sb = new StringBuilder();

    for (AcceptableOrderTypeModel s : fItems) {
      sb.append(s);
    }

    return sb.toString();
  }

  /**
   * Adds an acceptable order type.
   *
   * @param item The acceptable order type to add.
   */
  public void addItem(AcceptableOrderTypeModel item) {
    fItems.add(item);
  }

  /**
   * Sets the list of acceptable order types.
   *
   * @param items The list.
   */
  public void setItems(Collection<AcceptableOrderTypeModel> items) {
    fItems.clear();
    fItems.addAll(items);
  }

  /**
   * Returns the list of acceptable order types.
   *
   * @return The list.
   */
  public Set<AcceptableOrderTypeModel> getItems() {
    return fItems;
  }

  @Override
  public void copyFrom(Property property) {
    OrderTypesProperty other = (OrderTypesProperty) property;
    Set<AcceptableOrderTypeModel> items = new TreeSet<>(other.getItems());
    setItems(items);
  }

  @Override
  public String toString() {
    return getItems().stream()
        .map(type -> "(%s, %d)".formatted(type.getName(), type.getPriority()))
        .collect(Collectors.joining(", "));
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object clone() {
    OrderTypesProperty clone = (OrderTypesProperty) super.clone();
    clone.fItems
        = (TreeSet<AcceptableOrderTypeModel>) ((TreeSet<AcceptableOrderTypeModel>) fItems).clone();
    return clone;
  }

  @Override
  public boolean isPersistent() {
    return false;
  }
}
