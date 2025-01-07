// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.model;

import static java.util.Objects.requireNonNull;

import org.opentcs.data.model.AcceptableOrderType;

/**
 * A representation of an {@link AcceptableOrderType}.
 */
public class AcceptableOrderTypeModel {

  private final String name;
  private final int priority;

  /**
   * Creates a new instance.
   *
   * @param name The name of the order type.
   * @param priority Priority of the order type.
   */
  public AcceptableOrderTypeModel(String name, int priority) {
    this.name = requireNonNull(name, "name");
    this.priority = priority;
  }

  public String getName() {
    return name;
  }

  public int getPriority() {
    return priority;
  }
}
