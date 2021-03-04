/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.xmlhost.status.binding;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
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
   * Marshals this instance to its XML representation and returns that in a string.
   *
   * @return A <code>String</code> containing the XML representation of this instance.
   * @throws IllegalArgumentException If there was a problem marshalling this instance.
   */
  public String toXml()
      throws IllegalArgumentException {
    StringWriter stringWriter = new StringWriter();
    try {
      // Als XML in eine Datei schreiben.
      JAXBContext jc = JAXBContext.newInstance(TCSStatusMessageSet.class,
                                               StatusMessage.class,
                                               OrderStatusMessage.class,
                                               VehicleStatusMessage.class);
      Marshaller marshaller = jc.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(this, stringWriter);
    }
    catch (JAXBException exc) {
      throw new IllegalArgumentException("Exception marshalling data", exc);
    }
    return stringWriter.toString();
  }

  /**
   * Unmarshals an instance of this class from the given XML representation.
   *
   * @param xmlData A <code>String</code> containing the XML representation.
   * @return The status instance unmarshalled from the given String.
   * @throws IllegalArgumentException If there was a problem unmarshalling the given string.
   */
  public static TCSStatusMessageSet fromXml(String xmlData)
      throws IllegalArgumentException {
    requireNonNull(xmlData, "xmlData");

    StringReader stringReader = new StringReader(xmlData);
    try {
      JAXBContext jc = JAXBContext.newInstance(TCSStatusMessageSet.class,
                                               StatusMessage.class,
                                               OrderStatusMessage.class,
                                               VehicleStatusMessage.class);
      Unmarshaller unmarshaller = jc.createUnmarshaller();
      Object o = unmarshaller.unmarshal(stringReader);
      return (TCSStatusMessageSet) o;
    }
    catch (JAXBException exc) {
      throw new IllegalArgumentException("Exception unmarshalling data", exc);
    }
  }
}
