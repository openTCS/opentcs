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
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.opentcs.guing.util.IconToolkit;

/**
 * Die Repräsentation einer Station in der Baumansicht.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class LocationTypeUserObject
    extends AbstractUserObject {

  /**
   * Creates a new instance of StationUserObject
   *
   * @param modelComponent
   * @param view The openTCS view
   * @param editor The drawing editor
   * @param modelManager The model manager
   */
  @Inject
  public LocationTypeUserObject(@Assisted LocationTypeModel modelComponent,
                                OpenTCSView view,
                                OpenTCSDrawingEditor editor,
                                ModelManager modelManager) {
    super(modelComponent, view, editor, modelManager);
  }

  @Override
  public LocationTypeModel getModelComponent() {
    return (LocationTypeModel) super.getModelComponent();
  }

  @Override
  public ImageIcon getIcon() {
    return IconToolkit.instance().createImageIcon("tree/locationType.18x18.png");
  }

  @Override
  public void doubleClicked() {
    getView().figureSelected(getModelComponent());
  }
}
