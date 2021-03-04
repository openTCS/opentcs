/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.properties.table;

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
import org.opentcs.guing.components.dialogs.DetailsDialog;
import org.opentcs.guing.components.dialogs.DetailsDialogContent;
import org.opentcs.guing.components.dialogs.StandardDetailsDialog;
import org.opentcs.guing.components.properties.type.Property;
import org.opentcs.guing.util.UserMessageHelper;

/**
 * Basisimplementierung für einen
 * <code>CellEditor</code>, der aus einer Editor-Komponente (Textfeld, ComboBox
 * oder CheckBox) sowie aus einem kleinen Button mit drei Punkten besteht. Beim
 * Anklicken des kleinen Buttons erscheint ein Dialog, mit dessen Hilfe der Wert
 * eines Attributs komfortabler geändert werden kann.
 * <p>
 * Ein
 * <code>CellEditor</code> verwaltet ein {
 *
 * @see Property}, das bearbeitet werden kann (der Editor wird jedoch mehrfach,
 * genauer gesagt für eine bestimmte Klasse von Property eingesetzt).
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
   * Das Attribut.
   */
  protected Property fProperty;
  /**
   * Der Dialog, der eine komfortablere Bearbeitung des Attributs erlaubt.
   */
  protected DetailsDialog fDetailsDialog;
  /**
   * Die Komponente mit Textfeld, ComboBox oder CheckBox sowie dem Button mit
   * den drei Punkten.
   */
  protected final JComponent fComponent;
  /**
   * Utility class to show messages to the user, eg error messages.
   */
  protected final UserMessageHelper userMessageHelper;

  /**
   * Erezugt ein neues Objekt von AbstractCellEditor, wobei die
   * Editor-Komponente ein Textfeld ist.
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
   * Erezugt ein neues Objekt von AbstractCellEditor, wobei die
   * Editor-Komponente eine CheckBox ist.
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
   * Erezugt ein neues Objekt von AbstractCellEditor, wobei die
   * Editor-Komponente eine ComboBox ist.
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
   * Erzeugt die Komponente, die aus Editor und kleinem Button besteht.
   *
   * @return
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
   * Erzeugt den Button mit den drei Punkten. Soll kein Button erscheinen, muss
   * hier
   * <code>null</code> zurückgegeben werden.
   *
   * @return
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
   * Setzt den Dialog, mit dessen Hilfe der Eigenschaftswert komfortabel
   * bearbeitet werden kann.
   *
   * @param detailsDialog
   */
  public void setDetailsDialog(DetailsDialog detailsDialog) {
    fDetailsDialog = detailsDialog;
  }

  /**
   * Setzt den Eigenschaftswert.
   *
   * @param value
   */
  protected void setValue(Object value) {
    fProperty = (Property) value;
  }

  /**
   * Markiert das Attribut als geändert.
   */
  protected void markProperty() {
    fProperty.markChanged();
  }

  /**
   * Öffnet den Dialog, mit dessen Hilfe die Eigenschaften eines Attributs
   * komfortabler eingestellt werden können.
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
