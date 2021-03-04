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
import static java.util.Objects.requireNonNull;
import javax.swing.AbstractAction;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * An action to load the current kernel model in the plant overview.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
public class LoadModelFromKernelAction
    extends AbstractAction {

  public static final String ID = "synchronize.loadModelFromKernel";

  private final OpenTCSView openTCSView;

  /**
   * Creates a new instance.
   *
   * @param openTCSView The openTCS view.
   */
  public LoadModelFromKernelAction(OpenTCSView openTCSView) {
    this.openTCSView = requireNonNull(openTCSView);
    ResourceBundleUtil.getBundle().configureAction(this, ID, false);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    openTCSView.loadCurrentKernelModel();
  }
}
