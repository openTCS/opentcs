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
import java.util.Objects;
import org.opentcs.guing.base.model.ModelComponent;

/**
 * A property whose value is one out of a list of possible values.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @param <E> The type of the enum.
 */
public class SelectionProperty<E extends Enum<E>>
    extends AbstractProperty
    implements Selectable<E> {

  /**
   * The possible values.
   */
  private List<E> fPossibleValues;

  /**
   * Creates a new instance.
   *
   * @param model The model component.
   */
  public SelectionProperty(ModelComponent model) {
    this(model, new ArrayList<>(), "");
  }

  /**
   * Creates a new instance.
   *
   * @param model The model Component.
   * @param possibleValues The possible values.
   * @param value The value.
   */
  public SelectionProperty(ModelComponent model, List<E> possibleValues,
                           Object value) {
    super(model);
    setPossibleValues(possibleValues);
    fValue = value;
  }

  @Override
  public Object getComparableValue() {
    return fValue;
  }

  /**
   * Sets the possible values for this property.
   *
   * @param possibleValues A list of possible values.
   */
  @Override
  public void setPossibleValues(List<E> possibleValues) {
    fPossibleValues = Objects.requireNonNull(possibleValues, "possibleValues is null");
  }

  @Override
  public void setValue(Object value) {
    if (fPossibleValues.contains(value)
        || value instanceof AcceptableInvalidValue) {
      fValue = value;
    }
  }

  @Override
  public String toString() {
    return getValue().toString();
  }

  @Override
  public List<E> getPossibleValues() {
    return fPossibleValues;
  }

  @Override
  public void copyFrom(Property property) {
    AbstractProperty selectionProperty = (AbstractProperty) property;
    setValue(selectionProperty.getValue());
  }
}
