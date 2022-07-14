/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.application.menus.menubar;

import static java.util.Objects.requireNonNull;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.inject.Inject;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.opentcs.access.Kernel;
import org.opentcs.components.plantoverview.PluggablePanelFactory;
import org.opentcs.guing.common.application.OperationMode;
import org.opentcs.guing.common.application.PluginPanelManager;
import org.opentcs.guing.common.application.action.view.AddPluginPanelAction;
import org.opentcs.guing.common.components.dockable.DockingManager;
import org.opentcs.guing.common.util.I18nPlantOverview;
import org.opentcs.guing.common.util.PanelRegistry;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ViewPluginPanelsMenu
    extends JMenu {

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(I18nPlantOverview.MENU_PATH);

  /**
   * The plugin panel manager.
   */
  private final PluginPanelManager pluginPanelManager;
  /**
   * Provides the registered plugin panel factories.
   */
  private final PanelRegistry panelRegistry;
  /**
   * Manages docking frames.
   */
  private final DockingManager dockingManager;

  @Inject
  public ViewPluginPanelsMenu(PluginPanelManager pluginPanelManager,
                              PanelRegistry panelRegistry,
                              DockingManager dockingManager) {
    super(BUNDLE.getString("viewPluginPanelsMenu.text"));

    this.pluginPanelManager = requireNonNull(pluginPanelManager, "pluginPanelManager");
    this.panelRegistry = requireNonNull(panelRegistry, "panelRegistry");
    this.dockingManager = requireNonNull(dockingManager, "dockingManager");
  }

  public void setOperationMode(OperationMode mode) {
    requireNonNull(mode, "mode");

    evaluatePluginPanels(mode);
  }

  /**
   * Removes/adds plugin panels depending on the <code>OperationMode</code>.
   *
   * @param operationMode The operation mode.
   */
  private void evaluatePluginPanels(OperationMode operationMode) {
    Kernel.State kernelState = OperationMode.equivalent(operationMode);
    if (kernelState == null) {
      return;
    }

    removeAll();

    SortedSet<PluggablePanelFactory> factories = new TreeSet<>((factory1, factory2) -> {
      return factory1.getPanelDescription().compareTo(factory2.getPanelDescription());
    });
    factories.addAll(panelRegistry.getFactories());

    for (final PluggablePanelFactory factory : factories) {
      if (factory.providesPanel(kernelState)) {
        String title = factory.getPanelDescription();
        final JCheckBoxMenuItem utilMenuItem = new JCheckBoxMenuItem();
        utilMenuItem.setAction(new AddPluginPanelAction(pluginPanelManager, factory));
        utilMenuItem.setText(title);
        dockingManager.addPropertyChangeListener(new PluginPanelPropertyHandler(utilMenuItem));
        add(utilMenuItem);
      }
    }
    // If the menu is empty, add a single disabled menu item to it that explains
    // to the user that no plugin panels are available.
    if (getMenuComponentCount() == 0) {
      JMenuItem dummyItem = new JMenuItem(BUNDLE.getString("viewPluginPanelsMenu.menuItem_none.text"));
      dummyItem.setEnabled(false);
      add(dummyItem);
    }
  }
}
