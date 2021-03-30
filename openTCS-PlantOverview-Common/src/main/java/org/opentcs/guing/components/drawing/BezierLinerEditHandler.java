/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.drawing;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import org.jhotdraw.draw.BezierFigure;
import org.opentcs.guing.components.drawing.figures.PathConnection;
import org.opentcs.guing.components.drawing.figures.liner.BezierLinerEdit;
import org.opentcs.guing.model.elements.PathModel;

/**
 * Updates a bezier-style PathConnection's control points on edits.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class BezierLinerEditHandler
    implements UndoableEditListener {

  /**
   * Creates a new instance.
   */
  public BezierLinerEditHandler() {
  }

  @Override
  public void undoableEditHappened(UndoableEditEvent evt) {
    if (!(evt.getEdit() instanceof BezierLinerEdit)) {
      return;
    }
    BezierFigure owner = ((BezierLinerEdit) evt.getEdit()).getOwner();
    if (!(owner instanceof PathConnection)) {
      return;
    }

    PathConnection path = (PathConnection) owner;
    path.updateControlPoints();
    PathModel pathModel = path.getModel();
    pathModel.getPropertyPathControlPoints().markChanged();
    pathModel.propertiesChanged(path);
  }
}
