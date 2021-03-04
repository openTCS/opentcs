/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.menus.menubar;

import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.opentcs.components.plantoverview.PlantModelExporter;
import org.opentcs.guing.application.GuiManager;
import org.opentcs.guing.application.action.file.ExportPlantModelAction;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class FileExportMenu
    extends JMenu {

  private static final ResourceBundleUtil LABELS = ResourceBundleUtil.getBundle();

  @Inject
  public FileExportMenu(Set<PlantModelExporter> exporters,
                        GuiManager guiManager) {
    super(LABELS.getString("file.export.menu.text"));
    requireNonNull(exporters, "exporters");
    requireNonNull(guiManager, "guiManager");

    for (PlantModelExporter exporter : exporters) {
      add(new JMenuItem(new ExportPlantModelAction(exporter, guiManager)));
    }
  }
}
