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
import java.util.Set;
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

  /**
   * The X offset by which to move.
   */
  private final int dx;
  /**
   * The Y offset by which to move.
   */
  private final int dy;

  /**
   * Creates a new instance.
   *
   * @param editor The application's drawing editor.
   * @param dx The X offset by which to move.
   * @param dy The Y offset by which to move.
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

    // TODO: Make these factors configurable?
    if ((e.getModifiers() & ActionEvent.CTRL_MASK) > 0) {
      tx.translate(dx * 10, dy * 10);
    }
    else if ((e.getModifiers() & ActionEvent.SHIFT_MASK) > 0) {
      tx.translate(dx, dy);
    }
    else {
      tx.translate(dx * 5, dy * 5);
    }

    Set<Figure> transformedFigures = new HashSet<>();

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

  /**
   * Moves the selected figures to the right.
   */
  public static class East
      extends MoveAction {

    /**
     * This action's ID.
     */
    public final static String ID = "edit.moveEast";

    /**
     * Creates a new instance.
     *
     * @param editor The application's drawing editor.
     */
    public East(DrawingEditor editor) {
      super(editor, 1, 0);
      ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
      labels.configureAction(this, ID, false);
    }
  }

  /**
   * Moves the selected figures to the right.
   */
  public static class West
      extends MoveAction {

    /**
     * This action's ID.
     */
    public final static String ID = "edit.moveWest";

    /**
     * Creates a new instance.
     *
     * @param editor The application's drawing editor.
     */
    public West(DrawingEditor editor) {
      super(editor, -1, 0);
      ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
      labels.configureAction(this, ID, false);
    }
  }

  /**
   * Moves the selected figures upwards.
   */
  public static class North
      extends MoveAction {

    /**
     * This action's ID.
     */
    public final static String ID = "edit.moveNorth";

    /**
     * Creates a new instance.
     *
     * @param editor The application's drawing editor.
     */
    public North(DrawingEditor editor) {
      super(editor, 0, -1);
      ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
      labels.configureAction(this, ID, false);
    }
  }

  /**
   * Moves the selected figures downwards.
   */
  public static class South
      extends MoveAction {

    /**
     * This action's ID.
     */
    public final static String ID = "edit.moveSouth";

    /**
     * Creates a new instance.
     *
     * @param editor The application's drawing editor.
     */
    public South(DrawingEditor editor) {
      super(editor, 0, 1);
      ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
      labels.configureAction(this, ID, false);
    }
  }
}
