/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.customadapter;

import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;

/**
 * Custom vehicle model class that extends VehicleProcessModel.
 * Used to store and manage vehicle status information specific to custom adapters.
 */
public class CustomVehicleModel
    extends
    VehicleProcessModel {

  // Add custom properties
  private String customProperty;

  /**
   * Constructor.
   *
   * @param vehicle The associated vehicle object.
   */
  public CustomVehicleModel(Vehicle vehicle) {
    super(vehicle);
    this.customProperty = "Default Value";
  }

  /**
   * Get custom properties.
   *
   * @return The value of the custom property.
   */
  public String getCustomProperty() {
    return customProperty;
  }

  /**
   * Set custom properties.
   *
   * @param customProperty The new value to set.
   */
  public void setCustomProperty(String customProperty) {
    String oldValue = this.customProperty;
    this.customProperty = customProperty;

    // Notify listener property changed
    getPropertyChangeSupport().firePropertyChange("customProperty", oldValue, customProperty);
  }

  /**
   * Creates a transportable process model object.
   *
   * @return A VehicleProcessModelTO object containing the current model state.
   */
  public VehicleProcessModelTO createTransferableProcessModel() {
    VehicleProcessModelTO to = new VehicleProcessModelTO();

    to.setName(getName());
    to.setCommAdapterConnected(isCommAdapterConnected());
    to.setCommAdapterEnabled(isCommAdapterEnabled());
    to.setPosition(getPosition());
    to.setPrecisePosition(getPrecisePosition());
    to.setOrientationAngle(getOrientationAngle());
    to.setEnergyLevel(getEnergyLevel());
    to.setLoadHandlingDevices(getLoadHandlingDevices());
    to.setState(getState());

    // Add custom properties
//    Map<String, String> properties = new HashMap<>();
//    properties.put("customProperty", getCustomProperty());
//    to.setProperties(properties);

    return to;
  }

  // VehicleProcessModel The other methods in have already been implemented, we don't need to
  // override them Unless we want to change their behavior
}
