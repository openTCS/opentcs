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
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.opentcs.guing.application.action.ActionFactory;
import org.opentcs.guing.application.action.course.VehicleAction;
import org.opentcs.guing.model.ModelManager;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * A popup menu for vehicle-specific actions.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VehiclePopupMenu
    extends JPopupMenu {

  /**
   * Creates a new instance.
   *
   * @param modelManager Provides access to the current system model.
   * @param actionFactory A factory for menu actions.
   * @param model The vehicle for which the menu is intended to be shown.
   */
  @Inject
  public VehiclePopupMenu(ModelManager modelManager,
                          ActionFactory actionFactory,
                          @Assisted VehicleModel model) {
    requireNonNull(modelManager, "modelManager");
    requireNonNull(actionFactory, "actionFactory");
    requireNonNull(model, "model");

    JMenuItem mi = new JMenuItem(ResourceBundleUtil.getBundle().getString("VehicleAction.vehicle") + model.getName());
    // Disabled, Foreground, Background, ...
    mi.setEnabled(false);
    add(mi);

    addSeparator();

    add(actionFactory.createVehicleAction(VehicleAction.SCROLL_TO, model));

    JCheckBoxMenuItem followCheckBox = new JCheckBoxMenuItem();
    followCheckBox.setAction(actionFactory.createVehicleAction(VehicleAction.FOLLOW, model));
    followCheckBox.setSelected(model.isViewFollows());
    add(followCheckBox);

    addSeparator();

    VehicleAction vehicleAction;
    vehicleAction = actionFactory.createVehicleAction(VehicleAction.SEND_TO_POINT, model);
    vehicleAction.setEnabled(!modelManager.getModel().getPointModels().isEmpty());
    add(vehicleAction);
    vehicleAction = actionFactory.createVehicleAction(VehicleAction.SEND_TO_LOCATION, model);
    vehicleAction.setEnabled(!modelManager.getModel().getLocationModels().isEmpty());
    add(vehicleAction);

    addSeparator();

    JMenu withdrawSubMenu = new JMenu(ResourceBundleUtil.getBundle().getString(
        "course.vehicle.withdrawTransportOrderSubMenu.text"));

    vehicleAction = actionFactory.createVehicleAction(VehicleAction.WITHDRAW_TRANSPORT_ORDER, model);
    vehicleAction.setEnabled(model.isAvailableForOrder());
    withdrawSubMenu.add(vehicleAction);
    vehicleAction = actionFactory.createVehicleAction(VehicleAction.WITHDRAW_TRANSPORT_ORDER_DISABLE_VEHICLE, model);
    vehicleAction.setEnabled(model.isAvailableForOrder());
    withdrawSubMenu.add(vehicleAction);
    vehicleAction = actionFactory.createVehicleAction(VehicleAction.WITHDRAW_TRANSPORT_ORDER_IMMEDIATELY, model);
    vehicleAction.setEnabled(model.isAvailableForOrder());
    withdrawSubMenu.add(vehicleAction);
    
    withdrawSubMenu.addSeparator();
    
    withdrawSubMenu.add(actionFactory.createVehicleAction(VehicleAction.RELEASE_VEHICLE, model));

    add(withdrawSubMenu);

    add(actionFactory.createVehicleAction(VehicleAction.DISPATCH_VEHICLE, model));

  }

}
