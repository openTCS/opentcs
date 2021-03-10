/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.AbstractAction;
import static javax.swing.Action.LARGE_ICON_KEY;
import static javax.swing.Action.MNEMONIC_KEY;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.ImageIcon;
import org.opentcs.customizations.plantoverview.ApplicationFrame;
import org.opentcs.guing.components.dialogs.StandardContentDialog;
import org.opentcs.guing.exchange.TransportOrderUtil;
import org.opentcs.guing.transport.CreateTransportOrderPanel;
import org.opentcs.guing.transport.OrderCategorySuggestionsPool;
import static org.opentcs.guing.util.I18nPlantOverview.MENU_PATH;
import org.opentcs.guing.util.ImageDirectory;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * An action to trigger the creation of a transport order.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class CreateTransportOrderAction
    extends AbstractAction {

  /**
   * This action class's ID.
   */
  public static final String ID = "actions.createTransportOrder";

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(MENU_PATH);
  /**
   * A helper for creating transport orders with the kernel.
   */
  private final TransportOrderUtil orderUtil;
  /**
   * The parent component for dialogs shown by this action.
   */
  private final Component dialogParent;
  /**
   * Provides panels for entering new transport orders.
   */
  private final Provider<CreateTransportOrderPanel> orderPanelProvider;
  /**
   * The pool of suggested transport order categories.
   */
  private final OrderCategorySuggestionsPool categorySuggestionsPool;

  /**
   * Creates a new instance.
   *
   * @param orderUtil A helper for creating transport orders with the kernel.
   * @param dialogParent The parent component for dialogs shown by this action.
   * @param orderPanelProvider Provides panels for entering new transport orders.
   * @param categorySuggestionsPool The pool of suggested transport order categories.
   */
  @Inject
  public CreateTransportOrderAction(TransportOrderUtil orderUtil,
                                    @ApplicationFrame Component dialogParent,
                                    Provider<CreateTransportOrderPanel> orderPanelProvider,
                                    OrderCategorySuggestionsPool categorySuggestionsPool) {
    this.orderUtil = requireNonNull(orderUtil, "orderUtil");
    this.dialogParent = requireNonNull(dialogParent, "dialogParent");
    this.orderPanelProvider = requireNonNull(orderPanelProvider,
                                             "orderPanelProvider");
    this.categorySuggestionsPool = requireNonNull(categorySuggestionsPool,
                                                  "categorySuggestionsPool");

    putValue(NAME, BUNDLE.getString("createTransportOrderAction.name"));
    putValue(MNEMONIC_KEY, Integer.valueOf('T'));

    ImageIcon icon = ImageDirectory.getImageIcon("/toolbar/create-order.22.png");
    putValue(SMALL_ICON, icon);
    putValue(LARGE_ICON_KEY, icon);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    createTransportOrder();
  }

  public void createTransportOrder() {
    CreateTransportOrderPanel contentPanel = orderPanelProvider.get();
    StandardContentDialog dialog = new StandardContentDialog(dialogParent, contentPanel);
    dialog.setVisible(true);

    if (dialog.getReturnStatus() != StandardContentDialog.RET_OK) {
      return;
    }
    orderUtil.createTransportOrder(contentPanel.getDestinationModels(),
                                   contentPanel.getActions(),
                                   contentPanel.getSelectedDeadline(),
                                   contentPanel.getSelectedVehicle(),
                                   contentPanel.getSelectedCategory());

    categorySuggestionsPool.addCategorySuggestion(contentPanel.getSelectedCategory());
  }
}
