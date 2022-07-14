/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.application.action.view;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.opentcs.operationsdesk.application.OpenTCSView;
import static org.opentcs.operationsdesk.util.I18nPlantOverviewOperating.MENU_PATH;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * An action for adding new drawing views.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class AddDrawingViewAction
    extends AbstractAction {

  public final static String ID = "view.addDrawingView";

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(MENU_PATH);

  private final OpenTCSView view;

  /**
   * Creates a new instance.
   *
   * @param view The openTCS view
   */
  public AddDrawingViewAction(OpenTCSView view) {
    this.view = view;

    putValue(NAME, BUNDLE.getString("addDrawingViewAction.name"));
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    view.addDrawingView();
  }
}
