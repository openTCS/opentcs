/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.KernelStateTransitionEvent;
import org.opentcs.access.ModelTransitionEvent;
import org.opentcs.common.ClientConnectionMode;
import org.opentcs.common.KernelClientApplication;
import org.opentcs.common.PortalManager;
import static org.opentcs.common.PortalManager.ConnectionState.CONNECTED;
import static org.opentcs.common.PortalManager.ConnectionState.DISCONNECTED;
import org.opentcs.components.Lifecycle;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.customizations.ServiceCallWrapper;
import org.opentcs.customizations.controlcenter.ActiveInModellingMode;
import org.opentcs.customizations.controlcenter.ActiveInOperatingMode;
import static org.opentcs.kernelcontrolcenter.I18nKernelControlCenter.BUNDLE_PATH;
import org.opentcs.util.CallWrapper;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.event.EventSource;
import org.opentcs.util.gui.Icons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A GUI frontend for basic control over the kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
@SuppressWarnings("deprecation")
public class KernelControlCenter
    extends JFrame
    implements Lifecycle,
               EventHandler {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(KernelControlCenter.class);
  /**
   * This class's resource bundle.
   */
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_PATH);
  /**
   * The factory providing a ControlCenterInfoHandler
   */
  private final ControlCenterInfoHandlerFactory controlCenterInfoHandlerFactoy;
  /**
   * Providers for panels shown in modelling mode.
   */
  private final Collection<Provider<org.opentcs.components.kernel.ControlCenterPanel>> panelProvidersModelling;
  /**
   * Providers for panels shown in operating mode.
   */
  private final Collection<Provider<org.opentcs.components.kernel.ControlCenterPanel>> panelProvidersOperating;
  /**
   * An about dialog.
   */
  private final AboutDialog aboutDialog;
  /**
   * Panels currently active/shown.
   */
  private final Set<org.opentcs.components.kernel.ControlCenterPanel> activePanels = Collections.synchronizedSet(new HashSet<>());
  /**
   * The application running this panel.
   */
  private final KernelClientApplication application;
  /**
   * Where this instance registers for application events.
   */
  private final EventSource eventSource;
  /**
   * The service portal to use for kernel interaction.
   */
  private final KernelServicePortal servicePortal;
  /**
   * The portal manager.
   */
  private final PortalManager portalManager;
  /**
   * The call wrapper to use for service calls.
   */
  private final CallWrapper callWrapper;
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;
  /**
   * The ControlCenterInfoHandler.
   */
  private ControlCenterInfoHandler infoHandler;
  /**
   * The current Model Name.
   */
  private String currentModel = "";

  /**
   * Creates new form KernelControlCenter.
   *
   * @param application The application running this panel.
   * @param servicePortal The service portal to use for kernel interaction.
   * @param callWrapper The call wrapper to use for service calls.
   * @param portalManager The portal manager.
   * @param eventSource Where this instance registers for application events.
   * @param controlCenterInfoHandlerFactory The factory providing a ControlCenterInfoHandler.
   * @param panelProvidersModelling Providers for panels in modelling mode.
   * @param panelProvidersOperating Providers for panels in operating mode.
   */
  @Inject
  public KernelControlCenter(
      @Nonnull KernelClientApplication application,
      @Nonnull KernelServicePortal servicePortal,
      @Nonnull @ServiceCallWrapper CallWrapper callWrapper,
      @Nonnull PortalManager portalManager,
      @ApplicationEventBus EventSource eventSource,
      @Nonnull ControlCenterInfoHandlerFactory controlCenterInfoHandlerFactory,
      @Nonnull @ActiveInModellingMode Collection<Provider<org.opentcs.components.kernel.ControlCenterPanel>> panelProvidersModelling,
      @Nonnull @ActiveInOperatingMode Collection<Provider<org.opentcs.components.kernel.ControlCenterPanel>> panelProvidersOperating) {
    this.application = requireNonNull(application, "application");
    this.servicePortal = requireNonNull(servicePortal, "servicePortal");
    this.callWrapper = requireNonNull(callWrapper, "callWrapper");
    this.portalManager = requireNonNull(portalManager, "portalManager");
    this.eventSource = requireNonNull(eventSource, "eventSource");
    this.controlCenterInfoHandlerFactoy = requireNonNull(controlCenterInfoHandlerFactory,
                                                         "controlCenterInfoHandlerFactory");
    this.panelProvidersModelling = requireNonNull(panelProvidersModelling,
                                                  "panelProvidersModelling");
    this.panelProvidersOperating = requireNonNull(panelProvidersOperating,
                                                  "panelProvidersOperating");

    initComponents();
    setIconImages(Icons.getOpenTCSIcons());
    aboutDialog = new AboutDialog(this, false);
    aboutDialog.setAlwaysOnTop(true);
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void initialize() {
    if (initialized) {
      LOG.debug("Already initialized.");
      return;
    }

    registerControlCenterInfoHandler();
    eventSource.subscribe(this);

    enteringKernelState(Kernel.State.MODELLING);

    try {
      EventQueue.invokeAndWait(() -> setVisible(true));
    }
    catch (InterruptedException | InvocationTargetException exc) {
      throw new IllegalStateException("Unexpected exception initializing", exc);
    }

    initialized = true;
  }

  @Override
  public void terminate() {
    if (!initialized) {
      LOG.debug("Not initialized");
      return;
    }

    removePanels(activePanels);

    eventSource.unsubscribe(this);
    eventSource.unsubscribe(infoHandler);

    // Hide the window.
    setVisible(false);
    dispose();

    initialized = false;
  }

  private void onKernelConnect() {
    try {
      Kernel.State kernelState = callWrapper.call(() -> servicePortal.getState());
      enteringKernelState(kernelState);
    }
    catch (Exception ex) {
      LOG.warn("Error getting the kernel state", ex);
    }
  }

  private void onKernelDisconnect() {
    leavingKernelState(Kernel.State.OPERATING);
    enteringKernelState(Kernel.State.MODELLING);
  }

  @Override
  public void onEvent(Object event) {
    if (event instanceof ClientConnectionMode) {
      ClientConnectionMode applicationState = (ClientConnectionMode) event;
      switch (applicationState) {
        case ONLINE:
          onKernelConnect();
          break;
        case OFFLINE:
          onKernelDisconnect();
          break;
        default:
          LOG.debug("Unhandled connection state: {}", applicationState.name());
      }
    }
    else if (event instanceof PortalManager.ConnectionState) {
      PortalManager.ConnectionState connectionState = (PortalManager.ConnectionState) event;
      switch (connectionState) {
        case CONNECTED:
          updateWindowTitle();
          break;
        case DISCONNECTED:
          updateWindowTitle();
          break;
        default:
      }

      menuButtonConnect.setEnabled(!portalManager.isConnected());
      menuButtonDisconnect.setEnabled(portalManager.isConnected());
    }
    else if (event instanceof KernelStateTransitionEvent) {
      KernelStateTransitionEvent stateEvent = (KernelStateTransitionEvent) event;
      if (!stateEvent.isTransitionFinished()) {
        leavingKernelState(stateEvent.getLeftState());
      }
      else {
        enteringKernelState(stateEvent.getEnteredState());
      }
    }
    else if (event instanceof ModelTransitionEvent) {
      ModelTransitionEvent modelEvent = (ModelTransitionEvent) event;
      updateModelName(modelEvent.getNewModelName());
    }
  }

  /**
   * Perfoms some tasks when a state is being leaved.
   *
   * @param oldState The state we're leaving
   */
  private void leavingKernelState(Kernel.State oldState) {
    requireNonNull(oldState, "oldState");

    removePanels(activePanels);
    activePanels.clear();
  }

  /**
   * Notifies this control center that the kernel has entered a different state.
   *
   * @param newState
   */
  private void enteringKernelState(Kernel.State newState) {
    requireNonNull(newState, "newState");

    switch (newState) {
      case OPERATING:
        addPanels(panelProvidersOperating);
        break;
      case MODELLING:
        addPanels(panelProvidersModelling);
        break;
      default:
      // Do nada.
    }
    // Updating the window title
    updateWindowTitle();
  }

  private void addPanels(Collection<Provider<org.opentcs.components.kernel.ControlCenterPanel>> providers) {
    for (Provider<org.opentcs.components.kernel.ControlCenterPanel> provider : providers) {
      SwingUtilities.invokeLater(() -> addPanel(provider.get()));
    }
  }

  private void addPanel(org.opentcs.components.kernel.ControlCenterPanel panel) {
    panel.initialize();
    activePanels.add(panel);
    tabbedPaneMain.add(panel.getTitle(), panel);
  }

  private void removePanels(Collection<org.opentcs.components.kernel.ControlCenterPanel> panels) {
    List<org.opentcs.components.kernel.ControlCenterPanel> panelsCopy = new ArrayList<>(panels);
    SwingUtilities.invokeLater(() -> {
      for (org.opentcs.components.kernel.ControlCenterPanel panel : panelsCopy) {
        tabbedPaneMain.remove(panel);
        panel.terminate();
      }
    });
  }

  /**
   * Updates the model name to the current one.
   *
   * @param newModelName The new/updated model name.
   */
  private void updateModelName(String newModelName) {
    this.currentModel = newModelName;
    updateWindowTitle();
  }

  /**
   * Shows a message dialog to confirm the user wants to shut down the kernel.
   *
   * @return true for yes, false otherwise.
   */
  private boolean confirmExit() {
    int n = JOptionPane.showConfirmDialog(this,
                                          BUNDLE.getString("kernelControlCenter.optionPane_exitConfirmation.message"),
                                          BUNDLE.getString("kernelControlCenter.optionPane_exitConfirmation.title"),
                                          JOptionPane.YES_NO_OPTION);
    return n == JOptionPane.YES_OPTION;
  }

  /**
   * Adds the ControlCenterInfoHandler to the root logger.
   */
  private void registerControlCenterInfoHandler() {
    infoHandler = controlCenterInfoHandlerFactoy.createHandler(loggingTextArea);
    eventSource.subscribe(infoHandler);
  }

  private void updateWindowTitle() {
    String titleBase = BUNDLE.getString("kernelControlCenter.title");
    String loadedModel = currentModel.equals("") ? "" : " - " + "\"" + currentModel + "\"";
    String connectedTo = " - " + BUNDLE.getString("kernelControlCenter.title.connectedTo")
        + portalManager.getDescription()
        + " (" + portalManager.getHost()
        + ":"
        + portalManager.getPort() + ")";

    setTitle(titleBase + loadedModel + connectedTo);
  }

  private void exitApplication() {
    if (confirmExit()) {
      application.terminate();
    }
  }

  // CHECKSTYLE:OFF
  // Generated code starts here.
  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        tabbedPaneMain = new javax.swing.JTabbedPane();
        loggingPanel = new javax.swing.JPanel();
        loggingScrollPane = new javax.swing.JScrollPane();
        loggingTextArea = new javax.swing.JTextArea();
        loggingPropertyPanel = new javax.swing.JPanel();
        autoScrollCheckBox = new javax.swing.JCheckBox();
        menuBarMain = new javax.swing.JMenuBar();
        menuKernel = new javax.swing.JMenu();
        menuButtonConnect = new javax.swing.JMenuItem();
        menuButtonDisconnect = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        menuButtonExit = new javax.swing.JMenuItem();
        menuHelp = new javax.swing.JMenu();
        menuAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/kernelcontrolcenter/Bundle"); // NOI18N
        setTitle(bundle.getString("kernelControlCenter.title")); // NOI18N
        setMinimumSize(new java.awt.Dimension(1200, 750));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        loggingPanel.setLayout(new java.awt.BorderLayout());

        loggingTextArea.setEditable(false);
        loggingScrollPane.setViewportView(loggingTextArea);

        loggingPanel.add(loggingScrollPane, java.awt.BorderLayout.CENTER);

        loggingPropertyPanel.setLayout(new java.awt.GridBagLayout());

        autoScrollCheckBox.setSelected(true);
        autoScrollCheckBox.setText(bundle.getString("kernelControlCenter.checkBox_autoScroll.text")); // NOI18N
        autoScrollCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoScrollCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        loggingPropertyPanel.add(autoScrollCheckBox, gridBagConstraints);

        loggingPanel.add(loggingPropertyPanel, java.awt.BorderLayout.PAGE_START);

        tabbedPaneMain.addTab(bundle.getString("kernelControlCenter.tab_logging.title"), loggingPanel); // NOI18N

        getContentPane().add(tabbedPaneMain, java.awt.BorderLayout.CENTER);

        menuKernel.setText("KernelControlCenter");

        menuButtonConnect.setText(bundle.getString("kernelControlCenter.menu_kernel.menuItem_connect.text")); // NOI18N
        menuButtonConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuButtonConnectActionPerformed(evt);
            }
        });
        menuKernel.add(menuButtonConnect);

        menuButtonDisconnect.setText(bundle.getString("kernelControlCenter.menu_kernel.menuItem_disconnect.text")); // NOI18N
        menuButtonDisconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuButtonDisconnectActionPerformed(evt);
            }
        });
        menuKernel.add(menuButtonDisconnect);
        menuKernel.add(jSeparator1);

        menuButtonExit.setText(bundle.getString("kernelControlCenter.menu_kernel.menuItem_exit.text")); // NOI18N
        menuButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuButtonExitActionPerformed(evt);
            }
        });
        menuKernel.add(menuButtonExit);

        menuBarMain.add(menuKernel);

        menuHelp.setText(bundle.getString("kernelControlCenter.menu_help.text")); // NOI18N

        menuAbout.setText(bundle.getString("kernelControlCenter.menu_about.text")); // NOI18N
        menuAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAboutActionPerformed(evt);
            }
        });
        menuHelp.add(menuAbout);

        menuBarMain.add(menuHelp);

        setJMenuBar(menuBarMain);

        setSize(new java.awt.Dimension(1208, 782));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

  private void menuButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuButtonExitActionPerformed
    exitApplication();
  }//GEN-LAST:event_menuButtonExitActionPerformed

  private void autoScrollCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoScrollCheckBoxActionPerformed
    if (autoScrollCheckBox.isSelected()) {
      infoHandler.setAutoScroll(true);
    }
    else {
      infoHandler.setAutoScroll(false);
    }
  }//GEN-LAST:event_autoScrollCheckBoxActionPerformed

  private void menuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAboutActionPerformed
    aboutDialog.setLocationRelativeTo(null);
    aboutDialog.setVisible(true);
  }//GEN-LAST:event_menuAboutActionPerformed

  private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    exitApplication();
  }//GEN-LAST:event_formWindowClosing

  private void menuButtonConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuButtonConnectActionPerformed
    application.online(false);
  }//GEN-LAST:event_menuButtonConnectActionPerformed

  private void menuButtonDisconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuButtonDisconnectActionPerformed
    application.offline();
  }//GEN-LAST:event_menuButtonDisconnectActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoScrollCheckBox;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPanel loggingPanel;
    private javax.swing.JPanel loggingPropertyPanel;
    private javax.swing.JScrollPane loggingScrollPane;
    private javax.swing.JTextArea loggingTextArea;
    private javax.swing.JMenuItem menuAbout;
    private javax.swing.JMenuBar menuBarMain;
    private javax.swing.JMenuItem menuButtonConnect;
    private javax.swing.JMenuItem menuButtonDisconnect;
    private javax.swing.JMenuItem menuButtonExit;
    private javax.swing.JMenu menuHelp;
    private javax.swing.JMenu menuKernel;
    private javax.swing.JTabbedPane tabbedPaneMain;
    // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON
}
