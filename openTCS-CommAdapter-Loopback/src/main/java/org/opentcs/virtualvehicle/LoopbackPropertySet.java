/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.util.configuration.ConfigurationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A set of properties/configuration elements for a virtual vehicle.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@XmlRootElement
public final class LoopbackPropertySet {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      LoggerFactory.getLogger(LoopbackPropertySet.class);
  /**
   * This class's ConfigurationStore.
   */
  private static final ConfigurationStore configStore =
      ConfigurationStore.getStore(LoopbackPropertySet.class.getName());
  /**
   * The maximum allowed size for input files (in bytes). Files larger than
   * this will not be parsed.
   */
  private static final int maxAllowedFileSize;
  /**
   * The vehicle's maximum acceleration.
   */
  private int maxAcceleration;
  /**
   * The vehicle's maximum deceleration.
   */
  private int maxDeceleration;
  /**
   * The vehicle's maximum forward velocity.
   */
  private int maxForwardVelocity;
  /**
   * The vehicle's maximum reverse velocity.
   */
  private int maxReverseVelocity;
  /**
   * The default time the vehicle needs for operations not explicitly defined.
   */
  private int defaultOperatingTime;
  /**
   * Explicit definitions of additional specifications for operations.
   */
  private List<PropOperationSpec> operationSpecs = new LinkedList<>();

  static {
    maxAllowedFileSize = configStore.getInt("maxAllowedFileSize", 100 * 1024);
  }

  /**
   * Creates a new LoopbackPropertySet.
   */
  public LoopbackPropertySet() {
  }

  /**
   * Returns the default operating time.
   * 
   * @return The default operating time
   */
  @XmlAttribute(name = "defaultOperatingTime", required = true)
  public int getDefaultOperatingTime() {
    return defaultOperatingTime;
  }

  /**
   * Sets the default operating time.
   * 
   * @param defaultOperatingTime The new default operating time
   */
  public void setDefaultOperatingTime(int defaultOperatingTime) {
    this.defaultOperatingTime = defaultOperatingTime;
  }

  /**
   * Returns the maximum acceleration.
   * 
   * @return The maximum acceleration
   */
  @XmlAttribute(name = "maxAcceleration", required = true)
  public int getMaxAcceleration() {
    return maxAcceleration;
  }

  /**
   * Sets the maximum acceleration.
   * 
   * @param maxAcceleration The new maximum acceleration
   */
  public void setMaxAcceleration(int maxAcceleration) {
    this.maxAcceleration = maxAcceleration;
  }

  /**
   * Returns the maximum deceleration.
   * 
   * @return The maximum deceleration
   */
  @XmlAttribute(name = "maxDeceleration", required = true)
  public int getMaxDeceleration() {
    return maxDeceleration;
  }

  /**
   * Sets the maximum deceleration.
   * 
   * @param maxDeceleration The new maximum deceleration
   */
  public void setMaxDeceleration(int maxDeceleration) {
    this.maxDeceleration = maxDeceleration;
  }

  /**
   * Returns the maximum forward velocity.
   * 
   * @return The maximum forward velocity
   */
  @XmlAttribute(name = "maxForwardVelocity", required = true)
  public int getMaxForwardVelocity() {
    return maxForwardVelocity;
  }

  /**
   * Sets the maximum forward velocity.
   * 
   * @param maxForwardVelocity The new maximum forward velocity
   */
  public void setMaxForwardVelocity(int maxForwardVelocity) {
    this.maxForwardVelocity = maxForwardVelocity;
  }

  /**
   * Returns the maximum reverse velocity.
   * 
   * @return The maximum reverse velocity
   */
  @XmlAttribute(name = "maxReverseVelocity", required = true)
  public int getMaxReverseVelocity() {
    return maxReverseVelocity;
  }

  /**
   * Sets the maximum reverse velocity.
   * 
   * @param maxReverseVelocity The new maximum reverse velocity
   */
  public void setMaxReverseVelocity(int maxReverseVelocity) {
    this.maxReverseVelocity = maxReverseVelocity;
  }

  /**
   * Returns the operation specifications.
   * 
   * @return The operating specifications. 
   */
  @XmlElement(name = "operationSpec", required = false)
  public List<PropOperationSpec> getOperationSpecs() {
    return operationSpecs;
  }

