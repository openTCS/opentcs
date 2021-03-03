/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.panel;

import java.util.HashMap;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.opentcs.guing.application.GuiManager;
import org.opentcs.guing.components.dialogs.DetailsDialog;
import org.opentcs.guing.components.dialogs.DetailsDialogContent;
import org.opentcs.guing.components.dialogs.StandardDetailsDialog;
import org.opentcs.guing.components.properties.AbstractTableContent;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.properties.table.AttributesTableModel;
import org.opentcs.guing.components.properties.table.BooleanPropertyCellEditor;
import org.opentcs.guing.components.properties.table.BooleanPropertyCellRenderer;
import org.opentcs.guing.components.properties.table.ColorPropertyCellEditor;
import org.opentcs.guing.components.properties.table.ColorPropertyCellRenderer;
import org.opentcs.guing.components.properties.table.ComplexPropertyCellEditor;
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
import org.opentcs.guing.components.properties.type.Property;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.event.ConnectionChangeEvent;
import org.opentcs.guing.event.ConnectionChangeListener;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.PropertiesCollection;
import org.opentcs.guing.model.elements.AbstractConnection;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.guing.util.UserMessageHelper;

/**
 * Zeigt die Attribute eines ModelComponent in einer Tabelle. Die
 * PropertiesTable kann entweder nur die Attribute zu einem bestimmten
 * Fahrzeugtyp anzeigen oder je nach aktivem Fahrzeugtyp variieren.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class PropertiesTableContent
    extends AbstractTableContent
    implements AttributesChangeListener,
               ConnectionChangeListener,
               CellEditorListener {

  private static final Logger logger
      = Logger.getLogger(PropertiesTableContent.class.getName());
  /**
   * The drawing view.
   */
  private final GuiManager guiManager;
  /**
   * A parent for dialogs created by this instance.
   */
  private final JPanel dialogParent;

  /**
   * Creates a new instance.
   *
   * @param guiManager The GUI manager to be used.
   * @param dialogParent A parent for dialogs created by this instance.
   */
  public PropertiesTableContent(GuiManager guiManager, JPanel dialogParent) {
    this.guiManager = guiManager;
    this.dialogParent = requireNonNull(dialogParent, "dialogParent");
  }

  @Override	// AbstractTableContent
  public void tableModelChanged() {
    if (!fEvaluateTableChanges) {
      return;
    }

    if (fModel == null) {
      return;
    }

    if (fModel instanceof PropertiesCollection) {
      fModel.propertiesChanged(this);
      // updates some values required by PropertiesCollection
      setModel(fModel);
    }
    else {
      // Prüfen, ob Name gültig
      if (fModel.getName().isEmpty()) {
        try {
          if (fUndoRedoManager.canUndo()) {
            fUndoRedoManager.undo();
          }
        }
        catch (CannotUndoException ex) {
          logger.log(Level.WARNING, "Exception trying to undo", ex);
        }
      }
      // 2013-11-04 HH: Alle Änderungen im Modell speichern, nicht nur den Namen!
      fModel.propertiesChanged(this);
    }
  }

  @Override	// AbstractAttributesContent
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

  @Override	// AbstractAttributesContent
  public String getDescription() {
    if (fModel != null) {
      return fModel.getDescription();
    }

    return "";
  }

  @Override	// AbstractAttributesContent
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

  @Override	// AttributesChangeListener
  public void propertiesChanged(AttributesChangeEvent e) {
    if (e.getInitiator() == this) {
      // Setzt das SelectionTool zurück, wenn der Benutzer Änderungen
      // an der Tabelle vorgenommen hat
      guiManager.resetSelectionTool();
    }
//    else {
    // 2013-12-09 HH Test: Tabelle immer sofort aktualisieren - gibt das Performance-Verluste?
    fEvaluateTableChanges = false;
    ((DefaultTableModel) fTable.getModel()).fireTableDataChanged();
    fEvaluateTableChanges = true;
//    }
  }

  @Override	// ConnectionChangeListener
  public void connectionChanged(ConnectionChangeEvent e) {
    setTableContent(fModel.getProperties());
  }

  @Override
  public void editingStopped(ChangeEvent e) {
    
  }

  @Override
  public void editingCanceled(ChangeEvent e) {
    
  }

  @Override	// AbstractTableContent
  protected void setTableCellRenderers() {
    fTable.setDefaultRenderer(Object.class, new StandardPropertyCellRenderer());
    fTable.setDefaultRenderer(BooleanProperty.class, new BooleanPropertyCellRenderer());
    fTable.setDefaultRenderer(ColorProperty.class, new ColorPropertyCellRenderer());
  }

  @Override	// AbstractTableContent
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
    CoordinateCellEditor wrappedCoordinateCellEditor = 
        new CoordinateCellEditor(new JTextField(), umh);
    wrappedCoordinateCellEditor.addCellEditorListener(this);
    undoableEditor = new UndoableCellEditor(wrappedCoordinateCellEditor);
    undoableEditor.setDetailsDialog(dialog);
    undoableEditor.setUndoManager(fUndoRedoManager);
    fCellEditors.add(undoableEditor);
    fTable.setDefaultEditor(CoordinateProperty.class, undoableEditor);

    // Selection property: Path type etc.
    content = new SelectionPropertyEditorPanel();
    dialog = new StandardDetailsDialog(dialogParent, true, content);
    undoableEditor = 
        new UndoableCellEditor(new SelectionPropertyCellEditor(new JComboBox(), umh));
    undoableEditor.setDetailsDialog(dialog);
    undoableEditor.setUndoManager(fUndoRedoManager);
    fCellEditors.add(undoableEditor);
    fTable.setDefaultEditor(SelectionProperty.class, undoableEditor);

    // Boolean property: Path locked etc.
    undoableEditor = 
        new UndoableCellEditor(new BooleanPropertyCellEditor(new JCheckBox(), umh));
    undoableEditor.setUndoManager(fUndoRedoManager);
    fCellEditors.add(undoableEditor);
    fTable.setDefaultEditor(BooleanProperty.class, undoableEditor);

    // Abstract complex property:
    undoableEditor = 
        new UndoableCellEditor(new ComplexPropertyCellEditor(guiManager, dialogParent));
    undoableEditor.setUndoManager(fUndoRedoManager);
    fCellEditors.add(undoableEditor);
    fTable.setDefaultEditor(AbstractComplexProperty.class, undoableEditor);

    // Integer property:
    IntegerPropertyCellEditor integerPropertyCellEditor = 
        new IntegerPropertyCellEditor(new JFormattedTextField(), umh);
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

  @Override	// AbstractTableContent
  protected TableModel createTableModel(Map<String, Property> content) {
    AttributesTableModel model = new AttributesTableModel(guiManager);
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
