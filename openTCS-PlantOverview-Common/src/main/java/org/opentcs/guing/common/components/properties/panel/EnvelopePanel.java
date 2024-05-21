/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.properties.panel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import org.opentcs.data.model.Couple;
import org.opentcs.guing.base.components.properties.type.EnvelopesProperty;
import org.opentcs.guing.base.model.EnvelopeModel;
import org.opentcs.guing.common.components.dialogs.DialogContent;
import org.opentcs.guing.common.components.dialogs.InputValidationListener;
import org.opentcs.guing.common.model.SystemModel;
import org.opentcs.guing.common.util.I18nPlantOverview;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;
import org.opentcs.util.ExplainedBoolean;

/**
 * User interface to edit a single envelope.
 */
public class EnvelopePanel
    extends DialogContent {

  /**
   * The default key to use for envelopes.
   */
  public static final String DEFAULT_ENVELOPE_KEY = "";
  /**
   * The color in which the validation text is displayed when an envelope is valid.
   */
  private static final Color ENVELOPE_VALID = Color.decode("#2fb348");
  /**
   * The color in which the validation text is displayed when an envelope is invalid.
   */
  private static final Color ENVELOPE_INVALID = Color.decode("#d60b28");
  /**
   * The bundle to be used.
   */
  private final ResourceBundleUtil bundle
      = ResourceBundleUtil.getBundle(I18nPlantOverview.PROPERTIES_PATH);
  /**
   * The list of listeners to be notified about the validity of user input.
   */
  private final List<InputValidationListener> validationListeners = new ArrayList<>();
  private final EnvelopeModel envelopeTemplate;
  private final Mode mode;
  private final Set<String> propertyEnvelopeKeys;
  private final SystemModel systemModel;

  /**
   * Creates a new instance.
   *
   * @param envelopeTemplate The envelope that should be used as a tempalte (i.e. to fill this
   * panel's components).
   * @param mode The mode to use this panel in.
   * this panel belongs.
   * @param propertyEnvelopeKeys The envelope keys that are (already) defined in the
   * {@link EnvelopesProperty} to which the envelope being edited in this panel belongs.
   * @param systemModel The current system model.
   */
  public EnvelopePanel(EnvelopeModel envelopeTemplate,
                       Mode mode,
                       Set<String> propertyEnvelopeKeys,
                       SystemModel systemModel) {
    this.envelopeTemplate = requireNonNull(envelopeTemplate, "envelopeTemplate");
    this.mode = requireNonNull(mode, "mode");
    this.propertyEnvelopeKeys = requireNonNull(propertyEnvelopeKeys, "propertyEnvelopeKeys");
    this.systemModel = requireNonNull(systemModel, "systemModel");

    initComponents();
    initEnvelopeKeyCombobox();
    initTable();

    fillComponents();
  }

  @Override
  public String getDialogTitle() {
    return bundle.getString("envelopePanel.title");
  }

  @Override
  public void initFields() {
  }

  @Override
  public void update() {
  }

  /**
   * Registers the given {@link InputValidationListener} with this panel.
   *
   * @param listener The listener to register.
   */
  public void addInputValidationListener(InputValidationListener listener) {
    requireNonNull(listener, "listener");

    this.validationListeners.add(listener);
    validateEnvelope();
  }

  /**
   * Returns the {@link EnvelopeModel} that has been edited in this panel.
   *
   * @return The {@link EnvelopeModel} that has been edited in this panel or {@link Optional#EMPTY}
   * if the envelope model was invalid.
   */
  public Optional<EnvelopeModel> getEnvelopeModel() {
    if (!isEnvelopeValid().getValue()) {
      return Optional.empty();
    }

    return Optional.of(
        new EnvelopeModel(envelopeKeyComboBox.getSelectedItem().toString(),
                          getTableModel().getValues())
    );
  }

  private void initEnvelopeKeyCombobox() {
    envelopeKeys().stream()
        .sorted()
        .forEach(envelopeKeyComboBox::addItem);

    JTextField textField = (JTextField) (envelopeKeyComboBox.getEditor().getEditorComponent());
    textField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        validateEnvelope();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        validateEnvelope();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        validateEnvelope();
      }
    });
  }

  private void initTable() {
    couplesTable.getModel().addTableModelListener(e -> validateEnvelope());
  }

  private void fillComponents() {
    envelopeKeyComboBox.setSelectedItem(envelopeTemplate.getKey());
    getTableModel().setValues(envelopeTemplate.getVertices());
  }

  private Set<String> envelopeKeys() {
    Set<String> result = new HashSet<>();

    // Ensure there is at least the default envelope key.
    result.add(DEFAULT_ENVELOPE_KEY);

    // Extract all keys of envelopes defined at points and paths in the plant model.
    result.addAll(
        Stream.concat(
            systemModel.getPointModels().stream()
                .map(pointModel -> pointModel.getPropertyVehicleEnvelopes()),
            systemModel.getPathModels().stream()
                .map(pathModel -> pathModel.getPropertyVehicleEnvelopes())
        )
            .flatMap(property -> property.getValue().stream())
            .map(envelopeModel -> envelopeModel.getKey())
            .collect(Collectors.toSet())
    );

    // Extract all envelope keys defined for vehicles in the plant model.
    result.addAll(
        systemModel.getVehicleModels().stream()
            .map(vehicleModel -> vehicleModel.getPropertyEnvelopeKey().getText())
            .filter(envelopeKey -> envelopeKey != null)
            .collect(Collectors.toSet())
    );

    return result;
  }

  private void validateEnvelope() {
    ExplainedBoolean envelopeValid = isEnvelopeValid();
    validationTextArea.setForeground(envelopeValid.getValue() ? ENVELOPE_VALID : ENVELOPE_INVALID);
    validationTextArea.setText(envelopeValid.getReason());

    for (InputValidationListener validationListener : validationListeners) {
      validationListener.inputValidationSuccessful(envelopeValid.getValue());
    }
  }

  private ExplainedBoolean isEnvelopeValid() {
    JTextField envelopeKeyTextField
        = (JTextField) (envelopeKeyComboBox.getEditor().getEditorComponent());
    String envelopeKeyText = envelopeKeyTextField.getText();

    CoupleTableModel model = getTableModel();
    if (!(model.getRowCount() >= 4)) {
      return new ExplainedBoolean(
          false,
          bundle.getString("envelopePanel.textArea_validation.text.lessThanFourCoordinatesError")
      );
    }
    if (!(model.firstAndLastEquals())) {
      return new ExplainedBoolean(
          false,
          bundle.getString(
              "envelopePanel.textArea_validation.text.firstAndLastCoordianteNotEqualError"
          )
      );
    }
    if (isKeyAlreadyDefinedInProperty(envelopeKeyText)) {
      return new ExplainedBoolean(
          false,
          bundle.getString("envelopePanel.textArea_validation.text.envelopeKeyAlreadyDefinedError")
      );
    }

    return new ExplainedBoolean(
        true,
        bundle.getString("envelopePanel.textArea_validation.text.envelopeValid")
    );
  }

  private boolean isKeyAlreadyDefinedInProperty(String key) {
    switch (mode) {
      case CREATE:
        return propertyEnvelopeKeys.contains(key);
      case EDIT:
        return propertyEnvelopeKeys.stream()
            // When editing an envelope, ignore the key of the envelope being edited.
            .filter(k -> !Objects.equals(k, envelopeTemplate.getKey()))
            .anyMatch(k -> Objects.equals(k, key));
      default:
        throw new IllegalArgumentException("Unhandled edit mode: " + mode);
    }
  }

  private CoupleTableModel getTableModel() {
    return (CoupleTableModel) couplesTable.getModel();
  }

  // CHECKSTYLE:OFF
  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    envelopeKeyLabel = new javax.swing.JLabel();
    envelopeKeyComboBox = new javax.swing.JComboBox<>();
    coordintesLabel = new javax.swing.JLabel();
    controlPanel = new javax.swing.JPanel();
    addButton = new javax.swing.JButton();
    removeButton = new javax.swing.JButton();
    moveUpButton = new javax.swing.JButton();
    moveDownButton = new javax.swing.JButton();
    controlFiller = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
    couplesScrollPane = new javax.swing.JScrollPane();
    couplesTable = new javax.swing.JTable();
    validationLabel = new javax.swing.JLabel();
    validationScrollPane = new javax.swing.JScrollPane();
    validationTextArea = new javax.swing.JTextArea();

    setLayout(new java.awt.GridBagLayout());

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/plantoverview/panels/propertyEditing"); // NOI18N
    envelopeKeyLabel.setText(bundle.getString("envelopePanel.label_envelopeKey.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    add(envelopeKeyLabel, gridBagConstraints);

    envelopeKeyComboBox.setEditable(true);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    add(envelopeKeyComboBox, gridBagConstraints);

    coordintesLabel.setText(bundle.getString("envelopePanel.label_envelopeCoordinates.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(12, 0, 3, 0);
    add(coordintesLabel, gridBagConstraints);

    controlPanel.setLayout(new java.awt.GridBagLayout());

    addButton.setText(bundle.getString("envelopePanel.button_add.text")); // NOI18N
    addButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        addButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 15, 10, 0);
    controlPanel.add(addButton, gridBagConstraints);

    removeButton.setText(bundle.getString("envelopePanel.button_remove.text")); // NOI18N
    removeButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        removeButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 15, 10, 0);
    controlPanel.add(removeButton, gridBagConstraints);

    moveUpButton.setText(bundle.getString("envelopePanel.button_up.text")); // NOI18N
    moveUpButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        moveUpButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 15, 10, 0);
    controlPanel.add(moveUpButton, gridBagConstraints);

    moveDownButton.setText(bundle.getString("envelopePanel.button_down.text")); // NOI18N
    moveDownButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        moveDownButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
    controlPanel.add(moveDownButton, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weighty = 1.0;
    controlPanel.add(controlFiller, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    add(controlPanel, gridBagConstraints);

    couplesScrollPane.setPreferredSize(new java.awt.Dimension(300, 200));

    couplesTable.setModel(new CoupleTableModel());
    couplesScrollPane.setViewportView(couplesTable);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    add(couplesScrollPane, gridBagConstraints);

    validationLabel.setText(bundle.getString("envelopePanel.label_envelopeValidation.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(12, 0, 3, 0);
    add(validationLabel, gridBagConstraints);

    validationTextArea.setColumns(20);
    validationTextArea.setLineWrap(true);
    validationTextArea.setRows(2);
    validationScrollPane.setViewportView(validationTextArea);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    add(validationScrollPane, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents
  // CHECKSTYLE:ON

  private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
    getTableModel().add(new Couple(0, 0));
  }//GEN-LAST:event_addButtonActionPerformed

  private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
    getTableModel().remove(couplesTable.getSelectedRow());
  }//GEN-LAST:event_removeButtonActionPerformed

  private void moveDownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownButtonActionPerformed
    int selectedRow = couplesTable.getSelectedRow();
    if (getTableModel().moveDown(selectedRow)) {
      couplesTable.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
    }
  }//GEN-LAST:event_moveDownButtonActionPerformed

  private void moveUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpButtonActionPerformed
    int selectedRow = couplesTable.getSelectedRow();
    if (getTableModel().moveUp(selectedRow)) {
      couplesTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
    }
  }//GEN-LAST:event_moveUpButtonActionPerformed

  // CHECKSTYLE:OFF
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton addButton;
  private javax.swing.Box.Filler controlFiller;
  private javax.swing.JPanel controlPanel;
  private javax.swing.JLabel coordintesLabel;
  private javax.swing.JScrollPane couplesScrollPane;
  private javax.swing.JTable couplesTable;
  private javax.swing.JComboBox<String> envelopeKeyComboBox;
  private javax.swing.JLabel envelopeKeyLabel;
  private javax.swing.JButton moveDownButton;
  private javax.swing.JButton moveUpButton;
  private javax.swing.JButton removeButton;
  private javax.swing.JLabel validationLabel;
  private javax.swing.JScrollPane validationScrollPane;
  private javax.swing.JTextArea validationTextArea;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON

  /**
   * Defines the modes this panel can be used in.
   */
  public enum Mode {
    /**
     * The mode for creating envelopes.
     */
    CREATE,
    /**
     * The mode for editing envelopes.
     */
    EDIT;
  }

  private class CoupleTableModel
      extends AbstractTableModel {

    /**
     * The number of the "X" column.
     */
    private final int columnX = 0;
    /**
     * The number of the "Y" column.
     */
    private final int columnY = 1;
    /**
     * The column names.
     */
    private final String[] columnNames = new String[]{
      bundle.getString("envelopePanel.table_couples.column_x.headerText"),
      bundle.getString("envelopePanel.table_couples.column_y.headerText")
    };
    /**
     * Column classes.
     */
    private final Class<?>[] columnClasses = new Class<?>[]{
      String.class,
      String.class
    };
    /**
     * The values in this model.
     */
    private final List<Couple> values = new ArrayList<>();

    /**
     * Creates a new instance.
     */
    CoupleTableModel() {
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return columnClasses[columnIndex];
    }

    @Override
    public String getColumnName(int columnIndex) {
      return columnNames[columnIndex];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
      return true;
    }

    @Override
    public int getRowCount() {
      return values.size();
    }

    @Override
    public int getColumnCount() {
      return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      if (rowIndex < 0 || rowIndex >= getRowCount()) {
        return null;
      }

      Couple entry = values.get(rowIndex);
      switch (columnIndex) {
        case columnX:
          return entry.getX();
        case columnY:
          return entry.getY();
        default:
          throw new IllegalArgumentException("Invalid column index: " + columnIndex);
      }
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
      if (rowIndex < 0 || rowIndex >= getRowCount()) {
        throw new IllegalArgumentException("Invalid row index: " + rowIndex);
      }

      long newCoordinate;
      try {
        newCoordinate = Long.parseLong((String) value);
      }
      catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(
            couplesTable,
            bundle.getString("envelopePanel.optionPane_invalidNumberError.message")
        );
        return;
      }

      Couple prevEntry = values.get(rowIndex);
      switch (columnIndex) {
        case columnX:
          values.set(rowIndex, new Couple(newCoordinate, prevEntry.getY()));
          break;
        case columnY:
          values.set(rowIndex, new Couple(prevEntry.getX(), newCoordinate));
          break;
        default:
          throw new IllegalArgumentException("Invalid column index: " + columnIndex);
      }

      fireTableCellUpdated(rowIndex, columnIndex);
    }

    public boolean firstAndLastEquals() {
      return Objects.equals(values.get(0), values.get(values.size() - 1));
    }

    public void setValues(List<Couple> values) {
      requireNonNull(values, "values");

      this.values.clear();
      this.values.addAll(values);
      fireTableDataChanged();
    }

    public List<Couple> getValues() {
      return Collections.unmodifiableList(values);
    }

    public boolean add(Couple couple) {
      values.add(couple);
      fireTableRowsInserted(values.size() - 1, values.size() - 1);
      return true;
    }

    public boolean remove(int row) {
      if (!rowInBounds(row)) {
        return false;
      }

      values.remove(row);
      fireTableRowsDeleted(row, row);
      return true;
    }

    public boolean moveDown(int row) {
      if (!rowInBounds(row) || row == values.size() - 1) {
        return false;
      }

      Couple value = values.remove(row);
      values.add(row + 1, value);
      fireTableRowsUpdated(row, row + 1);
      return true;
    }

    public boolean moveUp(int row) {
      if (!rowInBounds(row) || row == 0) {
        return false;
      }

      Couple value = values.remove(row);
      values.add(row - 1, value);
      fireTableRowsUpdated(row - 1, row);
      return true;
    }

    private boolean rowInBounds(int row) {
      if (values.isEmpty()) {
        return false;
      }

      return row >= 0 && row <= values.size() - 1;
    }
  }
}
