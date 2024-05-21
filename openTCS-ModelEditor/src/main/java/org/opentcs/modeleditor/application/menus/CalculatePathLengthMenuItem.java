/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.application.menus;

import java.awt.event.ActionEvent;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.jhotdraw.draw.DrawingEditor;
import org.opentcs.guing.base.components.properties.event.NullAttributesChangeListener;
import org.opentcs.guing.base.components.properties.type.LengthProperty;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.common.components.dialogs.StandardContentDialog;
import org.opentcs.guing.common.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.common.components.drawing.figures.FigureConstants;
import org.opentcs.guing.common.components.properties.PropertyUndoActivity;
import org.opentcs.modeleditor.components.dialog.PathTypeSelectionPanel;
import org.opentcs.modeleditor.math.path.PathLengthFunction;
import org.opentcs.modeleditor.util.I18nPlantOverviewModeling;
import org.opentcs.thirdparty.guing.common.jhotdraw.application.action.edit.UndoRedoManager;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;
import org.opentcs.util.gui.Icons;

/**
 * A menu item to calculate and update the lengths of paths.
 */
public class CalculatePathLengthMenuItem
    extends JMenuItem {

  /**
   * The <code>DrawingEditor</code> instance.
   */
  private final DrawingEditor drawingEditor;
  /**
   * The UndoRedoManager instance to be used.
   */
  private final UndoRedoManager undoRedoManager;
  /**
   * The calculator to use to calculate the path length.
   */
  private final Provider<PathLengthFunction> pathLengthFunctionProvider;

  @Inject
  public CalculatePathLengthMenuItem(OpenTCSDrawingEditor drawingEditor,
                                     UndoRedoManager undoRedoManager,
                                     Provider<PathLengthFunction> pathLengthFunctionProvider) {
    super(ResourceBundleUtil.getBundle(I18nPlantOverviewModeling.MENU_PATH)
        .getString("calculatePathLengthMenuItem.text"));
    this.drawingEditor = requireNonNull(drawingEditor, "drawingEditor");
    this.undoRedoManager = requireNonNull(undoRedoManager, "undoRedoManager");
    this.pathLengthFunctionProvider
        = requireNonNull(pathLengthFunctionProvider, "pathLengthFunctionProvider");

    addActionListener(this::calculatePathLength);
  }

  private void calculatePathLength(ActionEvent e) {
    PathTypeSelectionPanel content = new PathTypeSelectionPanel();
    StandardContentDialog dialog
        = new StandardContentDialog(JOptionPane.getFrameForComponent(this), content);
    dialog.setIconImages(Icons.getOpenTCSIcons());
    dialog.setVisible(true);

    if (dialog.getReturnStatus() != StandardContentDialog.RET_OK) {
      return;
    }

    PathLengthFunction pathLengthFunction = pathLengthFunctionProvider.get();

    drawingEditor.getActiveView().getDrawing().getFiguresFrontToBack().stream()
        .map(figure -> figure.get(FigureConstants.MODEL))
        .filter(model -> model instanceof PathModel)
        .map(model -> (PathModel) model)
        .filter(path -> content.isPathTypeSelected(connectionType(path)))
        .forEach(path -> updatePath(path, pathLengthFunction));
  }

  private PathModel.Type connectionType(PathModel path) {
    return (PathModel.Type) path.getPropertyPathConnType().getValue();
  }

  private void updatePath(PathModel path, PathLengthFunction pathLengthFunction) {
    updatePathLength(path, Math.round(pathLengthFunction.applyAsDouble(path)));
    path.propertiesChanged(new NullAttributesChangeListener());
  }

  private void updatePathLength(PathModel path, double length) {
    LengthProperty pathLengthProperty = path.getPropertyLength();

    PropertyUndoActivity pua = new PropertyUndoActivity(pathLengthProperty);
    pua.snapShotBeforeModification();

    pathLengthProperty.setValueAndUnit(length, LengthProperty.Unit.MM);
    pathLengthProperty.markChanged();

    pua.snapShotAfterModification();
    undoRedoManager.addEdit(pua);
  }

}
