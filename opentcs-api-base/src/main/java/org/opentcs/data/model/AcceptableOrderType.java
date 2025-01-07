// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data.model;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * Acceptable order type with priority.
 */
public class AcceptableOrderType
    implements
      Serializable {

  private final String name;
  private final int priority;

  /**
   * Creates a new vehicle.
   *
   * @param name The name of the order type.
   * @param priority Priority of the order type, with a lower value indicating a higher priority.
   */
  public AcceptableOrderType(String name, int priority) {
    this.name = requireNonNull(name, "name");
    this.priority = priority;
  }

  /**
   * Returns the name of the order type.
   *
   * @return The name of the order type.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the priority of the order type, with a lower value indicating a higher priority.
   *
   * @return The priority of the order type.
   */
  public int getPriority() {
    return priority;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof AcceptableOrderType other)) {
      return false;
    }

    return Objects.equals(name, other.getName()) && priority == other.getPriority();
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, priority);
  }

  @Override
  public String toString() {
    return "AcceptableOrderType{"
        + "name=" + name
        + ", priority=" + priority
        + '}';
  }

}
