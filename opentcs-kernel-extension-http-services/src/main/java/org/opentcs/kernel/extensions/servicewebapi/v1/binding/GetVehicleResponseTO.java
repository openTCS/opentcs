// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.VehicleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.AcceptableOrderTypeTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.BoundingBoxTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * The current state of a vehicle.
 */
@JsonPropertyOrder(
  {
      "name", "properties", "length", "boundingBox", "energyLevelGood", "energyLevelCritical",
      "energyLevelSufficientlyRecharged", "energyLevelFullyRecharged", "energyLevel",
      "integrationLevel", "paused", "procState", "procStateTimestamp", "transportOrder",
      "currentPosition", "precisePosition", "orientationAngle", "state", "stateTimestamp",
      "allocatedResources", "claimedResources", "allowedOrderTypes", "acceptableOrderTypes",
      "envelopeKey"
  }
)
public class GetVehicleResponseTO {

  private String name = "";

  private Map<String, String> properties = new HashMap<>();

  private BoundingBoxTO boundingBox = new BoundingBoxTO(
      1000,
      1000,
      1000,
      new CoupleTO(0, 0)
  );

  private int energyLevelGood;

  private int energyLevelCritical;

  private int energyLevelSufficientlyRecharged;

  private int energyLevelFullyRecharged;

  private int energyLevel;

  private VehicleTO.IntegrationLevel integrationLevel = VehicleTO.IntegrationLevel.TO_BE_RESPECTED;

  private boolean paused;

  private VehicleTO.ProcState procState = VehicleTO.ProcState.IDLE;

  private Instant procStateTimestamp = Instant.EPOCH;

  private String transportOrder;

  private String currentPosition;

  private PrecisePosition precisePosition;

  private double orientationAngle;

  private VehicleTO.State state = VehicleTO.State.UNKNOWN;

  private Instant stateTimestamp = Instant.EPOCH;

  private List<List<String>> allocatedResources = new ArrayList<>();

  private List<List<String>> claimedResources = new ArrayList<>();

  private List<AcceptableOrderTypeTO> acceptableOrderTypes = new ArrayList<>();

  private String envelopeKey;

