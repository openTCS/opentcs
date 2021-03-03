/*
 * openTCS copyright information:
 * Copyright (c) 2008 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.xmlorders.binding;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A set of responses sent by the kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@XmlRootElement
public class TCSResponseSet {

  /**
   * A list of <code>TCSResponse</code>s.
   */
  private List<TCSResponse> responses = new LinkedList<>();

  /**
   * Creates a new instance.
   */
  public TCSResponseSet() {
    // Do nada.
  }

  /**
   * Returns the <code>TCSResponse</code>s.
   *
   * @return The list of responses.
   */
  @XmlElement(name = "response", required = true)
  public List<TCSResponse> getResponses() {
    return responses;
  }

  /**
   * Sets the <code>TCSResponse</code>s.
   *
   * @param responses The list of responses.
   */
  public void setResponses(List<TCSResponse> responses) {
    this.responses = responses;
  }

  /**
   * Marshals the data.
   *
   * @return The XML string.
   * @throws IllegalArgumentException If there was a problem marshalling this instance.
   */
  public String toXml()
      throws IllegalArgumentException {
    StringWriter stringWriter = new StringWriter();
    try {
      // Als XML in eine Datei schreiben.
      JAXBContext jc = JAXBContext.newInstance(TCSResponseSet.class,
                                               TransportResponse.class,
                                               ScriptResponse.class);
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
   * Reads a <code>TCSResponseSet</code> from an XML string.
   *
   * @param xmlData The XML data as a string.
   * @return The read <code>TCSResponseSet</code>.
   * @throws IllegalArgumentException If there was a problem unmarshalling the given string.
   */
  public static TCSResponseSet fromXml(String xmlData)
      throws IllegalArgumentException {
    requireNonNull(xmlData, "xmlData");

    StringReader stringReader = new StringReader(xmlData);
    try {
      JAXBContext jc = JAXBContext.newInstance(TCSResponseSet.class);
      Unmarshaller unmarshaller = jc.createUnmarshaller();
      Object o = unmarshaller.unmarshal(stringReader);
      return (TCSResponseSet) o;
    }
    catch (JAXBException exc) {
      throw new IllegalArgumentException("Exception unmarshalling data", exc);
    }
  }
}
