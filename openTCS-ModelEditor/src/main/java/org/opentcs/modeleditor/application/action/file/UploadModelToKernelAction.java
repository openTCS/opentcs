/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.application.action.file;

import java.awt.event.ActionEvent;
import java.util.Objects;
import javax.swing.AbstractAction;
import static javax.swing.Action.ACCELERATOR_KEY;
import static javax.swing.Action.MNEMONIC_KEY;
import javax.swing.KeyStroke;
import org.opentcs.modeleditor.application.OpenTCSView;
import static org.opentcs.modeleditor.util.I18nPlantOverviewModeling.MENU_PATH;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * An action to upload the (local) model to the kernel.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class UploadModelToKernelAction
    extends AbstractAction {

  public static final String ID = "file.uploadModelToKernel";

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(MENU_PATH);

  private final OpenTCSView openTCSView;

  /**
   * Creates a new instance.
   *
   * @param openTCSView The openTCS view
   */
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
