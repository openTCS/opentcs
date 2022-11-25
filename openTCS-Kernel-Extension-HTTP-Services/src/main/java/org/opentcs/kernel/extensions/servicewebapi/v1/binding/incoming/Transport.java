/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.incoming;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 * A transport order to be processed by the kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Transport {

  private boolean incompleteName;

  private Instant deadline;

  private String intendedVehicle;

  private List<Destination> destinations;

  private List<Property> properties;

  private List<String> dependencies;

  @JsonCreator
  public Transport(
      @JsonProperty(required = false, value = "incompleteName") boolean incompleteName,
      @Nullable @JsonProperty(required = false, value = "deadline") Instant deadline,
      @Nullable @JsonProperty(required = false, value = "intendedVehicle") String intendedVehicle,
      @Nonnull @JsonProperty(required = true, value = "destinations") List<Destination> destinations,
      @Nullable @JsonProperty(required = false, value = "properties") List<Property> properties,
      @Nullable @JsonProperty(required = false, value = "dependencies") List<String> dependencies) {
    this.incompleteName = incompleteName;
    this.deadline = deadline;
    this.intendedVehicle = intendedVehicle;
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
