/*
 *
 * Created on 20.08.2013 10:42:29
 */
package org.opentcs.guing.application.action.view;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.guing.util.DefaultLocationThemeManager;
import org.opentcs.guing.util.LocationThemeManager;
import org.opentcs.util.gui.plugins.LocationTheme;

/**
 * An action to set the default theme in the client.
 * 
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class LocationThemeAction
    extends AbstractAction {

  public static final String UNDEFINED = ResourceBundleUtil.getBundle().getString("openTCS.undefinedTheme");
  private final LocationTheme theme;
  private final OpenTCSView view;
  private final LocationThemeManager locationThemeManager;

  public LocationThemeAction(OpenTCSView view, LocationTheme theme) {
    this.theme = theme;
    this.view = view;
    this.locationThemeManager = DefaultLocationThemeManager.getInstance();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    locationThemeManager.updateDefaultTheme(theme);
    view.updateLocationThemes();
  }
}
