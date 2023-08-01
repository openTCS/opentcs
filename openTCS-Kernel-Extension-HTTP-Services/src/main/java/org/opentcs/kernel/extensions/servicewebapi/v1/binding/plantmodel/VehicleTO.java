/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel;

import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PropertyTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;

/**
 */
public class VehicleTO {

  private String name;
  private int length = 1000;
  private int energyLevelCritical = 30;
  private int energyLevelGood = 90;
  private int energyLevelFullyRecharged = 90;
  private int energyLevelSufficientlyRecharged = 30;
  private int maxVelocity = 1000;
  private int maxReverseVelocity = 1000;
  private Layout layout = new Layout();
  private List<PropertyTO> properties = List.of();

  @JsonCreator
  public VehicleTO(@Nonnull @JsonProperty(value = "name", required = true) String name) {
    this.name = requireNonNull(name, "name");
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public VehicleTO setName(@Nonnull String name) {
    this.name = requireNonNull(name, "name");
    return this;
  }

  @Nonnull
  public List<PropertyTO> getProperties() {
    return properties;
  }

  public VehicleTO setProperties(@Nonnull List<PropertyTO> properties) {
    this.properties = requireNonNull(properties, "properties");
    return this;
  }

  public int getLength() {
    return length;
  }

  public VehicleTO setLength(int length) {
    this.length = length;
    return this;
  }

  public int getEnergyLevelCritical() {
    return energyLevelCritical;
  }

  public VehicleTO setEnergyLevelCritical(int energyLevelCritical) {
    this.energyLevelCritical = energyLevelCritical;
    return this;
  }

  public int getEnergyLevelGood() {
    return energyLevelGood;
  }

  public VehicleTO setEnergyLevelGood(int energyLevelGood) {
    this.energyLevelGood = energyLevelGood;
    return this;
  }

  public int getEnergyLevelFullyRecharged() {
    return energyLevelFullyRecharged;
  }

  public VehicleTO setEnergyLevelFullyRecharged(int energyLevelFullyRecharged) {
    this.energyLevelFullyRecharged = energyLevelFullyRecharged;
    return this;
  }

  public int getEnergyLevelSufficientlyRecharged() {
    return energyLevelSufficientlyRecharged;
  }

  public VehicleTO setEnergyLevelSufficientlyRecharged(int energyLevelSufficientlyRecharged) {
    this.energyLevelSufficientlyRecharged = energyLevelSufficientlyRecharged;
    return this;
  }

  public int getMaxVelocity() {
    return maxVelocity;
  }

  public VehicleTO setMaxVelocity(int maxVelocity) {
    this.maxVelocity = maxVelocity;
    return this;
  }

  public int getMaxReverseVelocity() {
    return maxReverseVelocity;
  }

  public VehicleTO setMaxReverseVelocity(int maxReverseVelocity) {
    this.maxReverseVelocity = maxReverseVelocity;
    return this;
  }

  @Nonnull
  public Layout getLayout() {
    return layout;
  }

  public VehicleTO setLayout(@Nonnull Layout layout) {
    this.layout = requireNonNull(layout, "layout");
    return this;
  }

  public static class Layout {

    private String routeColor = "#00FF00";

    public Layout() {

    }

    @Nonnull
    public String getRouteColor() {
      return routeColor;
    }

    public Layout setRouteColor(@Nonnull String routeColor) {
      this.routeColor = requireNonNull(routeColor, "routeColor");
      return this;
    }

  }

}
