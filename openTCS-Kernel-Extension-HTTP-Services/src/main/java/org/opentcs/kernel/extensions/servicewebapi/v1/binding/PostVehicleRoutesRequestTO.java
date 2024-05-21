/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 */
public class PostVehicleRoutesRequestTO {

  private String sourcePoint;
  private List<String> destinationPoints;
  private List<String> resourcesToAvoid;

  @JsonCreator
  @SuppressWarnings("checkstyle:LineLength")
  public PostVehicleRoutesRequestTO(
      @Nonnull @JsonProperty(value = "destinationPoints", required = true) List<String> destinationPoints) {
    this.destinationPoints = requireNonNull(destinationPoints, "destinationPoints");
  }

  @Nullable
  public String getSourcePoint() {
    return sourcePoint;
  }

  public PostVehicleRoutesRequestTO setSourcePoint(@Nullable String sourcePoint) {
    this.sourcePoint = sourcePoint;
    return this;
  }

  @Nonnull
  public List<String> getDestinationPoints() {
    return destinationPoints;
  }

  public PostVehicleRoutesRequestTO setDestinationPoints(@Nonnull List<String> destinationPoints) {
    this.destinationPoints = requireNonNull(destinationPoints, "destinationPoints");
    return this;
  }

  @Nullable
  public List<String> getResourcesToAvoid() {
    return resourcesToAvoid;
  }

  public PostVehicleRoutesRequestTO setResourcesToAvoid(@Nullable List<String> resourcesToAvoid) {
    this.resourcesToAvoid = resourcesToAvoid;
    return this;
  }
}
