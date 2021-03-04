/**
 * (c): IML, JHotDraw.
 *
 * Changed by IML to allow access to ResourceBundle.
 *
 *
 * @(#)UngroupAction.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.opentcs.guing.application.action.draw;

import org.jhotdraw.draw.CompositeFigure;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.GroupFigure;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * UngroupAction.
 *
 * @author Werner Randelshofer
 */
public class UngroupAction
    extends GroupAction {

  private final static String ID = "edit.ungroupSelection";

  /**
   * Creates a new instance.
   *
   * @param editor The drawing editor
   */
  public UngroupAction(DrawingEditor editor) {
    super(editor, new GroupFigure(), false);
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    labels.configureAction(this, ID, false);
    updateEnabledState();
  }

  public UngroupAction(DrawingEditor editor, CompositeFigure prototype) {
    super(editor, prototype, false);
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    labels.configureAction(this, ID, false);
    updateEnabledState();
  }
}
