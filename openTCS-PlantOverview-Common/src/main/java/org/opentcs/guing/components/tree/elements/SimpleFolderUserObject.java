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
import org.opentcs.guing.application.GuiManager;
import org.opentcs.guing.model.CompositeModelComponent;
import org.opentcs.guing.persistence.ModelManager;

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
   * @param guiManager The gui manager.
   * @param modelManager Provides access to the currently loaded system model.
   */
  @Inject
  public SimpleFolderUserObject(@Assisted CompositeModelComponent dataObject,
                                GuiManager guiManager,
                                ModelManager modelManager) {
    super(dataObject, guiManager, modelManager);
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
