package org.opentcs.customadapter;

import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;

/**
 * A serializable representation of a {@link CustomProcessModel}.
 */
public class CustomProcessModelTO
    extends
      VehicleProcessModelTO {

  /**
   * Indicates which operation is a loading operation.
   */
  private String loadOperation = "Load";
  /**
   * Indicates which operation is an unloading operation.
   */
  private String unloadOperation = "Unload";
  /**
   * The maximum acceleration.
   */
  private double maxAcceleration;
  /**
   * The maximum deceleration.
   */
  private double maxDeceleration;
  /**
   * The maximum forward velocity.
   */
  private double maxFwdVelocity;
  /**
   * The maximum reverse velocity.
   */
  private double maxRevVelocity;

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

  public String getLoadOperation() {
    return loadOperation;
  }
  /**
   * Sets the load operation for the custom process model.
   *
   * @param loadOperation The name of the load operation.
   * @return The updated CustomProcessModelTO object.
   */
  public CustomProcessModelTO setLoadOperation(String loadOperation) {
    this.loadOperation = loadOperation;
    return this;
  }

  public String getUnloadOperation() {
    return unloadOperation;
  }
  /**
   * Sets the unload operation for the custom process model associated with the vehicle.
   *
   * @param unloadOperation The unload operation to set.
   * @return The updated CustomProcessModelTO object.
   */
  public CustomProcessModelTO setUnloadOperation(String unloadOperation) {
    this.unloadOperation = unloadOperation;
    return this;
  }

  public double getMaxAcceleration() {
    return maxAcceleration;
  }
  /**
   * Sets the maximum acceleration of the custom process model associated with the vehicle.
   *
   * @param maxAcceleration The new maximum acceleration to set.
   * @return The updated CustomProcessModelTO object.
   */
  public CustomProcessModelTO setMaxAcceleration(double maxAcceleration) {
    this.maxAcceleration = maxAcceleration;
    return this;
  }

  public double getMaxDeceleration() {
    return maxDeceleration;
  }
  /**
   * Sets the maximum deceleration for the custom process model associated with the vehicle.
   *
   * @param maxDeceleration The maximum deceleration to set.
   * @return The updated CustomProcessModelTO object.
   */
  public CustomProcessModelTO setMaxDeceleration(double maxDeceleration) {
    this.maxDeceleration = maxDeceleration;
    return this;
  }

  public double getMaxFwdVelocity() {
    return maxFwdVelocity;
  }
  /**
   * Sets the maximum forward velocity of the custom process model associated with the vehicle.
   *
   * @param maxFwdVelocity The new maximum forward velocity to set.
   * @return The updated CustomProcessModelTO object.
   */
  public CustomProcessModelTO setMaxFwdVelocity(double maxFwdVelocity) {
    this.maxFwdVelocity = maxFwdVelocity;
    return this;
  }

  public double getMaxRevVelocity() {
    return maxRevVelocity;
  }
  /**
   * Sets the maximum reverse velocity of the custom process model associated with the vehicle.
   *
   * @param maxRevVelocity The new maximum reverse velocity to set.
   * @return The updated CustomProcessModelTO object.
   */
  public CustomProcessModelTO setMaxRevVelocity(double maxRevVelocity) {
    this.maxRevVelocity = maxRevVelocity;
    return this;
  }
}
