/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * A {@code VehicleProfile} contains properties of a vehicle that can be loaded
 * from and saved to a XML file using the
 * {@link org.opentcs.virtualvehicle.VehicleProfiles VehicleProfiles} class.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
@XmlAccessorType(XmlAccessType.NONE)
public class VehicleProfile {

  /* !!!NOTE!!!
   * When adding/removing properties to/from the VehicleProfile the following
   * things need to be updated:
   * - private member with JAXB annotation
   * - default and copy constructor
   * - equals and hashCode methods
   * - getter and setter
   */
  /**
   * A resource bundle for internationalization.
   */
  private static final ResourceBundle bundle
      = ResourceBundle.getBundle("org/opentcs/virtualvehicle/Bundle");
  /**
   * Name of the profile.
   */
  @XmlAttribute(name = "name", required = true)
  private String name;
  /**
   * Capacity of the vehicle.
   */
  @XmlAttribute(name = "capacity", required = true)
  private double capacity;
  /**
   * Energy usage of the vehicle in idle state.
   */
  @XmlAttribute(name = "idlePower", required = true)
  private double idlePower;
  /**
   * Energy usage of the vehicle during movement.
   */
  @XmlAttribute(name = "movementPower", required = true)
  private double movementPower;
  /**
   * Energy usage of the vehicle during operation.
   */
  @XmlAttribute(name = "operationPower", required = true)
  private double operationPower;
  /**
   * Maximum forward velocity of the vehicle.
   */
  @XmlAttribute(name = "forwardVelocity", required = true)
  private int fwdVelocity;
  /**
   * Maximum reverse velocity of the vehicle.
   */
  @XmlAttribute(name = "reverseVelocity", required = true)
  private int revVelocity;
  /**
   * Maximum acceleration of the vehicle.
   */
  @XmlAttribute(name = "acceleration", required = true)
  private int acceleration;
  /**
   * Maximum deceleration of the vehicle.
   */
  @XmlAttribute(name = "deceleration", required = true)
  private int deceleration;
  /**
   * Default operating time of the vehicle for unspecified operations.
   */
  @XmlAttribute(name = "defaultOperatingTime", required = true)
  private int defaultOpTime;
  /**
   * List of optional operation specifications of the vehicle.
   */
  @XmlElement(name = "operationSpecification", required = false)
  private List<OperationSpec> opSpecs = new LinkedList<>();

  /**
   * Creates a new VehicleProfile.
   *
   * @param name name of the profile
   */
  public VehicleProfile(String name) {
    this();
    this.name = name;
  }

  /**
   * Copy construtor. Creates a new {@code VehicleProfile} from an existing one.
   *
   * @param profile VehicleProfile, or null if profile is null
   */
  public VehicleProfile(VehicleProfile profile) {
    name = profile.name;
    capacity = profile.capacity;
    idlePower = profile.idlePower;
    movementPower = profile.movementPower;
    operationPower = profile.operationPower;
    fwdVelocity = profile.fwdVelocity;
    revVelocity = profile.revVelocity;
    acceleration = profile.acceleration;
    deceleration = profile.deceleration;
    defaultOpTime = profile.defaultOpTime;
    List<OperationSpec> otherOpSpecList = profile.getOpSpecs();
    for (OperationSpec spec : otherOpSpecList) {
      opSpecs.add(new OperationSpec(spec));
    }
  }

