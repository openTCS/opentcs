// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.persistence.unified;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.opentcs.guing.common.util.I18nPlantOverview;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * Constants related to reading and writing the kernel's model file format.
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
      = new FileNameExtensionFilter(
          ResourceBundleUtil.getBundle(I18nPlantOverview.SYSTEM_PATH)
              .getFormatted("unifiedModelConstants.dialogFileFilter.description", FILE_ENDING_XML),
          FILE_ENDING_XML
      );
}
