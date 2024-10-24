// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.modeleditor.application.action.file;

import static java.util.Objects.requireNonNull;
import static javax.swing.Action.ACCELERATOR_KEY;
import static javax.swing.Action.MNEMONIC_KEY;
import static org.opentcs.modeleditor.util.I18nPlantOverviewModeling.MENU_PATH;

import jakarta.inject.Inject;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import org.opentcs.modeleditor.application.OpenTCSView;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * An action to load the current kernel model in the plant overview.
 */
public class DownloadModelFromKernelAction
    extends
      AbstractAction {

  /**
   * This action's ID.
   */
  public static final String ID = "file.downloadModelFromKernel";

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(MENU_PATH);
  /**
   * The OpenTCS view.
   */
  private final OpenTCSView openTCSView;

  /**
   * Creates a new instance.
   *
   * @param openTCSView The openTCS view.
   */
  @Inject
  @SuppressWarnings("this-escape")
  public DownloadModelFromKernelAction(OpenTCSView openTCSView) {
    this.openTCSView = requireNonNull(openTCSView);

    putValue(NAME, BUNDLE.getString("downloadModelFromKernelAction.name"));
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("downloadModelFromKernelAction.shortDescription"));
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("alt K"));
    putValue(MNEMONIC_KEY, Integer.valueOf('K'));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    openTCSView.downloadModelFromKernel();
  }
}
