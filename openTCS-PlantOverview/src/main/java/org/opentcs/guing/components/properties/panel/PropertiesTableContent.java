/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.properties.panel;

import com.google.inject.assistedinject.Assisted;
import java.util.HashMap;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.undo.CannotUndoException;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.guing.components.dialogs.DetailsDialog;
import org.opentcs.guing.components.dialogs.DetailsDialogContent;
import org.opentcs.guing.components.dialogs.StandardDetailsDialog;
import org.opentcs.guing.components.properties.AbstractTableContent;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.properties.table.AttributesTable;
import org.opentcs.guing.components.properties.table.AttributesTableModel;
import org.opentcs.guing.components.properties.table.BooleanPropertyCellEditor;
import org.opentcs.guing.components.properties.table.BooleanPropertyCellRenderer;
import org.opentcs.guing.components.properties.table.CellEditorFactory;
import org.opentcs.guing.components.properties.table.ColorPropertyCellEditor;
import org.opentcs.guing.components.properties.table.ColorPropertyCellRenderer;
import org.opentcs.guing.components.properties.table.CoordinateCellEditor;
import org.opentcs.guing.components.properties.table.IntegerPropertyCellEditor;
import org.opentcs.guing.components.properties.table.QuantityCellEditor;
import org.opentcs.guing.components.properties.table.SelectionPropertyCellEditor;
import org.opentcs.guing.components.properties.table.StandardPropertyCellRenderer;
import org.opentcs.guing.components.properties.table.StringPropertyCellEditor;
import org.opentcs.guing.components.properties.table.UndoableCellEditor;
import org.opentcs.guing.components.properties.type.AbstractComplexProperty;
import org.opentcs.guing.components.properties.type.AbstractQuantity;
import org.opentcs.guing.components.properties.type.BooleanProperty;
import org.opentcs.guing.components.properties.type.ColorProperty;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.IntegerProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.LocationTypeProperty;
import org.opentcs.guing.components.properties.type.Property;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.event.ConnectionChangeEvent;
import org.opentcs.guing.event.ConnectionChangeListener;
import org.opentcs.guing.event.ResetInteractionToolCommand;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.PropertiesCollection;
import org.opentcs.guing.model.elements.AbstractConnection;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.guing.util.UserMessageHelper;
import org.opentcs.util.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Zeigt die Attribute eines ModelComponent in einer Tabelle. Die
 * PropertiesTable kann entweder nur die Attribute zu einem bestimmten
 * Fahrzeugtyp anzeigen oder je nach aktivem Fahrzeugtyp variieren.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PropertiesTableContent
    extends AbstractTableContent
    implements AttributesChangeListener,
               ConnectionChangeListener,
               CellEditorListener {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PropertiesTableContent.class);
  /**
   * A factory for cell editors.
   */
  private final CellEditorFactory cellEditorFactory;
  /**
   * Provides attribute table models.
   */
  private final Provider<AttributesTableModel> tableModelProvider;
  /**
   * The application's event bus.
   */
  private final EventBus eventBus;
  /**
   * A parent for dialogs created by this instance.
   */
  private final JPanel dialogParent;

  /**
   * Creates a new instance.
   *
   * @param cellEditorFactory A factory for cell editors.
   * @param tableProvider Provides attribute tables.
   * @param tableModelProvider Provides attribute table models.
   * @param eventBus The application's event bus.
   * @param dialogParent A parent for dialogs created by this instance.
   */
  @Inject
  public PropertiesTableContent(CellEditorFactory cellEditorFactory,
                                Provider<AttributesTable> tableProvider,
                                Provider<AttributesTableModel> tableModelProvider,
                                @ApplicationEventBus EventBus eventBus,
                                @Assisted JPanel dialogParent) {
    super(tableProvider);
    this.cellEditorFactory = requireNonNull(cellEditorFactory,
                                            "cellEditorFactory");
    this.tableModelProvider = requireNonNull(tableModelProvider,
                                             "tableModelProvider");
    this.eventBus = requireNonNull(eventBus, "eventBus");
    this.dialogParent = requireNonNull(dialogParent, "dialogParent");
  }

  @Override  // AbstractTableContent
  public void tableModelChanged() {
    if (!fEvaluateTableChanges) {
      return;
    }

    if (fModel == null) {
      return;
    }

    if (fModel instanceof PropertiesCollection) {
      fModel.propertiesChanged(this);
      eventBus.onEvent(new ResetInteractionToolCommand(this));
      // updates some values required by PropertiesCollection
      setModel(fModel);
    }
    else {
      // Prï¿½fen, ob Name gï¿½ltig
      if (fModel.getName().isEmpty()) {
        try {
          if (fUndoRedoManager.canUndo()) {
            fUndoRedoManager.undo();
          }
        }
        catch (CannotUndoException ex) {
          LOG.warn("Exception trying to undo", ex);
        }
      }
      // 2013-11-04 HH: Alle ï¿½nderungen im Modell speichern, nicht nur den Namen!
      fModel.propertiesChanged(this);
      eventBus.onEvent(new ResetInteractionToolCommand(this));
    }
  }

  @Override  // AbstractAttributesContent
  public void setModel(ModelComponent model) {
    if (fModel != null) {
      fModel.removeAttributesChangeListener(this);

      if (fModel instanceof AbstractConnection) {
        ((AbstractConnection) fModel).removeConnectionChangeListener(this);
      }
    }

    fModel = model;
    fModel.addAttributesChangeListener(this);

    if (fModel instanceof AbstractConnection) {
      ((AbstractConnection) fModel).addConnectionChangeListener(this);
    }

    setTableContent(fModel.getProperties());
  }

  @Override  // AbstractAttributesContent
  public String getDescription() {
    if (fModel != null) {
      return fModel.getDescription();
    }

    return "";
  }

  @Override  // AbstractAttributesContent
  public void reset() {
    if (fModel != null) {
      fModel.removeAttributesChangeListener(this);

      if (fModel instanceof AbstractConnection) {
        ((AbstractConnection) fModel).removeConnectionChangeListener(this);
      }
    }

    fModel = null;
    setTableContent(new HashMap<String, Property>());
  }

  @Override  // AttributesChangeListener
  public void propertiesChanged(AttributesChangeEvent e) {
//    if (e.getInitiator() == this) {
//      // Setzt das SelectionTool zurï¿½ck, wenn der Benutzer ï¿½nderungen
//      // an der Tabelle vorgenommen hat
//      guiManager.resetSelectionTool();
//    }
//    else {
    // 2013-12-09 HH Test: Tabelle immer sofort aktualisieren - gibt das Performance-Verluste?
    fEvaluateTableChanges = false;
    ((DefaultTableModel) fTable.getModel()).fireTableDataChanged();
    fEvaluateTableChanges = true;
//    }
  }

  @Override  // ConnectionChangeListener
  public void connectionChanged(ConnectionChangeEvent e) {
    setTableContent(fModel.getProperties());
  }

  @Override
  public void editingStopped(ChangeEvent e) {

  }

  @Override
  public void editingCanceled(ChangeEvent e) {

  }

  @Override  // AbstractTableContent
  protected void setTableCellRenderers() {
    fTable.setDefaultRenderer(Object.class, new StandardPropertyCellRenderer());
    fTable.setDefaultRenderer(BooleanProperty.class, new BooleanPropertyCellRenderer());
    fTable.setDefaultRenderer(ColorProperty.class, new ColorPropertyCellRenderer());
  }

  @Override  // AbstractTableContent
  protected void setTableCellEditors() {
    // A dialog for entering values of complex properties
    DetailsDialog dialog;
    // The content panel of this dialog
    DetailsDialogContent content;
    // An editor allowing undo/redo
    UndoableCellEditor undoableEditor;
    UserMessageHelper umh = new UserMessageHelper();

    // String properties: Name etc.
    content = new StringPropertyEditorPanel();
    dialog = new StandardDetailsDialog(dialogParent, true, content);
    undoableEditor = new UndoableCellEditor(new StringPropertyCellEditor(new JTextField(), umh));
    undoableEditor.setDetailsDialog(dialog);
    undoableEditor.setUndoManager(fUndoRedoManager);
    fCellEditors.add(undoableEditor);
    fTable.setDefaultEditor(StringProperty.class, undoableEditor);

    // Abstract Quantity: Angle, Percent, Speed
    content = new QuantityEditorPanel();
    dialog = new StandardDetailsDialog(dialogParent, true, content);
    QuantityCellEditor wrappedQuantityCellEditor = new QuantityCellEditor(new JTextField(), umh);
    wrappedQuantityCellEditor.addCellEditorListener(this);
    undoableEditor = new UndoableCellEditor(wrappedQuantityCellEditor);
    undoableEditor.setDetailsDialog(dialog);
    undoableEditor.setUndoManager(fUndoRedoManager);
    fCellEditors.add(undoableEditor);
    fTable.setDefaultEditor(AbstractQuantity.class, undoableEditor);

    // Length properties: Path length, Vehicle length, Layout scale x/y
    content = new QuantityEditorPanel();
    dialog = new StandardDetailsDialog(dialogParent, true, content);
    wrappedQuantityCellEditor = new QuantityCellEditor(new JTextField(), umh);
    wrappedQuantityCellEditor.addCellEditorListener(this);
    undoableEditor = new UndoableCellEditor(wrappedQuantityCellEditor);
    undoableEditor.setDetailsDialog(dialog);
    undoableEditor.setUndoManager(fUndoRedoManager);
    fCellEditors.add(undoableEditor);
    fTable.setDefaultEditor(LengthProperty.class, undoableEditor);

    // Coordinate properties: x/y-coordinates of Points, Locations
    content = new QuantityEditorPanel();
    dialog = new StandardDetailsDialog(dialogParent, true, content);
    // CoordinateCellEditor with buttons "copy model <-> layout"
    CoordinateCellEditor wrappedCoordinateCellEditor
        = new CoordinateCellEditor(new JTextField(), umh);
    wrappedCoordinateCellEditor.addCellEditorListener(this);
    undoableEditor = new UndoableCellEditor(wrappedCoordinateCellEditor);
    undoableEditor.setDetailsDialog(dialog);
    undoableEditor.setUndoManager(fUndoRedoManager);
    fCellEditors.add(undoableEditor);
    fTable.setDefaultEditor(CoordinateProperty.class, undoableEditor);

    // Selection property: Path type etc.
    content = new SelectionPropertyEditorPanel();
    dialog = new StandardDetailsDialog(dialogParent, true, content);
    undoableEditor
        = new UndoableCellEditor(new SelectionPropertyCellEditor(new JComboBox<>(), umh));
    undoableEditor.setDetailsDialog(dialog);
    undoableEditor.setUndoManager(fUndoRedoManager);
    fCellEditors.add(undoableEditor);
    fTable.setDefaultEditor(SelectionProperty.class, undoableEditor);

    // Location type property
    content = new SelectionPropertyEditorPanel();
    dialog = new StandardDetailsDialog(dialogParent, true, content);
    undoableEditor
        = new UndoableCellEditor(new SelectionPropertyCellEditor(new JComboBox<>(), umh));
    undoableEditor.setDetailsDialog(dialog);
    undoableEditor.setUndoManager(fUndoRedoManager);
    fCellEditors.add(undoableEditor);
    fTable.setDefaultEditor(LocationTypeProperty.class, undoableEditor);

    // Boolean property: Path locked etc.
    undoableEditor
        = new UndoableCellEditor(new BooleanPropertyCellEditor(new JCheckBox(), umh));
    undoableEditor.setUndoManager(fUndoRedoManager);
    fCellEditors.add(undoableEditor);
    fTable.setDefaultEditor(BooleanProperty.class, undoableEditor);

    // Abstract complex property:
    undoableEditor = new UndoableCellEditor(
        cellEditorFactory.createComplexPropertyCellEditor(dialogParent));
    undoableEditor.setUndoManager(fUndoRedoManager);
    fCellEditors.add(undoableEditor);
    fTable.setDefaultEditor(AbstractComplexProperty.class, undoableEditor);

    // Integer property:
    IntegerPropertyCellEditor integerPropertyCellEditor
        = new IntegerPropertyCellEditor(new JFormattedTextField(), umh);
    undoableEditor = new UndoableCellEditor(integerPropertyCellEditor);
    undoableEditor.setUndoManager(fUndoRedoManager);
    fCellEditors.add(undoableEditor);
    fTable.setDefaultEditor(IntegerProperty.class, undoableEditor);

    // Color property:
    undoableEditor = new UndoableCellEditor(new ColorPropertyCellEditor());
    undoableEditor.setUndoManager(fUndoRedoManager);
    fCellEditors.add(undoableEditor);
    fTable.setDefaultEditor(ColorProperty.class, undoableEditor);
  }

  @Override  // AbstractTableContent
  protected TableModel createTableModel(Map<String, Property> content) {
    AttributesTableModel model = tableModelProvider.get();
    ResourceBundleUtil r = ResourceBundleUtil.getBundle();
    String attributeColumn = r.getString("PropertiesTableContent.column.attribute");
    String valueColumn = r.getString("PropertiesTableContent.column.value");
    model.setColumnIdentifiers(new Object[] {attributeColumn, valueColumn});

    for (Property property : content.values()) {
      model.addRow(new Object[] {property.getDescription(), property});
    }

    return model;
  }
}
