/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.tree.elements;

import com.google.inject.assistedinject.Assisted;
import javax.inject.Inject;
import javax.swing.ImageIcon;
import org.opentcs.guing.base.model.elements.LocationTypeModel;
import org.opentcs.guing.common.application.GuiManager;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.guing.common.util.IconToolkit;

/**
 * Represents a location type in the TreeView.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class LocationTypeUserObject
    extends AbstractUserObject {

  /**
   * Creates a new instance of StationUserObject
   *
   * @param modelComponent
   * @param guiManager The gui manager.
   * @param modelManager The model manager
   */
  @Inject
  public LocationTypeUserObject(@Assisted LocationTypeModel modelComponent,
                                GuiManager guiManager,
                                ModelManager modelManager) {
    super(modelComponent, guiManager, modelManager);
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
    getGuiManager().figureSelected(getModelComponent());
  }
}
