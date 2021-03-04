/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.xmlorders.binding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A script file containing orders.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@XmlRootElement
@XmlType(propOrder = {"sequentialDependencies", "orders"})
public class TCSScriptFile {

  /**
   * Whether or not the orders in this script are to be executed sequentially
   * (or in any order the dispatcher wants to process them in).
   */
  private boolean sequentialDependencies;
  /**
   * The orders to be processed.
   */
  private List<Order> orders = new LinkedList<>();

  /**
   * Creates a new instance.
   */
  public TCSScriptFile() {
    // Do nada.
  }

  /**
   * Returns the sequential dependencies.
   *
   * @return The dependencies.
   */
  @XmlAttribute(name = "sequentialDependencies", required = false)
  public boolean getSequentialDependencies() {
    return sequentialDependencies;
  }

  /**
   * Sets the sequential dependencies.
   *
   * @param sequentialDependencies The dependencies.
   */
  public void setSequentialDependencies(boolean sequentialDependencies) {
    this.sequentialDependencies = sequentialDependencies;
  }

  /**
   * Returns the orders.
   *
   * @return The orders.
   */
  @XmlElement(name = "order", required = true)
  public List<Order> getOrders() {
    return orders;
  }

  /**
   * Sets the orders.
   *
   * @param orders The new orders.
   */
  public void setOrders(List<Order> orders) {
    this.orders = orders;
  }

  /**
   * Marshals the data.
   *
   * @return The data as an XML string.
   */
  public String toXml() {
    StringWriter stringWriter = new StringWriter();
    try {
      // Als XML in eine Datei schreiben.
      JAXBContext jc = JAXBContext.newInstance(TCSScriptFile.class);
      Marshaller marshaller = jc.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(this, stringWriter);
    }
    catch (JAXBException exc) {
      throw new IllegalStateException("Exception marshalling data", exc);
    }
    return stringWriter.toString();
  }

  /**
   * Reads a <code>TCSScriptFile</code> from an XML string.
   *
   * @param xmlData XML data as a string.
   * @return The read <code>TCSScriptFile</code>.
   * @throws IOException If an exception occured while unmarshalling.
   */
  public static TCSScriptFile fromXml(String xmlData)
      throws IOException {
    if (xmlData == null) {
      throw new NullPointerException("xmlData is null");
    }
    StringReader stringReader = new StringReader(xmlData);
    try {
      JAXBContext jc = JAXBContext.newInstance(TCSScriptFile.class);
      Unmarshaller unmarshaller = jc.createUnmarshaller();
      Object o = unmarshaller.unmarshal(stringReader);
      return (TCSScriptFile) o;
    }
    catch (JAXBException exc) {
      throw new IOException("Exception unmarshalling data", exc);
    }
  }

  /**
   * Parses a <code>TCSScriptFile</code> from a file.
   *
   * @param sourceFile The source file.
   * @return The parsed <code>TCSScriptFile</code>.
   * @throws IOException If the file is invalid.
   */
  public static TCSScriptFile fromFile(File sourceFile)
      throws IOException {
    requireNonNull(sourceFile, "sourceFile");

    if (!sourceFile.isFile() || !sourceFile.canRead()) {
      throw new IOException(sourceFile.getAbsolutePath()
          + ": file is not a regular file or unreadable");
    }
    int fileSize = (int) sourceFile.length();
    InputStream inStream = new FileInputStream(sourceFile);
    byte[] buffer = new byte[fileSize];
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
   * A single order in a script file.
   */
  @XmlType(propOrder = {"destinations", "intendedVehicle"})
  public static class Order {

    /**
     * The list of destinations.
     */
    private List<Destination> destinations = new LinkedList<>();
    /**
     * The intended vehicle.
     */
    private String intendedVehicle;

    /**
     * Creates a new instance.
     */
    public Order() {
      // Do nada.
    }

    /**
     * Returns the destinations.
     *
     * @return The destinations.
     */
    @XmlElement(name = "destination", required = true)
    public List<Destination> getDestinations() {
      return destinations;
    }

    /**
     * Sets the destinations.
     *
     * @param destinations The new destinations.
     */
    public void setDestinations(List<Destination> destinations) {
      this.destinations = destinations;
    }

    /**
     * Returns the intended vehicle.
     *
     * @return The intended vehicle.
     */
    @XmlAttribute(name = "intendedVehicle", required = false)
    public String getIntendedVehicle() {
      return intendedVehicle;
    }

    /**
     * Sets the intended vehicle.
     *
     * @param intendedVehicle The new intended vehicle.
     */
    public void setIntendedVehicle(String intendedVehicle) {
      this.intendedVehicle = intendedVehicle;
    }
  }
}
