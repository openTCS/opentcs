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
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.guing.application.ApplicationFrame;
import org.opentcs.guing.application.ApplicationState;
import org.opentcs.guing.application.OpenTCSView;
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

    ResourceBundleUtil.getBundle().configureAction(this, ID);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    JOptionPane.showMessageDialog(
        dialogParent,
        "<html><p><b>" + OpenTCSView.NAME + "</b><br> "
        + bundle.getFormatted("openTCS.about.baseVersion", Environment.getBaselineVersion()) + "<br>"
        + bundle.getFormatted("openTCS.about.customization",
                              Environment.getCustomizationName(),
                              Environment.getCustomizationVersion()) + "<br>"
        + OpenTCSView.COPYRIGHT + "<br>"
        + bundle.getString("openTCS.about.runningOn") + "<br>"
        + "Java: " + System.getProperty("java.version") + ", " + System.getProperty("java.vendor") + "<br>"
        + "JVM: " + System.getProperty("java.vm.version") + ", " + System.getProperty("java.vm.vendor") + "<br>"
        + "OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + ", " + System.getProperty("os.arch") + "<br>"
        + "<b>Kernel</b><br>"
        + portalProvider.getPortalDescription()
        + "<br>" + bundle.getFormatted("openTCS.about.mode", appState.getOperationMode())
        + "</p></html>",
        bundle.getString("openTCS.about.title"),
        JOptionPane.PLAIN_MESSAGE,
        new ImageIcon(getClass().getResource("/org/opentcs/guing/res/symbols/openTCS/openTCS.300x132.gif")));
  }
}
