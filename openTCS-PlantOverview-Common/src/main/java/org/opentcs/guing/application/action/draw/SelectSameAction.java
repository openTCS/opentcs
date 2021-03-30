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
import static javax.swing.Action.SMALL_ICON;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.action.AbstractSelectedAction;
import static org.opentcs.guing.util.I18nPlantOverview.MODELVIEW_PATH;
import org.opentcs.guing.util.ImageDirectory;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * SelectSameAction.
 *
 * @author Werner Randelshofer
 */
public class SelectSameAction
    extends AbstractSelectedAction {

  public final static String ID = "edit.selectSame";

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(MODELVIEW_PATH);

  /**
   * Creates a new instance.
   *
   * @param editor The drawing editor
   */
  public SelectSameAction(DrawingEditor editor) {
    super(editor);

    putValue(NAME, BUNDLE.getString("selectSameAction.name"));
    putValue(SMALL_ICON, ImageDirectory.getImageIcon("/menu/kcharselect.png"));

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
