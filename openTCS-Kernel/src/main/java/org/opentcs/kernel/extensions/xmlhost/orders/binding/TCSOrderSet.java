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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.xml.sax.SAXException;

/**
 * A set of orders to be processed by the kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@XmlRootElement
public class TCSOrderSet {

  /**
   * The orders to be processed.
   */
  private List<TCSOrder> orders = new LinkedList<>();

  /**
   * Creates a new instance.
   */
  public TCSOrderSet() {
    // Do nada.
  }

  /**
   * Returns the orders to be processed.
   *
   * @return The orders to be processed.
   */
  @XmlElement(name = "order", required = true)
  public List<TCSOrder> getOrders() {
    return orders;
  }

  /**
   * Sets the orders to be processed.
   *
   * @param orders The orders to be processed.
   */
  public void setOrders(List<TCSOrder> orders) {
    this.orders = requireNonNull(orders, "orders");
  }

  /**
   * Marshals this instance to its XML representation.
   *
   * @param writer The writer to write this instance's XML representation to.
   * @throws IOException If there was a problem marshalling this instance.
   */
  public void toXml(@Nonnull Writer writer)
      throws IOException {
    requireNonNull(writer, "writer");

    try {
      createMarshaller().marshal(this, writer);
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
  public static TCSOrderSet fromXml(@Nonnull Reader reader)
      throws IOException {
    requireNonNull(reader, "reader");

    try {
      return (TCSOrderSet) createUnmarshaller().unmarshal(reader);
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
    return JAXBContext.newInstance(TCSOrderSet.class,
                                   Transport.class,
                                   TransportScript.class);
  }
}
