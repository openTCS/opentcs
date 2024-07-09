package org.opentcs.customadapter;

import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleProcessModel;

public class CustomProcessModel
    extends
      VehicleProcessModel {

  private String customProperty;
  private String loadOperation = "Load";
  private String unloadOperation = "Unload";

  /**
   * Constructs a new CustomProcessModel object with the specified attachedVehicle.
   *
   * @param attachedVehicle the vehicle object that this process model is attached to
   */
  public CustomProcessModel(Vehicle attachedVehicle) {
    super(attachedVehicle);
    this.customProperty = "Default Value";
  }

  public String getLoadOperation() {
    return loadOperation;
  }

  /**
   * Sets the value of the loadOperation attribute.
   *
   * @param loadOperation the new value for the loadOperation attribute
   *
   * @see CustomProcessModel#loadOperation
   * @see CustomProcessModel#setLoadOperation(String)
   * @see CustomProcessModel.Attribute#LOAD
   */
  public void setLoadOperation(String loadOperation) {
    String oldValue = this.loadOperation;
    this.loadOperation = loadOperation;
    getPropertyChangeSupport().firePropertyChange("loadOperation", oldValue, loadOperation);
  }

  public String getUnloadOperation() {
    return unloadOperation;
  }

  /**
   * Sets the value of the unloadOperation attribute in the CustomProcessModel class.
   *
   * @param unloadOperation the new value for the unloadOperation attribute
   */
  public void setUnloadOperation(String unloadOperation) {
    String oldValue = this.unloadOperation;
    this.unloadOperation = unloadOperation;
    getPropertyChangeSupport().firePropertyChange("unloadOperation", oldValue, unloadOperation);
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
    CUSTOM_PROPERTY,
    /**
     * Custom load operation attribute.
     */
    LOAD,
    /**
     * Custom unload operation attribute.
     */
    UNLOAD
    // If you have other custom attributes, you can add them here
  }
}
