// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import java.util.List;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PropertyTO;

/**
 */
public class LocationTypeTO {

  private String name;
  private List<String> allowedOperations = List.of();
  private List<String> allowedPeripheralOperations = List.of();
  private Layout layout = new Layout();
  private List<PropertyTO> properties = List.of();

  @JsonCreator
  public LocationTypeTO(
      @Nonnull
      @JsonProperty(value = "name", required = true)
      String name
  ) {
    this.name = requireNonNull(name, "name");
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public LocationTypeTO setName(
      @Nonnull
      String name
  ) {
    this.name = requireNonNull(name, "name");
    return this;
  }

  @Nonnull
  public List<PropertyTO> getProperties() {
    return properties;
  }

  public LocationTypeTO setProperties(
      @Nonnull
      List<PropertyTO> properties
  ) {
    this.properties = requireNonNull(properties, "properties");
    return this;
  }

  @Nonnull
  public List<String> getAllowedOperations() {
    return allowedOperations;
  }

  public LocationTypeTO setAllowedOperations(
      @Nonnull
      List<String> allowedOperations
  ) {
    this.allowedOperations = requireNonNull(allowedOperations, "allowedOperations");
    return this;
  }

  @Nonnull
  public List<String> getAllowedPeripheralOperations() {
    return allowedPeripheralOperations;
  }

  public LocationTypeTO setAllowedPeripheralOperations(
      @Nonnull
      List<String> allowedPeripheralOperations
  ) {
    this.allowedPeripheralOperations = requireNonNull(
        allowedPeripheralOperations,
        "allowedPeripheralOperations"
    );
    return this;
  }

  @Nonnull
  public Layout getLayout() {
    return layout;
  }

  public LocationTypeTO setLayout(
      @Nonnull
      Layout layout
  ) {
    this.layout = requireNonNull(layout, "layout");
    return this;
  }

  public static class Layout {

    private String locationRepresentation = LocationRepresentationTO.NONE.name();

    public Layout() {

    }

    @Nonnull
    public String getLocationRepresentation() {
      return locationRepresentation;
    }

    public Layout setLocationRepresentation(
        @Nonnull
        String locationRepresentation
    ) {
      this.locationRepresentation = requireNonNull(
          locationRepresentation,
          "locationRepresentation"
      );
      return this;
    }

  }

}
