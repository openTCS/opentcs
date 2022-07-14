/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.application.action.file;

import java.awt.event.ActionEvent;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import static javax.swing.Action.ACCELERATOR_KEY;
import static javax.swing.Action.MNEMONIC_KEY;
import javax.swing.KeyStroke;
import org.opentcs.modeleditor.application.OpenTCSView;
import static org.opentcs.modeleditor.util.I18nPlantOverviewModeling.MENU_PATH;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * An action to load the current kernel model in the plant overview.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class DownloadModelFromKernelAction
    extends AbstractAction {

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
