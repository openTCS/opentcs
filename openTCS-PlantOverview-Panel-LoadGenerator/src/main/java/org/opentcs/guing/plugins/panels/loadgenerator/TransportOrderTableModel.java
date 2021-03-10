/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.table.AbstractTableModel;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import static org.opentcs.guing.plugins.panels.loadgenerator.I18nPlantOverviewPanelLoadGenerator.BUNDLE_PATH;
import org.opentcs.guing.plugins.panels.loadgenerator.xmlbinding.TransportOrderEntry;
import org.opentcs.guing.plugins.panels.loadgenerator.xmlbinding.TransportOrdersDocument;

/**
 * A table model for transport orders.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
class TransportOrderTableModel
    extends AbstractTableModel {

  /**
   * This classe's bundle.
   */
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_PATH);
  /**
   * The column names.
   */
  private static final String[] COLUMN_NAMES = new String[] {
    "#",
    BUNDLE.getString("transportOrderTableModel.column_deadline.headerText"),
    BUNDLE.getString("transportOrderTableModel.column_vehicle.headerText")};
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

  public TransportOrdersDocument toXmlDocument() {
    TransportOrdersDocument result = new TransportOrdersDocument();

    for (TransportOrderData curData : transportOrderDataList) {
      result.getTransportOrders().add(new TransportOrderEntry(
              curData.getDeadline(),
              curData.getDriveOrders(),
              curData.getIntendedVehicle() == null ? null : curData.getIntendedVehicle().getName(),
              curData.getProperties())
      );
    }

    return result;
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
