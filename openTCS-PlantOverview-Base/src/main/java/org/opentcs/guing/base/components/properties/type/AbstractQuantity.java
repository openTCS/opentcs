/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.components.properties.type;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import org.opentcs.guing.base.model.ModelComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation for properties having a value and a unit.
 * (Examples: 1 s, 200 m, 30 m/s.)
 * Also specifies conversion relations (see {@link Relation}) between units that allow conversion
 * to other units.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 * @param <U> The enum type.
 */
public abstract class AbstractQuantity<U extends Enum<U>>
    extends AbstractProperty {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AbstractQuantity.class);
  /**
   * The unit's enum class;
   */
  private final Class<U> fUnitClass;
  /**
   * List of possible units.
   */
  private final List<U> fPossibleUnits;
  /**
   * List of relations between different units.
   */
  private final List<Relation<U>> fRelations;
  /**
   * Current unit.
   */
  private U fUnit;
  /**
   * Whether or not this property is an integer value.
   */
  private boolean fIsInteger;
  /**
   * Whether or not this property is unsigned.
   */
  private boolean fIsUnsigned;
  /**
   * A {@link ValidRangePair} indicating the range of the valid values
   * for this quantity.
   */
  protected ValidRangePair validRange = new ValidRangePair();

  /**
   * Creates a new instance.
   *
   * @param model The model component.
   * @param value The value.
   * @param unit The unit.
   * @param unitClass The unit class.
   * @param relations The relations.
   */
  public AbstractQuantity(ModelComponent model, double value, U unit, Class<U> unitClass,
                          List<Relation<U>> relations) {
    super(model);
    fUnit = requireNonNull(unit, "unit");
    fUnitClass = requireNonNull(unitClass, "unitClass");
    fPossibleUnits = Arrays.asList(unitClass.getEnumConstants());
    fRelations = requireNonNull(relations, "relations");
    fIsInteger = false;
    fIsUnsigned = false;
    initValidRange();
    setValue(value);
  }

  /**
   * Sets the new valid range.
   *
   * @param newRange The new {@link ValidRangePair}.
   */
  public void setValidRangePair(ValidRangePair newRange) {
    validRange = Objects.requireNonNull(newRange, "newRange is null");
  }

  /**
   * Returns the valid range for this quantity.
   *
   * @return The {@link ValidRangePair}.
   */
  public ValidRangePair getValidRange() {
    return validRange;
  }

  /**
   * Initializes the valid range of values.
   */
  protected abstract void initValidRange();

  /**
   * Sets the value of this property to be an integer or decimal number.
   *
   * @param isInteger Whether the value is an integer.
   */
  public void setInteger(boolean isInteger) {
    fIsInteger = isInteger;
  }

  /**
   * Returns true if the value of this property is an integer value.
   *
   * @return Whether the value is an integer.
   */
  public boolean isInteger() {
    return fIsInteger;
  }

  /**
   * Sets the value of this property to be unsigned or not.
   *
   * @param isUnsigned Whether the value is unsigned.
   */
  public void setUnsigned(boolean isUnsigned) {
    this.fIsUnsigned = isUnsigned;
  }

  /**
   * Indicates whether or not the value is unsigned or not.
   *
   * @return Whether the value is unsigned.
   */
  public boolean isUnsigned() {
    return fIsUnsigned;
  }

  @Override
  public Object getValue() {
    try {
      double value = Double.parseDouble(fValue.toString());

      if (isInteger()) {
        return (int) (value + 0.5);
      }
      else {
        return value;
      }
    }
    catch (NumberFormatException nfe) {
      LOG.info("Error parsing value", nfe);
      return fValue;
    }
  }

  /**
   * Returns the value of this property converted to the specified unit.
   *
   * @param unit The unit return.
   * @return The value by the given unit.
   */
  public double getValueByUnit(U unit) {
    try {
      @SuppressWarnings("unchecked")
      AbstractQuantity<U> property = (AbstractQuantity<U>) clone();
      // PercentProperty threw
      // java.lang.ClassCastException: java.lang.Integer cannot be cast to java.lang.Double
      // this is a workaround 12.09.14
      double value;
      if (isInteger()) {
        value = ((Integer) getValue()).doubleValue();
      }
      else {
        value = (double) getValue();
      }
      property.setValueAndUnit(value, getUnit());
      property.convertTo(unit);
      if (isInteger()) {
        value = ((Integer) property.getValue()).doubleValue();
      }
      else {
        value = (double) property.getValue();
      }

      return value;
    }
    catch (IllegalArgumentException e) {
      LOG.error("Exception: ", e);
    }

    return Double.NaN;
  }

  /**
   * Converts the property to the new unit.
   *
   * @param unit The new unit to use.
   */
  public void convertTo(U unit) {
    if (!fPossibleUnits.contains(unit)) {
      return;
    }

    if (fUnit.equals(unit)) {
      return;
    }

    int indexUnitA = fPossibleUnits.indexOf(fUnit);
    int indexUnitB = fPossibleUnits.indexOf(unit);

    int lowerIndex;
    int upperIndex;
    if (indexUnitA < indexUnitB) {
      lowerIndex = indexUnitA;
      upperIndex = indexUnitB;
    }
    else {
      lowerIndex = indexUnitB;
      upperIndex = indexUnitA;
    }

    double relationValue = 1.0;

    for (int i = lowerIndex; i < upperIndex; i++) {
      U unitA = fPossibleUnits.get(i);
      U unitB = fPossibleUnits.get(i + 1);

      Relation<U> relation = findFittingRelation(unitA, unitB);
      relationValue *= relation.relationValue();
    }

    U lowerUnit = fPossibleUnits.get(lowerIndex);
    U upperUnit = fPossibleUnits.get(upperIndex);

    Relation<U> relation = new Relation<>(lowerUnit, upperUnit, relationValue);
    Relation.Operation operation = relation.getOperation(fUnit, unit);

    fUnit = unit;

    switch (operation) {
      case DIVISION:
        setValue((double) fValue / relation.relationValue());
        break;
      case MULTIPLICATION:
        setValue((double) fValue * relation.relationValue());
        break;
      default:
        throw new IllegalArgumentException("Unhandled operation: " + operation);
    }

  }

  /**
   * Returns the current unit for this property.
   *
   * @return The unit.
   */
  public U getUnit() {
    return fUnit;
  }

  /**
   * Checks if this property is applicable to the specified unit.
   *
   * @param unit The unit.
   * @return {@code true}, if the given unit is a valid/possible one, otherwise {@code false}.
   */
  public boolean isPossibleUnit(U unit) {
    return fPossibleUnits.contains(unit);
  }

  /**
   * Checks if the given string is a valid/possible unit.
   *
   * @param unitString The unit as a string.
   * @return {@code true}, if the given string is a valid/possible unit, otherwise {@code false}.
   */
  public boolean isPossibleUnit(String unitString) {
    for (U unit : fPossibleUnits) {
      if (Objects.equals(unitString, unit.toString())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Set the value and unit for this property.
   * {@link IllegalArgumentException} is thrown if the unit is not applicable to this property.
   *
   * @param value The new value.
   * @param unit The new unit.
   * @throws IllegalArgumentException If the given unit is not usable.
   */
  public void setValueAndUnit(double value, U unit)
      throws IllegalArgumentException {
    if (!isPossibleUnit(unit)) {
      throw new IllegalArgumentException(String.format("'%s' is not a valid unit.", unit));
    }
    if (!Double.isNaN(value)) {
      if (fValue instanceof Double) {
        if ((double) fValue != value) {
          markChanged();
        }
      }
      else {
        markChanged();
      }
    }

    fUnit = unit;

    setValue(value);

    if (fIsUnsigned) {
      setValue(Math.abs(value));
    }
  }

  public void setValueAndUnit(double value, String unitString)
      throws IllegalArgumentException {
    requireNonNull(unitString);

    for (U unit : fUnitClass.getEnumConstants()) {
      if (unitString.equals(unit.toString())) {
        setValueAndUnit(value, unit);
        return;
      }
    }
    throw new IllegalArgumentException("Unknown unit \"" + unitString + "\"");
  }

  @Override
  public String toString() {
    if (fValue instanceof Integer) {
      return ((int) fValue) + " " + fUnit;
    }
    else if (fValue instanceof Double) {
      return fValue + " " + fUnit;
    }
    else {
      return fValue.toString();
    }
  }

  /**
   * Returns a list of possible units for this property.
   *
   * @return A list of possible units.
   */
  public List<U> getPossibleUnits() {
    return fPossibleUnits;
  }

  @Override
  public void copyFrom(Property property) {
    @SuppressWarnings("unchecked")
    AbstractQuantity<U> quantity = (AbstractQuantity<U>) property;

    try {
      if (quantity.getValue() instanceof Double) {
        setValueAndUnit((double) quantity.getValue(), quantity.getUnit());
      }
      else if (quantity.getValue() instanceof Integer) {
        setValueAndUnit(((Integer) quantity.getValue()).doubleValue(), quantity.getUnit());
      }
    }
    catch (IllegalArgumentException e) {
      LOG.error("Exception: ", e);
    }
  }

  /**
   * Finds the conversion relation that is applicable for both specified units.
   *
   * @return the conversion relation for the units.
   */
  private Relation<U> findFittingRelation(U unitFrom, U unitTo) {
    for (Relation<U> relation : fRelations) {
      if (relation.fits(unitFrom, unitTo)) {
        return relation;
      }
    }

    return null;
  }

  public class ValidRangePair {

    private double min = Double.NEGATIVE_INFINITY;
    private double max = Double.MAX_VALUE;

    public ValidRangePair() {
    }

    public ValidRangePair(double min, double max) {
      this.min = min;
      this.max = max;
    }

    /**
     * Returns whether the given value is in the valid range.
     *
     * @param value The value to test.
     * @return <code>true</code> if the value is in range, <code>false</code>
     * otherwise.
     */
    public boolean isValueValid(double value) {
      return value >= min && value <= max;
    }

    public double getMin() {
      return min;
    }

    public ValidRangePair setMin(double min) {
      this.min = min;
      return this;
    }

    public double getMax() {
      return max;
    }

    public ValidRangePair setMax(double max) {
      this.max = max;
      return this;
    }
  }
}
