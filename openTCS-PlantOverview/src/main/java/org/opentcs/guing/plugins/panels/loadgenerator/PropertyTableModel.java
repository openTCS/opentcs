/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import static java.util.Objects.requireNonNull;
import javax.swing.table.AbstractTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table model for transport order proerties.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
class PropertyTableModel
    extends AbstractTableModel {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PropertyTableModel.class);
  /**
   * The column names.
   */
  private static final String[] COLUMN_NAMES = new String[] {
    "Key",
    "Value"
  };
  /**
   * The column classes.
   */
  private static final Class<?>[] COLUMN_CLASSES = new Class<?>[] {
    String.class,
    String.class
  };
  /**
   * The properties we're maintaining.
   */
  private List<PropEntry> data = new ArrayList<>();

  /**
   * Creates a new PropertyTableModel.
   *
   * @param data Map containing the current properties
   */
  public PropertyTableModel(Map<String, String> data) {
    super();
    requireNonNull(data, "data");

    for (Entry<String, String> entry : data.entrySet()) {
      this.data.add(new PropEntry(entry.getKey(), entry.getValue()));
    }
  }

  @Override
  public int getRowCount() {
    return data.size();
  }

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    if (rowIndex < 0 || rowIndex >= data.size()) {
      return null;
    }
    PropEntry entry = data.get(rowIndex);

    switch (columnIndex) {
      case 0:
        return entry.getKey();
      case 1:
        return entry.getValue();
      default:
        throw new IllegalArgumentException("Invalid columnIndex: "
            + columnIndex);
    }
  }

  @Override
  public String getColumnName(int columnIndex) {
    try {
      return COLUMN_NAMES[columnIndex];
    }
    catch (ArrayIndexOutOfBoundsException exc) {
      LOG.warn("Invalid columnIndex", exc);
      return "FEHLER";
    }
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return COLUMN_CLASSES[columnIndex];
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    switch (columnIndex) {
      case 0:
        return true;
      case 1:
        return true;
      default:
        throw new IllegalArgumentException("Invalid columnIndex: "
            + columnIndex);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (rowIndex < 0 || rowIndex >= data.size()) {
      return;
    }
    PropEntry entry = data.get(rowIndex);

    if (aValue == null) {
      return;
    }
    switch (columnIndex) {
      case 0:
        entry.setKey((String) aValue);
        break;
      case 1:
        entry.setValue((String) aValue);
        break;
      default:
        throw new IllegalArgumentException("Unhandled columnIndex: "
            + columnIndex);
    }
  }

  /**
   * Returns the properties as a list.
   *
   * @return The list holding the properties
   */
  public List<PropEntry> getList() {
    return data;
  }

  /**
   * A class for editing properties.
   */
  public static class PropEntry {

    /**
     * The key.
     */
    private String key;
    /**
     * The value.
     */
    private String value;

    /**
     * Creates an empty PropEntry.
     */
    public PropEntry() {
      // Do nada.
    }

    /**
     * Creates a new PropEntry.
     *
     * @param key The key
     * @param value The value
     */
    public PropEntry(String key, String value) {
      this.key = key;
      this.value = value;
    }

    /**
     * Returns the key.
     *
     * @return The key
     */
    public String getKey() {
      return key;
    }

    /**
     * Sets the key.
     *
     * @param key The new key
     */
    public void setKey(String key) {
      this.key = key;
    }

    /**
     * Returns the value.
     *
     * @return The value
     */
    public String getValue() {
      return value;
    }

    /**
     * Sets the value.
     *
     * @param value The new value
     */
    public void setValue(String value) {
      this.value = value;
    }
  }
}
