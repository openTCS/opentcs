/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.outgoing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.Vehicle.IntegrationLevel;
import org.opentcs.data.model.Vehicle.ProcState;
import org.opentcs.data.model.Vehicle.State;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class VehicleState {

  private String name;

  private Map<String, String> properties = new HashMap<>();

  private int length;

  private int energyLevelGood;

  private int energyLevelCritical;

  private int energyLevel;

  private IntegrationLevel integrationLevel = IntegrationLevel.TO_BE_RESPECTED;

  private boolean paused;

  private ProcState procState = ProcState.IDLE;

  private String transportOrder;

  private String currentPosition;

  private PrecisePosition precisePosition;

  private State state = State.UNKNOWN;

  private List<List<String>> allocatedResources = new ArrayList<>();

  private List<List<String>> claimedResources = new ArrayList<>();

  private VehicleState() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = requireNonNull(name, "name");
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public int getEnergyLevelGood() {
    return energyLevelGood;
  }

  public void setEnergyLevelGood(int energyLevelGood) {
    this.energyLevelGood = energyLevelGood;
  }

  public int getEnergyLevelCritical() {
    return energyLevelCritical;
  }

  public void setEnergyLevelCritical(int energyLevelCritical) {
    this.energyLevelCritical = energyLevelCritical;
  }

  public int getEnergyLevel() {
    return energyLevel;
  }

  public void setEnergyLevel(int energyLevel) {
    this.energyLevel = energyLevel;
  }

  public IntegrationLevel getIntegrationLevel() {
    return integrationLevel;
  }

  public void setIntegrationLevel(IntegrationLevel integrationLevel) {
    this.integrationLevel = requireNonNull(integrationLevel, "integrationLevel");
  }

  public boolean isPaused() {
    return paused;
  }

  public void setPaused(boolean paused) {
    this.paused = paused;
  }

  public ProcState getProcState() {
    return procState;
  }

  public void setProcState(Vehicle.ProcState procState) {
    this.procState = requireNonNull(procState, "procState");
  }

  public String getTransportOrder() {
    return transportOrder;
  }

  public void setTransportOrder(String transportOrder) {
    this.transportOrder = transportOrder;
  }

  public String getCurrentPosition() {
    return currentPosition;
  }

  public void setCurrentPosition(String currentPosition) {
    this.currentPosition = currentPosition;
  }

  public PrecisePosition getPrecisePosition() {
    return precisePosition;
  }

  public void setPrecisePosition(PrecisePosition precisePosition) {
    this.precisePosition = precisePosition;
  }

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = requireNonNull(state, "state");
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

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = requireNonNull(properties, "properties");
  }

  /**
   * Creates a <Code>VehicleState</Code> instance from a <Code>Vehicle</Code> instance.
   *
   * @param vehicle The vehicle whose properties will be used to create a <Code>VehicleState</Code>
   * instance.
   * @return A new <Code>VehicleState</Code> instance filled with data from the given vehicle.
   */
  public static VehicleState fromVehicle(Vehicle vehicle) {
    if (vehicle == null) {
      return null;
    }
    VehicleState vehicleState = new VehicleState();
    vehicleState.setName(vehicle.getName());
    vehicleState.setProperties(vehicle.getProperties());
    vehicleState.setLength(vehicle.getLength());
    vehicleState.setEnergyLevelGood(vehicle.getEnergyLevelGood());
    vehicleState.setEnergyLevelCritical(vehicle.getEnergyLevelCritical());
    vehicleState.setEnergyLevel(vehicle.getEnergyLevel());
    vehicleState.setIntegrationLevel(vehicle.getIntegrationLevel());
    vehicleState.setPaused(vehicle.isPaused());
    vehicleState.setProcState(vehicle.getProcState());
    vehicleState.setTransportOrder(nameOfNullableReference(vehicle.getTransportOrder()));
    vehicleState.setCurrentPosition(nameOfNullableReference(vehicle.getCurrentPosition()));
    if (vehicle.getPrecisePosition() != null) {
      vehicleState.setPrecisePosition(new PrecisePosition(vehicle.getPrecisePosition().getX(),
                                                          vehicle.getPrecisePosition().getY(),
                                                          vehicle.getPrecisePosition().getZ()));
    }
    vehicleState.setState(vehicle.getState());
    vehicleState.setAllocatedResources(toListOfListOfNames(vehicle.getAllocatedResources()));
    vehicleState.setClaimedResources(toListOfListOfNames(vehicle.getClaimedResources()));
    return vehicleState;
  }

  private static String nameOfNullableReference(@Nullable TCSObjectReference<?> reference) {
    return reference == null ? null : reference.getName();
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
