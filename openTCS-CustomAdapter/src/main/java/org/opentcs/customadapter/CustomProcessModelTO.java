package org.opentcs.customadapter;

import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;

public class CustomProcessModelTO extends VehicleProcessModelTO {

  private String customProperty;

  public CustomProcessModelTO() {
    super();
  }

  public String getCustomProperty() {
    return customProperty;
  }

  public CustomProcessModelTO setCustomProperty(String customProperty) {
    this.customProperty = customProperty;
    return this;
  }

  // You can add getters and setters for other custom properties here if needed

  // We keep this method, but only handle custom properties
  public static CustomProcessModelTO createFrom(CustomProcessModel model) {
    CustomProcessModelTO to = new CustomProcessModelTO();
    to.setCustomProperty(model.getCustomProperty());
    // If there are other custom properties, set them here
    return to;
  }
}
