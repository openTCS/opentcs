// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.modeleditor.application.action.file;

import static java.util.Objects.requireNonNull;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.opentcs.components.plantoverview.PlantModelImporter;
import org.opentcs.guing.common.application.GuiManager;

/**
 */
public class ImportPlantModelAction
    extends
      AbstractAction {

  private final PlantModelImporter importer;
  private final GuiManager guiManager;

  /**
   * Creates a new instance.
   *
   * @param importer The importer.
   * @param guiManager The gui manager
   */
  @SuppressWarnings("this-escape")
  public ImportPlantModelAction(PlantModelImporter importer, GuiManager guiManager) {
    this.importer = requireNonNull(importer, "importer");
    this.guiManager = requireNonNull(guiManager, "guiManager");
    this.putValue(NAME, importer.getDescription());
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    guiManager.importModel(importer);
  }
}
