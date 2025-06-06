// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.plugins.panels.loadgenerator.xmlbinding;

import static java.util.Objects.requireNonNull;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Stores a drive order definition for XML marshalling/unmarshalling.
 */
@XmlType(propOrder = {"locationName", "vehicleOperation"})
public class DriveOrderEntry {

  /**
   * The name of this drive order's location.
   */
  private String locationName;
  /**
   * The operation to be executed at the location.
   */
  private String vehicleOperation;

  /**
   * Creates a new instance.
   *
   * @param locationName The reference to the drive order's location.
   * @param vehicleOperation The available operation type at the location.
   */
  public DriveOrderEntry(String locationName, String vehicleOperation) {
    this.locationName = requireNonNull(locationName, "locationName");
    this.vehicleOperation = requireNonNull(vehicleOperation, "vehicleOperation");
  }

  /**
   * Creates a new instance.
   */
  public DriveOrderEntry() {
  }

  /**
   * Returns a reference to the location.
   *
   * @return The reference to the location.
   */
  @XmlAttribute(name = "locationName", required = true)
  public String getLocationName() {
    return locationName;
  }

  /**
   * Sets the drive order location.
   *
   * @param locationName The new location.
   */
  public void setLocationName(String locationName) {
    this.locationName = locationName;
  }

  /**
   * Returns this drive order's operation.
   *
   * @return The drive order's operation.
   */
  @XmlAttribute(name = "vehicleOperation", required = true)
  public String getVehicleOperation() {
    return this.vehicleOperation;
  }

  /**
   * Sets the drive order vehicle operation.
   *
   * @param vehicleOperation The new operation
   */
  public void setVehicleOperation(String vehicleOperation) {
    this.vehicleOperation = vehicleOperation;
  }
}
