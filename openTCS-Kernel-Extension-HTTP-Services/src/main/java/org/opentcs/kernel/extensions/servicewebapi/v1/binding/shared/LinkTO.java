/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared;

import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;

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

  public LinkTO setPointName(@Nonnull String pointName) {
    this.pointName = requireNonNull(pointName, "pointName");
    return this;
  }

  @Nonnull
  public Set<String> getAllowedOperations() {
    return allowedOperations;
  }

  public LinkTO setAllowedOperations(@Nonnull Set<String> allowedOperations) {
    this.allowedOperations = requireNonNull(allowedOperations, "allowedOperations");
    return this;
  }

}
