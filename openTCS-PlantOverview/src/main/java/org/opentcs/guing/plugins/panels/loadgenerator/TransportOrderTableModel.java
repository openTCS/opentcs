/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator;

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
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import javax.swing.table.AbstractTableModel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A table model for transport orders.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
@XmlRootElement
@XmlType(propOrder = {"xmlData"})
class TransportOrderTableModel
    extends AbstractTableModel {

  /**
   * This classe's bundle.
   */
  private static final ResourceBundle BUNDLE
      = ResourceBundle.getBundle("org/opentcs/guing/plugins/panels/loadgenerator/Bundle");

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(TransportOrderTableModel.class);
  /**
   * The column names.
   */
  private static final String[] COLUMN_NAMES = new String[] {
    "#",
    BUNDLE.getString("deadline"),
    BUNDLE.getString("vehicle")};
  /**
   * The column classes.
   */
  private static final Class<?>[] COLUMN_CLASSES = new Class<?>[] {
    Integer.class,
    TransportOrderData.Deadline.class,
    TCSObjectReference.class,};
  /**
   * The actual content.
   */
  private final List<TransportOrderData> transportOrderDataList = new ArrayList<>();
  /**
   * The transportOrderDataList as an XML structure.
   */
  @XmlElement(name = "transportOrders", required = true)
  private final List<TransportOrderXMLStructure> xmlData = new ArrayList<>();

  /**
   * Creates a new instance.
   */
  public TransportOrderTableModel() {
  }

  /**
   * Adds a <code>TransportOrderData</code>.
   *
   * @param data The new transport order data
   */
  public void addData(TransportOrderData data) {
    int newIndex = transportOrderDataList.size();
    transportOrderDataList.add(data);
    fireTableRowsInserted(newIndex, newIndex);
  }

  /**
   * Removes a <code>TransportOrderData</code>.
   *
   * @param row Index indicating which transport order data shall be removed
   */
  public void removeData(int row) {
    transportOrderDataList.remove(row);
    fireTableRowsDeleted(row, row);
  }

  /**
   * Returns the <code>TransportOrderData</code> at the given index.
   *
   * @param row Index indicating which data shall be returned
   * @return The transport order data at the given index
   */
  public TransportOrderData getDataAt(int row) {
    if (row >= 0) {
      return transportOrderDataList.get(row);
    }
    else {
      return null;
    }
  }

  @Override
  public int getRowCount() {
    return transportOrderDataList.size();
  }

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    TransportOrderData data = transportOrderDataList.get(rowIndex);

    switch (columnIndex) {
      case 0:
        return rowIndex + 1;
      case 1:
        return data.getDeadline();
      case 2:
        return data.getIntendedVehicle();
      default:
        throw new IllegalArgumentException("Invalid columnIndex: " + columnIndex);
    }
  }

  @Override
  public String getColumnName(int columnIndex) {
    return COLUMN_NAMES[columnIndex];
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return COLUMN_CLASSES[columnIndex];
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    switch (columnIndex) {
      case 0:
        return false;
      case 1:
        return true;
      case 2:
        return true;
      default:
        throw new IllegalArgumentException("Invalid columnIndex: " + columnIndex);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    TransportOrderData data = transportOrderDataList.get(rowIndex);
    switch (columnIndex) {
      case 1:
        data.setDeadline((TransportOrderData.Deadline) aValue);
        break;
      case 2:
        data.setIntendedVehicle((TCSObjectReference<Vehicle>) aValue);
        break;
      default:
        throw new IllegalArgumentException("Invalid columnIndex: " + columnIndex);
    }
  }

  /**
   * Creates XML classes from the actual classes.
   */
  private void createXMLStructure() {
    xmlData.clear();
    for (TransportOrderData curData : transportOrderDataList) {
      xmlData.add(new TransportOrderXMLStructure(
          curData.getName(),
          curData.getDeadline(),
          curData.getDriveOrders(),
          curData.getIntendedVehicle() == null ? null : curData.getIntendedVehicle().getName(),
          curData.getProperties()));
    }
  }

  /**
   * Marshals the data.
   *
   * @return Data as XML string
   */
  public String toXml() {
    StringWriter stringWriter = new StringWriter();
    try {
      // Als XML in eine Datei schreiben.
      JAXBContext jc = JAXBContext.newInstance(TransportOrderTableModel.class);
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

    createXMLStructure();
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
  @SuppressWarnings("unchecked")
  public static List<TransportOrderXMLStructure> fromXml(String xmlData) {
    requireNonNull(xmlData, "xmlData");

    StringReader stringReader = new StringReader(xmlData);
    try {
      JAXBContext jc = JAXBContext.newInstance(TransportOrderTableModel.class);
      Unmarshaller unmarshaller = jc.createUnmarshaller();
      Object o = unmarshaller.unmarshal(stringReader);
      return ((TransportOrderTableModel) o).xmlData;
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
  public static List<TransportOrderXMLStructure> fromFile(File sourceFile)
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
        throw new IOException("read() returned unexpected value: " + bytesRead
            + ", should be :" + fileSize);
      }
    }
    String fileContent = new String(buffer);
    return fromXml(fileContent);
  }

  /**
   * Returns the list containing all <code>TransportOrderData</code>.
   *
   * @return The list containing all data
   */
  public List<TransportOrderData> getList() {
    return transportOrderDataList;
  }
}
