/**
 * (c): IML, JHotDraw.
 *
 * Changed by IML to allow access to ResourceBundle.
 *
 *
 * @(#)AlignAction.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.opentcs.guing.application.action.draw;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.action.AbstractSelectedAction;
import org.jhotdraw.draw.event.TransformEdit;
import org.jhotdraw.undo.CompositeEdit;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Aligns the selected figures.
 *
 * XXX - Fire edit events
 *
 * @author Werner Randelshofer
 */
public abstract class AlignAction
    extends AbstractSelectedAction {

  protected ResourceBundleUtil labels = ResourceBundleUtil.getBundle();

  /**
   * Creates a new instance.
   *
   * @param editor The drawing editor
   */
  public AlignAction(DrawingEditor editor) {
    super(editor);
    updateEnabledState();
  }

  @Override
  protected final void updateEnabledState() {
    if (getView() != null) {
      setEnabled(getView().isEnabled()
          && getView().getSelectionCount() > 1);
    }
    else {
      setEnabled(false);
    }
  }

  @Override
  public void actionPerformed(java.awt.event.ActionEvent e) {
    CompositeEdit edit = new CompositeEdit(labels.getString("edit.align.text"));
    fireUndoableEditHappened(edit);
    alignFigures(getView().getSelectedFigures(), getSelectionBounds());
    fireUndoableEditHappened(edit);
  }

  protected abstract void alignFigures(Collection<?> selectedFigures,
                                       Rectangle2D.Double selectionBounds);

  /**
   * Returns the bounds of the selected figures.
   */
  protected Rectangle2D.Double getSelectionBounds() {
    Rectangle2D.Double bounds = null;

    for (Figure f : getView().getSelectedFigures()) {
      if (bounds == null) {
        bounds = f.getBounds();
      }
      else {
        bounds.add(f.getBounds());
      }
    }

    return bounds;
  }

  public static class North
      extends AlignAction {

    public North(DrawingEditor editor) {
      super(editor);
      labels.configureAction(this, "edit.alignNorth", false);
    }

    @Override
    protected void alignFigures(Collection<?> selectedFigures, Rectangle2D.Double selectionBounds) {
      double y = selectionBounds.y;

      for (Figure f : getView().getSelectedFigures()) {
        if (f.isTransformable()) {
          f.willChange();
          Rectangle2D.Double b = f.getBounds();
          AffineTransform tx = new AffineTransform();
          tx.translate(0, y - b.y);
          f.transform(tx);
          f.changed();
          fireUndoableEditHappened(new TransformEdit(f, tx));
        }
      }
    }
  }

  public static class East
      extends AlignAction {

    public East(DrawingEditor editor) {
      super(editor);
      labels.configureAction(this, "edit.alignEast", false);
    }

    @Override
    protected void alignFigures(Collection<?> selectedFigures, Rectangle2D.Double selectionBounds) {
      double x = selectionBounds.x + selectionBounds.width;

      for (Figure f : getView().getSelectedFigures()) {
        if (f.isTransformable()) {
          f.willChange();
          Rectangle2D.Double b = f.getBounds();
          AffineTransform tx = new AffineTransform();
          tx.translate(x - b.x - b.width, 0);
          f.transform(tx);
          f.changed();
          fireUndoableEditHappened(new TransformEdit(f, tx));
        }
      }
    }
  }

  public static class West
      extends AlignAction {

    public West(DrawingEditor editor) {
      super(editor);
      labels.configureAction(this, "edit.alignWest", false);
    }

    @Override
    protected void alignFigures(Collection<?> selectedFigures, Rectangle2D.Double selectionBounds) {
      double x = selectionBounds.x;

      for (Figure f : getView().getSelectedFigures()) {
        if (f.isTransformable()) {
          f.willChange();
          Rectangle2D.Double b = f.getBounds();
          AffineTransform tx = new AffineTransform();
          tx.translate(x - b.x, 0);
          f.transform(tx);
          f.changed();
          fireUndoableEditHappened(new TransformEdit(f, tx));
        }
      }
    }
  }

  public static class South
      extends AlignAction {

    public South(DrawingEditor editor) {
      super(editor);
      labels.configureAction(this, "edit.alignSouth", false);
    }

    @Override
    protected void alignFigures(Collection<?> selectedFigures, Rectangle2D.Double selectionBounds) {
      double y = selectionBounds.y + selectionBounds.height;

      for (Figure f : getView().getSelectedFigures()) {
        if (f.isTransformable()) {
          f.willChange();
          Rectangle2D.Double b = f.getBounds();
          AffineTransform tx = new AffineTransform();
          tx.translate(0, y - b.y - b.height);
          f.transform(tx);
          f.changed();
          fireUndoableEditHappened(new TransformEdit(f, tx));
        }
      }
    }
  }

  public static class Vertical
      extends AlignAction {

    public Vertical(DrawingEditor editor) {
      super(editor);
      labels.configureAction(this, "edit.alignVertical", false);
    }

    @Override
    protected void alignFigures(Collection<?> selectedFigures, Rectangle2D.Double selectionBounds) {
      double y = selectionBounds.y + selectionBounds.height / 2;

      for (Figure f : getView().getSelectedFigures()) {
        if (f.isTransformable()) {
          f.willChange();
          Rectangle2D.Double b = f.getBounds();
          AffineTransform tx = new AffineTransform();
          tx.translate(0, y - b.y - b.height / 2);
          f.transform(tx);
          f.changed();
          fireUndoableEditHappened(new TransformEdit(f, tx));
        }
      }
    }
  }

  public static class Horizontal
      extends AlignAction {

    public Horizontal(DrawingEditor editor) {
      super(editor);
      labels.configureAction(this, "edit.alignHorizontal", false);
    }

    @Override
    protected void alignFigures(Collection<?> selectedFigures, Rectangle2D.Double selectionBounds) {
      double x = selectionBounds.x + selectionBounds.width / 2;

      for (Figure f : getView().getSelectedFigures()) {
        if (f.isTransformable()) {
          f.willChange();
          Rectangle2D.Double b = f.getBounds();
          AffineTransform tx = new AffineTransform();
          tx.translate(x - b.x - b.width / 2, 0);
          f.transform(tx);
          f.changed();
          fireUndoableEditHappened(new TransformEdit(f, tx));
        }
      }
    }
  }
}