  /**
   * Sets the operation specifications.
   * 
   * @param operationSpecs The new operation specifications
   */
  public void setOperationSpecs(List<PropOperationSpec> operationSpecs) {
    this.operationSpecs = operationSpecs;
  }

  /**
   * Writes this LoopbackPropertySet to an XML file.
   * 
   * @return The XML file
   */
  public String toXml() {
    StringWriter stringWriter = new StringWriter();
    try {
      // Als XML in eine Datei schreiben.
      JAXBContext jc = JAXBContext.newInstance(LoopbackPropertySet.class);
      Marshaller marshaller = jc.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(this, stringWriter);
    }
    catch (JAXBException exc) {
      log.warn("Exception marshalling data", exc);
      throw new IllegalStateException("Exception marshalling data", exc);
    }
    return stringWriter.toString();
  }

  /**
   * Writes a file.
   * 
   * @param file The file to be written
   * @throws IOException If an exception occured while writing the file
   */
  public void toFile(File file)
      throws IOException {
    if (file == null) {
      throw new NullPointerException("file is null");
    }
    OutputStream outStream = new FileOutputStream(file);
    outStream.write(toXml().getBytes());
    outStream.flush();
    outStream.close();
  }

  /**
   * Reads a LoopbackPropertySet from an XML string.
   * 
   * @param xmlData The XML data.
   * @return The read LoopbackPropertySet
   */
  public static LoopbackPropertySet fromXml(String xmlData) {
    if (xmlData == null) {
      throw new NullPointerException("xmlData is null");
    }
    StringReader stringReader = new StringReader(xmlData);
    try {
      JAXBContext jc = JAXBContext.newInstance(LoopbackPropertySet.class);
      Unmarshaller unmarshaller = jc.createUnmarshaller();
      Object o = unmarshaller.unmarshal(stringReader);
      return (LoopbackPropertySet) o;
    }
    catch (JAXBException exc) {
      log.warn("Exception unmarshalling data", exc);
      throw new IllegalStateException("Exception unmarshalling data", exc);
    }
  }

  /**
   * Returns a LoopbackPropertySet from a file.
   * 
   * @param sourceFile The file containing the XML file.
   * @return The read LoopbackPropertySet
   * @throws IOException If an exception occured while reading the file
   */
  public static LoopbackPropertySet fromFile(File sourceFile)
      throws IOException {
    if (sourceFile == null) {
      throw new NullPointerException("sourceFile is null");
    }
    final String path = sourceFile.getAbsolutePath();
    if (!sourceFile.isFile() || !sourceFile.canRead()) {
      throw new IOException(path + ": file not a regular file or unreadable");
    }
    if (sourceFile.length() > maxAllowedFileSize) {
      throw new IOException(path + ": file larger than allowed: "
          + sourceFile.length() + " > " + maxAllowedFileSize);
    }
    int fileSize = (int) sourceFile.length();
    byte[] buffer = new byte[fileSize];
    InputStream inStream = new FileInputStream(sourceFile);
    try {
      int bytesRead = inStream.read(buffer);
      if (bytesRead != fileSize) {
        throw new IOException("read() returned unexpected value: " + bytesRead
            + ", should be :" + fileSize);
      }
    }
    finally {
      inStream.close();
    }
    String fileContent = new String(buffer);
    return fromXml(fileContent);
  }

  /**
   * Nested property for OperationSpecs.
   */
  public static final class PropOperationSpec {

    /**
     * The name of the operation.
     */
    private String operation;
    /**
     * The time required for the operation.
     */
    private int operatingTime;
    /**
     * Load handling devices of the vehicle after this operation.
     */
    private List<PropLoadHandlingDevice> loadHandlingDevices = new LinkedList<>();
    /**
     * Does the operation change the current load handling devices of the vehicle?
     */
    private boolean changesLoadCondition;

    /**
     * Creates a new instance.
     */
    public PropOperationSpec() {
      // Do nada
    }

    /**
     * Constructs a new instance from the given <code>OperationSpec</code>.
     * @param operationSpec The original OperationSpec.
     */
    public PropOperationSpec(OperationSpec operationSpec) {
      setOperation(operationSpec.getOperationName());
      setOperatingTime(operationSpec.getOperatingTime());
      setChangesLoadCondition(operationSpec.changesLoadCondition());
      if (operationSpec.changesLoadCondition()) {
        List<PropLoadHandlingDevice> devices = new LinkedList<>();
        for (LoadHandlingDevice device
             : operationSpec.getLoadCondition()) {
          devices.add(new PropLoadHandlingDevice(device));
        }
        setLoadHandlingDevices(devices);
      }
    }

