package org.opentcs.customadapter;

import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleProcessModel;

public class CustomProcessModel
    extends
      VehicleProcessModel {

  private String customProperty;

  public CustomProcessModel(Vehicle attachedVehicle) {
    super(attachedVehicle);
    this.customProperty = "Default Value";
  }

  public String getCustomProperty() {
    return customProperty;
  }

  public void setCustomProperty(String customProperty) {
    String oldValue = this.customProperty;
    this.customProperty = customProperty;
    getPropertyChangeSupport().firePropertyChange("customProperty", oldValue, customProperty);
  }

  // You can add getters and setters for other custom properties here if needed

  // Enumeration of custom properties
  public enum Attribute {
    CUSTOM_PROPERTY
    // If you have other custom attributes, you can add them here
  }
}
