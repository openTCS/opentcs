/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.properties;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.guing.components.drawing.course.Origin;
import org.opentcs.guing.components.drawing.figures.FigureConstants;
import org.opentcs.guing.components.drawing.figures.LabeledFigure;
import org.opentcs.guing.components.drawing.figures.TCSFigure;
import org.opentcs.guing.components.properties.event.NullAttributesChangeListener;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.model.AbstractFigureComponent;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Ein Undo für die Änderung eines Attributs.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class CoordinateUndoActivity
    extends javax.swing.undo.AbstractUndoableEdit {

  private final CoordinateProperty property;
  private final CoordinateProperty pxModel;
  private final CoordinateProperty pyModel;
  private CoordinateProperty pxBeforeModification;
  private CoordinateProperty pyBeforeModification;
  private CoordinateProperty pxAfterModification;
  private CoordinateProperty pyAfterModification;
  private final LabeledFigure bufferedFigure;
  private final AffineTransform bufferedTransform;
  private boolean saveTransform;

  /**
   * Creates a new instance of CoordinateUndoActivity
   *
   * @param property
   */
  public CoordinateUndoActivity(CoordinateProperty property) {
    this.property = property;
    AbstractFigureComponent model = (AbstractFigureComponent) property.getModel();
    pxModel = (CoordinateProperty) model.getProperty(AbstractFigureComponent.MODEL_X_POSITION);
    pyModel = (CoordinateProperty) model.getProperty(AbstractFigureComponent.MODEL_Y_POSITION);
    bufferedFigure = (LabeledFigure) model.getFigure();
    bufferedTransform = new AffineTransform();
  }

  /**
   * Sets if the transform should be saved.
   * 
   * @param saveTransform True to save, false otherwise.
   */
  public void setSaveTransform(boolean saveTransform) {
    this.saveTransform = saveTransform;
    if (saveTransform) {
      AbstractFigureComponent model = (AbstractFigureComponent) property.getModel();
      StringProperty pxLayout = (StringProperty) model.getProperty(ElementPropKeys.POINT_POS_X);
      StringProperty pyLayout = (StringProperty) model.getProperty(ElementPropKeys.POINT_POS_Y);

      Origin origin = bufferedFigure.get(FigureConstants.ORIGIN);
      TCSFigure pf = bufferedFigure.getPresentationFigure();
      double zoomScale = pf.getZoomPoint().scale();
      double xModel = pxModel.getValueByUnit(CoordinateProperty.Unit.MM) / (zoomScale * origin.getScaleX());
      double yModel = pyModel.getValueByUnit(CoordinateProperty.Unit.MM) / (-zoomScale * origin.getScaleY());
      String sx = (String) pxLayout.getComparableValue();
      double xLayout = Double.parseDouble(sx) / (zoomScale * origin.getScaleX());
      String sy = (String) pyLayout.getComparableValue();
      double yLayout = Double.parseDouble(sy) / (-zoomScale * origin.getScaleY());

      bufferedTransform.translate(
          xModel - xLayout,
          yModel - yLayout);
    }
  }

  /**
   * Erstellt eine Momentaufnahme vor der Änderung des Attributs.
   */
  public void snapShotBeforeModification() {
    pxBeforeModification = (CoordinateProperty) pxModel.clone();
    pyBeforeModification = (CoordinateProperty) pyModel.clone();
  }

  /**
   * Erstellt eine Momentaufnahme nach der Änderung des Attributs.
   */
  public void snapShotAfterModification() {
    pxAfterModification = (CoordinateProperty) pxModel.clone();
    pyAfterModification = (CoordinateProperty) pyModel.clone();
  }

  @Override
  public String getPresentationName() {
    if (saveTransform) {
      // Figure was changed
      return ResourceBundleUtil.getBundle().getString("edit.transform.text");
    }
    else {
      // Model coordinate was edited
      return ResourceBundleUtil.getBundle().getString("edit.coordinates.text");
    }
  }

  @Override
  public void undo() throws CannotUndoException {
    super.undo();

    pxModel.copyFrom(pxBeforeModification);
    pyModel.copyFrom(pyBeforeModification);
    pxModel.markChanged();
    pyModel.markChanged();

    if (saveTransform) {
      try {
        AffineTransform inverse = bufferedTransform.createInverse();
        bufferedFigure.willChange();
        bufferedFigure.transform(inverse);
        bufferedFigure.changed();
      }
      catch (NoninvertibleTransformException e) {
      }
    }

    pxModel.getModel().propertiesChanged(new NullAttributesChangeListener());
  }

  @Override
  public void redo() throws CannotRedoException {
    super.redo();

    pxModel.copyFrom(pxAfterModification);
    pyModel.copyFrom(pyAfterModification);
    pxModel.markChanged();
    pyModel.markChanged();

    if (saveTransform) {
      bufferedFigure.willChange();
      bufferedFigure.transform(bufferedTransform);
      bufferedFigure.changed();
    }

    pxModel.getModel().propertiesChanged(new NullAttributesChangeListener());
  }
}
