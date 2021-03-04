/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.file;

import java.awt.event.ActionEvent;
import static java.util.Objects.requireNonNull;
import javax.swing.AbstractAction;
import org.opentcs.components.plantoverview.PlantModelExporter;
import org.opentcs.guing.application.GuiManager;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ExportPlantModelAction
    extends AbstractAction {

  private final PlantModelExporter exporter;
  private final GuiManager guiManager;

  /**
   * Creates a new instance.
   *
   * @param exporter The importer.
   * @param guiManager The gui manager
   */
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
