// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.application.action.view;

import static java.util.Objects.requireNonNull;
import static org.opentcs.operationsdesk.util.I18nPlantOverviewOperating.MENU_PATH;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.opentcs.operationsdesk.application.OpenTCSView;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * An action for adding new peripheral job views.
 */
public class AddPeripheralJobViewAction
    extends
      AbstractAction {

  /**
   * This action's ID.
   */
  public static final String ID = "view.addPeripheralJobView";

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(MENU_PATH);

  private final OpenTCSView view;

  /**
   * Creates a new instance.
   *
   * @param view The openTCS view
   */
  @SuppressWarnings("this-escape")
  public AddPeripheralJobViewAction(OpenTCSView view) {
    this.view = requireNonNull(view, "view");

    putValue(NAME, BUNDLE.getString("addPeripheralJobViewAction.name"));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    view.addPeripheralJobsView();
  }
}
