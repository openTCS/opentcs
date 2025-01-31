// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.getevents;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;

/**
 * A status message containing information about a vehicle.
 */
public class VehicleStatusMessage
    extends
      StatusMessage {

  private String vehicleName = "";

  private String transportOrderName = "";

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

  public String getTransportOrderName() {
    return transportOrderName;
  }

  public VehicleStatusMessage setTransportOrderName(String transportOrderName) {
    this.transportOrderName = transportOrderName;
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

  public static VehicleStatusMessage fromVehicle(
      Vehicle vehicle,
      long sequenceNumber
  ) {
    return fromVehicle(vehicle, sequenceNumber, Instant.now());
  }

  public static VehicleStatusMessage fromVehicle(
      Vehicle vehicle,
      long sequenceNumber,
      Instant creationTimeStamp
  ) {
    VehicleStatusMessage vehicleMessage = new VehicleStatusMessage();
    vehicleMessage.setSequenceNumber(sequenceNumber);
    vehicleMessage.setCreationTimeStamp(creationTimeStamp);
    vehicleMessage.setVehicleName(vehicle.getName());
    vehicleMessage.setTransportOrderName(
        vehicle.getTransportOrder() == null ? null : vehicle.getTransportOrder().getName()
    );
    vehicleMessage.setPosition(
        vehicle.getCurrentPosition() == null ? null : vehicle.getCurrentPosition().getName()
    );
    vehicleMessage.setPaused(vehicle.isPaused());
    vehicleMessage.setState(vehicle.getState());
    vehicleMessage.setStateTimestamp(vehicle.getStateTimestamp());
    vehicleMessage.setProcState(vehicle.getProcState());
    vehicleMessage.setProcStateTimestamp(vehicle.getProcStateTimestamp());
    if (vehicle.getPose().getPosition() != null) {
      vehicleMessage.setPrecisePosition(
          new PrecisePosition(
              vehicle.getPose().getPosition().getX(),
              vehicle.getPose().getPosition().getY(),
              vehicle.getPose().getPosition().getZ()
          )
      );
    }
    else {
      vehicleMessage.setPrecisePosition(null);
    }
    vehicleMessage.setOrientationAngle(vehicle.getPose().getOrientationAngle());
    vehicleMessage.setAllocatedResources(toListOfListOfNames(vehicle.getAllocatedResources()));
    vehicleMessage.setClaimedResources(toListOfListOfNames(vehicle.getClaimedResources()));
    return vehicleMessage;
  }

  private static List<List<String>> toListOfListOfNames(
      List<Set<TCSResourceReference<?>>> resources
  ) {
    List<List<String>> result = new ArrayList<>(resources.size());

    for (Set<TCSResourceReference<?>> resSet : resources) {
      result.add(
          resSet.stream()
              .map(resRef -> resRef.getName())
              .collect(Collectors.toList())
      );
    }

    return result;
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
