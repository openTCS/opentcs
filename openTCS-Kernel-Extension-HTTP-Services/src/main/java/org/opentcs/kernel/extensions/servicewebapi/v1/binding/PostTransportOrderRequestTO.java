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
import java.time.Instant;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.posttransportorder.Destination;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 * A transport order to be processed by the kernel.
 */
public class PostTransportOrderRequestTO {

  private boolean incompleteName;

  private boolean dispensable;

  private Instant deadline;

  private String intendedVehicle;

  private String peripheralReservationToken;

  private String wrappingSequence;

  private String type;

  private List<Destination> destinations;

  private List<Property> properties;

  private List<String> dependencies;

  // CHECKSTYLE:OFF (because of very long parameter declarations)
  @JsonCreator
  public PostTransportOrderRequestTO(
      @JsonProperty(required = false, value = "incompleteName") boolean incompleteName,
      @JsonProperty(required = false, value = "dispensable") boolean dispensable,
      @Nullable @JsonProperty(required = false, value = "deadline") Instant deadline,
      @Nullable @JsonProperty(required = false, value = "intendedVehicle") String intendedVehicle,
      @Nullable @JsonProperty(required = false, value = "peripheralReservationToken") String peripheralReservationToken,
      @Nullable @JsonProperty(required = false, value = "wrappingSequence") String wrappingSequence,
      @Nullable @JsonProperty(required = false, value = "type") String type,
      @Nonnull @JsonProperty(required = true, value = "destinations") List<Destination> destinations,
      @Nullable @JsonProperty(required = false, value = "properties") List<Property> properties,
      @Nullable @JsonProperty(required = false, value = "dependencies") List<String> dependencies) {
    this.incompleteName = incompleteName;
    this.dispensable = dispensable;
    this.deadline = deadline;
    this.intendedVehicle = intendedVehicle;
    this.peripheralReservationToken = peripheralReservationToken;
    this.wrappingSequence = wrappingSequence;
    this.type = type;
    this.destinations = requireNonNull(destinations, "destinations");
    this.properties = properties;
    this.dependencies = dependencies;
  }
  // CHECKSTYLE:ON

  public PostTransportOrderRequestTO() {
  }

  public boolean isIncompleteName() {
    return incompleteName;
  }

  public PostTransportOrderRequestTO setIncompleteName(boolean incompleteName) {
    this.incompleteName = incompleteName;
    return this;
  }

  public boolean isDispensable() {
    return dispensable;
  }

  public PostTransportOrderRequestTO setDispensable(boolean dispensable) {
    this.dispensable = dispensable;
    return this;
  }

  @Nullable
  public Instant getDeadline() {
    return deadline;
  }

  public PostTransportOrderRequestTO setDeadline(@Nullable Instant deadline) {
    this.deadline = deadline;
    return this;
  }

  @Nullable
  public String getIntendedVehicle() {
    return intendedVehicle;
  }

  public PostTransportOrderRequestTO setIntendedVehicle(@Nullable String intendedVehicle) {
    this.intendedVehicle = intendedVehicle;
    return this;
  }

  @Nullable
  public String getPeripheralReservationToken() {
    return peripheralReservationToken;
  }

  public PostTransportOrderRequestTO setPeripheralReservationToken(
      @Nullable String peripheralReservationToken) {
    this.peripheralReservationToken = peripheralReservationToken;
    return this;
  }

  @Nullable
  public String getWrappingSequence() {
    return wrappingSequence;
  }

  public PostTransportOrderRequestTO setWrappingSequence(@Nullable String wrappingSequence) {
    this.wrappingSequence = wrappingSequence;
    return this;
  }

  @Nullable
  public String getType() {
    return type;
  }

  public PostTransportOrderRequestTO setType(@Nullable String type) {
    this.type = type;
    return this;
  }

  @Nonnull
  public List<Destination> getDestinations() {
    return destinations;
  }

  public PostTransportOrderRequestTO setDestinations(@Nonnull List<Destination> destinations) {
    this.destinations = requireNonNull(destinations, "destinations");
    return this;
  }

  @Nullable
  public List<Property> getProperties() {
    return properties;
  }

  public PostTransportOrderRequestTO setProperties(@Nullable List<Property> properties) {
    this.properties = properties;
    return this;
  }

  @Nullable
  public List<String> getDependencies() {
    return dependencies;
  }

  public PostTransportOrderRequestTO setDependencies(@Nullable List<String> dependencies) {
    this.dependencies = dependencies;
    return this;
  }
}
