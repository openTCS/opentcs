// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.getvehicleroutes;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.VehicleTO;

/**
 * The web API representation of a route.
 */
public class RouteTO {

  private String destinationPoint = "";
  private long costs = -1;
  private List<Step> steps;

  public RouteTO() {
  }

  @Nonnull
  public String getDestinationPoint() {
    return destinationPoint;
  }

  public RouteTO setDestinationPoint(
      @Nonnull
      String destinationPoint
  ) {
    this.destinationPoint = requireNonNull(destinationPoint, "destinationPoint");
    return this;
  }

  public long getCosts() {
    return costs;
  }

  public RouteTO setCosts(long costs) {
    this.costs = costs;
    return this;
  }

  public List<Step> getSteps() {
    return steps;
  }

  public RouteTO setSteps(List<Step> steps) {
    this.steps = steps;
    return this;
  }

  public static class Step {

    private String path;
    private String sourcePoint;
    private String destinationPoint = "";
    private String vehicleOrientation = VehicleTO.Orientation.UNDEFINED.name();

    public Step() {
    }

    @Nullable
    public String getPath() {
      return path;
    }

    public Step setPath(String path) {
      this.path = path;
      return this;
    }

    @Nullable
    public String getSourcePoint() {
      return sourcePoint;
    }

    public Step setSourcePoint(String sourcePoint) {
      this.sourcePoint = sourcePoint;
      return this;
    }

    @Nonnull
    public String getDestinationPoint() {
      return destinationPoint;
    }

    public Step setDestinationPoint(
        @Nonnull
        String destinationPoint
    ) {
      this.destinationPoint = requireNonNull(destinationPoint, "destinationPoint");
      return this;
    }

    @Nonnull
    public String getVehicleOrientation() {
      return vehicleOrientation;
    }

    public Step setVehicleOrientation(
        @Nonnull
        String vehicleOrientation
    ) {
      this.vehicleOrientation = requireNonNull(vehicleOrientation, "vehicleOrientation");
      return this;
    }
  }
}
