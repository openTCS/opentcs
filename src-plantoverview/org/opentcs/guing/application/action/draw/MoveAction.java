/**
 * (c): IML, JHotDraw.
 * 
 * Changed by IML to allow access to ResourceBundle.
 *
 * 
 * @(#)MoveAction.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.opentcs.guing.application.action.draw;

import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.util.HashSet;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.action.AbstractSelectedAction;
import org.jhotdraw.draw.event.TransformEdit;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Moves the selected figures by one unit.
 *
 * @author Werner Randelshofer
 */
public abstract class MoveAction
    extends AbstractSelectedAction {

  private final int dx, dy;

  /**
   * Creates a new instance.
   */
  public MoveAction(DrawingEditor editor, int dx, int dy) {
    super(editor);
    this.dx = dx;
    this.dy = dy;
    updateEnabledState();
  }

  @Override
  public void actionPerformed(java.awt.event.ActionEvent e) {
    AffineTransform tx = new AffineTransform();

    // TODO: Die Faktoren konfigurierbar machen?
    if ((e.getModifiers() & ActionEvent.CTRL_MASK) > 0) {
      tx.translate(dx * 10, dy * 10);
    }
    else if ((e.getModifiers() & ActionEvent.SHIFT_MASK) > 0) {
      tx.translate(dx, dy);
    }
    else {
      tx.translate(dx * 5, dy * 5);
    }

    HashSet<Figure> transformedFigures = new HashSet<>();

    for (Figure f : getView().getSelectedFigures()) {
      if (f.isTransformable()) {
        transformedFigures.add(f);
        f.willChange();
        f.transform(tx);
        f.changed();
      }
    }

    fireUndoableEditHappened(new TransformEdit(transformedFigures, tx));
  }

  public static class East
      extends MoveAction {

    public final static String ID = "edit.moveEast";

    public East(DrawingEditor editor) {
      super(editor, 1, 0);
      ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
      labels.configureAction(this, ID, false);
    }
  }

  public static class West
      extends MoveAction {

    public final static String ID = "edit.moveWest";

    public West(DrawingEditor editor) {
      super(editor, -1, 0);
      ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
      labels.configureAction(this, ID, false);
    }
  }

  public static class North
      extends MoveAction {

    public final static String ID = "edit.moveNorth";

    public North(DrawingEditor editor) {
      super(editor, 0, -1);
      ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
      labels.configureAction(this, ID, false);
    }
  }

  public static class South
      extends MoveAction {

    public final static String ID = "edit.moveSouth";

    public South(DrawingEditor editor) {
      super(editor, 0, 1);
      ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
      labels.configureAction(this, ID, false);
    }
  }
}
