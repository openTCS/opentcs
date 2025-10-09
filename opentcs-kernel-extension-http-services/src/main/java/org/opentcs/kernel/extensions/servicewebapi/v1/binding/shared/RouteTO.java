// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;

public class RouteTO {
  private long costs = -1;
  private List<Step> steps;

  public RouteTO(
      long costs,
      List<Step> steps
  ) {
    this.costs = costs;
    this.steps = requireNonNull(steps, "steps");
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
    private String destinationPoint;
    private VehicleOrientationTO vehicleOrientation;
    private int routeIndex;
    private long costs;
    private boolean executionAllowed;
    private ReroutingTypeTO reroutingType;

    public Step(
        String path,
        String sourcePoint,
        String destinationPoint,
        VehicleOrientationTO vehicleOrientation,
        int routeIndex,
        long costs,
        boolean executionAllowed,
        ReroutingTypeTO reroutingType
    ) {
      this.path = path;
      this.sourcePoint = sourcePoint;
      this.destinationPoint = requireNonNull(destinationPoint, "destinationPoint");
      this.vehicleOrientation = requireNonNull(vehicleOrientation, "vehicleOrientation");
      this.routeIndex = routeIndex;
      this.costs = costs;
      this.executionAllowed = executionAllowed;
      this.reroutingType = reroutingType;
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
    public VehicleOrientationTO getVehicleOrientation() {
      return vehicleOrientation;
    }

    public Step setVehicleOrientation(
        @Nonnull
        VehicleOrientationTO vehicleOrientation
    ) {
      this.vehicleOrientation = requireNonNull(vehicleOrientation, "vehicleOrientation");
      return this;
    }

    public int getRouteIndex() {
      return routeIndex;
    }

    public Step setRouteIndex(int routeIndex) {
      this.routeIndex = routeIndex;
      return this;
    }

    public long getCosts() {
      return costs;
    }

    public Step setCosts(long costs) {
      this.costs = costs;
      return this;
    }

    public boolean isExecutionAllowed() {
      return executionAllowed;
    }

    public Step setExecutionAllowed(boolean executionAllowed) {
      this.executionAllowed = executionAllowed;
      return this;
    }

    public ReroutingTypeTO getReroutingType() {
      return reroutingType;
    }

    public Step setReroutingType(ReroutingTypeTO reroutingType) {
      this.reroutingType = reroutingType;
      return this;
    }

    // CHECKSTYLE:OFF
    public enum VehicleOrientationTO {
      FORWARD,
      BACKWARD,
      UNDEFINED
    }

    public enum ReroutingTypeTO {
      REGULAR,
      FORCED
    }
    // CHECKSTYLE:ON
  }
}
