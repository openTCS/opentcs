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
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.opentcs.guing.application.action.ActionFactory;
import org.opentcs.guing.application.action.course.DispatchVehicleAction;
import org.opentcs.guing.application.action.course.FollowVehicleAction;
import org.opentcs.guing.application.action.course.ReleaseVehicleAction;
import org.opentcs.guing.application.action.course.ScrollToVehicleAction;
import org.opentcs.guing.application.action.course.SendVehicleToLocationAction;
import org.opentcs.guing.application.action.course.SendVehicleToPointAction;
import org.opentcs.guing.application.action.course.WithdrawAction;
import org.opentcs.guing.application.action.course.WithdrawAndDisableAction;
import org.opentcs.guing.application.action.course.WithdrawImmediatelyAction;
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

    final ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();

    JMenuItem mi = new JMenuItem(bundle.getString("VehicleAction.vehicle") + model.getName());
    // Disabled, Foreground, Background, ...
    mi.setEnabled(false);
    add(mi);

    addSeparator();

    Action action;

    action = actionFactory.createScrollToVehicleAction(model);
    bundle.configureAction(action, ScrollToVehicleAction.ID);
    add(action);

    action = actionFactory.createFollowVehicleAction(model);
    bundle.configureAction(action, FollowVehicleAction.ID);
    JCheckBoxMenuItem followCheckBox = new JCheckBoxMenuItem();
    followCheckBox.setAction(action);
    followCheckBox.setSelected(model.isViewFollows());
    add(followCheckBox);

    addSeparator();

    action = actionFactory.createSendVehicleToPointAction(model);
    bundle.configureAction(action, SendVehicleToPointAction.ID);
    action.setEnabled(!modelManager.getModel().getPointModels().isEmpty());
    add(action);

    action = actionFactory.createSendVehicleToLocationAction(model);
    bundle.configureAction(action, SendVehicleToLocationAction.ID);
    action.setEnabled(!modelManager.getModel().getLocationModels().isEmpty());
    add(action);

    addSeparator();

    JMenu withdrawSubMenu
        = new JMenu(bundle.getString("course.vehicle.withdrawTransportOrderSubMenu.text"));

    action = actionFactory.createWithdrawAction(model);
    bundle.configureAction(action, WithdrawAction.ID);
    action.setEnabled(model.isAvailableForOrder());
    withdrawSubMenu.add(action);

    action = actionFactory.createWithdrawAndDisableAction(model);
    bundle.configureAction(action, WithdrawAndDisableAction.ID);
    action.setEnabled(model.isAvailableForOrder());
    withdrawSubMenu.add(action);

    action = actionFactory.createWithdrawImmediatelyAction(model);
    bundle.configureAction(action, WithdrawImmediatelyAction.ID);
    action.setEnabled(model.isAvailableForOrder());
    withdrawSubMenu.add(action);

    withdrawSubMenu.addSeparator();

    action = actionFactory.createReleaseVehicleAction(model);
    bundle.configureAction(action, ReleaseVehicleAction.ID);
    withdrawSubMenu.add(action);

    add(withdrawSubMenu);

    action = actionFactory.createDispatchVehicleAction(model);
    bundle.configureAction(action, DispatchVehicleAction.ID);
    add(action);

  }

}
