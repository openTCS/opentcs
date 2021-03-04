/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.synchronize;

import com.google.inject.Inject;
import java.awt.event.ActionEvent;
import static java.util.Objects.requireNonNull;
import javax.swing.AbstractAction;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * An action to switch to operating mode.
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class SwitchToOperatingAction
    extends AbstractAction {

  /**
   * The id of this action. (Often used to map a key combination in the ressources.)
   */
  public static final String ID = "file.mode.switchToOperating";

  /**
   * The view.
   */
  private final OpenTCSView view;

  @Inject
  public SwitchToOperatingAction(OpenTCSView view) {
    this.view = requireNonNull(view, "view");
    ResourceBundleUtil.getBundle().configureAction(this, ID, false);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    view.switchPlantOverviewState(OperationMode.OPERATING);
  }
}
