// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.modeleditor.application.action.file;

import static javax.swing.Action.ACCELERATOR_KEY;
import static javax.swing.Action.MNEMONIC_KEY;
import static org.opentcs.modeleditor.util.I18nPlantOverviewModeling.MENU_PATH;

import java.awt.event.ActionEvent;
import java.util.Objects;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import org.opentcs.modeleditor.application.OpenTCSView;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * An action to upload the (local) model to the kernel.
 */
public class UploadModelToKernelAction
    extends
      AbstractAction {

  /**
   * This action's ID.
   */
  public static final String ID = "file.uploadModelToKernel";

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(MENU_PATH);

  private final OpenTCSView openTCSView;

  /**
   * Creates a new instance.
   *
   * @param openTCSView The openTCS view
   */
  @SuppressWarnings("this-escape")
  public UploadModelToKernelAction(OpenTCSView openTCSView) {
    this.openTCSView = Objects.requireNonNull(openTCSView);

    putValue(NAME, BUNDLE.getString("uploadModelToKernelAction.name"));
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("uploadModelToKernelAction.shortDescription"));
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("alt P"));
    putValue(MNEMONIC_KEY, Integer.valueOf('P'));

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    openTCSView.uploadModelToKernel();
  }
}
