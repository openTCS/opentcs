/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.menus;

import com.google.inject.assistedinject.Assisted;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Figure;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.guing.application.action.edit.UndoRedoManager;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.components.drawing.figures.FigureConstants;
import org.opentcs.guing.components.properties.CoordinateUndoActivity;
import org.opentcs.guing.components.properties.event.NullAttributesChangeListener;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.event.ResetInteractionToolCommand;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.util.event.EventHandler;

/**
 * A menu item for copying the value of the layout properties of selected points
 * or locations to the corresponding model properties.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class LayoutToModelMenuItem
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
   * Where we send events.
   */
  private final EventHandler eventHandler;
  /**
   * A flag if the values of ALL points and location shall be copied when
   * the menu item is clicked. If false only the selected figures will be
   * considered.
   */
  private final boolean copyAll;

  /**
   * Creates a new instance.
   *
   * @param drawingEditor A <code>DrawingEditor</code> instance.
   * @param undoRedoManager The application's undo/redo manager.
   * @param eventHandler Where this instance sends events.
   * @param copyAll Indicates whether the values of ALL points and locations
   * shall be copied when the menu item is clicked. If false only the selected
   * figures will be considered.
   */
  @Inject
  public LayoutToModelMenuItem(OpenTCSDrawingEditor drawingEditor,
                               UndoRedoManager undoRedoManager,
                               @ApplicationEventBus EventHandler eventHandler,
                               @Assisted boolean copyAll) {
    super(ResourceBundleUtil.getBundle().getString("propertiesTable.fromLayout"));
    this.drawingEditor = requireNonNull(drawingEditor, "drawingEditor");
    this.undoRedoManager = requireNonNull(undoRedoManager, "undoRedoManager");
    this.eventHandler = requireNonNull(eventHandler, "eventHandler");
    this.copyAll = copyAll;

    setIcon(new ImageIcon(
        getClass().getClassLoader().getResource("org/opentcs/guing/res/symbols/menu/arrow-up-3.png")));
    setMargin(new Insets(0, 2, 0, 2));
    addActionListener();
  }

  private void addActionListener() {
    addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        for (Figure figure : copyAll
            ? getView().getDrawing().getFiguresFrontToBack()
            : getView().getSelectedFigures()) {
          ModelComponent model = figure.get(FigureConstants.MODEL);
          if (model instanceof PointModel || model instanceof LocationModel) {
            updateModelX(model);
            updateModelY(model);
            model.propertiesChanged(new NullAttributesChangeListener());
            eventHandler.onEvent(new ResetInteractionToolCommand(this));
          }
        }
      }
    });
  }

  private DrawingView getView() {
    return drawingEditor.getActiveView();
  }

  private void updateModelY(ModelComponent model)
      throws IllegalArgumentException {
    CoordinateProperty modelProperty;
    CoordinateUndoActivity cua;
    if (model instanceof PointModel) {
      modelProperty = (CoordinateProperty) model.getProperty(PointModel.MODEL_Y_POSITION);
    }
    else {
      modelProperty = (CoordinateProperty) model.getProperty(LocationModel.MODEL_Y_POSITION);
    }
    cua = new CoordinateUndoActivity(modelProperty);
    cua.snapShotBeforeModification();
    StringProperty spy;
    if (model instanceof PointModel) {
      spy = (StringProperty) model.getProperty(ElementPropKeys.POINT_POS_Y);
    }
    else {
      spy = (StringProperty) model.getProperty(ElementPropKeys.LOC_POS_Y);
    }

    if (!spy.getText().isEmpty()) {
      modelProperty.setValueAndUnit(Double.parseDouble(spy.getText()), modelProperty.getUnit());
      modelProperty.markChanged();
    }
    cua.setSaveTransform(false);
    cua.snapShotAfterModification();
    undoRedoManager.addEdit(cua);
  }

  private void updateModelX(ModelComponent model)
      throws IllegalArgumentException {
    CoordinateProperty modelProperty;
    if (model instanceof PointModel) {
      modelProperty = (CoordinateProperty) model.getProperty(PointModel.MODEL_X_POSITION);
    }
    else {
      modelProperty = (CoordinateProperty) model.getProperty(LocationModel.MODEL_X_POSITION);
    }
    CoordinateUndoActivity cua = new CoordinateUndoActivity(modelProperty);
    cua.snapShotBeforeModification();
    StringProperty spx;
    if (model instanceof PointModel) {
      spx = (StringProperty) model.getProperty(ElementPropKeys.POINT_POS_X);
    }
    else {
      spx = (StringProperty) model.getProperty(ElementPropKeys.LOC_POS_X);
    }
    if (!spx.getText().isEmpty()) {
      modelProperty.setValueAndUnit(Double.parseDouble(spx.getText()), modelProperty.getUnit());
      modelProperty.markChanged();
    }
    cua.setSaveTransform(false);
    cua.snapShotAfterModification();
    undoRedoManager.addEdit(cua);
  }
}
