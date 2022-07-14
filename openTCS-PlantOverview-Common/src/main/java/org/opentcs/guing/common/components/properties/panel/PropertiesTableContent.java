/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.properties.panel;

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
import javax.swing.ListCellRenderer;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.undo.CannotUndoException;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.guing.base.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.base.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.base.components.properties.type.AbstractComplexProperty;
import org.opentcs.guing.base.components.properties.type.AbstractQuantity;
import org.opentcs.guing.base.components.properties.type.BlockTypeProperty;
import org.opentcs.guing.base.components.properties.type.BooleanProperty;
import org.opentcs.guing.base.components.properties.type.ColorProperty;
import org.opentcs.guing.base.components.properties.type.CoordinateProperty;
import org.opentcs.guing.base.components.properties.type.IntegerProperty;
import org.opentcs.guing.base.components.properties.type.LengthProperty;
import org.opentcs.guing.base.components.properties.type.LinerTypeProperty;
import org.opentcs.guing.base.components.properties.type.LocationTypeProperty;
import org.opentcs.guing.base.components.properties.type.PointTypeProperty;
import org.opentcs.guing.base.components.properties.type.Property;
import org.opentcs.guing.base.components.properties.type.SelectionProperty;
import org.opentcs.guing.base.components.properties.type.StringProperty;
import org.opentcs.guing.base.event.ConnectionChangeEvent;
import org.opentcs.guing.base.event.ConnectionChangeListener;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.PropertiesCollection;
import org.opentcs.guing.base.model.elements.AbstractConnection;
import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.common.components.dialogs.DetailsDialog;
import org.opentcs.guing.common.components.dialogs.DetailsDialogContent;
import org.opentcs.guing.common.components.dialogs.StandardDetailsDialog;
import org.opentcs.guing.common.components.properties.AbstractTableContent;
import org.opentcs.guing.common.components.properties.PropertiesComponentsFactory;
import org.opentcs.guing.common.components.properties.table.AttributesTable;
import org.opentcs.guing.common.components.properties.table.AttributesTableModel;
import org.opentcs.guing.common.components.properties.table.BlockTypePropertyCellRenderer;
import org.opentcs.guing.common.components.properties.table.BooleanPropertyCellEditor;
import org.opentcs.guing.common.components.properties.table.BooleanPropertyCellRenderer;
import org.opentcs.guing.common.components.properties.table.CellEditorFactory;
import org.opentcs.guing.common.components.properties.table.ColorPropertyCellEditor;
import org.opentcs.guing.common.components.properties.table.ColorPropertyCellRenderer;
import org.opentcs.guing.common.components.properties.table.CoordinateCellEditor;
import org.opentcs.guing.common.components.properties.table.IntegerPropertyCellEditor;
import org.opentcs.guing.common.components.properties.table.LinerTypePropertyCellRenderer;
import org.opentcs.guing.common.components.properties.table.PointTypePropertyCellRenderer;
import org.opentcs.guing.common.components.properties.table.QuantityCellEditor;
import org.opentcs.guing.common.components.properties.table.SelectionPropertyCellEditor;
import org.opentcs.guing.common.components.properties.table.StandardPropertyCellRenderer;
import org.opentcs.guing.common.components.properties.table.StringPropertyCellEditor;
import org.opentcs.guing.common.components.properties.table.UndoableCellEditor;
import org.opentcs.guing.common.event.ResetInteractionToolCommand;
import org.opentcs.guing.common.util.I18nPlantOverview;
import org.opentcs.guing.common.util.UserMessageHelper;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.gui.StringListCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows the attributes of a model component in a table.
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
   * The components factory.
   */
  private final PropertiesComponentsFactory componentsFactory;

  /**
   * Creates a new instance.
   *
   * @param cellEditorFactory A factory for cell editors.
   * @param tableProvider Provides attribute tables.
   * @param tableModelProvider Provides attribute table models.
   * @param eventBus The application's event bus.
   * @param dialogParent A parent for dialogs created by this instance.
   * @param componentsFactory The components factory.
   */
  @Inject
  public PropertiesTableContent(CellEditorFactory cellEditorFactory,
                                Provider<AttributesTable> tableProvider,
                                Provider<AttributesTableModel> tableModelProvider,
                                @ApplicationEventBus EventBus eventBus,
                                @Assisted JPanel dialogParent,
                                PropertiesComponentsFactory componentsFactory) {
    super(tableProvider);
    this.cellEditorFactory = requireNonNull(cellEditorFactory,
                                            "cellEditorFactory");
    this.tableModelProvider = requireNonNull(tableModelProvider,
                                             "tableModelProvider");
    this.eventBus = requireNonNull(eventBus, "eventBus");
    this.dialogParent = requireNonNull(dialogParent, "dialogParent");
    this.componentsFactory = requireNonNull(componentsFactory, "componentsFactory");
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
    fEvaluateTableChanges = false;
    ((DefaultTableModel) fTable.getModel()).fireTableDataChanged();
    fEvaluateTableChanges = true;
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
    fTable.setDefaultRenderer(BlockTypeProperty.class, new BlockTypePropertyCellRenderer());
    fTable.setDefaultRenderer(LinerTypeProperty.class, new LinerTypePropertyCellRenderer());
    fTable.setDefaultRenderer(PointTypeProperty.class, new PointTypePropertyCellRenderer());
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

    ListCellRenderer<Object> defaultRenderer = new StringListCellRenderer<>(this::objectToString);

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
        = componentsFactory.createCoordinateCellEditor(new JTextField(), umh);
    wrappedCoordinateCellEditor.addCellEditorListener(this);
    undoableEditor = new UndoableCellEditor(wrappedCoordinateCellEditor);
    undoableEditor.setDetailsDialog(dialog);
    undoableEditor.setUndoManager(fUndoRedoManager);
    fCellEditors.add(undoableEditor);
    fTable.setDefaultEditor(CoordinateProperty.class, undoableEditor);

    // Selection property
    content = new SelectionPropertyEditorPanel(defaultRenderer);
    dialog = new StandardDetailsDialog(dialogParent, true, content);
    undoableEditor
        = new UndoableCellEditor(new SelectionPropertyCellEditor(new JComboBox<>(), umh));
    undoableEditor.setDetailsDialog(dialog);
    undoableEditor.setUndoManager(fUndoRedoManager);
    fCellEditors.add(undoableEditor);
    fTable.setDefaultEditor(SelectionProperty.class, undoableEditor);

    // Point type property
    ListCellRenderer<PointModel.Type> pointTypeRenderer
        = new StringListCellRenderer<>(type -> type.getDescription());
    content = new SelectionPropertyEditorPanel(pointTypeRenderer);
    dialog = new StandardDetailsDialog(dialogParent, true, content);
    JComboBox<PointModel.Type> pointTypeComboBox = new JComboBox<>();
    pointTypeComboBox.setRenderer(pointTypeRenderer);
    undoableEditor
        = new UndoableCellEditor(new SelectionPropertyCellEditor(pointTypeComboBox, umh));
    undoableEditor.setDetailsDialog(dialog);
    undoableEditor.setUndoManager(fUndoRedoManager);
    fCellEditors.add(undoableEditor);
    fTable.setDefaultEditor(PointTypeProperty.class, undoableEditor);

    // Liner type property
    ListCellRenderer<PathModel.Type> linerTypeRenderer
        = new StringListCellRenderer<>(type -> type.getDescription());
    content = new SelectionPropertyEditorPanel(linerTypeRenderer);
    dialog = new StandardDetailsDialog(dialogParent, true, content);
    JComboBox<PathModel.Type> linerTypeComboBox = new JComboBox<>();
    linerTypeComboBox.setRenderer(linerTypeRenderer);
    undoableEditor
        = new UndoableCellEditor(new SelectionPropertyCellEditor(linerTypeComboBox, umh));
    undoableEditor.setDetailsDialog(dialog);
    undoableEditor.setUndoManager(fUndoRedoManager);
    fCellEditors.add(undoableEditor);
    fTable.setDefaultEditor(LinerTypeProperty.class, undoableEditor);

    // Block type property
    ListCellRenderer<BlockModel.Type> blockTypeRenderer
        = new StringListCellRenderer<>(type -> type.getDescription());
    content = new SelectionPropertyEditorPanel(blockTypeRenderer);
    dialog = new StandardDetailsDialog(dialogParent, true, content);
    JComboBox<BlockModel.Type> blockTypeComboBox = new JComboBox<>();
    blockTypeComboBox.setRenderer(blockTypeRenderer);
    undoableEditor
        = new UndoableCellEditor(new SelectionPropertyCellEditor(blockTypeComboBox, umh));
    undoableEditor.setDetailsDialog(dialog);
    undoableEditor.setUndoManager(fUndoRedoManager);
    fCellEditors.add(undoableEditor);
    fTable.setDefaultEditor(BlockTypeProperty.class, undoableEditor);

    // Location type property
    content = new SelectionPropertyEditorPanel(defaultRenderer);
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
    ResourceBundleUtil r = ResourceBundleUtil.getBundle(I18nPlantOverview.PROPERTIES_PATH);
    String attributeColumn = r.getString("propertiesTableContent.column_attribute.headerText");
    String valueColumn = r.getString("propertiesTableContent.column_value.headerText");
    model.setColumnIdentifiers(new Object[]{attributeColumn, valueColumn});

    for (Property property : content.values()) {
      model.addRow(new Object[]{property.getDescription(), property});
    }

    return model;
  }

  private String objectToString(Object o) {
    return o == null ? "" : o.toString();
  }
}
