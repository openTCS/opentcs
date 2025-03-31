// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.plugins.panels.loadgenerator.xmlbinding;

import static java.util.Objects.requireNonNull;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
@XmlRootElement(name = "transportOrders")
@XmlType(propOrder = {"transportOrders"})
public class TransportOrdersDocument {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(TransportOrdersDocument.class);

  /**
   * The transport orders.
   */
  private final List<TransportOrderEntry> transportOrders = new ArrayList<>();

  /**
   * Creates a new instance.
   */
  public TransportOrdersDocument() {
  }

  @XmlElement(name = "transportOrder")
  public List<TransportOrderEntry> getTransportOrders() {
    return transportOrders;
  }

  /**
   * Marshals the data.
   *
   * @return Data as XML string
   */
  public String toXml() {
    StringWriter stringWriter = new StringWriter();
    try {
      JAXBContext jc = JAXBContext.newInstance(TransportOrdersDocument.class);
      Marshaller marshaller = jc.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(this, stringWriter);
    }
    catch (JAXBException exc) {
      LOG.warn("Exception marshalling data", exc);
      throw new IllegalStateException("Exception marshalling data", exc);
    }
    return stringWriter.toString();
  }

  /**
   * Writes the file.
   *
   * @param file The file to write
   * @throws IOException If an exception occured while writing
   */
  public void toFile(File file)
      throws IOException {
    requireNonNull(file, "file");

    try (OutputStream outStream = new FileOutputStream(file)) {
      outStream.write(toXml().getBytes());
      outStream.flush();
    }
  }

  /**
   * Reads a list of <code>TransportOrderXMLStructure</code>s from an XML file.
   *
   * @param xmlData The XML data
   * @return The list of data
   */
  public static TransportOrdersDocument fromXml(String xmlData) {
    requireNonNull(xmlData, "xmlData");

    StringReader stringReader = new StringReader(xmlData);
    try {
      JAXBContext jc = JAXBContext.newInstance(TransportOrdersDocument.class);
      Unmarshaller unmarshaller = jc.createUnmarshaller();
      Object o = unmarshaller.unmarshal(stringReader);
      return (TransportOrdersDocument) o;
    }
    catch (JAXBException exc) {
      LOG.warn("Exception unmarshalling data", exc);
      throw new IllegalStateException("Exception unmarshalling data", exc);
    }
  }

  /**
   * Reads a list of <code>TransportOrderXMLStructure</code>s from a file.
   *
   * @param sourceFile The file
   * @return The list of data
   * @throws IOException If an exception occured while reading
   */
  public static TransportOrdersDocument fromFile(File sourceFile)
      throws IOException {
    requireNonNull(sourceFile, "sourceFile");

    final String path = sourceFile.getAbsolutePath();
    if (!sourceFile.isFile() || !sourceFile.canRead()) {
      throw new IOException(path + ": file not a regular file or unreadable");
    }
    int fileSize = (int) sourceFile.length();
    byte[] buffer = new byte[fileSize];
    try (InputStream inStream = new FileInputStream(sourceFile)) {
      int bytesRead = inStream.read(buffer);
      if (bytesRead != fileSize) {
        throw new IOException(
            "read() returned unexpected value: " + bytesRead + ", should be :" + fileSize
        );
      }
    }
    String fileContent = new String(buffer);
    return fromXml(fileContent);
  }

}
