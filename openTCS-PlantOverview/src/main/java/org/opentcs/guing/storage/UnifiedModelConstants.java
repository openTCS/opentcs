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
 * Constants related to reading and writing the kernel's model file format.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface UnifiedModelConstants {

  /**
   * File ending of locally saved models as xml file.
   */
  String FILE_ENDING_XML = "xml";
  /**
   * The file filter this persistor supports.
   */
  FileFilter DIALOG_FILE_FILTER
      = new FileNameExtensionFilter(ResourceBundleUtil.getBundle()
          .getFormatted("PlantOverview.kernelFile.name", FILE_ENDING_XML),
                                    FILE_ENDING_XML);
}
