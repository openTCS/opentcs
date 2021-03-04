/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.menus.menubar;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import org.opentcs.guing.application.ApplicationState;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.application.action.ViewActionMap;
import org.opentcs.guing.application.action.synchronize.SwitchToModellingAction;
import org.opentcs.guing.application.action.synchronize.SwitchToOperatingAction;
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
   * @param actionMap The application's action map.
   */
  @Inject
  public FileModeMenu(final ApplicationState appState,
                      final OpenTCSView view,
                      final ViewActionMap actionMap) {
    super(labels.getString("file.mode.setMode"));
    requireNonNull(view, "view");
    requireNonNull(actionMap, "actionMap");

    final ButtonGroup modeButtonGroup = new ButtonGroup();

    modellingModeItem = new JCheckBoxMenuItem(actionMap.get(SwitchToModellingAction.ID));
    add(modellingModeItem);
    modeButtonGroup.add(modellingModeItem);

    operatingModeItem = new JCheckBoxMenuItem(actionMap.get(SwitchToOperatingAction.ID));
    add(operatingModeItem);
    modeButtonGroup.add(operatingModeItem);
  }

  public void setOperationMode(OperationMode mode) {
    requireNonNull(mode, "mode");

    modellingModeItem.setSelected(mode == OperationMode.MODELLING);
    operatingModeItem.setSelected(mode == OperationMode.OPERATING);
  }
}
