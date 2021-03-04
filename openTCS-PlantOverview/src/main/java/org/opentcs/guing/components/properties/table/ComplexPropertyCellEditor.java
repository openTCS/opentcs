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

import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import org.opentcs.guing.components.dialogs.DetailsDialogContent;
import org.opentcs.guing.components.dialogs.StandardDetailsDialog;
import org.opentcs.guing.components.properties.type.AbstractComplexProperty;

/**
 * Ein CellEditor für Attribute vom Typ {
 *
 * @see AbstractComplexProperty} sowie Unterklassen. Der Editor ist ein Button;
 * beim Anklicken öffnet sich sofort ein DetailsDialog. Einen Button mit drei
 * Punkten gibt es nicht extra. Das liegt daran, dass Attribute vom Typ {
 * @see AbstractComplexProperty} so speziell und mitunter komplex sind, dass
 * eine Bearbeitung mit einer ComboBox, einem Textfeld oder einer CheckBox in
 * aller Regel keinen Sinn macht.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class ComplexPropertyCellEditor
    extends javax.swing.AbstractCellEditor
    implements javax.swing.table.TableCellEditor {

  /**
   * The button for showing the details dialog.
   */
  private final JButton fButton = new JButton();
  /**
   * Provides the appropriate dialog content for a given property.
   */
  private final Map<Class<? extends AbstractComplexProperty>, Provider<DetailsDialogContent>> contentMap;
  /**
   * A parent for dialogs created by this instance.
   */
  private final JPanel dialogParent;
  /**
   * The property being edited.
   */
  private AbstractComplexProperty fProperty;

  /**
   * Creates a new instance.
   *
   * @param contentMap Provides the appropriate content for a given property.
   * @param dialogParent A parent for dialogs created by this instance.
   */
  @Inject
  public ComplexPropertyCellEditor(
      Map<Class<? extends AbstractComplexProperty>, Provider<DetailsDialogContent>> contentMap,
      @Assisted JPanel dialogParent) {
    this.contentMap = requireNonNull(contentMap, "contentMap");
    this.dialogParent = requireNonNull(dialogParent, "dialogParent");

    fButton.setFont(new Font("Dialog", Font.PLAIN, 12));
    fButton.setBorder(null);
    fButton.setHorizontalAlignment(JButton.LEFT);
    fButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        showDialog();
      }
    });
  }

  @Override
  public Object getCellEditorValue() {
    return fProperty;
  }

  @Override
  public Component getTableCellEditorComponent(
      JTable table, Object value, boolean isSelected, int row, int column) {

    fProperty = (AbstractComplexProperty) value;
    fButton.setText(fProperty.toString());
    fButton.setBackground(table.getBackground());

    return fButton;
  }

  /**
   * Shows the dialog for editing the property.
   */
  private void showDialog() {
    DetailsDialogContent content = contentMap.get(fProperty.getClass()).get();

    StandardDetailsDialog detailsDialog
        = new StandardDetailsDialog(dialogParent, true, content);
    detailsDialog.setLocationRelativeTo(dialogParent);

    detailsDialog.getDialogContent().setProperty(fProperty);
    detailsDialog.activate();
    detailsDialog.setVisible(true);

    if (detailsDialog.getReturnStatus() == StandardDetailsDialog.RET_OK) {
      stopCellEditing();
//      fireEditingStopped(); // bewirkt nichts
    }
    else {
      cancelCellEditing();
    }
  }
}
