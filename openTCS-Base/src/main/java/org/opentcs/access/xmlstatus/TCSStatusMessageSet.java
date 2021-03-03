/*
 * openTCS copyright information:
 * Copyright (c) 2008 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.xmlstatus;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
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
    if (statusMessages == null) {
      throw new NullPointerException("statusMessages is null");
    }
    this.statusMessages = statusMessages;
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
    if (timeStamp == null) {
      throw new NullPointerException("timeStamp is null");
    }
    this.timeStamp = timeStamp;
  }

  /**
   * Marshals this instance to its XML representation and returns that in a
   * string.
   *
   * @return A <code>String</code> containing the XML representation of this
   * instance.
   */
  public String toXml() {
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
      throw new IllegalStateException("Exception marshalling data", exc);
    }
    return stringWriter.toString();
  }

  /**
   * Unmarshals an instance of this class from the given XML representation.
   *
   * @param xmlData A <code>String</code> containing the XML representation.
   * @return The status instance unmarshalled from the given String.
   */
  public static TCSStatusMessageSet fromXml(String xmlData) {
    if (xmlData == null) {
      throw new NullPointerException("xmlData is null");
    }
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
      throw new IllegalStateException("Exception unmarshalling data", exc);
    }
  }
}
