// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.util.Set;

/**
 */
public class LinkTO {

  private String pointName = "";
  private Set<String> allowedOperations = Set.of();

  public LinkTO() {
  }

  @Nonnull
  public String getPointName() {
    return pointName;
  }

  public LinkTO setPointName(
      @Nonnull
      String pointName
  ) {
    this.pointName = requireNonNull(pointName, "pointName");
    return this;
  }

  @Nonnull
  public Set<String> getAllowedOperations() {
    return allowedOperations;
  }

  public LinkTO setAllowedOperations(
      @Nonnull
      Set<String> allowedOperations
  ) {
    this.allowedOperations = requireNonNull(allowedOperations, "allowedOperations");
    return this;
  }

}
