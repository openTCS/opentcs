/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.menus;

import com.google.inject.assistedinject.Assisted;
import java.util.Collection;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.application.action.ActionFactory;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.persistence.ModelManager;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * A popup menu for actions for multiple selected vehicles.
 *
 * @author Leonard Schuengel (Fraunhofer IML)
 */
public class VehiclePopupMenu
    extends JPopupMenu {

  /**
   * Creates a new instance.
   *
   * @param modelManager Provides access to the current system model.
   * @param actionFactory A factory for menu actions.
   * @param vehicles a set of all currently selected Vehicles.
   */
  @Inject
  public VehiclePopupMenu(ModelManager modelManager,
                          ActionFactory actionFactory,
                          @Assisted Collection<VehicleModel> vehicles) {
    requireNonNull(modelManager, "modelManager");
    requireNonNull(actionFactory, "actionFactory");
    requireNonNull(vehicles, "vehicles");

    final ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(I18nPlantOverview.VEHICLEPOPUP_PATH);
    VehicleModel singleVehicle = vehicles.stream().findFirst().get();
    JCheckBoxMenuItem checkBoxMenuItem;
    Action action;

    JMenuItem mi = new JMenuItem();
    mi.setEnabled(false);
    if (vehicles.size() == 1) {
      mi.setText(bundle.getString("vehiclePopupMenu.menuItem_singleVehicle.text") + singleVehicle.getName());
    }
    else {
      mi.setText(bundle.getString("vehiclePopupMenu.menuItem_multipleVehicles.text"));
    }
    add(mi);

    addSeparator();

    if (vehicles.size() == 1) {
      action = actionFactory.createScrollToVehicleAction(singleVehicle);
      add(action);

      action = actionFactory.createFollowVehicleAction(singleVehicle);
      JCheckBoxMenuItem followCheckBox = new JCheckBoxMenuItem();
      followCheckBox.setAction(action);
      followCheckBox.setSelected(singleVehicle.isViewFollows());
      add(followCheckBox);

      addSeparator();
    }

    if (vehicles.size() == 1) {
      action = actionFactory.createSendVehicleToPointAction(singleVehicle);
      action.setEnabled(singleVehicle.isAvailableForOrder()
          && !modelManager.getModel().getPointModels().isEmpty());
      add(action);

      action = actionFactory.createSendVehicleToLocationAction(singleVehicle);
      action.setEnabled(singleVehicle.isAvailableForOrder()
          && !modelManager.getModel().getLocationModels().isEmpty());
      add(action);

      addSeparator();
    }

    JMenu integrateSubMenu
        = new JMenu(bundle.getString("vehiclePopupMenu.subMenu_integrate.text"));

    action = actionFactory.createIntegrationLevelChangeAction(vehicles,
                                                              Vehicle.IntegrationLevel.TO_BE_IGNORED);
    action.setEnabled(!isAnyProcessingOrder(vehicles));
    checkBoxMenuItem = new JCheckBoxMenuItem(action);
    checkBoxMenuItem.setSelected(isAnyAtIntegrationLevel(vehicles,
                                                         Vehicle.IntegrationLevel.TO_BE_IGNORED));
    integrateSubMenu.add(checkBoxMenuItem);

    action = actionFactory.createIntegrationLevelChangeAction(vehicles,
                                                              Vehicle.IntegrationLevel.TO_BE_NOTICED);
    action.setEnabled(!isAnyProcessingOrder(vehicles));
    checkBoxMenuItem = new JCheckBoxMenuItem(action);
    checkBoxMenuItem.setSelected(isAnyAtIntegrationLevel(vehicles,
                                                         Vehicle.IntegrationLevel.TO_BE_NOTICED));
    integrateSubMenu.add(checkBoxMenuItem);

    action = actionFactory.createIntegrationLevelChangeAction(vehicles,
                                                              Vehicle.IntegrationLevel.TO_BE_RESPECTED);
    checkBoxMenuItem = new JCheckBoxMenuItem(action);
    checkBoxMenuItem.setSelected(isAnyAtIntegrationLevel(vehicles,
                                                         Vehicle.IntegrationLevel.TO_BE_RESPECTED));
    integrateSubMenu.add(checkBoxMenuItem);

    action = actionFactory.createIntegrationLevelChangeAction(vehicles,
                                                              Vehicle.IntegrationLevel.TO_BE_UTILIZED);
    checkBoxMenuItem = new JCheckBoxMenuItem(action);
    checkBoxMenuItem.setSelected(isAnyAtIntegrationLevel(vehicles,
                                                         Vehicle.IntegrationLevel.TO_BE_UTILIZED));
    integrateSubMenu.add(checkBoxMenuItem);

    add(integrateSubMenu);

    addSeparator();

    JMenu withdrawSubMenu
        = new JMenu(bundle.getString("vehiclePopupMenu.subMenu_withdraw.text"));

    action = actionFactory.createWithdrawAction(vehicles, false);
    action.setEnabled(isAnyProcessingOrder(vehicles));
    withdrawSubMenu.add(action);

    action = actionFactory.createWithdrawAction(vehicles, true);
    action.setEnabled(isAnyProcessingOrder(vehicles));
    withdrawSubMenu.add(action);

    add(withdrawSubMenu);
  }

  private boolean isAnyProcessingOrder(Collection<VehicleModel> vehicles) {
    return vehicles.stream().anyMatch(vehicle -> isProcessingOrder(vehicle));
  }

  private boolean isProcessingOrder(VehicleModel vehicle) {
    return vehicle.getPropertyProcState().getValue() == Vehicle.ProcState.PROCESSING_ORDER
        || vehicle.getPropertyProcState().getValue() == Vehicle.ProcState.AWAITING_ORDER;
  }

  private boolean isAnyAtIntegrationLevel(Collection<VehicleModel> vehicles,
                                          Vehicle.IntegrationLevel level) {
    return vehicles.stream().anyMatch(
        vehicle -> vehicle.getPropertyIntegrationLevel().getComparableValue().equals(level)
    );
  }
}
