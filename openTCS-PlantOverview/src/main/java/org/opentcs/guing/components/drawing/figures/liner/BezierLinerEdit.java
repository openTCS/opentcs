/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.drawing.figures.liner;

import javax.swing.SwingUtilities;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.jhotdraw.draw.BezierFigure;
import org.jhotdraw.draw.event.BezierNodeEdit;
import org.jhotdraw.geom.BezierPath;
import org.opentcs.guing.components.drawing.figures.PathConnection;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 *
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class BezierLinerEdit
    extends javax.swing.undo.AbstractUndoableEdit {

  private final BezierFigure fOwner;
  private final BezierNodeEdit fNodeEdit;

  /**
   * @param owner A path
   */
  public BezierLinerEdit(BezierFigure owner) {
    fOwner = owner;
    BezierPath.Node node = owner.getNode(0);
    fNodeEdit = new BezierNodeEdit(owner, 0, node, node);
  }

  /**
   *
   * @return The associated PathConnection
   */
  public BezierFigure getOwner() {
    return fOwner;
  }

  @Override // AbstractUndoableEdit
  public boolean isSignificant() {
    return false;
  }

  @Override // AbstractUndoableEdit
  public String getPresentationName() {
    return ResourceBundleUtil.getBundle().getString("edit.bezierNode.movePoint.text");
  }

  @Override // AbstractUndoableEdit
  public void redo() throws CannotRedoException {
    fNodeEdit.redo();
    updateProperties();
  }

  @Override // AbstractUndoableEdit
  public void undo() throws CannotUndoException {
    fNodeEdit.undo();
    updateProperties();
  }

  private void updateProperties() {
    SwingUtilities.invokeLater(() -> {
      PathConnection path = (PathConnection) fOwner;
      path.updateControlPoints();
      path.getModel().getPropertyPathControlPoints().markChanged();
      path.getModel().propertiesChanged(path);
    });
  }
}
