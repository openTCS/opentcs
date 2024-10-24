// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.modeleditor.application.action.file;

import static java.util.Objects.requireNonNull;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.opentcs.components.plantoverview.PlantModelExporter;
import org.opentcs.guing.common.application.GuiManager;

/**
 */
public class ExportPlantModelAction
    extends
      AbstractAction {

  private final PlantModelExporter exporter;
  private final GuiManager guiManager;

  /**
   * Creates a new instance.
   *
   * @param exporter The importer.
   * @param guiManager The gui manager
   */
  @SuppressWarnings("this-escape")
  public ExportPlantModelAction(PlantModelExporter exporter, GuiManager guiManager) {
    this.exporter = requireNonNull(exporter, "exporter");
    this.guiManager = requireNonNull(guiManager, "guiManager");
    this.putValue(NAME, exporter.getDescription());
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    guiManager.exportModel(exporter);
  }
}