  public GetVehicleResponseTO() {
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public GetVehicleResponseTO setName(
      @Nonnull
      String name
  ) {
    this.name = requireNonNull(name, "name");
    return this;
  }

  @Deprecated
  public int getLength() {
    return (int) boundingBox.getLength();
  }

  @Deprecated
  public GetVehicleResponseTO setLength(int length) {
    this.boundingBox = boundingBox.setLength(length);
    return this;
  }

  @Nonnull
  public BoundingBoxTO getBoundingBox() {
    return boundingBox;
  }

  public GetVehicleResponseTO setBoundingBox(
      @Nonnull
      BoundingBoxTO boundingBox
  ) {
    this.boundingBox = requireNonNull(boundingBox, "boundingBox");
    return this;
  }

  public int getEnergyLevelGood() {
    return energyLevelGood;
  }

  public GetVehicleResponseTO setEnergyLevelGood(int energyLevelGood) {
    this.energyLevelGood = energyLevelGood;
    return this;
  }

  public int getEnergyLevelCritical() {
    return energyLevelCritical;
  }

  public GetVehicleResponseTO setEnergyLevelCritical(int energyLevelCritical) {
    this.energyLevelCritical = energyLevelCritical;
    return this;
  }

  public int getEnergyLevelSufficientlyRecharged() {
    return energyLevelSufficientlyRecharged;
  }

  public GetVehicleResponseTO setEnergyLevelSufficientlyRecharged(
      int energyLevelSufficientlyRecharged
  ) {
    this.energyLevelSufficientlyRecharged = energyLevelSufficientlyRecharged;
    return this;
  }

  public int getEnergyLevelFullyRecharged() {
    return energyLevelFullyRecharged;
  }

  public GetVehicleResponseTO setEnergyLevelFullyRecharged(int energyLevelFullyRecharged) {
    this.energyLevelFullyRecharged = energyLevelFullyRecharged;
    return this;
  }

  public int getEnergyLevel() {
    return energyLevel;
  }

  public GetVehicleResponseTO setEnergyLevel(int energyLevel) {
    this.energyLevel = energyLevel;
    return this;
  }

  public VehicleTO.IntegrationLevel getIntegrationLevel() {
    return integrationLevel;
  }

  public GetVehicleResponseTO setIntegrationLevel(VehicleTO.IntegrationLevel integrationLevel) {
    this.integrationLevel = requireNonNull(integrationLevel, "integrationLevel");
    return this;
  }

  public boolean isPaused() {
    return paused;
  }

  public GetVehicleResponseTO setPaused(boolean paused) {
    this.paused = paused;
    return this;
  }

  public VehicleTO.ProcState getProcState() {
    return procState;
  }

  public GetVehicleResponseTO setProcState(VehicleTO.ProcState procState) {
    this.procState = requireNonNull(procState, "procState");
    return this;
  }

  public Instant getProcStateTimestamp() {
    return procStateTimestamp;
  }

  public GetVehicleResponseTO setProcStateTimestamp(Instant procStateTimestamp) {
    this.procStateTimestamp = requireNonNull(procStateTimestamp, "procStateTimestamp");
    return this;
  }

  public String getTransportOrder() {
    return transportOrder;
  }

  public GetVehicleResponseTO setTransportOrder(String transportOrder) {
    this.transportOrder = transportOrder;
    return this;
  }

  public String getCurrentPosition() {
    return currentPosition;
  }

  public GetVehicleResponseTO setCurrentPosition(String currentPosition) {
    this.currentPosition = currentPosition;
    return this;
  }

  public PrecisePosition getPrecisePosition() {
    return precisePosition;
  }

  public GetVehicleResponseTO setPrecisePosition(PrecisePosition precisePosition) {
    this.precisePosition = precisePosition;
    return this;
  }

  public double getOrientationAngle() {
    return orientationAngle;
  }

  public GetVehicleResponseTO setOrientationAngle(double orientationAngle) {
    this.orientationAngle = orientationAngle;
    return this;
  }

  public VehicleTO.State getState() {
    return state;
  }

  public GetVehicleResponseTO setState(VehicleTO.State state) {
    this.state = requireNonNull(state, "state");
    return this;
  }

  public Instant getStateTimestamp() {
    return stateTimestamp;
  }

  public GetVehicleResponseTO setStateTimestamp(Instant stateTimestamp) {
    this.stateTimestamp = requireNonNull(stateTimestamp, "stateTimestamp");
    return this;
  }

  public List<List<String>> getAllocatedResources() {
    return allocatedResources;
  }

  public GetVehicleResponseTO setAllocatedResources(List<List<String>> allocatedResources) {
    this.allocatedResources = requireNonNull(allocatedResources, "allocatedResources");
    return this;
  }

  public List<List<String>> getClaimedResources() {
    return claimedResources;
  }

  public GetVehicleResponseTO setClaimedResources(List<List<String>> claimedResources) {
    this.claimedResources = requireNonNull(claimedResources, "claimedResources");
    return this;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public GetVehicleResponseTO setProperties(Map<String, String> properties) {
    this.properties = requireNonNull(properties, "properties");
    return this;
  }

  @Deprecated
  public List<String> getAllowedOrderTypes() {
    return getAcceptableOrderTypes().stream()
        .map(AcceptableOrderTypeTO::getName)
        .collect(Collectors.toList());
  }

  public List<AcceptableOrderTypeTO> getAcceptableOrderTypes() {
    return acceptableOrderTypes;
  }

  public GetVehicleResponseTO setAcceptableOrderTypes(
      List<AcceptableOrderTypeTO> acceptableOrderTypes
  ) {
    this.acceptableOrderTypes = requireNonNull(acceptableOrderTypes, "acceptableOrderTypes");
    return this;
  }

  @ScheduledApiChange(when = "7.0", details = "Envelope key will become non-null.")
  @Nullable
  public String getEnvelopeKey() {
    return envelopeKey;
  }

  @ScheduledApiChange(when = "7.0", details = "Envelope key will become non-null.")
  public GetVehicleResponseTO setEnvelopeKey(
      @Nullable
      String envelopeKey
  ) {
    this.envelopeKey = envelopeKey;
    return this;
  }

  /**
   * A precise position of a vehicle.
   */
  public static class PrecisePosition {

    private long x;

    private long y;

    private long z;

    /**
     * Creates a new instance.
     */
    public PrecisePosition() {
    }

    /**
     * Creates a new instance.
     *
     * @param x x value
     * @param y y value
     * @param z z value
     */
    public PrecisePosition(long x, long y, long z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }

    public long getX() {
      return x;
    }

    public void setX(long x) {
      this.x = x;
    }

    public long getY() {
      return y;
    }

    public void setY(long y) {
      this.y = y;
    }

    public long getZ() {
      return z;
    }

    public void setZ(long z) {
      this.z = z;
    }
  }
}
