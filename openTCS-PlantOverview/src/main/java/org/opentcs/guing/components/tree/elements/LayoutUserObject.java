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
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.model.ModelManager;
import org.opentcs.guing.model.elements.LayoutModel;

/**
 * Die Repräsentation eines Point-Objekts in der Baumansicht.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LayoutUserObject
    extends FigureUserObject {

  /**
   * Creates a new instance.
   *
   * @param model The corresponding data object
   * @param view The openTCS view
   * @param editor The drawing editor
   * @param modelManager The model manager
   */
  @Inject
  public LayoutUserObject(@Assisted LayoutModel model,
                          OpenTCSView view,
                          OpenTCSDrawingEditor editor,
                          ModelManager modelManager) {
    super(model, view, editor, modelManager);
  }

  @Override
  public LayoutModel getModelComponent() {
    return (LayoutModel) super.getModelComponent();
  }
}
