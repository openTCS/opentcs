/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.tree.elements;

import com.google.inject.assistedinject.Assisted;
import javax.inject.Inject;
import javax.swing.JComponent;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.model.CompositeModelComponent;
import org.opentcs.guing.model.ModelManager;

/**
 * Ein einfacher Ordner in der Baumansicht, dem keine weitere Funktionalität
 * zugeordnet ist.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class SimpleFolderUserObject
    extends AbstractUserObject {

  /**
   * Creates a new instance.
   *
   * @param dataObject The associated model component.
   * @param view The application's main view.
   * @param editor The application's drawing editor.
   * @param modelManager Provides access to the currently loaded system model.
   */
  @Inject
  public SimpleFolderUserObject(@Assisted CompositeModelComponent dataObject,
                                OpenTCSView view,
                                OpenTCSDrawingEditor editor,
                                ModelManager modelManager) {
    super(dataObject, view, editor, modelManager);
  }

  @Override // AbstractUserObject
  public boolean removed() {
    return false;
  }

  @Override // AbstractUserObject
  public void rightClicked(JComponent component, int x, int y) {
    // Empty - no popup menu to be displayed.
  }
}
