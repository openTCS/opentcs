/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.outgoing;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;

/**
 * A status message containing information about a vehicle.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public class VehicleStatusMessage
    extends StatusMessage {

  private String vehicleName = "";

  private String transportOrderName = "";

  private String position;

  private PrecisePosition precisePosition;

  private boolean paused;

  private Vehicle.State state;

  private Vehicle.ProcState procState;

  private List<List<String>> allocatedResources = new ArrayList<>();

  private List<List<String>> claimedResources = new ArrayList<>();

  /**
   * Creates a new instance.
   */
  public VehicleStatusMessage() {
  }

  public String getVehicleName() {
    return vehicleName;
  }

  public void setVehicleName(String vehicleName) {
    this.vehicleName = vehicleName;
  }

  public String getTransportOrderName() {
    return transportOrderName;
  }

  public void setTransportOrderName(String transportOrderName) {
    this.transportOrderName = transportOrderName;
  }

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  public PrecisePosition getPrecisePosition() {
    return precisePosition;
  }

  public void setPrecisePosition(PrecisePosition precisePosition) {
    this.precisePosition = precisePosition;
  }

  public boolean isPaused() {
    return paused;
  }

  public void setPaused(boolean paused) {
    this.paused = paused;
  }

  public Vehicle.State getState() {
    return state;
  }

  public void setState(Vehicle.State state) {
    this.state = state;
  }

  public Vehicle.ProcState getProcState() {
    return procState;
  }

  public void setProcState(Vehicle.ProcState procState) {
    this.procState = procState;
  }

  public List<List<String>> getAllocatedResources() {
    return allocatedResources;
  }

  public void setAllocatedResources(List<List<String>> allocatedResources) {
    this.allocatedResources = requireNonNull(allocatedResources, "allocatedResources");
  }

  public List<List<String>> getClaimedResources() {
    return claimedResources;
  }

  public void setClaimedResources(List<List<String>> claimedResources) {
    this.claimedResources = requireNonNull(claimedResources, "claimedResources");
  }

  public static VehicleStatusMessage fromVehicle(Vehicle vehicle,
                                                 long sequenceNumber) {
    return fromVehicle(vehicle, sequenceNumber, Instant.now());
  }

  public static VehicleStatusMessage fromVehicle(Vehicle vehicle,
                                                 long sequenceNumber,
                                                 Instant creationTimeStamp) {
    VehicleStatusMessage vehicleMessage = new VehicleStatusMessage();
    vehicleMessage.setSequenceNumber(sequenceNumber);
    vehicleMessage.setCreationTimeStamp(creationTimeStamp);
    vehicleMessage.setVehicleName(vehicle.getName());
    vehicleMessage.setTransportOrderName(
        vehicle.getTransportOrder() == null ? null : vehicle.getTransportOrder().getName());
    vehicleMessage.setPosition(
        vehicle.getCurrentPosition() == null ? null : vehicle.getCurrentPosition().getName());
    vehicleMessage.setPaused(vehicle.isPaused());
    vehicleMessage.setState(vehicle.getState());
    vehicleMessage.setProcState(vehicle.getProcState());
    if (vehicle.getPrecisePosition() != null) {
      vehicleMessage.setPrecisePosition(new PrecisePosition(vehicle.getPrecisePosition().getX(),
                                                            vehicle.getPrecisePosition().getY(),
                                                            vehicle.getPrecisePosition().getZ()));
    }
    vehicleMessage.setAllocatedResources(toListOfListOfNames(vehicle.getAllocatedResources()));
    vehicleMessage.setClaimedResources(toListOfListOfNames(vehicle.getClaimedResources()));
    return vehicleMessage;
  }

  private static List<List<String>> toListOfListOfNames(
      List<Set<TCSResourceReference<?>>> resources) {
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
