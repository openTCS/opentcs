/**
 * (c): IML, JHotDraw.
 *
 * Changed by IML to allow access to ResourceBundle.
 *
 *
 * @(#)SelectSameAction.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.opentcs.guing.application.action.draw;

import java.util.HashSet;
import java.util.Set;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.action.AbstractSelectedAction;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * SelectSameAction.
 *
 * @author Werner Randelshofer
 */
public class SelectSameAction
    extends AbstractSelectedAction {

  public final static String ID = "edit.selectSame";

  /**
   * Creates a new instance.
   *
   * @param editor The drawing editor
   */
  public SelectSameAction(DrawingEditor editor) {
    super(editor);
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    labels.configureAction(this, ID, false);
    updateEnabledState();
  }

  @Override
  public void actionPerformed(java.awt.event.ActionEvent e) {
    selectSame();
  }

  public void selectSame() {
    Set<Class<?>> selectedClasses = new HashSet<>();

    for (Figure selected : getView().getSelectedFigures()) {
      selectedClasses.add(selected.getClass());
    }

    for (Figure f : getDrawing().getChildren()) {
      if (selectedClasses.contains(f.getClass())) {
        getView().addToSelection(f);
      }
    }
  }
}
