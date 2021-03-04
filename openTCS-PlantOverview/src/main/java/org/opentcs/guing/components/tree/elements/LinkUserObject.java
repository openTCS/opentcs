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
import javax.swing.ImageIcon;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.model.ModelManager;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.util.IconToolkit;

/**
 * Die Repräsentation eines Links in der Baumansicht.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class LinkUserObject
    extends FigureUserObject {

  /**
   * Creates a new instance of LinkUserObject
   *
   * @param modelComponent The corresponding data object
   * @param view The openTCS view
   * @param editor The drawing editor
   * @param modelManager The model manager
   */
  @Inject
  public LinkUserObject(@Assisted LinkModel modelComponent,
                        OpenTCSView view,
                        OpenTCSDrawingEditor editor,
                        ModelManager modelManager) {
    super(modelComponent, view, editor, modelManager);
  }

  @Override
  public LinkModel getModelComponent() {
    return (LinkModel) super.getModelComponent();
  }

  @Override
  public ImageIcon getIcon() {
    return IconToolkit.instance().createImageIcon("tree/link.18x18.png");
  }
}
