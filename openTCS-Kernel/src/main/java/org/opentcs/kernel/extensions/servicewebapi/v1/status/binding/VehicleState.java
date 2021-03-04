/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.status.binding;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.HashMap;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nullable;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.Vehicle.IntegrationLevel;
import org.opentcs.data.model.Vehicle.ProcState;
import org.opentcs.data.model.Vehicle.State;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class VehicleState {

  @JsonPropertyDescription("The name of the vehicle")
  private String name;

  @JsonPropertyDescription("A set of properties (key-value pairs) associated with this object.")
  private Map<String, String> properties = new HashMap<>();

  @JsonPropertyDescription("The vehicle's length (in mm).")
  private int length;

  @JsonPropertyDescription(
      "The value at/above which the vehicle's energy level is considered 'good'.")
  private int energyLevelGood;

  @JsonPropertyDescription(
      "The value at/below which the vehicle's energy level is considered 'critical'.")
  private int energyLevelCritical;

  @JsonPropertyDescription("The vehicle's remaining energy (in percent of the maximum).")
  private int energyLevel;

  @JsonPropertyDescription("The vehicle's integration level.")
  private IntegrationLevel integrationLevel = IntegrationLevel.TO_BE_RESPECTED;

  @JsonPropertyDescription("The vehicle's current processing state.")
  private ProcState procState = ProcState.IDLE;

  @JsonPropertyDescription("The name of the transport order the vehicle is currently processing.")
  private String transportOrder;

  @JsonPropertyDescription("The name of the point which the vehicle currently occupies.")
  private String currentPosition;

  @JsonPropertyDescription("The vehicle's current state.")
  private State state = State.UNKNOWN;

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

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = requireNonNull(state, "state");
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
    vehicleState.setProcState(vehicle.getProcState());
    vehicleState.setTransportOrder(nameOfNullableReference(vehicle.getTransportOrder()));
    vehicleState.setCurrentPosition(nameOfNullableReference(vehicle.getCurrentPosition()));
    vehicleState.setState(vehicle.getState());
    return vehicleState;
  }

  private static String nameOfNullableReference(@Nullable TCSObjectReference<?> reference) {
    return reference == null ? null : reference.getName();
  }

}
