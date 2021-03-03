/*
 *
 * Created on 11.09.2013 11:45:27
 */
package org.opentcs.guing.application.action.view;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.guing.util.DefaultVehicleThemeManager;
import org.opentcs.guing.util.VehicleThemeManager;
import org.opentcs.util.gui.plugins.VehicleTheme;

/**
 * An action to set the default theme in the client.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class VehicleThemeAction
    extends AbstractAction {

  public static final String UNDEFINED
      = ResourceBundleUtil.getBundle().getString("openTCS.undefinedVehTheme");
  private final VehicleTheme theme;
  private final OpenTCSView view;
  private final VehicleThemeManager vehicleThemeManager;

  public VehicleThemeAction(OpenTCSView view, VehicleTheme theme) {
    this.theme = theme;
    this.view = view;
    this.vehicleThemeManager = DefaultVehicleThemeManager.getInstance();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    vehicleThemeManager.updateDefaultTheme(theme);
    view.updateVehicleThemes();
  }
}
