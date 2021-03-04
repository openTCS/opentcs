/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.xmlhost.orders.binding;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.xml.sax.SAXException;

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
   * Marshals this instance to its XML representation.
   *
   * @param writer The writer to write this instance's XML representation to.
   * @throws IOException If there was a problem marshalling this instance.
   */
  public void toXml(@Nonnull Writer writer)
      throws IOException {
    try {
      Marshaller marshaller = createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(this, writer);
    }
    catch (JAXBException | SAXException exc) {
      throw new IOException("Exception marshalling data", exc);
    }
  }

  /**
   * Unmarshals an instance of this class from the given XML representation.
   *
   * @param reader Provides the XML representation to parse to an instance.
   * @return The instance unmarshalled from the given reader.
   * @throws IOException If there was a problem unmarshalling the given string.
   */
  public static TCSScriptFile fromXml(@Nonnull Reader reader)
      throws IOException {
    requireNonNull(reader, "reader");

    try {
      return (TCSScriptFile) createUnmarshaller().unmarshal(reader);
    }
    catch (JAXBException | SAXException exc) {
      throw new IOException("Exception unmarshalling data", exc);
    }
  }

  private static Marshaller createMarshaller()
      throws JAXBException, SAXException {
    Marshaller marshaller = createContext().createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    return marshaller;
  }

  private static Unmarshaller createUnmarshaller()
      throws JAXBException, SAXException {
    Unmarshaller unmarshaller = createContext().createUnmarshaller();
    return unmarshaller;
  }

  private static JAXBContext createContext()
      throws JAXBException {
    return JAXBContext.newInstance(TCSScriptFile.class);
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
