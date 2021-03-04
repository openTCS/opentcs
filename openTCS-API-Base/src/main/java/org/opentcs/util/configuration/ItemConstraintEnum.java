/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.opentcs.util.Enums;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Item Constraint for Boolean type value.
 *
 * @author Preity Gupta (Fraunhofer IML)
 * @deprecated Use interface bindings and configuration mechanism provided via applications'
 * dependency injection.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public class ItemConstraintEnum
    extends ItemConstraint {

  /**
   * String values for Enum constants.
   */
  private final Set<String> values;

  /**
   * Creates a constraint of type Enum.
   *
   * @param newValues Array of strings containing enum values.
   */
  public ItemConstraintEnum(String[] newValues) {
    super(ConfigurationDataType.ENUM, 0, 0, new HashSet<>(Arrays.asList(newValues)));
    values = new HashSet<>(Arrays.asList(newValues));
  }

  /**
   * Creates a constraint of type Enum.
   *
   * @param newValues Set of strings containing enum values.
   */
  public ItemConstraintEnum(Set<String> newValues) {
    super(ConfigurationDataType.ENUM, 0, 0, newValues);
    values = newValues;
  }

  /**
   * Creates a constraint of type Enum.
   *
   * @param enumClass Enum class containing enum values.
   */
  public ItemConstraintEnum(Class<? extends Enum<?>> enumClass) {
    super(ConfigurationDataType.ENUM, 0, 0, Enums.asStringSet(enumClass));
    values = Enums.asStringSet(enumClass);
  }

  @Override
  public boolean accepts(String value) {
    return values.contains(value);

  }
}
