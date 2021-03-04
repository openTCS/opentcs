/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator;

import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;

/**
 * This class provides the data structure for storing
 * and holding the elements typically drive order consists of:
 * location and available operation type at this location.
 *
 * @author Iryna Felko (Fraunhofer IML)
 */
public class DriveOrderStructure {

  /**
   * The reference to this drive order's location.
   */
  private TCSObjectReference<Location> location;
  /**
   * The available oparation type at the location.
   */
  private String vehicleOperation;

  /**
   * Creates the new instance of DriveOrderStructure.
   *
   * @param referenceToLocation The reference to the drive order's location.
   * @param newVehicleOperation The available operation type at the location.
   */
  public DriveOrderStructure(TCSObjectReference<Location> referenceToLocation,
                             String newVehicleOperation) {
    if (referenceToLocation == null) {
      throw new IllegalArgumentException("location argument is null!");
    }
    if (newVehicleOperation == null) {
      throw new IllegalArgumentException("vehicleOperation argument is null!");
    }
    location = referenceToLocation;
    vehicleOperation = newVehicleOperation;
  }

  /**
   * Creates an empty DriveOrderStructure.
   */
  public DriveOrderStructure() {
    location = null;
    vehicleOperation = null;
  }

  /**
   * Returns a reference to the location.
   *
   * @return The reference to the location.
   */
  public TCSObjectReference<Location> getDriveOrderLocation() {
    return this.location;
  }

  /**
   * Sets the location of this order.
   *
   * @param loc The new location
   */
  public void setDriveOrderLocation(TCSObjectReference<Location> loc) {
    location = loc;
  }

  /**
   * Sets the vehicle operation of this order.
   *
   * @param op The new operation
   */
  public void setDriveOrderVehicleOperation(String op) {
    vehicleOperation = op;
  }

  /**
   * Returns this drive order's operation type.
   *
   * @return The drive order's operation type.
   */
  public String getDriveOrderVehicleOperation() {
    return this.vehicleOperation;
  }
}
