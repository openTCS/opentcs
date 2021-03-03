/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.menus.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import org.opentcs.guing.application.ApplicationState;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * A menu for setting the application's mode of operation.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class FileModeMenu
    extends JMenu {

  private static final ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
  private final JCheckBoxMenuItem modellingModeItem;
  private final JCheckBoxMenuItem operatingModeItem;

  /**
   * Creates a new instance.
   *
   * @param appState Stores the application's current mode of operation.
   * @param view The application's main view.
   */
  @Inject
  public FileModeMenu(final ApplicationState appState,
                      final OpenTCSView view) {
    super(labels.getString("file.mode.setMode"));
    requireNonNull(view, "view");

    final ButtonGroup bgMode = new ButtonGroup();

    modellingModeItem
        = new JCheckBoxMenuItem(labels.getString("kernel.stateModelling"));
    add(modellingModeItem);

    if (appState.getOperationMode().equals(OperationMode.MODELLING)) {
      modellingModeItem.setSelected(true);
    }

    bgMode.add(modellingModeItem);

    modellingModeItem.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            view.switchPlantOverviewState(OperationMode.MODELLING);
            if (!appState.hasOperationMode(OperationMode.MODELLING)) {
              operatingModeItem.setSelected(true);
            }
          }
        }
    );

    operatingModeItem
        = new JCheckBoxMenuItem(labels.getString("kernel.stateOperating"));
    add(operatingModeItem);

    if (appState.getOperationMode().equals(OperationMode.OPERATING)) {
      operatingModeItem.setSelected(true);
    }

    bgMode.add(operatingModeItem);

    operatingModeItem.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            view.switchPlantOverviewState(OperationMode.OPERATING);
            if (!appState.hasOperationMode(OperationMode.OPERATING)) {
              modellingModeItem.setSelected(true);
            }
          }
        }
    );
  }

  public void setOperationMode(OperationMode mode) {
    requireNonNull(mode, "mode");

    modellingModeItem.setSelected(mode == OperationMode.MODELLING);
    operatingModeItem.setSelected(mode == OperationMode.OPERATING);
  }
}
