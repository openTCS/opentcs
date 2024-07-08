package org.opentcs.customadapter;

import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;

public class CustomProcessModelTO
    extends
      VehicleProcessModelTO {

  private String customProperty;

  /**
   * A serializable representation of a {@link CustomProcessModel}.
   * <p>
   * This class extends the {@link VehicleProcessModelTO} class and provides additional methods
   * and properties specific to the custom process model.
   * </p>
   */
  public CustomProcessModelTO() {
    super();
  }

  public String getCustomProperty() {
    return customProperty;
  }

  /**
   * Sets the value of the customProperty attribute in the CustomProcessModelTO object.
   *
   * @param customProperty the new value for the customProperty attribute
   * @return The updated CustomProcessModelTO object
   */
  public CustomProcessModelTO setCustomProperty(String customProperty) {
    this.customProperty = customProperty;
    return this;
  }

  // You can add getters and setters for other custom properties here if needed

  /**
   * This method creates a CustomProcessModelTO object from a CustomProcessModel object.
   *
   * @param model The CustomProcessModel object to create the CustomProcessModelTO from.
   * @return The created CustomProcessModelTO object.
   */
  // We keep this method, but only handle custom properties
  public static CustomProcessModelTO createFrom(CustomProcessModel model) {
    CustomProcessModelTO to = new CustomProcessModelTO();
    to.setCustomProperty(model.getCustomProperty());
    // If there are other custom properties, set them here
    return to;
  }
}
