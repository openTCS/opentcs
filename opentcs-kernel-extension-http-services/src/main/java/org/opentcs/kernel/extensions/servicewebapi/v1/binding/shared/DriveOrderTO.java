// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared;

import static java.util.Objects.requireNonNull;

import java.util.List;

/**
 */
public class DriveOrderTO {
  private String name;
  private DestinationTO destination;
  private String transportOrder;
  private RouteTO route;
  private StateTO state;

  public DriveOrderTO(
      String name,
      DestinationTO destination,
      String transportOrder,
      RouteTO route,
      StateTO state
  ) {
    this.name = requireNonNull(name, "name");
    this.destination = requireNonNull(destination, "destination");
    this.transportOrder = requireNonNull(transportOrder, "transportOrder");
    this.route = route;
    this.state = requireNonNull(state, "state");
  }

  public String getName() {
    return name;
  }

  public DriveOrderTO setName(String name) {
    this.name = requireNonNull(name, "name");
    return this;
  }

  public DestinationTO getDestination() {
    return destination;
  }

  public DriveOrderTO setDestination(DestinationTO destination) {
    this.destination = requireNonNull(destination, "destination");
    return this;
  }

  public String getTransportOrder() {
    return transportOrder;
  }

  public DriveOrderTO setTransportOrder(String transportOrder) {
    this.transportOrder = requireNonNull(transportOrder, "transportOrder");
    return this;
  }

  public RouteTO getRoute() {
    return route;
  }

  public DriveOrderTO setRoute(RouteTO route) {
    this.route = requireNonNull(route, "route");
    return this;
  }

  public StateTO getState() {
    return state;
  }

  public DriveOrderTO setState(StateTO state) {
    this.state = requireNonNull(state, "state");
    return this;
  }

  public static class DestinationTO {

    private String destination;
    private String operation;
    private List<Property> properties;

    public DestinationTO(
        String destination,
        String operation,
        List<Property> properties
    ) {
      this.destination = requireNonNull(destination, "destination");
      this.operation = requireNonNull(operation, "operation");
      this.properties = requireNonNull(properties, "properties");
    }

    public String getDestination() {
      return destination;
    }

    public DestinationTO setDestination(String destination) {
      this.destination = requireNonNull(destination, "destination");
      return this;
    }

    public String getOperation() {
      return operation;
    }

    public DestinationTO setOperation(String operation) {
      this.operation = requireNonNull(operation, "operation");
      return this;
    }

    public List<Property> getProperties() {
      return properties;
    }

    public DestinationTO setProperties(List<Property> properties) {
      this.properties = requireNonNull(properties, "properties");
      return this;
    }
  }

  // CHECKSTYLE:OFF
  public enum StateTO {
    PRISTINE,
    TRAVELLING,
    OPERATING,
    FINISHED,
    FAILED
  }
  // CHECKSTYLE:ON
}
