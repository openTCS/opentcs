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

import com.google.inject.assistedinject.Assisted;
import java.awt.geom.AffineTransform;
import static java.util.Objects.requireNonNull;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.opentcs.guing.components.drawing.figures.LabeledFigure;
import org.opentcs.guing.components.properties.event.NullAttributesChangeListener;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.PositionableModelComponent;
import org.opentcs.guing.persistence.ModelManager;

/**
 * An undo for the modification of a coordinate property.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class CoordinateUndoActivity
    extends AbstractUndoableEdit {

  protected final CoordinateProperty property;
  protected final CoordinateProperty pxModel;
  protected final CoordinateProperty pyModel;
  protected final LabeledFigure bufferedFigure;
  protected final AffineTransform bufferedTransform = new AffineTransform();
  private CoordinateProperty pxBeforeModification;
  private CoordinateProperty pyBeforeModification;
  private CoordinateProperty pxAfterModification;
  private CoordinateProperty pyAfterModification;

  /**
   * Creates a new instance.
   *
   * @param property The affected property.
   * @param modelManager The model manager to be used.
   */
  public CoordinateUndoActivity(@Assisted CoordinateProperty property,
                                ModelManager modelManager) {
    this.property = requireNonNull(property, "property");

    ModelComponent model = property.getModel();
    pxModel = (CoordinateProperty) model.getProperty(PositionableModelComponent.MODEL_X_POSITION);
    pyModel = (CoordinateProperty) model.getProperty(PositionableModelComponent.MODEL_Y_POSITION);
    bufferedFigure = (LabeledFigure) modelManager.getModel().getFigure(model);
  }

  /**
   * Erstellt eine Momentaufnahme vor der Änderung des Attributs.
   */
  public void snapShotBeforeModification() {
    pxBeforeModification = (CoordinateProperty) pxModel.clone();
    pyBeforeModification = (CoordinateProperty) pyModel.clone();

    saveTransformBeforeModification();
  }

  /**
   * Erstellt eine Momentaufnahme nach der Änderung des Attributs.
   */
  public void snapShotAfterModification() {
    pxAfterModification = (CoordinateProperty) pxModel.clone();
    pyAfterModification = (CoordinateProperty) pyModel.clone();
  }

  @Override
  public void undo()
      throws CannotUndoException {
    super.undo();

    pxModel.copyFrom(pxBeforeModification);
    pyModel.copyFrom(pyBeforeModification);
    pxModel.markChanged();
    pyModel.markChanged();

    saveTransformForUndo();

    pxModel.getModel().propertiesChanged(new NullAttributesChangeListener());
  }

  @Override
  public void redo()
      throws CannotRedoException {
    super.redo();

    pxModel.copyFrom(pxAfterModification);
    pyModel.copyFrom(pyAfterModification);
    pxModel.markChanged();
    pyModel.markChanged();

    saveTransformForRedo();

    pxModel.getModel().propertiesChanged(new NullAttributesChangeListener());
  }

  protected abstract void saveTransformBeforeModification();

  protected abstract void saveTransformForUndo();

  protected abstract void saveTransformForRedo();
}
