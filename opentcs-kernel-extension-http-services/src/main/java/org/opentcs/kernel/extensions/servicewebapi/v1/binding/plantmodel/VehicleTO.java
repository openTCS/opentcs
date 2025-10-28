// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nonnull;
import java.util.List;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.BoundingBoxTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PropertyTO;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 */
@JsonPropertyOrder(
  {
      "name", "length", "boundingBox", "energyLevelCritical", "energyLevelGood",
      "energyLevelFullyRecharged", "energyLevelFullyRecharged", "energyLevelSufficientlyRecharged",
      "maxVelocity", "maxReverseVelocity", "layout", "properties"
  }
)
public class VehicleTO {

  private String name;
  private BoundingBoxTO boundingBox = new BoundingBoxTO(1000, 1000, 1000, new CoupleTO(0, 0));
  private int energyLevelCritical = 30;
  private int energyLevelGood = 90;
  private int energyLevelFullyRecharged = 90;
  private int energyLevelSufficientlyRecharged = 30;
  private int maxVelocity = 1000;
  private int maxReverseVelocity = 1000;
  private Layout layout = new Layout();
  private List<PropertyTO> properties = List.of();

  @JsonCreator
  public VehicleTO(
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

  public VehicleTO setName(
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

  public VehicleTO setProperties(
      @Nonnull
      List<PropertyTO> properties
  ) {
    this.properties = requireNonNull(properties, "properties");
    return this;
  }

  @ScheduledApiChange(
      when = "Web API v2",
      details = "Redundant, as the whole bounding box is now reflected."
  )
  public int getLength() {
    return (int) boundingBox.getLength();
  }

  @ScheduledApiChange(
      when = "Web API v2",
      details = "Redundant, as the whole bounding box is now reflected."
  )
  public VehicleTO setLength(int length) {
    this.boundingBox = boundingBox.setLength(length);
    return this;
  }

  @Nonnull
  public BoundingBoxTO getBoundingBox() {
    return boundingBox;
  }

  public VehicleTO setBoundingBox(
      @Nonnull
      BoundingBoxTO boundingBox
  ) {
    this.boundingBox = requireNonNull(boundingBox, "boundingBox");
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

  public VehicleTO setLayout(
      @Nonnull
      Layout layout
  ) {
    this.layout = requireNonNull(layout, "layout");
    return this;
  }

  // CHECKSTYLE:OFF
  public enum State {

    UNKNOWN,
    UNAVAILABLE,
    ERROR,
    IDLE,
    EXECUTING,
    CHARGING
  }
  // CHECKSTYLE:ON

  // CHECKSTYLE:OFF
  public enum IntegrationLevel {

    TO_BE_IGNORED,
    TO_BE_NOTICED,
    TO_BE_RESPECTED,
    TO_BE_UTILIZED
  }
  // CHECKSTYLE:ON

  // CHECKSTYLE:OFF
  public enum ProcState {

    IDLE,
    AWAITING_ORDER,
    PROCESSING_ORDER
  }
  // CHECKSTYLE:ON

  // CHECKSTYLE:OFF
  public enum Orientation {

    UNDEFINED
  }
  // CHECKSTYLE:ON

  public static class Layout {

    private String routeColor = "#00FF00";

    public Layout() {

    }

    @Nonnull
    public String getRouteColor() {
      return routeColor;
    }

    public Layout setRouteColor(
        @Nonnull
        String routeColor
    ) {
      this.routeColor = requireNonNull(routeColor, "routeColor");
      return this;
    }

  }

}
