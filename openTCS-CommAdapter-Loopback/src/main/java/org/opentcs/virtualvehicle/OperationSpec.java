/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;

/**
 * Additional information for a specific operation. This specification is used
 * by the virtual vehicle while processing the operation to simulate it`s
 * effects.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
@XmlType
// Prevent implicit serialization of public fields and JavaBean properties:
@XmlAccessorType(XmlAccessType.NONE)
public final class OperationSpec {

  /**
   * Name of the operation.
   */
  @XmlAttribute(name = "name", required = true)
  private final String operation;
  /**
   * Time needed for executing this oepration.
   */
  @XmlAttribute(name = "operatingTime", required = true)
  private final int operatingTime;
  /**
   * Load handling devices of the vehicle after this operation.
   */
  private List<LoadHandlingDevice> loadCondition;
  /**
   * Does the operation change the current load handling devices of the vehicle?
   */
  @XmlAttribute(name = "changesLoadCondition", required = false)
  private boolean changesLoadCondition;

  /**
   * Construct a new OperationSpec with default values.
   * This default constructor is primarily meant for JAXB.
   */
  private OperationSpec() {
    operation = "";
    operatingTime = 0;
    loadCondition = new LinkedList<>();
    changesLoadCondition = false;
  }

  /**
   * Construct a new OperationSpec with the specified operatingTime. The
   * operation will have no effect on the load handling devices of the operating
   * vehicle.
   *
   * @param operation The operation's name.
   * @param operatingTime Time needed for executing the operation.
   */
  public OperationSpec(String operation, int operatingTime) {
    this.operation = Objects.requireNonNull(operation, "operation name is null");
    this.operatingTime = operatingTime;
    loadCondition = new LinkedList<>();
    changesLoadCondition = false;
  }

  /**
   * Construct a new OperationSpec.
   *
   * @param operation The operation's name.
   * @param operatingTime Time needed for executing the operation.
   * @param loadCondition Load handling devices of the vehicle after executing
   * this operation.
   */
  public OperationSpec(String operation, int operatingTime,
                       List<LoadHandlingDevice> loadCondition) {
    this(operation, operatingTime);
    this.loadCondition = loadCondition;
    changesLoadCondition = true;
  }

  /**
   * Copy constructor.
   *
   * @param opSpec Original instance
   */
  public OperationSpec(OperationSpec opSpec) {
    this(opSpec.getOperationName(), opSpec.getOperatingTime());
    if (opSpec.changesLoadCondition()) {
      changesLoadCondition = true;
      // Load handling devices are immutable, so we can copy construct the list
      loadCondition = new LinkedList<>(opSpec.getLoadCondition());
    }
  }

  /**
   * Get the operation's name.
   *
   * @return The operation's name.
   */
  public String getOperationName() {
    return operation;
  }

  /**
   * Get the time needed to execute the associated operation.
   *
   * @return Operating time
   */
  public int getOperatingTime() {
    return operatingTime;
  }

  /**
   * Get the load handling devices and their states after this operation.
   *
   * @return List of load handling devices. Might be empty.
   */
  public List<LoadHandlingDevice> getLoadCondition() {
    return loadCondition;
  }

  /**
   * True, if the operation changes the load handling devices of the operating
   * vehicle (as specified by <code>getLoadCondition()</code>. Otherwise the
   * operation has no effect on the vehicle's load handling devices and
   * <code>getLoadCondition()</code> can be ignored.
   *
   * @return Does the operation changes the load handling devices?
   */
  public boolean changesLoadCondition() {
    return changesLoadCondition;
  }

  /**
   * Return a list of JAXB compatible load handling device objects.
   *
   * @return list of devices
   */
  @XmlElement(name = "loadHandlingDevice", required = false)
  private List<LoadHandlingDeviceXml> getLoadConditionXml() {
    List<LoadHandlingDeviceXml> returnedList = new LinkedList<>();
    for (LoadHandlingDevice device : loadCondition) {
      returnedList.add(new LoadHandlingDeviceXml(device));
    }
    return returnedList;
  }

  /**
   * Set the LoadHandlingDevices via a list of LoadHandlingDeviceXML.
   *
   * @param devices JAXB compatible list of load handling devices
   */
  private void setLoadConditionXml(List<LoadHandlingDeviceXml> devices) {
    loadCondition.clear();
    for (LoadHandlingDeviceXml device : devices) {
      loadCondition.add(device.toLoadHandlingDevice());
    }
  }

  @Override
  public String toString() {
    return getOperationName();
  }

  /**
   * Class that provides a JAXB interface to a <code>LoadHandlingDevice</code>.
   */
  @XmlAccessorType(XmlAccessType.NONE)
  private static final class LoadHandlingDeviceXml {

    /**
     * A name/label for this device.
     */
    @XmlAttribute(name = "label", required = true)
    private String label;
    /**
     * A flag indicating whether this device is filled to its maximum capacity
     * or not.
     */
    @XmlAttribute(name = "full", required = true)
    private boolean full;

    /**
     * Creates a new instance.
     */
    public LoadHandlingDeviceXml() {
      // Do nada.
    }

    /**
     * Construct a new instance from the given <code>LoadHandlingDevice</code>.
     *
     * @param device The original device.
     */
    public LoadHandlingDeviceXml(LoadHandlingDevice device) {
      setLabel(device.getLabel());
      setFull(device.isFull());
    }

    /**
     * Creates an instance of <code>LoadHandlingDevice</code> with the
     * properties in this <code>PropLoadHandlingDevice</code>.
     *
     * @return a <code>LoadHandlingDevice</code>
     */
    public LoadHandlingDevice toLoadHandlingDevice() {
      return new LoadHandlingDevice(getLabel(), getFull());
    }

    /**
     * Set the label.
     *
     * @param label The label
     */
    public void setLabel(String label) {
      this.label = label;
    }

    /**
     * Returns the label.
     *
     * @return The label
     */
    public String getLabel() {
      return label;
    }

    /**
     * Sets the full-flag.
     *
     * @param full The full-flag.
     */
    public void setFull(boolean full) {
      this.full = full;
    }

    /**
     * Returns the full-flag.
     *
     * @return The full-flag
     */
    public boolean getFull() {
      return full;
    }
  }
}
