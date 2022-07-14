/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.properties.panel;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.awt.Dimension;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableModel;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.guing.base.components.properties.type.KeyValueProperty;
import org.opentcs.guing.base.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.base.components.properties.type.Property;
import org.opentcs.guing.common.components.dialogs.DetailsDialogContent;
import org.opentcs.guing.common.components.dialogs.StandardDetailsDialog;
import org.opentcs.guing.common.util.I18nPlantOverview;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * UI for editing a set of key value pairs.
 *
 * @see KeyValueSetProperty
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class KeyValueSetPropertyEditorPanel
    extends KeyValueSetPropertyViewerEditorPanel
    implements DetailsDialogContent {

  /**
   * A resource bundle.
   */
  private final ResourceBundleUtil resBundle
      = ResourceBundleUtil.getBundle(I18nPlantOverview.PROPERTIES_PATH);
  /**
   * A provider that provides new instances of KeyValuePropertyEditorPanels
   */
  private final Provider<KeyValuePropertyEditorPanel> editorProvider;

  /**
   * Creates a new instance.
   *
   * @param editorProvider a guice injected provider of KeyValuePropertyEditorPanel Instances
   */
  @Inject
  public KeyValueSetPropertyEditorPanel(Provider<KeyValuePropertyEditorPanel> editorProvider) {
    this.editorProvider = requireNonNull(editorProvider, "editorProvider");

    initComponents();

    itemsTable.setModel(new ItemsTableModel());

    setPreferredSize(new Dimension(350, 200));

    itemsTable.getSelectionModel().addListSelectionListener((ListSelectionEvent evt) -> {
      if (evt.getValueIsAdjusting()) {
        return;
      }

      handleSelectionChanged();
    });
  }

  @Override
  public void setProperty(Property property) {
    super.setProperty(property);
    ItemsTableModel model = (ItemsTableModel) itemsTable.getModel();

    model.setRowCount(0);

    for (KeyValueProperty p : getProperty().getItems()) {
      model.addRow(new String[]{p.getKey(), p.getValue()});
    }

    sortItems();
    updateView();
  }

  @Override
  public void updateValues() {
    List<KeyValueProperty> items = new LinkedList<>();
    TableModel model = itemsTable.getModel();
    int size = model.getRowCount();

    for (int i = 0; i < size; i++) {
      String key = (String) model.getValueAt(i, 0);
      String value = (String) model.getValueAt(i, 1);
      items.add(new KeyValueProperty(null, key, value));
    }

    getProperty().setItems(items);
  }

  @Override
  public String getTitle() {
    return resBundle.getString("keyValueSetPropertyEditorPanel.title");
  }

  /**
   * Returns the selected key/value property.
   *
   * @return the selected key/value property.
   */
  private KeyValueProperty getSelectedKeyValueProperty() {
    int i = itemsTable.getSelectedRow();

    if (i == -1) {
      return null;
    }

    String key = (String) itemsTable.getValueAt(i, 0);
    String value = (String) itemsTable.getValueAt(i, 1);

    return new KeyValueProperty(null, key, value);
  }

  /**
   * Selects a row/an item based on a key.
   *
   * @param key The key for the row/item to select.
   */
  private void selectItem(String key) {
    for (int i = 0; i < itemsTable.getRowCount(); i++) {
      if (itemsTable.getValueAt(i, 0).equals(key)) {
        itemsTable.getSelectionModel().setSelectionInterval(i, i);
        break;
      }
    }
  }

  /**
   * Add a new key/value pair.
   *
   * @param key the key.
   * @param value the value.
   */
  private void addItem(String key, String value) {
    for (int i = 0; i < itemsTable.getRowCount(); i++) {
      if (itemsTable.getValueAt(i, 0).equals(key)) {
        JOptionPane.showMessageDialog(
            this,
            resBundle.getString("keyValueSetPropertyEditorPanel.optionPane_keyAlreadyExists.message") + ": " + key);
        return;
      }
    }

    ItemsTableModel model = (ItemsTableModel) itemsTable.getModel();
    model.addRow(new Object[]{key, value});
  }

  /**
   * Searches the key-value list using the old key and updates the key-value pair with the new key
   * and the (new) value.
   * If the old key equals the new key, only the value will be updated.
   *
   * @param oldKey The old key of the key-value pair to be updated.
   * @param newKey The new key of the key-value pair to be updated.
   * @param value The new value.
   */
  private void updateItem(String oldKey, String newKey, String value) {
    // Searching for the edited key-value pair...
    for (int oldKeyRow = 0; oldKeyRow < itemsTable.getRowCount(); oldKeyRow++) {
      if (Objects.equals(itemsTable.getValueAt(oldKeyRow, 0), oldKey)) {
        // Searching for another key-value pair with the same key as newKey...
        for (int newKeyRow = 0; newKeyRow < itemsTable.getRowCount(); newKeyRow++) {
          // If there is already a different row with the new key, notify the user and abort.
          if (oldKeyRow != newKeyRow
              && Objects.equals(itemsTable.getValueAt(newKeyRow, 0), newKey)) {
            JOptionPane.showMessageDialog(
                this,
                resBundle.getString("keyValueSetPropertyEditorPanel.optionPane_keyAlreadyExists.message") + ": " + newKey);
            return;
          }
        }
        // If its a legit edit, update the key-value pair.
        itemsTable.setValueAt(value, oldKeyRow, 1);
        itemsTable.setValueAt(newKey, oldKeyRow, 0);
      }
    }
  }

  private void edit() {
    KeyValueProperty p = getSelectedKeyValueProperty();

    if (p == null) {
      return;
    }
    KeyValueProperty pOld = new KeyValueProperty(p.getModel(), p.getKey(), p.getValue());
    JDialog parent = (JDialog) getTopLevelAncestor();
    KeyValuePropertyEditorPanel content = editorProvider.get();
    content.setProperty(p);

    StandardDetailsDialog dialog = new StandardDetailsDialog(parent, true, content);

    dialog.setLocationRelativeTo(parent);
    dialog.setVisible(true);

    if (dialog.getReturnStatus() == StandardDetailsDialog.RET_OK) {
      updateItem(pOld.getKey(), p.getKey(), p.getValue());
      sortItems();
      selectItem(p.getKey());

      updateView();
    }
  }

  /**
   * Adds a new entry.
   */
  private void add() {
    JDialog parent = (JDialog) getTopLevelAncestor();

    KeyValueProperty p = new KeyValueProperty(null);
    KeyValuePropertyEditorPanel content = editorProvider.get();
    content.setProperty(p);
    StandardDetailsDialog dialog = new StandardDetailsDialog(parent, true, content);
    dialog.setLocationRelativeTo(parent);
    dialog.setVisible(true);

    if (dialog.getReturnStatus() == StandardDetailsDialog.RET_OK) {
      addItem(p.getKey(), p.getValue());
      sortItems();
      selectItem(p.getKey());

      updateView();
    }
  }

  private void handleSelectionChanged() {
    updateView();
  }

  /**
   * Updates the UI based on whether or not an entry is selected.
   */
  private void updateView() {
    final TableModel model = itemsTable.getModel();
    boolean selectedAreEditable = true;

    for (int selRowIndex : itemsTable.getSelectedRows()) {
      String key = (String) model.getValueAt(selRowIndex, 0);
      if (key.equals(ObjectPropConstants.LOC_DEFAULT_REPRESENTATION)
          || key.equals(ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION)) {
        selectedAreEditable = false;
        break;
      }
    }

    boolean enableEditing = false;
    boolean enableRemoval = false;
    // Only allow removal of properties if at least one is selected and all of
    // them are editable.
    if (itemsTable.getSelectedRowCount() > 0 && selectedAreEditable) {
      enableRemoval = true;
    }
    // Only allow editing for the selection if exactly one property is selected
    // and it is editable.
    if (itemsTable.getSelectedRowCount() == 1 && selectedAreEditable) {
      enableEditing = true;
    }
    editButton.setEnabled(enableEditing);
    removeButton.setEnabled(enableRemoval);
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    itemsScrollPane = new javax.swing.JScrollPane();
    itemsTable = new javax.swing.JTable();
    controlPanel = new javax.swing.JPanel();
    addButton = new javax.swing.JButton();
    editButton = new javax.swing.JButton();
    removeButton = new javax.swing.JButton();

    setLayout(new java.awt.BorderLayout());

    itemsTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {

      }
    ));
    itemsScrollPane.setViewportView(itemsTable);

    add(itemsScrollPane, java.awt.BorderLayout.CENTER);

    controlPanel.setLayout(new java.awt.GridBagLayout());

    addButton.setFont(addButton.getFont());
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/plantoverview/panels/propertyEditing"); // NOI18N
    addButton.setText(bundle.getString("keyValueSetPropertyEditorPanel.button_add.text")); // NOI18N
    addButton.setOpaque(false);
    addButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        addButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    controlPanel.add(addButton, gridBagConstraints);

    editButton.setFont(editButton.getFont());
    editButton.setText(bundle.getString("keyValueSetPropertyEditorPanel.button_edit.text")); // NOI18N
    editButton.setOpaque(false);
    editButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        editButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    controlPanel.add(editButton, gridBagConstraints);

    removeButton.setFont(removeButton.getFont());
    removeButton.setText(bundle.getString("keyValueSetPropertyEditorPanel.button_remove.text")); // NOI18N
    removeButton.setOpaque(false);
    removeButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        removeButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
    gridBagConstraints.weighty = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    controlPanel.add(removeButton, gridBagConstraints);

    add(controlPanel, java.awt.BorderLayout.EAST);
  }// </editor-fold>//GEN-END:initComponents

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
      int selectedRowIndex = itemsTable.getSelectedRow();

      if (selectedRowIndex == -1) {
        return;
      }

      ((ItemsTableModel) itemsTable.getModel()).removeRow(selectedRowIndex);

      updateView();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
      edit();
    }//GEN-LAST:event_editButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
      add();
    }//GEN-LAST:event_addButtonActionPerformed

  // CHECKSTYLE:OFF
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton addButton;
  private javax.swing.JPanel controlPanel;
  private javax.swing.JButton editButton;
  private javax.swing.JScrollPane itemsScrollPane;
  private javax.swing.JTable itemsTable;
  private javax.swing.JButton removeButton;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON

}
