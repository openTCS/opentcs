/*
 * openTCS copyright information:
 * Copyright (c) 2007 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.controlcenter.vehicles;

import java.awt.Color;
import java.awt.Font;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.BasicCommunicationAdapter;
import org.opentcs.drivers.Message;
import org.opentcs.drivers.MovementCommand;
import org.opentcs.drivers.VehicleModel;
import org.opentcs.drivers.VelocityHistory;

/**
 * Displays information about a vehicle (VehicleModel) graphically.
 *
 * @author Iryna Felko (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
final class DetailPanel
    extends JPanel
    implements Observer {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(DetailPanel.class.getName());
  /**
   * A <code>DateFormat</code> instance for formatting message's time stamps.
   */
  private final DateFormat dateFormat =
      new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
  /**
   * This internal frame's border title.
   */
  private final String defaultBorderTitle;
  /**
   * The adapter specific list of JPanels.
   */
  private final List<JPanel> customPanelList = new LinkedList<>();
  /**
   * The logging table model to use.
   */
  private final LogTableModel loggingTableModel = new LogTableModel();
  /**
   * The vehicle model of the vehicle current associated with this window.
   */
  private VehicleModel vehicleModel;
  /**
   * The max velocity of the vehicle.
   */
  private int maxVelocity;
  /**
   * The comm adapter currently attached to the vehicle (model).
   */
  private BasicCommunicationAdapter commAdapter;

  /**
   * Creates a new DetailPanel.
   *
   * @param newBorderTitle The new border title.
   */
  DetailPanel(final String newBorderTitle) {
    initComponents();
    //Setup ChartPanel and Plot:
    ResourceBundle bundle = java.util.ResourceBundle.getBundle(
        "org/opentcs/kernel/controlcenter/vehicles/Bundle");
    JFreeChart chart = ChartFactory.createLineChart(
        bundle.getString("Velocity_graph"),
        bundle.getString("Time"),
        bundle.getString("Speed"),
        null,
        PlotOrientation.VERTICAL, true, true, false);
    chart.setBackgroundPaint(Color.white);
    chart.setTitle(new TextTitle(
        bundle.getString("Velocity_graph"),
        new Font(TextTitle.DEFAULT_FONT.getFontName(), Font.BOLD, 14)));
    CategoryPlot plot = (CategoryPlot) chart.getPlot();
    plot.setBackgroundPaint(Color.white);
    plot.setRangeGridlinePaint(Color.black);
    CategoryAxis xAxis = (CategoryAxis) plot.getDomainAxis();
    xAxis.setTickLabelPaint(new Color(0, 0, 0, 0));
    xAxis.setTickLabelFont(xAxis.getTickLabelFont().deriveFont(1));
    ChartPanel chartPanel = (ChartPanel) curvePanel;
    chartPanel.setChart(chart);
    chartPanel.setRangeZoomable(false);
    chartPanel.setDomainZoomable(false);
    // Remove the panel for the command queues from the tabbed pane again. It's
    // not really usable, yet.
    tabbedPane.remove(queuesPanel);

    defaultBorderTitle = newBorderTitle;
    this.setBorderTitle(defaultBorderTitle);

    curStateTxt.setText(Vehicle.State.UNKNOWN.toString());

    loggingTable.setModel(loggingTableModel);
    loggingTable.getColumnModel().getColumn(0).setPreferredWidth(40);
    loggingTable.getColumnModel().getColumn(1).setPreferredWidth(110);
    loggingTable.getSelectionModel().addListSelectionListener(new RowListener());
    // Make sure we start with an empty panel.
    detachFromVehicle();
  }

  // Implementation of interface Observer starts here.
  @Override
  public void update(Observable o, Object arg) {
    if (!(o instanceof VehicleModel)) {
      throw new IllegalArgumentException("Unexpectedly notified by a "
          + o.getClass().getName());
    }
    if (arg instanceof Message) {
      loggingTableModel.addRow((Message) arg);
    }
    else if (arg instanceof VelocityHistory) {
      updateChart((VelocityHistory) arg);
    }
    update();
  }

  /**
   * Updates the velocity chart.
   * 
   * @param velo The velocity history.
   */
  private void updateChart(VelocityHistory velo) {
    ChartPanel chartPanel = (ChartPanel) curvePanel;
    CategoryPlot plot = (CategoryPlot) chartPanel.getChart().getPlot();
    DefaultCategoryDataset dataset = createDataset(velo);
    plot.setDataset(dataset);
  }

  /**
   * Creates a dataset based on the velocity history.
   * 
   * @param velo The velocity history.
   * @return A <code>DefaultCategoryDataset</code> based on the history.
   */
  private DefaultCategoryDataset createDataset(VelocityHistory velo) {
    if (velo == null) {
      return null;
    }
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    int j = 0;
    for (int i : velo.getVelocities()) {
      dataset.addValue(i, vehicleModel.getName(), j + "");
      dataset.addValue(maxVelocity + 5, java.util.ResourceBundle.getBundle(
          "org/opentcs/kernel/controlcenter/vehicles/Bundle").getString("MaxVelocity"), j + "");
      j++;
    }

    return dataset;
  }

  /**
   * Updates all components.
   */
  private void update() {
    log.finer("method entry");

    // If the comm adapter has changed, we have to update a few things.
    if (commAdapter != vehicleModel.getCommunicationAdapter()) {
      // If there was a comm adapter and it changed, we need to clean up a few
      // things first.
      if (commAdapter != null) {
        // Detach and forget all custom panels of the old comm adapter.
        commAdapter.detachCustomDisplayPanels(customPanelList);
        removeCustomPanels();
        customPanelList.clear();
        // As there's no adapter, the user can't enable it.
        chkBoxEnable.setSelected(false);
        chkBoxEnable.setEnabled(false);
      }
      // Update the comm adapter reference.
      commAdapter = vehicleModel.getCommunicationAdapter();
      // If we have a new comm adapter, set up a few things.
      if (commAdapter != null) {
        // Update the custom panels displayed.
        updateCustomPanels();
        // Now that there's a new adapter, the user can enable/disable it.
        chkBoxEnable.setEnabled(true);
      }
    }

    // No matter if the adapter changed or not - update information to be
    // displayed if an adapter is available.
    if (commAdapter != null) {
      chkBoxEnable.setSelected(commAdapter.isEnabled());
      synchronized (commAdapter) {
        Queue<MovementCommand> cmdQueue = commAdapter.getCommandQueue();
        CommandListModel cmdListModel = (CommandListModel) cmdQueueList.getModel();
        cmdListModel.setValues(cmdQueue);
        // Now the sent queue...
        cmdQueue = commAdapter.getSentQueue();
        cmdListModel = (CommandListModel) sentQueueList.getModel();
        cmdListModel.setValues(cmdQueue);
      }
    }

    // Reset the velocity chart if there is no data to display,
    // otherwise the data of the previous attached vehicle will be displayed.
    if (vehicleModel.getCommunicationAdapter() == null
        || !vehicleModel.getCommunicationAdapter().isEnabled()) {
      VelocityHistory velo = new VelocityHistory(100, 10);
      velo.addVelocityValue(0);
      velo.addVelocityValue(maxVelocity);
      updateChart(velo);
    }

    // Position and state can always be updated.
    chkBoxEnable.setEnabled(commAdapter != null);
    chkBoxEnable.setSelected(commAdapter != null && commAdapter.isEnabled());
    curPosTxt.setText(vehicleModel.getPosition());
    curStateTxt.setText(vehicleModel.getState().toString());
  }

  // Methods not declared in any interface start here.
  /**
   * Attaches this panel to a vehicle.
   *
   * @param newVehicleModel The <code>VehicleModel</code> to be attached to.
   */
  void attachToVehicle(VehicleModel newVehicleModel) {
    if (newVehicleModel == null) {
      throw new NullPointerException("newVehicleModel is null");
    }
    // Clean up first - but only if we're not reattaching the vehicle model
    // which is already attached to this panel.
    if (vehicleModel != null && vehicleModel != newVehicleModel) {
      vehicleModel.setSelectedTab(tabbedPane.getSelectedIndex());
      detachFromVehicle();
    }
    vehicleModel = newVehicleModel;
    vehicleModel.addObserver(this);
    setBorderTitle(vehicleModel.getName());
    // Ensure the tabbed pane containing vehicle information is shown.
    this.removeAll();
    this.add(tabbedPane);

    // Init vehicle status.
    loggingTableModel.clear();
    for (Message curMessage : vehicleModel.getMessages()) {
      loggingTableModel.addRow(curMessage);
    }
    maxVelocity = newVehicleModel.getVehicle().getMaxVelocity();
    update();
    // Update panel contents.
    this.validate();
    tabbedPane.setSelectedIndex(vehicleModel.getSelectedTab());
  }

  /**
   * Detaches this panel from a vehicle (if it is currently attached to any).
   */
  void detachFromVehicle() {
    if (vehicleModel != null) {
      vehicleModel.deleteObserver(this);
      // Remove all custom panels and let the comm adapter know we don't need
      // them any more.
      removeCustomPanels();
      if (commAdapter != null) {
        commAdapter.detachCustomDisplayPanels(customPanelList);
        commAdapter = null;
      }
      customPanelList.clear();
      vehicleModel = null;
    }
    // Clear the log message table.
    loggingTableModel.clear();
    setBorderTitle(defaultBorderTitle);
    // Remove the contents of this panel.
    this.removeAll();
    this.add(noVehiclePanel);
    // Update panel contents.
    this.validate();
  }

  /**
   * Update the list of custom panels in the tabbed pane.
   */
  private void updateCustomPanels() {
    for (JPanel curPanel : customPanelList) {
      tabbedPane.remove(curPanel);
    }
    customPanelList.clear();
    if (commAdapter != null) {
      customPanelList.addAll(commAdapter.getCustomDisplayPanels());
      for (JPanel curPanel : customPanelList) {
        String title = curPanel.getAccessibleContext().getAccessibleName();
        tabbedPane.addTab(title, curPanel);
      }
    }
  }

  /**
   * Removes the custom panels from this panel's tabbed pane.
   */
  private void removeCustomPanels() {
    for (JPanel i : customPanelList) {
      tabbedPane.remove(i);
    }
    tabbedPane.setSelectedIndex(0);
  }

  /**
   * Sets this panel's border title.
   *
   * @param newTitle This panel's new border title.
   */
  private void setBorderTitle(String newTitle) {
    if (newTitle == null) {
      throw new NullPointerException("newTitle is null");
    }
    ((TitledBorder) getBorder()).setTitle(newTitle);
    // Trigger a repaint - the title sometimes looks strange otherwise.
    repaint();
  }

  /**
   * This method appends the selected message to the text area in the log tab.
   *
   * @param row The selected row to detect the message to be shown in the text
   * area.
   */
  private void outputLogMessage(int row) {
    Message message = (Message) loggingTableModel.getRow(row);
    String timestamp = dateFormat.format(new Date(message.getTimestamp()));
    String output = timestamp + " (" + message.getType() + "):\n"
        + message.getMessage();
    loggingTextArea.setText(output);
  }

  // CHECKSTYLE:OFF
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    logPopupMenu = new javax.swing.JPopupMenu();
    clearMenuItem = new javax.swing.JMenuItem();
    modeButtonGroup = new javax.swing.ButtonGroup();
    loggingTablePopupMenu = new javax.swing.JPopupMenu();
    filterMenu = new javax.swing.JMenu();
    everythingCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    warningsCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    errorsCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    sortingMenu = new javax.swing.JMenu();
    messageTypeCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    dateCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    velocityCurvePopupMenu = new javax.swing.JPopupMenu();
    clearVelocityCurveMenuItem = new javax.swing.JMenuItem();
    noVehiclePanel = new javax.swing.JPanel();
    noVehicleLabel = new javax.swing.JLabel();
    tabbedPane = new javax.swing.JTabbedPane();
    overviewTabPanel = new javax.swing.JPanel();
    headPanel = new javax.swing.JPanel();
    statusPanel = new javax.swing.JPanel();
    adapterStatusPanel = new javax.swing.JPanel();
    chkBoxEnable = new javax.swing.JCheckBox();
    statusFiguresPanel = new javax.swing.JPanel();
    curPosLbl = new javax.swing.JLabel();
    curPosTxt = new javax.swing.JTextField();
    curStateLbl = new javax.swing.JLabel();
    curStateTxt = new javax.swing.JTextField();
    fillingLbl = new javax.swing.JLabel();
    logoPanel = new javax.swing.JPanel();
    logoLbl = new javax.swing.JLabel();
    curvePanel = new ChartPanel(null, false, false, false, false, true);
    logPanel = new javax.swing.JPanel();
    logTableScrollPane = new javax.swing.JScrollPane();
    loggingTable = new javax.swing.JTable();
    logTextScrollPane = new javax.swing.JScrollPane();
    loggingTextArea = new javax.swing.JTextArea();
    queuesPanel = new javax.swing.JPanel();
    cmdQueuePanel = new javax.swing.JPanel();
    jScrollPane1 = new javax.swing.JScrollPane();
    cmdQueueList = new javax.swing.JList<MovementCommand>();
    clearCmdQueueButton = new javax.swing.JButton();
    sentQueuePanel = new javax.swing.JPanel();
    jScrollPane2 = new javax.swing.JScrollPane();
    sentQueueList = new javax.swing.JList<MovementCommand>();
    sendAgainButton = new javax.swing.JButton();
    removeButton = new javax.swing.JButton();

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/opentcs/kernel/controlcenter/vehicles/Bundle"); // NOI18N
    clearMenuItem.setText(bundle.getString("ClearLogMessageText")); // NOI18N
    clearMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        clearMenuItemActionPerformed(evt);
      }
    });
    logPopupMenu.add(clearMenuItem);

    filterMenu.setText(bundle.getString("FilterMessages")); // NOI18N
    filterMenu.setActionCommand(" message filtering");

    everythingCheckBoxMenuItem.setText(bundle.getString("FilterMessagesShowAll")); // NOI18N
    everythingCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        everythingCheckBoxMenuItemActionPerformed(evt);
      }
    });
    filterMenu.add(everythingCheckBoxMenuItem);

    warningsCheckBoxMenuItem.setText(bundle.getString("FilterMessagesShowErrorsAndWarnings")); // NOI18N
    warningsCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        warningsCheckBoxMenuItemActionPerformed(evt);
      }
    });
    filterMenu.add(warningsCheckBoxMenuItem);

    errorsCheckBoxMenuItem.setText(bundle.getString("FilterMessagesShowErrors")); // NOI18N
    errorsCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        errorsCheckBoxMenuItemActionPerformed(evt);
      }
    });
    filterMenu.add(errorsCheckBoxMenuItem);

    loggingTablePopupMenu.add(filterMenu);

    sortingMenu.setText(bundle.getString("SortMessages")); // NOI18N

    messageTypeCheckBoxMenuItem.setText(bundle.getString("SortMessagesByType")); // NOI18N
    messageTypeCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        messageTypeCheckBoxMenuItemActionPerformed(evt);
      }
    });
    sortingMenu.add(messageTypeCheckBoxMenuItem);

    dateCheckBoxMenuItem.setText(bundle.getString("SortMessagesByTimestamp")); // NOI18N
    dateCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        dateCheckBoxMenuItemActionPerformed(evt);
      }
    });
    sortingMenu.add(dateCheckBoxMenuItem);

    loggingTablePopupMenu.add(sortingMenu);

    clearVelocityCurveMenuItem.setText(bundle.getString("ClearVelocityGraph")); // NOI18N
    clearVelocityCurveMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        clearVelocityCurveMenuItemActionPerformed(evt);
      }
    });
    velocityCurvePopupMenu.add(clearVelocityCurveMenuItem);

    noVehiclePanel.setLayout(new java.awt.BorderLayout());

    noVehicleLabel.setFont(noVehicleLabel.getFont().deriveFont(noVehicleLabel.getFont().getSize()+3f));
    noVehicleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    noVehicleLabel.setText(bundle.getString("NoVehicleAttached")); // NOI18N
    noVehiclePanel.add(noVehicleLabel, java.awt.BorderLayout.CENTER);

    setBorder(javax.swing.BorderFactory.createTitledBorder(""));
    setLayout(new java.awt.BorderLayout());

    tabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

    overviewTabPanel.setLayout(new java.awt.GridBagLayout());

    headPanel.setLayout(new java.awt.BorderLayout());

    statusPanel.setLayout(new javax.swing.BoxLayout(statusPanel, javax.swing.BoxLayout.LINE_AXIS));

    adapterStatusPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("AdapterStatus"))); // NOI18N
    adapterStatusPanel.setLayout(new java.awt.BorderLayout());

    chkBoxEnable.setText(bundle.getString("EnableAdapter")); // NOI18N
    chkBoxEnable.setEnabled(false);
    chkBoxEnable.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        chkBoxEnableActionPerformed(evt);
      }
    });
    adapterStatusPanel.add(chkBoxEnable, java.awt.BorderLayout.CENTER);

    statusPanel.add(adapterStatusPanel);

    statusFiguresPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("VehicleStatus"))); // NOI18N
    statusFiguresPanel.setLayout(new java.awt.GridBagLayout());

    curPosLbl.setText(bundle.getString("CurrentPosition")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    statusFiguresPanel.add(curPosLbl, gridBagConstraints);

    curPosTxt.setEditable(false);
    curPosTxt.setColumns(9);
    curPosTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    curPosTxt.setText("Point-0001");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    statusFiguresPanel.add(curPosTxt, gridBagConstraints);

    curStateLbl.setText(bundle.getString("CurrentState")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
    statusFiguresPanel.add(curStateLbl, gridBagConstraints);

    curStateTxt.setEditable(false);
    curStateTxt.setColumns(9);
    curStateTxt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    curStateTxt.setText("UNKNOWN");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
    statusFiguresPanel.add(curStateTxt, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.weightx = 1.0;
    statusFiguresPanel.add(fillingLbl, gridBagConstraints);

    statusPanel.add(statusFiguresPanel);

    headPanel.add(statusPanel, java.awt.BorderLayout.WEST);

    logoPanel.setBackground(new java.awt.Color(255, 255, 255));
    logoPanel.setLayout(new java.awt.BorderLayout());

    logoLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    logoLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/kernel/controlcenter/res/logos/opentcs_logo.gif"))); // NOI18N
    logoPanel.add(logoLbl, java.awt.BorderLayout.CENTER);

    headPanel.add(logoPanel, java.awt.BorderLayout.CENTER);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    overviewTabPanel.add(headPanel, gridBagConstraints);

    curvePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
    curvePanel.setMinimumSize(new java.awt.Dimension(20, 200));
    curvePanel.setLayout(new java.awt.BorderLayout());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weighty = 1.0;
    overviewTabPanel.add(curvePanel, gridBagConstraints);

    logPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Messages"))); // NOI18N
    logPanel.setPreferredSize(new java.awt.Dimension(468, 200));
    logPanel.setLayout(new java.awt.BorderLayout());

    logTableScrollPane.setComponentPopupMenu(loggingTablePopupMenu);

    loggingTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {
        "Time stamp", "Message"
      }
    ) {
      boolean[] canEdit = new boolean [] {
        false, false
      };

      public boolean isCellEditable(int rowIndex, int columnIndex) {
        return canEdit [columnIndex];
      }
    });
    loggingTable.setComponentPopupMenu(loggingTablePopupMenu);
    loggingTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    logTableScrollPane.setViewportView(loggingTable);

    logPanel.add(logTableScrollPane, java.awt.BorderLayout.CENTER);

    loggingTextArea.setEditable(false);
    loggingTextArea.setColumns(20);
    loggingTextArea.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
    loggingTextArea.setLineWrap(true);
    loggingTextArea.setRows(3);
    loggingTextArea.setComponentPopupMenu(logPopupMenu);
    logTextScrollPane.setViewportView(loggingTextArea);

    logPanel.add(logTextScrollPane, java.awt.BorderLayout.SOUTH);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    overviewTabPanel.add(logPanel, gridBagConstraints);

    tabbedPane.addTab(bundle.getString("GeneralStatus"), overviewTabPanel); // NOI18N

    queuesPanel.setLayout(new java.awt.GridBagLayout());

    cmdQueuePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Command queue"));
    cmdQueuePanel.setMinimumSize(new java.awt.Dimension(272, 50));
    cmdQueuePanel.setLayout(new java.awt.BorderLayout(0, 3));

    cmdQueueList.setModel(new CommandListModel());
    cmdQueueList.setCellRenderer(new CommandListCellRenderer());
    jScrollPane1.setViewportView(cmdQueueList);

    cmdQueuePanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

    clearCmdQueueButton.setText("Clear");
    clearCmdQueueButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        clearCmdQueueButtonActionPerformed(evt);
      }
    });
    cmdQueuePanel.add(clearCmdQueueButton, java.awt.BorderLayout.SOUTH);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    queuesPanel.add(cmdQueuePanel, gridBagConstraints);

    sentQueuePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Sent queue"));
    sentQueuePanel.setMinimumSize(new java.awt.Dimension(272, 50));
    sentQueuePanel.setLayout(new java.awt.GridBagLayout());

    sentQueueList.setModel(new CommandListModel());
    sentQueueList.setCellRenderer(new CommandListCellRenderer());
    jScrollPane2.setViewportView(sentQueueList);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    sentQueuePanel.add(jScrollPane2, gridBagConstraints);

    sendAgainButton.setText("Send again");
    sendAgainButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        sendAgainButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    sentQueuePanel.add(sendAgainButton, gridBagConstraints);

    removeButton.setText("Remove");
    removeButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        removeButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_END;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    sentQueuePanel.add(removeButton, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    queuesPanel.add(sentQueuePanel, gridBagConstraints);

    tabbedPane.addTab(bundle.getString("CommandQueues"), queuesPanel); // NOI18N

    add(tabbedPane, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents
  private void clearVelocityCurveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearVelocityCurveMenuItemActionPerformed
    //velocityCurve.clear();
  }//GEN-LAST:event_clearVelocityCurveMenuItemActionPerformed

  private void dateCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dateCheckBoxMenuItemActionPerformed
    dateCheckBoxMenuItem.setSelected(true);
    loggingTableModel.sortByDate();
    messageTypeCheckBoxMenuItem.setSelected(false);
  }//GEN-LAST:event_dateCheckBoxMenuItemActionPerformed

  private void messageTypeCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_messageTypeCheckBoxMenuItemActionPerformed
    messageTypeCheckBoxMenuItem.setSelected(true);
    loggingTableModel.sortByType();
    dateCheckBoxMenuItem.setSelected(false);
  }//GEN-LAST:event_messageTypeCheckBoxMenuItemActionPerformed

  private void warningsCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_warningsCheckBoxMenuItemActionPerformed
    loggingTableModel.filterMessages(LogTableModel.errorsAndWarningsFilter);
    warningsCheckBoxMenuItem.setSelected(true);
    errorsCheckBoxMenuItem.setSelected(false);
    everythingCheckBoxMenuItem.setSelected(false);
  }//GEN-LAST:event_warningsCheckBoxMenuItemActionPerformed

  private void everythingCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_everythingCheckBoxMenuItemActionPerformed
    loggingTableModel.filterMessages(LogTableModel.noFilter);
    everythingCheckBoxMenuItem.setSelected(true);
    errorsCheckBoxMenuItem.setSelected(false);
    warningsCheckBoxMenuItem.setSelected(false);
  }//GEN-LAST:event_everythingCheckBoxMenuItemActionPerformed

  private void errorsCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_errorsCheckBoxMenuItemActionPerformed
    loggingTableModel.filterMessages(LogTableModel.errorsFilter);
    errorsCheckBoxMenuItem.setSelected(true);
    everythingCheckBoxMenuItem.setSelected(false);
    warningsCheckBoxMenuItem.setSelected(false);
  }//GEN-LAST:event_errorsCheckBoxMenuItemActionPerformed

  private void clearMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearMenuItemActionPerformed
    loggingTextArea.setText("");
  }//GEN-LAST:event_clearMenuItemActionPerformed

  private void sendAgainButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendAgainButtonActionPerformed
    for (MovementCommand moveCmd : sentQueueList.getSelectedValuesList()) {
      vehicleModel.getCommunicationAdapter().sendCommand(moveCmd);
    }
  }//GEN-LAST:event_sendAgainButtonActionPerformed

  private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
    for (MovementCommand moveCmd : sentQueueList.getSelectedValuesList()) {
      vehicleModel.getCommunicationAdapter().getSentQueue().remove(moveCmd);
    }
    CommandListModel model = (CommandListModel) sentQueueList.getModel();
    model.setValues(vehicleModel.getCommunicationAdapter().getSentQueue());
  }//GEN-LAST:event_removeButtonActionPerformed

  private void clearCmdQueueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearCmdQueueButtonActionPerformed
    BasicCommunicationAdapter adapter =
        vehicleModel.getCommunicationAdapter();
    if (adapter != null) {
      adapter.clearCommandQueue();
    }
  }//GEN-LAST:event_clearCmdQueueButtonActionPerformed

  private void chkBoxEnableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkBoxEnableActionPerformed
    if (chkBoxEnable.isSelected()) {
      vehicleModel.getCommunicationAdapter().enable();
    }
    else {
      vehicleModel.getCommunicationAdapter().disable();
    }
  }//GEN-LAST:event_chkBoxEnableActionPerformed
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel adapterStatusPanel;
  private javax.swing.JCheckBox chkBoxEnable;
  private javax.swing.JButton clearCmdQueueButton;
  private javax.swing.JMenuItem clearMenuItem;
  private javax.swing.JMenuItem clearVelocityCurveMenuItem;
  private javax.swing.JList<MovementCommand> cmdQueueList;
  private javax.swing.JPanel cmdQueuePanel;
  private javax.swing.JLabel curPosLbl;
  private javax.swing.JTextField curPosTxt;
  private javax.swing.JLabel curStateLbl;
  private javax.swing.JTextField curStateTxt;
  private javax.swing.JPanel curvePanel;
  private javax.swing.JCheckBoxMenuItem dateCheckBoxMenuItem;
  private javax.swing.JCheckBoxMenuItem errorsCheckBoxMenuItem;
  private javax.swing.JCheckBoxMenuItem everythingCheckBoxMenuItem;
  private javax.swing.JLabel fillingLbl;
  private javax.swing.JMenu filterMenu;
  private javax.swing.JPanel headPanel;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JPanel logPanel;
  private javax.swing.JPopupMenu logPopupMenu;
  private javax.swing.JScrollPane logTableScrollPane;
  private javax.swing.JScrollPane logTextScrollPane;
  private javax.swing.JTable loggingTable;
  private javax.swing.JPopupMenu loggingTablePopupMenu;
  private javax.swing.JTextArea loggingTextArea;
  private javax.swing.JLabel logoLbl;
  private javax.swing.JPanel logoPanel;
  private javax.swing.JCheckBoxMenuItem messageTypeCheckBoxMenuItem;
  private javax.swing.ButtonGroup modeButtonGroup;
  private javax.swing.JLabel noVehicleLabel;
  private javax.swing.JPanel noVehiclePanel;
  private javax.swing.JPanel overviewTabPanel;
  private javax.swing.JPanel queuesPanel;
  private javax.swing.JButton removeButton;
  private javax.swing.JButton sendAgainButton;
  private javax.swing.JList<MovementCommand> sentQueueList;
  private javax.swing.JPanel sentQueuePanel;
  private javax.swing.JMenu sortingMenu;
  private javax.swing.JPanel statusFiguresPanel;
  private javax.swing.JPanel statusPanel;
  private javax.swing.JTabbedPane tabbedPane;
  private javax.swing.JPopupMenu velocityCurvePopupMenu;
  private javax.swing.JCheckBoxMenuItem warningsCheckBoxMenuItem;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON

  /**
   * A <code>ListSelectionListener</code> for handling the logging table
   * selection events.
   */
  private final class RowListener
      implements ListSelectionListener {
    
    /**
     * Creates a new instance.
     */
    private RowListener() {
      // Do nada.
    }

    @Override
    public void valueChanged(ListSelectionEvent event) {
      if (event.getValueIsAdjusting()) {
        return;
      }
      if (loggingTable.getSelectedRow() >= 0) {
        outputLogMessage(loggingTable.getSelectedRow());
      }
      else {
        loggingTextArea.setText("");
      }
    }
  }

  /**
   * A <code>ListModel</code> for the adapter's command queue and sent queue.
   */
  private static final class CommandListModel
      extends AbstractListModel<MovementCommand> {

    /**
     * A list containing the actual values.
     */
    private final List<MovementCommand> values = new ArrayList<>();
    
    /**
     * Creates a new instance.
     */
    private CommandListModel() {
      // Do nada.
    }

    // Implementation of interface ListModel starts here.
    @Override
    public MovementCommand getElementAt(int index) {
      return values.get(index);
    }

    @Override
    public int getSize() {
      return values.size();
    }

    // Class-specific methods start here.
    /**
     * Clears the current values and replaces them.
     * 
     * @param newValues The new values.
     */
    private void setValues(Collection<MovementCommand> newValues) {
      Objects.requireNonNull(newValues, "newValues is null");
      final int endIndex = Math.max(values.size(), newValues.size()) - 1;
      final int endIndexBefore = values.size() - 1;
      values.clear();
      fireIntervalRemoved(this, 0, endIndexBefore + 1);
      values.addAll(newValues);
//      fireContentsChanged(this, 0, endIndex);
      fireIntervalAdded(this, 0, newValues.size());
    }
  }
}