    /**
     * Creates an instance of <code>OperationSpec</code> with the 
     * properties in this <code>PropOperationSpec</code>.
     * @return an <code>OperationSpec</code>
     */
    public OperationSpec toOperationSpec() {
      OperationSpec opSpec;
      String name = getOperation();
      int time = getOperatingTime();
      if (changesLoadCondition) {
        List<LoadHandlingDevice> devices = new LinkedList<>();
        // Create LoadHandlingDevices
        for (PropLoadHandlingDevice deviceProp : getLoadHandlingDevices()) {
          devices.add(deviceProp.toLoadHandlingDevice());
        }
        opSpec = new OperationSpec(name, time, devices);
      }
      else {
        opSpec = new OperationSpec(name, time);
      }
      return opSpec;
    }

    /**
     * Returns the operating time.
     * 
     * @return The operating time
     */
    @XmlAttribute(name = "operatingTime", required = true)
    public int getOperatingTime() {
      return operatingTime;
    }

    /**
     * Sets the operating time.
     * 
     * @param operatingTime The new operating time
     */
    public void setOperatingTime(int operatingTime) {
      this.operatingTime = operatingTime;
    }

    /**
     * Returns the operation.
     * 
     * @return The operation
     */
    @XmlAttribute(name = "operation", required = true)
    public String getOperation() {
      return operation;
    }

    /**
     * Sets the operation.
     * 
     * @param operation The new operation
     */
    public void setOperation(String operation) {
      this.operation = operation;
    }

    /**
     * Gets the load handling devices.
     * @return The load handling devices.
     */
    @XmlElement(name = "loadHandlingDevice", required = false)
    public List<PropLoadHandlingDevice> getLoadHandlingDevices() {
      return loadHandlingDevices;
    }

    /**
     * Sets the load handling devices.
     * @param loadHandlingDevices The new load handling devices.
     */
    public void setLoadHandlingDevices(
        List<PropLoadHandlingDevice> loadHandlingDevices) {
      this.loadHandlingDevices = loadHandlingDevices;
    }

    /**
     * Get whether the load condition is changed.
     * @return changesLoadCondition
     */
    @XmlAttribute(name = "changesLoadCondition", required = true)
    public boolean getChangesLoadCondition() {
      return changesLoadCondition;
    }

    /**
     * Set the changesLoadCondition flag.
     * @param changesLoadCondition changesLoadCondition flag. 
     */
    public void setChangesLoadCondition(boolean changesLoadCondition) {
      this.changesLoadCondition = changesLoadCondition;
    }
  }

  /**
   * Nested property for load handling devices.
   */
  public static final class PropLoadHandlingDevice {

    /**
     * A name/label for this device.
     */
    private String label;
    /**
     * A flag indicating whether this device is filled to its maximum capacity or
     * not.
     */
    private boolean full;

    /**
     * Creates a new instance.
     */
    public PropLoadHandlingDevice() {
      // Do nada.
    }

    /**
     * Construct a new instance from the given <code>LoadHandlingDevice</code>.
     * @param device The original device.
     */
    public PropLoadHandlingDevice(LoadHandlingDevice device) {
      setLabel(device.getLabel());
      setFull(device.isFull());
    }

    /**
     * Creates an instance of <code>LoadHandlingDevice</code> with the 
     * properties in this <code>PropLoadHandlingDevice</code>.
     * @return a <code>LoadHandlingDevice</code>
     */
    public LoadHandlingDevice toLoadHandlingDevice() {
      return new LoadHandlingDevice(getLabel(), getFull());
    }

    /**
     * Set the label.
     * @param label The label
     */
    public void setLabel(String label) {
      this.label = label;
    }

    /**
     * Returns the label.
     * @return The label 
     */
    @XmlAttribute(name = "label", required = true)
    public String getLabel() {
      return label;
    }

    /**
     * Sets the full-flag.
     * @param full The full-flag.
     */
    public void setFull(boolean full) {
      this.full = full;
    }

    /**
     * Returns the full-flag.
     * @return The full-flag
     */
    @XmlAttribute(name = "full", required = true)
    public boolean getFull() {
      return full;
    }
  }
}
