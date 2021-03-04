/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.controlcenter;

import java.awt.Color;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.access.TCSKernelStateEvent;
import org.opentcs.access.TCSModelTransitionEvent;
import org.opentcs.access.TCSNotificationEvent;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.components.kernel.ControlCenterPanel;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.customizations.kernel.ActiveInModellingMode;
import org.opentcs.customizations.kernel.ActiveInOperatingMode;
import org.opentcs.util.configuration.ConfigurationStore;
import org.opentcs.util.eventsystem.AcceptingTCSEventFilter;
import org.opentcs.util.eventsystem.EventListener;
import org.opentcs.util.eventsystem.TCSEvent;
import org.opentcs.util.gui.Icons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A GUI frontend for basic control over the kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class KernelControlCenter
    extends JFrame
    implements KernelExtension,
               EventListener<TCSEvent> {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(KernelControlCenter.class);
  /**
   * This class's ConfigurationStore.
   */
  private static final ConfigurationStore CONFIG_STORE
      = ConfigurationStore.getStore(KernelControlCenter.class.getName());
  /**
   * The key of the bundle string that's always in this frame's title.
   */
  private static final String TITLE_BASE = "KernelControlCenter.titleBase";
  /**
   * This class's resource bundle.
   */
  private static final ResourceBundle BUNDLE;
  /**
   * The kernel.
   */
  private final LocalKernel kernel;
  /**
   * Providers for panels shown in modelling mode.
   */
  private final Collection<Provider<ControlCenterPanel>> panelProvidersModelling;
  /**
   * Providers for panels shown in operating mode.
   */
  private final Collection<Provider<ControlCenterPanel>> panelProvidersOperating;
  /**
   * An about dialog.
   */
  private final AboutDialog aboutDialog;
  /**
   * Panels currently active/shown.
   */
  private final Set<ControlCenterPanel> activePanels = Collections.synchronizedSet(new HashSet<>());
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;
  /**
   * The kernel's current state.
   */
  private Kernel.State kernelState;
  /**
   * The ControlCenterInfoHandler.
   */
  private ControlCenterInfoHandler infoHandler;
  /**
   * The current Model Name.
   */
  private String currentModel = "";

  static {
    String language = CONFIG_STORE.getString("language", "ENGLISH");
    if (language.equals("GERMAN")) {
      Locale.setDefault(Locale.GERMAN);
    }
    else {
      Locale.setDefault(Locale.ENGLISH);
    }
    BUNDLE = ResourceBundle.getBundle("org/opentcs/kernel/controlcenter/Bundle");
    // Look and feel
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (ClassNotFoundException | InstantiationException | IllegalAccessException
               | UnsupportedLookAndFeelException ex) {
      LOG.warn("Exception setting look and feel", ex);
    }
  }

  /**
   * Creates new form KernelControlCenter.
   *
   * @param kernel The kernel.
   * @param panelProvidersModelling Providers for panels in modelling mode.
   * @param panelProvidersOperating Providers for panels in operating mode.
   */
  @Inject
  public KernelControlCenter(
      @Nonnull LocalKernel kernel,
      @Nonnull @ActiveInModellingMode Collection<Provider<ControlCenterPanel>> panelProvidersModelling,
      @Nonnull @ActiveInOperatingMode Collection<Provider<ControlCenterPanel>> panelProvidersOperating) {
    this.kernel = requireNonNull(kernel, "kernel");
    this.panelProvidersModelling = requireNonNull(panelProvidersModelling,
                                                  "panelProvidersModelling");
    this.panelProvidersOperating = requireNonNull(panelProvidersOperating,
                                                  "panelProvidersOperating");

    initComponents();
    setIconImages(Icons.getOpenTCSIcons());
    aboutDialog = new AboutDialog(this, false);
    aboutDialog.setAlwaysOnTop(true);
    registerControlCenterInfoHandler();
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

    kernel.addEventListener(this, new AcceptingTCSEventFilter());

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
    // Hide the window.
    setVisible(false);
    dispose();

    initialized = false;
  }

  // Methods declared in EventListener<TCSEvent> start here.
  @Override
  public void processEvent(TCSEvent event) {
    if (event instanceof TCSKernelStateEvent) {
      TCSKernelStateEvent stateEvent = (TCSKernelStateEvent) event;
      if (!stateEvent.isTransitionFinished()) {
        leavingKernelState(stateEvent.getLeftState());
      }
      else {
        enteringKernelState(stateEvent.getEnteredState());
      }
    }
    else if (event instanceof TCSModelTransitionEvent) {
      TCSModelTransitionEvent modelEvent = (TCSModelTransitionEvent) event;
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

        menuButtonOperating.setSelected(true);
        menuButtonModel.setEnabled(false);
        newModelMenuItem.setEnabled(false);
        break;
      case MODELLING:
        addPanels(panelProvidersModelling);

        menuButtonModelling.setSelected(true);
        menuButtonModel.setEnabled(true);
        newModelMenuItem.setEnabled(true);
        break;
      default:
      // Do nada.
    }
    // Remember the new state.
    kernelState = newState;
    // Updating the window title
    setWindowTitle();
  }

  private void addPanels(Collection<Provider<ControlCenterPanel>> providers) {
    for (Provider<ControlCenterPanel> provider : providers) {
      SwingUtilities.invokeLater(() -> addPanel(provider.get()));
    }
  }

  private void addPanel(ControlCenterPanel panel) {
    panel.initialize();
    activePanels.add(panel);
    tabbedPaneMain.add(panel.getTitle(), panel);
  }

  private void removePanels(Collection<ControlCenterPanel> panels) {
    List<ControlCenterPanel> panelsCopy = new ArrayList<>(panels);
    SwingUtilities.invokeLater(() -> {
      for (ControlCenterPanel panel : panelsCopy) {
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
    setWindowTitle();
  }

  /**
   * Shows a message dialog to confirm the user wants to shut down the kernel.
   *
   * @return true for yes, false otherwise.
   */
  private boolean confirmExit() {
    int n = JOptionPane.showConfirmDialog(this,
                                          BUNDLE.getString("EXIT_MESSAGE"),
                                          BUNDLE.getString("EXIT_TITLE"),
                                          JOptionPane.YES_NO_OPTION);
    return n == JOptionPane.YES_OPTION;
  }

  /**
   * Adds the ControlCenterInfoHandler to the root logger.
   */
  private void registerControlCenterInfoHandler() {
    infoHandler = new ControlCenterInfoHandler(loggingTextArea);
    kernel.addEventListener(infoHandler, (event) -> event instanceof TCSNotificationEvent);

    maxDocLengthTextField.setText("" + infoHandler.getMaxDocLength());
  }

  /**
   * Updates the window's title.
   */
  private void setWindowTitle() {
    if ("".equals(currentModel)) {
      setTitle(BUNDLE.getString(TITLE_BASE) + " - " + kernelStateString(kernelState));
    }
    else {
      setTitle(BUNDLE.getString(TITLE_BASE) + " - "
          + kernelStateString(kernelState) + " - \"" + currentModel + "\"");
    }
  }

  /**
   * Returns a user friendly string for the given kernel state.
   *
   * @param state The kernel state to be converted.
   * @return A user friendly string for the given kernel state.
   */
  private String kernelStateString(Kernel.State state) {
    return BUNDLE.getString("KernelControlCenter.kernelState." + state.name());
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

    buttonGroupKernelMode = new javax.swing.ButtonGroup();
    buttonGroupLookAndFeels = new javax.swing.ButtonGroup();
    tabbedPaneMain = new javax.swing.JTabbedPane();
    loggingPanel = new javax.swing.JPanel();
    loggingScrollPane = new javax.swing.JScrollPane();
    loggingTextArea = new javax.swing.JTextArea();
    loggingPropertyPanel = new javax.swing.JPanel();
    autoScrollCheckBox = new javax.swing.JCheckBox();
    maxDocLengthTextField = new javax.swing.JTextField();
    maxDocLengthSaveButton = new javax.swing.JButton();
    maxDocLengthLabel = new javax.swing.JLabel();
    menuBarMain = new javax.swing.JMenuBar();
    menuKernel = new javax.swing.JMenu();
    menuKernelMode = new javax.swing.JMenu();
    menuButtonModelling = new javax.swing.JRadioButtonMenuItem();
    menuButtonOperating = new javax.swing.JRadioButtonMenuItem();
    newModelMenuItem = new javax.swing.JMenuItem();
    menuButtonModel = new javax.swing.JMenuItem();
    jSeparator1 = new javax.swing.JPopupMenu.Separator();
    menuButtonExit = new javax.swing.JMenuItem();
    menuSettings = new javax.swing.JMenu();
    menuLanguage = new javax.swing.JMenu();
    menuGerman = new javax.swing.JMenuItem();
    menuEnglish = new javax.swing.JMenuItem();
    menuItemSaveSettings = new javax.swing.JMenuItem();
    menuHelp = new javax.swing.JMenu();
    menuAbout = new javax.swing.JMenuItem();

    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    setTitle(BUNDLE.getString(TITLE_BASE));
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
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/opentcs/kernel/controlcenter/Bundle"); // NOI18N
    autoScrollCheckBox.setText(bundle.getString("AutoScroll")); // NOI18N
    autoScrollCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        autoScrollCheckBoxActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    loggingPropertyPanel.add(autoScrollCheckBox, gridBagConstraints);

    maxDocLengthTextField.setInputVerifier(new InputVerifier() {
      public boolean verify(JComponent input) {
        JTextField tf = (JTextField) input;
        try {
          int result = Integer.parseInt(tf.getText());
          if(result >= 1000) {
            maxDocLengthTextField.setForeground(Color.black);
            return true;
          }
          else {
            maxDocLengthTextField.setForeground(Color.red);
            return false;
          }
        }
        catch (NumberFormatException e) {
          maxDocLengthTextField.setForeground(Color.red);
          return false;
        }
      }
    });
    maxDocLengthTextField.setPreferredSize(new java.awt.Dimension(80, 20));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    loggingPropertyPanel.add(maxDocLengthTextField, gridBagConstraints);

    maxDocLengthSaveButton.setText(bundle.getString("Save")); // NOI18N
    maxDocLengthSaveButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        maxDocLengthSaveButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
    loggingPropertyPanel.add(maxDocLengthSaveButton, gridBagConstraints);

    maxDocLengthLabel.setText(bundle.getString("LoggingLimitation")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    loggingPropertyPanel.add(maxDocLengthLabel, gridBagConstraints);

    loggingPanel.add(loggingPropertyPanel, java.awt.BorderLayout.PAGE_START);

    tabbedPaneMain.addTab(bundle.getString("Logging"), loggingPanel); // NOI18N

    getContentPane().add(tabbedPaneMain, java.awt.BorderLayout.CENTER);

    menuKernel.setText("Kernel");

    menuKernelMode.setText(bundle.getString("Mode")); // NOI18N

    buttonGroupKernelMode.add(menuButtonModelling);
    menuButtonModelling.setSelected(true);
    menuButtonModelling.setText(bundle.getString("KernelModeModelling")); // NOI18N
    menuButtonModelling.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        menuButtonModellingActionPerformed(evt);
      }
    });
    menuKernelMode.add(menuButtonModelling);

    buttonGroupKernelMode.add(menuButtonOperating);
    menuButtonOperating.setText(bundle.getString("KernelModeOperating")); // NOI18N
    menuButtonOperating.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        menuButtonOperatingActionPerformed(evt);
      }
    });
    menuKernelMode.add(menuButtonOperating);

    menuKernel.add(menuKernelMode);

    newModelMenuItem.setText(bundle.getString("NewModel")); // NOI18N
    newModelMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        newModelMenuItemActionPerformed(evt);
      }
    });
    menuKernel.add(newModelMenuItem);

    menuButtonModel.setText(bundle.getString("SwitchModel")); // NOI18N
    menuButtonModel.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        menuButtonModelActionPerformed(evt);
      }
    });
    menuKernel.add(menuButtonModel);
    menuKernel.add(jSeparator1);

    menuButtonExit.setText(bundle.getString("Exit")); // NOI18N
    menuButtonExit.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        menuButtonExitActionPerformed(evt);
      }
    });
    menuKernel.add(menuButtonExit);

    menuBarMain.add(menuKernel);

    menuSettings.setText(bundle.getString("Settings")); // NOI18N

    menuLanguage.setText(bundle.getString("Language")); // NOI18N

    menuGerman.setText(bundle.getString("German")); // NOI18N
    if(Locale.getDefault().equals(Locale.GERMAN)) {
      menuGerman.setEnabled(false);
    }
    menuGerman.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        menuGermanActionPerformed(evt);
      }
    });
    menuLanguage.add(menuGerman);

    menuEnglish.setText(bundle.getString("English")); // NOI18N
    if(Locale.getDefault().equals(Locale.ENGLISH)) {
      menuEnglish.setEnabled(false);
    }
    menuEnglish.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        menuEnglishActionPerformed(evt);
      }
    });
    menuLanguage.add(menuEnglish);

    menuSettings.add(menuLanguage);

    menuItemSaveSettings.setText(bundle.getString("saveSettings")); // NOI18N
    menuItemSaveSettings.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        menuItemSaveSettingsActionPerformed(evt);
      }
    });
    menuSettings.add(menuItemSaveSettings);

    menuBarMain.add(menuSettings);

    menuHelp.setText(bundle.getString("Help")); // NOI18N

    menuAbout.setText(bundle.getString("AboutOpenTCS")); // NOI18N
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
    if (confirmExit()) {
      kernel.setState(Kernel.State.SHUTDOWN);
    }
  }//GEN-LAST:event_menuButtonExitActionPerformed

  private void autoScrollCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoScrollCheckBoxActionPerformed
    if (autoScrollCheckBox.isSelected()) {
      infoHandler.setAutoScroll(true);
    }
    else {
      infoHandler.setAutoScroll(false);
    }
  }//GEN-LAST:event_autoScrollCheckBoxActionPerformed

  private void maxDocLengthSaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxDocLengthSaveButtonActionPerformed
    if (maxDocLengthTextField.getInputVerifier().verify(maxDocLengthTextField)) {
      int newMaxDocLength = Integer.parseInt(maxDocLengthTextField.getText());
      infoHandler.setMaxDocLength(newMaxDocLength);
    }
    else {
      maxDocLengthTextField.setForeground(Color.red);
    }
  }//GEN-LAST:event_maxDocLengthSaveButtonActionPerformed

  private void menuButtonModellingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuButtonModellingActionPerformed
    kernel.setState(Kernel.State.MODELLING);
    menuButtonModel.setEnabled(true);

  }//GEN-LAST:event_menuButtonModellingActionPerformed

  private void menuButtonOperatingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuButtonOperatingActionPerformed
    kernel.setState(Kernel.State.OPERATING);
    menuButtonModel.setEnabled(false);
  }//GEN-LAST:event_menuButtonOperatingActionPerformed

  private void menuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAboutActionPerformed
    aboutDialog.setLocationRelativeTo(null);
    aboutDialog.setVisible(true);
  }//GEN-LAST:event_menuAboutActionPerformed

  private void menuButtonModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuButtonModelActionPerformed
    Optional<String> modelName = Optional.ofNullable(kernel.getPersistentModelName());
    if (modelName.isPresent()) {
      // Show confirmation dialog
      String message = new StringBuilder()
          .append(BUNDLE.getString("loadModelMessagePart1"))
          .append(" ")
          .append(modelName.get())
          .append(BUNDLE.getString("loadModelMessagePart2"))
          .toString();
      int reply = JOptionPane.showConfirmDialog(null,
                                                message,
                                                BUNDLE.getString("loadModelConfirmTitle"),
                                                JOptionPane.YES_NO_OPTION,
                                                JOptionPane.QUESTION_MESSAGE);
      if (reply != JOptionPane.YES_OPTION) {
        return;
      }
      // Load model
      LOG.info("Loading model: " + modelName.get());
      kernel.loadPlantModel();
      LOG.info("Finished loading the model.");
    }
    else {
      JOptionPane.showMessageDialog(null,
                                    BUNDLE.getString("loadModelInfoMessage"),
                                    BUNDLE.getString("loadModelInfoTitle"),
                                    JOptionPane.WARNING_MESSAGE);
      LOG.info("No model available, keeping current model.");
    }
  }//GEN-LAST:event_menuButtonModelActionPerformed

  private void menuGermanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuGermanActionPerformed
    Locale.setDefault(Locale.GERMAN);
    CONFIG_STORE.setString("language", "GERMAN");
    JOptionPane.showMessageDialog(this,
                                  BUNDLE.getString("RestartToApplyChanges"),
                                  BUNDLE.getString("RestartRequired"),
                                  JOptionPane.INFORMATION_MESSAGE);
  }//GEN-LAST:event_menuGermanActionPerformed

  private void menuEnglishActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuEnglishActionPerformed
    Locale.setDefault(Locale.ENGLISH);
    CONFIG_STORE.setString("language", "ENGLISH");
    JOptionPane.showMessageDialog(this,
                                  BUNDLE.getString("RestartToApplyChanges"),
                                  BUNDLE.getString("RestartRequired"),
                                  JOptionPane.INFORMATION_MESSAGE);
  }//GEN-LAST:event_menuEnglishActionPerformed

  private void menuItemSaveSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemSaveSettingsActionPerformed
    kernel.savePlantModel();
  }//GEN-LAST:event_menuItemSaveSettingsActionPerformed

  private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    if (confirmExit()) {
      kernel.setState(Kernel.State.SHUTDOWN);
      dispose();
    }
  }//GEN-LAST:event_formWindowClosing

    private void newModelMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newModelMenuItemActionPerformed
      if (kernelState == Kernel.State.MODELLING) {
        String message = BUNDLE.getString("ConfirmNewModelMsg");
        String dialogTitle = "ConfirmNewModelTitle";
        // display the JOptionPane showConfirmDialog
        int reply = JOptionPane.showConfirmDialog(null,
                                                  message,
                                                  dialogTitle,
                                                  JOptionPane.YES_NO_OPTION);
        if (reply == JOptionPane.YES_OPTION) {
          // creating new model
          kernel.createPlantModel(new PlantModelCreationTO(Kernel.DEFAULT_MODEL_NAME));
          setWindowTitle();
        }
      }
    }//GEN-LAST:event_newModelMenuItemActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox autoScrollCheckBox;
  private javax.swing.ButtonGroup buttonGroupKernelMode;
  private javax.swing.ButtonGroup buttonGroupLookAndFeels;
  private javax.swing.JPopupMenu.Separator jSeparator1;
  private javax.swing.JPanel loggingPanel;
  private javax.swing.JPanel loggingPropertyPanel;
  private javax.swing.JScrollPane loggingScrollPane;
  private javax.swing.JTextArea loggingTextArea;
  private javax.swing.JLabel maxDocLengthLabel;
  private javax.swing.JButton maxDocLengthSaveButton;
  private javax.swing.JTextField maxDocLengthTextField;
  private javax.swing.JMenuItem menuAbout;
  private javax.swing.JMenuBar menuBarMain;
  private javax.swing.JMenuItem menuButtonExit;
  private javax.swing.JMenuItem menuButtonModel;
  private javax.swing.JRadioButtonMenuItem menuButtonModelling;
  private javax.swing.JRadioButtonMenuItem menuButtonOperating;
  private javax.swing.JMenuItem menuEnglish;
  private javax.swing.JMenuItem menuGerman;
  private javax.swing.JMenu menuHelp;
  private javax.swing.JMenuItem menuItemSaveSettings;
  private javax.swing.JMenu menuKernel;
  private javax.swing.JMenu menuKernelMode;
  private javax.swing.JMenu menuLanguage;
  private javax.swing.JMenu menuSettings;
  private javax.swing.JMenuItem newModelMenuItem;
  private javax.swing.JTabbedPane tabbedPaneMain;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON
}
