/*
 * openTCS copyright information:
 * Copyright (c) 2017 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.storage;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Constants related to reading and writing the plant overview's own model file format.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface ModelJAXBConstants {

  /**
   * File name extension of locally saved models as opentcs file.
   */
  String FILE_ENDING_OPENTCS = "opentcs";

  /**
   * The file filter this persistor supports.
   */
  FileFilter DIALOG_FILE_FILTER
      = new FileNameExtensionFilter(ResourceBundleUtil.getBundle()
          .getFormatted("PlantOverview.modelFile.name", FILE_ENDING_OPENTCS),
                                    FILE_ENDING_OPENTCS);
}
