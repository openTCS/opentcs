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
 *
 * @author Stefan Walter (Fraunhofer IML)
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

  public boolean hasIncompleteName() {
    return incompleteName;
  }

  public void setIncompleteName(boolean incompleteName) {
    this.incompleteName = incompleteName;
  }

  public boolean isDispensable() {
    return dispensable;
  }

  public void setDispensable(boolean dispensable) {
    this.dispensable = dispensable;
  }

  @Nullable
  public Instant getDeadline() {
    return deadline;
  }

  public void setDeadline(@Nullable Instant deadline) {
    this.deadline = deadline;
  }

  @Nullable
  public String getIntendedVehicle() {
    return intendedVehicle;
  }

  public void setIntendedVehicle(@Nullable String intendedVehicle) {
    this.intendedVehicle = intendedVehicle;
  }

  @Nullable
  public String getPeripheralReservationToken() {
    return peripheralReservationToken;
  }

  public void setPeripheralReservationToken(@Nullable String peripheralReservationToken) {
    this.peripheralReservationToken = peripheralReservationToken;
  }

  @Nullable
  public String getWrappingSequence() {
    return wrappingSequence;
  }

  public void setWrappingSequence(@Nullable String wrappingSequence) {
    this.wrappingSequence = wrappingSequence;
  }

  @Nullable
  public String getType() {
    return type;
  }

  public void setType(@Nullable String type) {
    this.type = type;
  }

  @Nonnull
  public List<Destination> getDestinations() {
    return destinations;
  }

  public void setDestinations(@Nonnull List<Destination> destinations) {
    this.destinations = requireNonNull(destinations, "destinations");
  }

  @Nullable
  public List<Property> getProperties() {
    return properties;
  }

  public void setProperties(@Nullable List<Property> properties) {
    this.properties = properties;
  }

  @Nullable
  public List<String> getDependencies() {
    return dependencies;
  }

  public void setDependencies(@Nullable List<String> dependencies) {
    this.dependencies = dependencies;
  }
}
