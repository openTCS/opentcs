// SPDX-FileCopyrightText: The original authors of JHotDraw and all its contributors
// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.thirdparty.guing.common.jhotdraw.application.action.draw;

import static javax.swing.Action.SMALL_ICON;
import static org.opentcs.guing.common.util.I18nPlantOverview.TOOLBAR_PATH;

import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.util.HashSet;
import java.util.Set;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.action.AbstractSelectedAction;
import org.jhotdraw.draw.event.TransformEdit;
import org.opentcs.guing.common.util.ImageDirectory;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * Moves the selected figures by one unit.
 *
 * @author Werner Randelshofer
 */
public abstract class MoveAction
    extends
      AbstractSelectedAction {

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(TOOLBAR_PATH);
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
  @SuppressWarnings("this-escape")
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
      extends
        MoveAction {

    /**
     * This action's ID.
     */
    public static final String ID = "edit.moveEast";

    /**
     * Creates a new instance.
     *
     * @param editor The application's drawing editor.
     */
    @SuppressWarnings("this-escape")
    public East(DrawingEditor editor) {
      super(editor, 1, 0);

      putValue(SHORT_DESCRIPTION, BUNDLE.getString("moveAction.east.shortDescription"));
      putValue(SMALL_ICON, ImageDirectory.getImageIcon("/toolbar/draw-arrow-forward.png"));
    }
  }

  /**
   * Moves the selected figures to the right.
   */
  public static class West
      extends
        MoveAction {

    /**
     * This action's ID.
     */
    public static final String ID = "edit.moveWest";

    /**
     * Creates a new instance.
     *
     * @param editor The application's drawing editor.
     */
    @SuppressWarnings("this-escape")
    public West(DrawingEditor editor) {
      super(editor, -1, 0);

      putValue(SHORT_DESCRIPTION, BUNDLE.getString("moveAction.west.shortDescription"));
      putValue(SMALL_ICON, ImageDirectory.getImageIcon("/toolbar/draw-arrow-back.png"));
    }
  }

  /**
   * Moves the selected figures upwards.
   */
  public static class North
      extends
        MoveAction {

    /**
     * This action's ID.
     */
    public static final String ID = "edit.moveNorth";

    /**
     * Creates a new instance.
     *
     * @param editor The application's drawing editor.
     */
    @SuppressWarnings("this-escape")
    public North(DrawingEditor editor) {
      super(editor, 0, -1);

      putValue(SHORT_DESCRIPTION, BUNDLE.getString("moveAction.north.shortDescription"));
      putValue(SMALL_ICON, ImageDirectory.getImageIcon("/toolbar/draw-arrow-up.png"));
    }
  }

  /**
   * Moves the selected figures downwards.
   */
  public static class South
      extends
        MoveAction {

    /**
     * This action's ID.
     */
    public static final String ID = "edit.moveSouth";

    /**
     * Creates a new instance.
     *
     * @param editor The application's drawing editor.
     */
    @SuppressWarnings("this-escape")
    public South(DrawingEditor editor) {
      super(editor, 0, 1);

      putValue(SHORT_DESCRIPTION, BUNDLE.getString("moveAction.south.shortDescription"));
      putValue(SMALL_ICON, ImageDirectory.getImageIcon("/toolbar/draw-arrow-down.png"));
    }
  }
}
