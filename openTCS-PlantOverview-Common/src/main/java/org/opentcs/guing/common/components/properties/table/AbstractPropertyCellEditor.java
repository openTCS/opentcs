/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.properties.table;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.opentcs.guing.base.components.properties.type.Property;
import org.opentcs.guing.common.components.dialogs.DetailsDialog;
import org.opentcs.guing.common.components.dialogs.DetailsDialogContent;
import org.opentcs.guing.common.components.dialogs.StandardDetailsDialog;
import org.opentcs.guing.common.util.UserMessageHelper;

/**
 * Base implementation for a cell editor to edit a property.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public abstract class AbstractPropertyCellEditor
    extends javax.swing.DefaultCellEditor {

  /**
   * The color a cell is painted in when a property informs about
   * different values.
   */
  public static final Color DIFFERENT_VALUE_COLOR = new Color(202, 225, 255);
  /**
   * The property.
   */
  protected Property fProperty;
  /**
   * The details dialog that allows editing of the property.
   */
  protected DetailsDialog fDetailsDialog;
  /**
   * The UI component.
   */
  protected final JComponent fComponent;
  /**
   * Utility class to show messages to the user, eg error messages.
   */
  protected final UserMessageHelper userMessageHelper;

  /**
   * Creates a new instance.
   *
   * @param textField
   * @param umh
   */
  public AbstractPropertyCellEditor(JTextField textField, UserMessageHelper umh) {
    super(textField);
    fComponent = createComponent();
    userMessageHelper = Objects.requireNonNull(umh, "umh is null");
  }

  /**
   * Creates a new instance.
   *
   * @param checkBox
   * @param umh
   */
  public AbstractPropertyCellEditor(JCheckBox checkBox, UserMessageHelper umh) {
    super(checkBox);
    fComponent = createComponent();
    userMessageHelper = Objects.requireNonNull(umh, "umh is null");
  }

  /**
   * Creates a new instance.
   *
   * @param comboBox
   * @param umh
   */
  public AbstractPropertyCellEditor(JComboBox<?> comboBox, UserMessageHelper umh) {
    super(comboBox);
    fComponent = createComponent();
    userMessageHelper = Objects.requireNonNull(umh, "umh is null");
  }

  /**
   * Creates the component that edits the property.
   *
   * @return The component that edits the property.
   */
  protected JComponent createComponent() {
    JPanel panel = new JPanel();
    panel.setLayout(new java.awt.BorderLayout());
    panel.add(getComponent(), java.awt.BorderLayout.CENTER);

    JComponent button = createButtonDetailsDialog();

    if (button != null) {
      panel.add(button, java.awt.BorderLayout.EAST);
    }

    return panel;
  }

  /**
   * Creates a button with three dots.
   *
   * @return A button with three dots or null.
   */
  protected JComponent createButtonDetailsDialog() {
    JButton button = new JButton("...");
    button.setMargin(new Insets(0, 2, 0, 2));
    button.addActionListener(new ActionListener() {

      @Override // ActionListener
      public void actionPerformed(ActionEvent e) {
        showDialog();
        fireEditingStopped();
      }
    });

    return button;
  }

  /**
   * Set the dialog that alows for easy edit of the property.
   *
   * @param detailsDialog The dialog that allows editing of the property.
   */
  public void setDetailsDialog(DetailsDialog detailsDialog) {
    fDetailsDialog = detailsDialog;
  }

  /**
   * Set the property.
   *
   * @param value The property.
   */
  protected void setValue(Object value) {
    fProperty = (Property) value;
  }

  /**
   * Mark the property as changed.
   */
  protected void markProperty() {
    fProperty.markChanged();
  }

  /**
   * Opens the dialog.
   */
  protected void showDialog() {
    if (fDetailsDialog != null) {
      DetailsDialogContent content = fDetailsDialog.getDialogContent();
      content.setProperty(fProperty);

      StandardDetailsDialog dialog = (StandardDetailsDialog) fDetailsDialog;
      dialog.setLocationRelativeTo(dialog.getParentComponent());
      dialog.setVisible(true);

      delegate.setValue(fProperty);
      stopCellEditing();
    }
  }

  /**
   * Sets the focus to the actual component (JTextField etc.).
   */
  public void setFocusToComponent() {
    getComponent().requestFocusInWindow();
  }
}