  /**
   * Create the default profile.
   */
  public VehicleProfile() {
    name = bundle.getString("defaultVehicleProfile");
    capacity = 1000;
    idlePower = 0;
    movementPower = 0;
    operationPower = 0;
    fwdVelocity = 1000;
    revVelocity = -1000;
    acceleration = 500;
    deceleration = -500;
    defaultOpTime = 1000;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VehicleProfile)) {
      return false;
    }
    VehicleProfile other = (VehicleProfile) obj;
    if (!name.equals(other.name)) {
      return false;
    }
    else if (capacity != other.capacity) {
      return false;
    }
    else if (idlePower != other.idlePower) {
      return false;
    }
    else if (movementPower != other.movementPower) {
      return false;
    }
    else if (operationPower != other.operationPower) {
      return false;
    }
    else if (fwdVelocity != other.fwdVelocity) {
      return false;
    }
    else if (revVelocity != other.revVelocity) {
      return false;
    }
    else if (acceleration != other.acceleration) {
      return false;
    }
    else if (deceleration != other.deceleration) {
      return false;
    }
    else if (defaultOpTime != other.defaultOpTime) {
      return false;
    }
    else {
      return true;
    }
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 29 * hash + Objects.hashCode(this.name);
    hash = 29 * hash + (int) (Double.doubleToLongBits(this.capacity) 
        ^ (Double.doubleToLongBits(this.capacity) >>> 32));
    hash = 29 * hash + (int) (Double.doubleToLongBits(this.idlePower) 
        ^ (Double.doubleToLongBits(this.idlePower) >>> 32));
    hash = 29 * hash + (int) (Double.doubleToLongBits(this.movementPower) 
        ^ (Double.doubleToLongBits(this.movementPower) >>> 32));
    hash = 29 * hash + (int) (Double.doubleToLongBits(this.operationPower) 
        ^ (Double.doubleToLongBits(this.operationPower) >>> 32));
    hash = 29 * hash + this.fwdVelocity;
    hash = 29 * hash + this.revVelocity;
    hash = 29 * hash + this.acceleration;
    hash = 29 * hash + this.deceleration;
    hash = 29 * hash + this.defaultOpTime;
    return hash;
  }

  /**
   * Set the vehicle's name.
   *
   * @param name the name
   */
  public void setName(String name) {
    this.name = Objects.requireNonNull(name, "VehicleProfile name is null.");
  }

  /**
   * Get the vehicle's name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Set the vehicle's capacity.
   *
   * @param capacity the capacity
   */
  public void setCapacity(double capacity) {
    this.capacity = capacity;
  }

  /**
   * Get the vehicle's capacity.
   *
   * @return the capacity
   */
  public double getCapacity() {
    return capacity;
  }

  /**
   * Set the vehicle's idle power.
   *
   * @param idlePower the idle power 
   */
  public void setIdlePower(double idlePower) {
    this.idlePower = idlePower;
  }

  /**
   * Get the vehicle's idle power.
   *
   * @return the idle power 
   */
  public double getIdlePower() {
    return idlePower;
  }

  /**
   * Set the vehicle's movement power.
   *
   * @param movementPower the movement power 
   */
  public void setMovementPower(double movementPower) {
    this.movementPower = movementPower;
  }

  /**
   * Get the vehicle's movement power.
   *
   * @return the movement power 
   */
  public double getMovementPower() {
    return movementPower;
  }

  /**
   * Set the vehicle's operation power.
   *
   * @param operationPower the operation power 
   */
  public void setOperationPower(double operationPower) {
    this.operationPower = operationPower;
  }

  /**
   * Get the vehicle's operation power.
   *
   * @return the operation power 
   */
  public double getOperationPower() {
    return operationPower;
  }

  /**
   * Set the vehicle's max forward velocity.
   *
   * @param velocity the velocity
   */
  public void setFwdVelocity(int velocity) {
    fwdVelocity = velocity;
  }

  /**
   * Get the vehicle's max forward velocity.
   *
   * @return the velocity
   */
  public int getFwdVelocity() {
    return fwdVelocity;
  }

  /**
   * Set the vehicle's max reverse velocity.
   *
   * @param velocity the velocity
   */
  public void setRevVelocity(int velocity) {
    revVelocity = velocity;
  }

  /**
   * Get the vehicle's max reverse velocity.
   *
   * @return the velocity
   */
  public int getRevVelocity() {
    return revVelocity;
  }

  /**
   * Set the vehicle's max acceleration.
   *
   * @param acceleration the acceleration
   */
  public void setAcceleration(int acceleration) {
    this.acceleration = acceleration;
  }

  /**
   * Get the vehicle's max acceleration.
   *
   * @return the acceleration
   */
  public int getAcceleration() {
    return acceleration;
  }

  /**
   * Set the vehicle's max deceleration.
   *
   * @param deceleration the deceleration
   */
  public void setDeceleration(int deceleration) {
    this.deceleration = deceleration;
  }

  /**
   * Get the vehicle's max deceleration.
   *
   * @return the deceleration
   */
  public int getDeceleration() {
    return deceleration;
  }

  /**
   * Set the vehicle's default operating time.
   *
   * @param time the operating time in ms
   */
  public void setDefaultOpTime(int time) {
    this.defaultOpTime = time;
  }

  /**
   * Get the vehicle's default operating time.
   *
   * @return the operating time in ms
   */
  public int getDefaultOpTime() {
    return defaultOpTime;
  }

  /**
   * Set the operation specifications.
   *
   * @param opSpecs the list of specifications
   */
  public void setOpSpecs(List<OperationSpec> opSpecs) {
    if (opSpecs != null) {
      this.opSpecs = opSpecs;
    }
  }

  /**
   * Get the operation specifications.
   *
   * @return list of specifications
   */
  public List<OperationSpec> getOpSpecs() {
    return opSpecs;
  }
}
