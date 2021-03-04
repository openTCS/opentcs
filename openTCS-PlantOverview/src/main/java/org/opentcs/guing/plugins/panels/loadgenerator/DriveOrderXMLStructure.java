/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * A class to save <code>DriveOrderStructure</code>s.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
@XmlType(propOrder = {"driveOrderLocation", "driveOrderVehicleOperation"})
@XmlSeeAlso(TransportOrderTableModel.class)
public class DriveOrderXMLStructure {

  /**
   * The reference to this drive order's location.
   */
  private String location;
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
  public DriveOrderXMLStructure(String referenceToLocation,
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
  public DriveOrderXMLStructure() {
    location = null;
    vehicleOperation = null;
  }

  /**
   * Returns a reference to the location.
   *
   * @return The reference to the location.
   */
  @XmlAttribute(name = "driveOrderLocation", required = true)
  public String getDriveOrderLocation() {
    return this.location;
  }

  /**
   * Sets the drive order location.
   *
   * @param loc The new location.
   */
  public void setDriveOrderLocation(String loc) {
    location = loc;
  }

  /**
   * Sets the drive order vehicle operation.
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
  @XmlAttribute(name = "driveOrderVehicleOperation", required = true)
  public String getDriveOrderVehicleOperation() {
    return this.vehicleOperation;
  }
}
