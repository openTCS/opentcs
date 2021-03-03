package org.opentcs.guing.application.action.view;

import java.awt.event.ActionEvent;
import java.util.Objects;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.util.gui.plugins.PanelFactory;

/**
 * An action to add a plugin panel.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class AddPluginPanelAction
    extends AbstractAction {

  public final static String ID = "view.addPluginPanel";
  private final PanelFactory factory;
  private final OpenTCSView view;

  public AddPluginPanelAction(OpenTCSView view, PanelFactory factory) {
    this.view = Objects.requireNonNull(view, "view is null");
    this.factory = Objects.requireNonNull(factory, "panelID is null");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
    view.showPluginPanel(factory, item.isSelected());
  }
}
