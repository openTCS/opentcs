/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.menus.menubar;

import static java.util.Objects.requireNonNull;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.inject.Inject;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.opentcs.access.Kernel;
import org.opentcs.components.plantoverview.PluggablePanelFactory;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.application.action.view.AddPluginPanelAction;
import org.opentcs.guing.components.dockable.DockingManager;
import org.opentcs.guing.util.PanelRegistry;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ViewPluginPanelsMenu
    extends JMenu {

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle();

  /**
   * The application's main view.
   */
  private final OpenTCSView view;
  /**
   * Provides the registered plugin panel factories.
   */
  private final PanelRegistry panelRegistry;
  /**
   * Manages docking frames.
   */
  private final DockingManager dockingManager;

  @Inject
  public ViewPluginPanelsMenu(OpenTCSView view,
                              PanelRegistry panelRegistry,
                              DockingManager dockingManager) {
    super(BUNDLE.getString("view.pluginPanels.text"));

    this.view = requireNonNull(view, "view");
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
        utilMenuItem.setAction(new AddPluginPanelAction(view, factory));
        utilMenuItem.setText(title);
        dockingManager.addPropertyChangeListener(new PluginPanelPropertyHandler(utilMenuItem));
        add(utilMenuItem);
      }
    }
    // If the menu is empty, add a single disabled menu item to it that explains
    // to the user that no plugin panels are available.
    if (getMenuComponentCount() == 0) {
      JMenuItem dummyItem = new JMenuItem(BUNDLE.getString("view.pluginPanels.noneAvailable.text"));
      dummyItem.setEnabled(false);
      add(dummyItem);
    }
  }
}
