package org.opentcs.customadapter;

import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleProcessModel;

public class CustomProcessModel
    extends
      VehicleProcessModel {

  private String customProperty;

  /**
   * Constructs a new CustomProcessModel object with the specified attachedVehicle.
   *
   * @param attachedVehicle the vehicle object that this process model is attached to
   */
  public CustomProcessModel(Vehicle attachedVehicle) {
    super(attachedVehicle);
    this.customProperty = "Default Value";
  }

  public String getCustomProperty() {
    return customProperty;
  }

  /**
   * Sets the value of the customProperty attribute.
   *
   * @param customProperty the new value for the customProperty attribute
   */
  public void setCustomProperty(String customProperty) {
    String oldValue = this.customProperty;
    this.customProperty = customProperty;
    getPropertyChangeSupport().firePropertyChange("customProperty", oldValue, customProperty);
  }

  // You can add getters and setters for other custom properties here if needed

  /**
   * The Attribute enumeration.
   */
  public enum Attribute {

    /**
     * Custom property attribute.
     */
    CUSTOM_PROPERTY
    // If you have other custom attributes, you can add them here
  }
}
