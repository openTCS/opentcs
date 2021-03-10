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
import javax.inject.Inject;
import javax.swing.AbstractAction;
import static javax.swing.Action.ACCELERATOR_KEY;
import static javax.swing.Action.MNEMONIC_KEY;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import static org.opentcs.guing.util.I18nPlantOverview.MENU_PATH;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * An action to load the current kernel model in the plant overview.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
public class LoadModelFromKernelAction
    extends AbstractAction {

  public static final String ID = "synchronize.loadModelFromKernel";

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(MENU_PATH);
  /**
   * The OpenTCS view.
   */
  private final OpenTCSView openTCSView;
  /**
   * The drawing editor.
   */
  private final OpenTCSDrawingEditor openTCSDrawingEditor;

  /**
   * Creates a new instance.
   *
   * @param openTCSView The openTCS view.
   * @param openTCSDrawingEditor The editor and coordinator of drawings.
   */
  @Inject
  public LoadModelFromKernelAction(OpenTCSView openTCSView,
                                   OpenTCSDrawingEditor openTCSDrawingEditor) {
    this.openTCSView = requireNonNull(openTCSView);
    this.openTCSDrawingEditor = requireNonNull(openTCSDrawingEditor);

    putValue(NAME, BUNDLE.getString("loadModelFromKernelAction.name"));
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("loadModelFromKernelAction.shortDescription"));
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("alt K"));
    putValue(MNEMONIC_KEY, Integer.valueOf('K'));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    openTCSView.loadCurrentKernelModel();
    //All loaded model figures are by default visible, including the vehicles.
    //However the vehicles must not be shown in modelling mode.
    //Therefore we hide we vehicles after the model is loaded and the triggered events are finished.
    SwingUtilities.invokeLater(() -> openTCSDrawingEditor.showVehicles(false));
  }
}
