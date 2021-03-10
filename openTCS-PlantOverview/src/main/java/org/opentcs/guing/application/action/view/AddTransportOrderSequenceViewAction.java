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
import javax.swing.AbstractAction;
import org.opentcs.guing.application.OpenTCSView;
import static org.opentcs.guing.util.I18nPlantOverview.MENU_PATH;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * An action for adding new transport order sequence views.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class AddTransportOrderSequenceViewAction
    extends AbstractAction {

  public final static String ID = "view.addOrderSequenceView";

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(MENU_PATH);

  private final OpenTCSView view;

  /**
   * Creates a new instance.
   *
   * @param view The openTCS view
   */
  public AddTransportOrderSequenceViewAction(OpenTCSView view) {
    this.view = view;

    putValue(NAME, BUNDLE.getString("addTransportOrderSequenceViewAction.name"));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    view.addTransportOrderSequenceView();
  }
}
