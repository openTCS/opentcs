/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.app;

import java.awt.Component;
import java.awt.event.ActionEvent;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import static javax.swing.Action.LARGE_ICON_KEY;
import static javax.swing.Action.MNEMONIC_KEY;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.customizations.plantoverview.ApplicationFrame;
import org.opentcs.guing.application.ApplicationState;
import org.opentcs.guing.application.OpenTCSView;
import static org.opentcs.guing.util.I18nPlantOverview.MENU_PATH;
import org.opentcs.guing.util.ImageDirectory;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.util.Environment;

/**
 * Displays a dialog showing information about the application.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class AboutAction
    extends AbstractAction {

  /**
   * This action's ID.
   */
  public final static String ID = "application.about";

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(MENU_PATH);
  /**
   * Stores the application's current state.
   */
  private final ApplicationState appState;
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
  /**
   * The parent component for dialogs shown by this action.
   */
  private final Component dialogParent;

  /**
   * Creates a new instance.
   *
   * @param appState Stores the application's current state.
   * @param portalProvider Provides access to a portal.
   * @param dialogParent The parent component for dialogs shown by this action.
   */
  @Inject
  public AboutAction(ApplicationState appState,
                     SharedKernelServicePortalProvider portalProvider,
                     @ApplicationFrame Component dialogParent) {
    this.appState = requireNonNull(appState, "appState");
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
    this.dialogParent = requireNonNull(dialogParent, "dialogParent");

    putValue(NAME, BUNDLE.getString("aboutAction.name"));
    putValue(MNEMONIC_KEY, Integer.valueOf('A'));

    ImageIcon icon = ImageDirectory.getImageIcon("/menu/help-contents.png");
    putValue(SMALL_ICON, icon);
    putValue(LARGE_ICON_KEY, icon);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    JOptionPane.showMessageDialog(
        dialogParent,
        "<html><p><b>" + OpenTCSView.NAME + "</b><br> "
        + BUNDLE.getFormatted("aboutAction.optionPane_applicationInformation.message.baselineVersion", Environment.getBaselineVersion()) + "<br>"
        + BUNDLE.getFormatted("aboutAction.optionPane_applicationInformation.message.customization",
                              Environment.getCustomizationName(),
                              Environment.getCustomizationVersion()) + "<br>"
        + BUNDLE.getString("aboutAction.optionPane_applicationInformation.message.copyright") + "<br>"
        + BUNDLE.getString("aboutAction.optionPane_applicationInformation.message.runningOn") + "<br>"
        + "Java: " + System.getProperty("java.version") + ", " + System.getProperty("java.vendor") + "<br>"
        + "JVM: " + System.getProperty("java.vm.version") + ", " + System.getProperty("java.vm.vendor") + "<br>"
        + "OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + ", " + System.getProperty("os.arch") + "<br>"
        + "<b>Kernel</b><br>"
        + portalProvider.getPortalDescription()
        + "<br>" + BUNDLE.getFormatted("aboutAction.optionPane_applicationInformation.message.mode", appState.getOperationMode())
        + "</p></html>",
        BUNDLE.getString("aboutAction.optionPane_applicationInformation.title"),
        JOptionPane.PLAIN_MESSAGE,
        new ImageIcon(getClass().getResource("/org/opentcs/guing/res/symbols/openTCS/openTCS.300x132.gif")));
  }
}
