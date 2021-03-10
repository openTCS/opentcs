/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.synchronize;

import java.awt.event.ActionEvent;
import java.util.Objects;
import javax.swing.AbstractAction;
import static javax.swing.Action.ACCELERATOR_KEY;
import static javax.swing.Action.MNEMONIC_KEY;
import javax.swing.KeyStroke;
import org.opentcs.guing.application.OpenTCSView;
import static org.opentcs.guing.util.I18nPlantOverview.MENU_PATH;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * An action to save the (local) model in the kernel.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
public class PersistInKernelAction
    extends AbstractAction {

  public static final String ID = "synchronize.saveModelInKernel";

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(MENU_PATH);

  private final OpenTCSView openTCSView;

  /**
   * Creates a new instance.
   *
   * @param openTCSView The openTCS view
   */
  public PersistInKernelAction(OpenTCSView openTCSView) {
    this.openTCSView = Objects.requireNonNull(openTCSView);

    putValue(NAME, BUNDLE.getString("persistInKernelAction.name"));
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("persistInKernelAction.shortDescription"));
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("alt P"));
    putValue(MNEMONIC_KEY, Integer.valueOf('P'));

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    openTCSView.persistModel();
  }
}
