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
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.application.action.ActionFactory;
import org.opentcs.guing.application.action.course.FollowVehicleAction;
import org.opentcs.guing.application.action.course.IntegrationLevelIgnoreAction;
import org.opentcs.guing.application.action.course.IntegrationLevelNoticeAction;
import org.opentcs.guing.application.action.course.IntegrationLevelRespectAction;
import org.opentcs.guing.application.action.course.IntegrationLevelUtilizeAction;
import org.opentcs.guing.application.action.course.ScrollToVehicleAction;
import org.opentcs.guing.application.action.course.SendVehicleToLocationAction;
import org.opentcs.guing.application.action.course.SendVehicleToPointAction;
import org.opentcs.guing.application.action.course.WithdrawAction;
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
    action.setEnabled(model.isAvailableForOrder()
        && !modelManager.getModel().getPointModels().isEmpty());
    add(action);

    action = actionFactory.createSendVehicleToLocationAction(model);
    bundle.configureAction(action, SendVehicleToLocationAction.ID);
    action.setEnabled(model.isAvailableForOrder()
        && !modelManager.getModel().getLocationModels().isEmpty());
    add(action);

    addSeparator();

    JMenu integrateSubMenu
        = new JMenu(bundle.getString("course.vehicle.integrateVehicleSubMenu.text"));
    JCheckBoxMenuItem checkBoxMenuItem;

    action = actionFactory.createIntegrationLevelIgnoreAction(model);
    bundle.configureAction(action, IntegrationLevelIgnoreAction.ID);
    action.setEnabled(!isProcessingOrder(model));
    checkBoxMenuItem = new JCheckBoxMenuItem(action);
    checkBoxMenuItem.setSelected(model.getPropertyIntegrationLevel().getComparableValue()
        .equals(Vehicle.IntegrationLevel.TO_BE_IGNORED));
    integrateSubMenu.add(checkBoxMenuItem);

    action = actionFactory.createIntegrationLevelNoticeAction(model);
    bundle.configureAction(action, IntegrationLevelNoticeAction.ID);
    action.setEnabled(!isProcessingOrder(model));
    checkBoxMenuItem = new JCheckBoxMenuItem(action);
    checkBoxMenuItem.setSelected(model.getPropertyIntegrationLevel().getComparableValue()
        .equals(Vehicle.IntegrationLevel.TO_BE_NOTICED));
    integrateSubMenu.add(checkBoxMenuItem);

    action = actionFactory.createIntegrationLevelRespectAction(model);
    bundle.configureAction(action, IntegrationLevelRespectAction.ID);
    checkBoxMenuItem = new JCheckBoxMenuItem(action);
    checkBoxMenuItem.setSelected(model.getPropertyIntegrationLevel().getComparableValue()
        .equals(Vehicle.IntegrationLevel.TO_BE_RESPECTED));
    integrateSubMenu.add(checkBoxMenuItem);

    action = actionFactory.createIntegrationLevelUtilizeAction(model);
    bundle.configureAction(action, IntegrationLevelUtilizeAction.ID);
    checkBoxMenuItem = new JCheckBoxMenuItem(action);
    checkBoxMenuItem.setSelected(model.getPropertyIntegrationLevel().getComparableValue()
        .equals(Vehicle.IntegrationLevel.TO_BE_UTILIZED));
    integrateSubMenu.add(checkBoxMenuItem);

    add(integrateSubMenu);

    JMenu withdrawSubMenu
        = new JMenu(bundle.getString("course.vehicle.withdrawTransportOrderSubMenu.text"));

    action = actionFactory.createWithdrawAction(model);
    bundle.configureAction(action, WithdrawAction.ID);
    action.setEnabled(isProcessingOrder(model));
    withdrawSubMenu.add(action);

    action = actionFactory.createWithdrawImmediatelyAction(model);
    bundle.configureAction(action, WithdrawImmediatelyAction.ID);
    action.setEnabled(isProcessingOrder(model));
    withdrawSubMenu.add(action);

    add(withdrawSubMenu);
  }

  private boolean isProcessingOrder(VehicleModel vehicle) {
    return (vehicle.getPropertyProcState().getValue() == Vehicle.ProcState.PROCESSING_ORDER)
        || (vehicle.getPropertyProcState().getValue() == Vehicle.ProcState.AWAITING_ORDER);
  }
}
