/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.view;

import java.awt.event.ActionEvent;
import static java.util.Objects.requireNonNull;
import javax.swing.AbstractAction;
import org.opentcs.guing.application.OpenTCSView;
import static org.opentcs.guing.util.I18nPlantOverview.MENU_PATH;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * An action for adding new transport order views.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class AddTransportOrderViewAction
    extends AbstractAction {

  public final static String ID = "view.addTransportOrderView";
  
  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(MENU_PATH);
  
  private final OpenTCSView view;

  /**
   * Creates a new instance.
   *
   * @param view The openTCS view
   */
  public AddTransportOrderViewAction(OpenTCSView view) {
    this.view = requireNonNull(view, "view");
    
    putValue(NAME, BUNDLE.getString("addTransportOrderViewAction.name"));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    view.addTransportOrderView();
  }
}
