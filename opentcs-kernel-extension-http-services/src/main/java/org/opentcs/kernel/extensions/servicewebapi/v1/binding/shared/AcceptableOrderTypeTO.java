// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;

public class AcceptableOrderTypeTO
    implements
      Serializable {

  private final String name;
  private final int priority;

  @JsonCreator
  public AcceptableOrderTypeTO(
      @JsonProperty(value = "name", required = true)
      String name,
      @JsonProperty(value = "priority", required = true)
      int priority
  ) {
    this.name = requireNonNull(name, "name");
    this.priority = priority;
  }

  public String getName() {
    return name;
  }

  public int getPriority() {
    return priority;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof AcceptableOrderTypeTO other)) {
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
    return "AcceptableOrderTypeTO{"
        + "name=" + name
        + ", priority=" + priority
        + '}';
  }

}
