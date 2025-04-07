// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.getevents;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.opentcs.data.model.Vehicle;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.AcceptableOrderTypeTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.BoundingBoxTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 * A status message containing information about a vehicle.
 */
public class VehicleStatusMessage
    extends
      StatusMessage {

  private String vehicleName = "";

  private List<Property> properties = new ArrayList<>();

  private String transportOrderName = "";

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

  private Vehicle.IntegrationLevel integrationLevel = Vehicle.IntegrationLevel.TO_BE_RESPECTED;

  private String position;

  private PrecisePosition precisePosition;

  private double orientationAngle;

  private boolean paused;

  private Vehicle.State state;

  private Instant stateTimestamp = Instant.EPOCH;

  private Vehicle.ProcState procState;

  private Instant procStateTimestamp = Instant.EPOCH;

  private List<List<String>> allocatedResources = new ArrayList<>();

  private List<List<String>> claimedResources = new ArrayList<>();

  private List<AcceptableOrderTypeTO> acceptableOrderTypes = new ArrayList<>();

  private String envelopeKey;

  /**
   * Creates a new instance.
   */
  public VehicleStatusMessage() {
  }

  @Override
  public VehicleStatusMessage setSequenceNumber(long sequenceNumber) {
    return (VehicleStatusMessage) super.setSequenceNumber(sequenceNumber);
  }

  @Override
  public VehicleStatusMessage setCreationTimeStamp(Instant creationTimeStamp) {
    return (VehicleStatusMessage) super.setCreationTimeStamp(creationTimeStamp);
  }

  public String getVehicleName() {
    return vehicleName;
  }

  public VehicleStatusMessage setVehicleName(String vehicleName) {
    this.vehicleName = vehicleName;
    return this;
  }

  public List<Property> getProperties() {
    return properties;
  }

  public VehicleStatusMessage setProperties(List<Property> properties) {
    this.properties = properties;
    return this;
  }

  public String getTransportOrderName() {
    return transportOrderName;
  }

  public VehicleStatusMessage setTransportOrderName(String transportOrderName) {
    this.transportOrderName = transportOrderName;
    return this;
  }

  @Nonnull
  public BoundingBoxTO getBoundingBox() {
    return boundingBox;
  }

  public VehicleStatusMessage setBoundingBox(
      @Nonnull
      BoundingBoxTO boundingBox
  ) {
    this.boundingBox = requireNonNull(boundingBox, "boundingBox");
    return this;
  }

  public int getEnergyLevelGood() {
    return energyLevelGood;
  }

  public VehicleStatusMessage setEnergyLevelGood(int energyLevelGood) {
    this.energyLevelGood = energyLevelGood;
    return this;
  }

  public int getEnergyLevelCritical() {
    return energyLevelCritical;
  }

  public VehicleStatusMessage setEnergyLevelCritical(int energyLevelCritical) {
    this.energyLevelCritical = energyLevelCritical;
    return this;
  }

  public int getEnergyLevelSufficientlyRecharged() {
    return energyLevelSufficientlyRecharged;
  }

  public VehicleStatusMessage setEnergyLevelSufficientlyRecharged(
      int energyLevelSufficientlyRecharged
  ) {
    this.energyLevelSufficientlyRecharged = energyLevelSufficientlyRecharged;
    return this;
  }

  public int getEnergyLevelFullyRecharged() {
    return energyLevelFullyRecharged;
  }

  public VehicleStatusMessage setEnergyLevelFullyRecharged(int energyLevelFullyRecharged) {
    this.energyLevelFullyRecharged = energyLevelFullyRecharged;
    return this;
  }

  public int getEnergyLevel() {
    return energyLevel;
  }

  public VehicleStatusMessage setEnergyLevel(int energyLevel) {
    this.energyLevel = energyLevel;
    return this;
  }

  public Vehicle.IntegrationLevel getIntegrationLevel() {
    return integrationLevel;
  }

  public VehicleStatusMessage setIntegrationLevel(Vehicle.IntegrationLevel integrationLevel) {
    this.integrationLevel = requireNonNull(integrationLevel, "integrationLevel");
    return this;
  }

  public String getPosition() {
    return position;
  }

  public VehicleStatusMessage setPosition(String position) {
    this.position = position;
    return this;
  }

  public PrecisePosition getPrecisePosition() {
    return precisePosition;
  }

  public VehicleStatusMessage setPrecisePosition(PrecisePosition precisePosition) {
    this.precisePosition = precisePosition;
    return this;
  }

  public double getOrientationAngle() {
    return orientationAngle;
  }

  public VehicleStatusMessage setOrientationAngle(double orientationAngle) {
    this.orientationAngle = orientationAngle;
    return this;
  }

  public boolean isPaused() {
    return paused;
  }

  public VehicleStatusMessage setPaused(boolean paused) {
    this.paused = paused;
    return this;
  }

  public Vehicle.State getState() {
    return state;
  }

  public VehicleStatusMessage setState(Vehicle.State state) {
    this.state = state;
    return this;
  }

  public Instant getStateTimestamp() {
    return stateTimestamp;
  }

  public VehicleStatusMessage setStateTimestamp(Instant stateTimestamp) {
    this.stateTimestamp = requireNonNull(stateTimestamp, "stateTimestamp");
    return this;
  }

  public Vehicle.ProcState getProcState() {
    return procState;
  }

  public VehicleStatusMessage setProcState(Vehicle.ProcState procState) {
    this.procState = procState;
    return this;
  }

  public Instant getProcStateTimestamp() {
    return procStateTimestamp;
  }

  public VehicleStatusMessage setProcStateTimestamp(Instant procStateTimestamp) {
    this.procStateTimestamp = requireNonNull(procStateTimestamp, "procStateTimestamp");
    return this;
  }

  public List<List<String>> getAllocatedResources() {
    return allocatedResources;
  }

  public VehicleStatusMessage setAllocatedResources(List<List<String>> allocatedResources) {
    this.allocatedResources = requireNonNull(allocatedResources, "allocatedResources");
    return this;
  }

  public List<List<String>> getClaimedResources() {
    return claimedResources;
  }

  public VehicleStatusMessage setClaimedResources(List<List<String>> claimedResources) {
    this.claimedResources = requireNonNull(claimedResources, "claimedResources");
    return this;
  }

  public List<AcceptableOrderTypeTO> getAcceptableOrderTypes() {
    return acceptableOrderTypes;
  }

  public VehicleStatusMessage setAcceptableOrderTypes(
      List<AcceptableOrderTypeTO> acceptableOrderTypes
  ) {
    this.acceptableOrderTypes = requireNonNull(acceptableOrderTypes, "acceptableOrderTypes");
    return this;
  }

  @Nonnull
  public String getEnvelopeKey() {
    return envelopeKey;
  }

  public VehicleStatusMessage setEnvelopeKey(
      @Nonnull
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
