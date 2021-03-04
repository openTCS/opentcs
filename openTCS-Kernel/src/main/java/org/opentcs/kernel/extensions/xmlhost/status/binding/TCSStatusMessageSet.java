/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.xmlhost.status.binding;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;
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
import org.xml.sax.SAXException;

/**
 * A set of status messages sent via the status channel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@XmlRootElement
public class TCSStatusMessageSet {

  /**
   * The point of time at which this status message set was created.
   */
  private Date timeStamp = new Date();
  /**
   * The status messages.
   */
  private List<StatusMessage> statusMessages = new LinkedList<>();

  /**
   * Creates a new TCSStatusMessageSet.
   */
  public TCSStatusMessageSet() {
  }

  /**
   * Returns the status messages.
   *
   * @return The status messages.
   */
  @XmlElement(name = "statusMessage", required = true)
  public List<StatusMessage> getStatusMessages() {
    return statusMessages;
  }

  /**
   * Sets the status messages.
   *
   * @param statusMessages The status messages.
   */
  public void setStatusMessages(List<StatusMessage> statusMessages) {
    this.statusMessages = requireNonNull(statusMessages, "statusMessages");
  }

  /**
   * Returns the time stamp.
   *
   * @return The time stamp.
   */
  @XmlAttribute(name = "timeStamp", required = true)
  public Date getTimeStamp() {
    return timeStamp;
  }

  /**
   * Sets the time stamp.
   *
   * @param timeStamp The time stamp.
   */
  public void setTimeStamp(Date timeStamp) {
    this.timeStamp = requireNonNull(timeStamp, "timeStamp");
  }

  /**
   * Marshals this instance to its XML representation and writes it to the given writer.
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
  public static TCSStatusMessageSet fromXml(@Nonnull Reader reader)
      throws IOException {
    requireNonNull(reader, "reader");

    try {
      return (TCSStatusMessageSet) createUnmarshaller().unmarshal(reader);
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
    return JAXBContext.newInstance(TCSStatusMessageSet.class,
                                   StatusMessage.class,
                                   OrderStatusMessage.class,
                                   VehicleStatusMessage.class);
  }
}
