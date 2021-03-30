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
import javax.swing.JPopupMenu;
import org.opentcs.guing.application.GuiManager;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.persistence.ModelManager;

/**
 * Ein Fahrzeug-Objekt in der Baumansicht.
 * <p>
 * <b>Entwurfsmuster:</b> Befehl.
 * VehicleUserObject ist ein konkreter Befehl.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class VehicleUserObjectModeling
    extends VehicleUserObject {

  /**
   * Creates a new instance.
   *
   * @param model The corresponding vehicle object.
   * @param guiManager The gui manager.
   * @param modelManager Provides the current system model.
   */
  @Inject
  public VehicleUserObjectModeling(@Assisted VehicleModel model,
                                   GuiManager guiManager,
                                   ModelManager modelManager) {
    super(model, guiManager, modelManager);
  }

  @Override  // AbstractUserObject
  public JPopupMenu getPopupMenu() {
    return null;
  }
}
