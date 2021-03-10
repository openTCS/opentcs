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

import com.google.inject.assistedinject.Assisted;
import java.awt.Component;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.opentcs.guing.components.properties.CoordinateUndoActivity;
import org.opentcs.guing.components.properties.PropertiesComponentsFactory;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.util.UserMessageHelper;

/**
 * Ein CellEditor für Attribute vom Typ {CoordinateQuantity}.
 * Der Editor umfasst:
 * - ein Textfeld zur schnellen Eingabe
 * - den Button mit drei Punkten, bei dessen Anklicken sich ein
 * DetailsDialog zum komfortablen Bearbeiten des Attributs öffnet
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class CoordinateCellEditor
    extends QuantityCellEditor {

  /**
   * The components factory.
   */
  private final PropertiesComponentsFactory componentsFactory;
  /**
   * Undo/Redo for a coordinate property.
   */
  private CoordinateUndoActivity coordinateUndoActivity;

  /**
   * Creates a new instance.
   *
   * @param textField
   */
  @Inject
  public CoordinateCellEditor(@Assisted JTextField textField,
                              @Assisted UserMessageHelper umh,
                              PropertiesComponentsFactory componentsFactory) {
    super(textField, umh);
    this.componentsFactory = requireNonNull(componentsFactory, "componentsFactory");
  }

  /**
   * The table cell contains (from left to right):
   * 1. An editable textfield
   * 2. A button with "arrow down" symbol to copy values from model to layout
   * 3. A button with "arrow up" symbol to copy values from layout to model
   * 4. A button with "..." symbol to open a dialog with extended editor
   * functionality.
   *
   * @return
   */
  @Override  // AbstractPropertyCellEditor
  protected JComponent createComponent() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(getComponent());
    JComponent button = createButtonDetailsDialog();
    panel.add(button);

    return panel;
  }

  /**
   * Liefert die CellEditor-Komponente. Wird bei Bearbeitungsbeginn aufgerufen.
   * Die Editor-Komponente ist mit dem Wert des Attributs zu füllen.
   *
   * @param table
   * @param value
   * @param row
   * @param isSelected
   * @param column
   * @return
   */
  @Override  // DefaultCellEditor
  public Component getTableCellEditorComponent(
      JTable table, Object value, boolean isSelected, int row, int column) {

    CoordinateProperty coordinateProperty = (CoordinateProperty) value;
    coordinateUndoActivity
        = componentsFactory.createLayoutToModelCoordinateUndoActivity(coordinateProperty);
    coordinateUndoActivity.snapShotBeforeModification();

    return super.getTableCellEditorComponent(table, value, isSelected, row, column);
  }

  /**
   * Liefert das Attribut nach der Bearbeitung. Der Wert des Editors ist in das
   * Attribut zu schreiben.
   *
   * @return
   */
  @Override  // DefaultCellEditor
  public Object getCellEditorValue() {
    Object result = super.getCellEditorValue();

    coordinateUndoActivity.snapShotAfterModification();

    return result;
  }
}
