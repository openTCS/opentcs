/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.components.dialogs;

import com.google.inject.assistedinject.Assisted;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Arrays;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import javax.inject.Inject;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jhotdraw.draw.Figure;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.base.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.base.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.common.components.properties.SelectionPropertiesComponent;
import org.opentcs.guing.common.components.tree.ComponentsTreeViewManager;
import org.opentcs.guing.common.components.tree.TreeViewManager;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.operationsdesk.application.menus.MenuFactory;
import org.opentcs.operationsdesk.components.drawing.figures.VehicleFigure;
import org.opentcs.operationsdesk.util.I18nPlantOverviewOperating;
import org.opentcs.operationsdesk.util.VehicleCourseObjectFactory;

/**
 * A single vehicle in the {@link VehiclesPanel}.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class SingleVehicleView
    extends JPanel
    implements AttributesChangeListener,
               Comparable<SingleVehicleView> {

  /**
   * The resource bundle this component uses.
   */
  private static final ResourceBundle BUNDLE
      = ResourceBundle.getBundle(I18nPlantOverviewOperating.VEHICLEVIEW_PATH);
  /**
   * The color definition for orange.
   */
  private static final Color ORANGE = new Color(0xff, 0xdd, 0x75);
  /**
   * The color definition for green.
   */
  private static final Color GREEN = new Color(0x77, 0xdb, 0x6c);
  /**
   * The Vehicle to be displayed.
   */
  private final VehicleModel fVehicleModel;
  /**
   * The tree view's manager (for selecting the vehicle when it's clicked on).
   */
  private final TreeViewManager treeViewManager;
  /**
   * The properties component (for displaying properties of the vehicle when
   * it's clicked on).
   */
  private final SelectionPropertiesComponent propertiesComponent;
  /**
   * The drawing editor (for accessing the currently active drawing view).
   */
  private final OpenTCSDrawingEditor drawingEditor;
  /**
   * A factory for popup menus.
   */
  private final MenuFactory menuFactory;
  /**
   * The model manager.
   */
  private final ModelManager modelManager;
  /**
   * Panel to draw on.
   */
  private final JPanel fVehicleView;

  /**
   * Creates new instance.
   *
   * @param vehicle The vehicle to be displayed.
   * @param treeViewManager The tree view's manager (for selecting the vehicle
   * when it's clicked on).
   * @param propertiesComponent The properties component (for displaying
   * properties of the vehicle when it's clicked on).
   * @param drawingEditor The drawing editor (for accessing the currently active
   * drawing view).
   * @param crsObjFactory A factory to create vehicle figures.
   * @param menuFactory A factory for popup menus.
   * @param modelManager The model manager.
   */
  @Inject
  public SingleVehicleView(@Assisted VehicleModel vehicle,
                           ComponentsTreeViewManager treeViewManager,
                           SelectionPropertiesComponent propertiesComponent,
                           OpenTCSDrawingEditor drawingEditor,
                           VehicleCourseObjectFactory crsObjFactory,
                           MenuFactory menuFactory,
                           ModelManager modelManager) {
    this.fVehicleModel = requireNonNull(vehicle, "vehicle");
    this.treeViewManager = requireNonNull(treeViewManager, "treeViewManager");
    this.propertiesComponent = requireNonNull(propertiesComponent,
                                              "propertiesComponent");
    this.drawingEditor = requireNonNull(drawingEditor, "drawingEditor");
    this.menuFactory = requireNonNull(menuFactory, "menuFactory");
    this.modelManager = requireNonNull(modelManager, "modelManager");
    requireNonNull(crsObjFactory, "crsObjFactory");
    this.fVehicleView = new VehicleView(fVehicleModel, crsObjFactory.createVehicleFigure(fVehicleModel));

    initComponents();

    vehiclePanel.add(fVehicleView, BorderLayout.CENTER);

    vehicle.addAttributesChangeListener(this);

    vehicleLabel.setText(vehicle.getName());
    updateVehicle();
  }

  private void showPopup(int x, int y) {
    menuFactory.createVehiclePopupMenu(Arrays.asList(fVehicleModel)).show(this, x, y);
  }

  private void updateVehicle() {
    updateVehicleIntegrationLevel();
    updateVehicleState();
    updateVehiclePosition();
    updateEnergyLevel();
    updateVehicleDestination();

    revalidate();
  }

  private void updateVehicleDestination() {
    PointModel destinationPoint = getVehicleModel().getDriveOrderDestination();
    if (destinationPoint != null) {
      destinationValueLabel.setText(destinationPoint.getName());
    }
    else {
      destinationValueLabel.setText("-");
    }
  }

  private void updateVehicleIntegrationLevel() {
    Vehicle.IntegrationLevel integrationLevel
        = (Vehicle.IntegrationLevel) fVehicleModel.getPropertyIntegrationLevel().getValue();
    switch (integrationLevel) {
      case TO_BE_IGNORED:
      case TO_BE_NOTICED:
        integratedStateLabel.setText(BUNDLE.getString("singleVehicleView.label_integratedState.no.text"));
        integratedStateLabel.setOpaque(false);
        break;
      case TO_BE_RESPECTED:
        integratedStateLabel.setText(BUNDLE.getString("singleVehicleView.label_integratedState.partially.text"));
        integratedStateLabel.setOpaque(true);
        integratedStateLabel.setBackground(ORANGE);
        break;
      case TO_BE_UTILIZED:
        integratedStateLabel.setText(BUNDLE.getString("singleVehicleView.label_integratedState.fully.text"));
        integratedStateLabel.setOpaque(true);
        integratedStateLabel.setBackground(GREEN);
        break;
      default:
        integratedStateLabel.setText(integrationLevel.name());
        integratedStateLabel.setOpaque(false);
    }
  }

  private void updateVehicleState() {
    Vehicle.State state = (Vehicle.State) fVehicleModel.getPropertyState().getValue();

    vehicleStateValueLabel.setText(state.toString());

    switch (state) {
      case ERROR:
      case UNAVAILABLE:
      case UNKNOWN:
        vehicleStateValueLabel.setBackground(ORANGE);
        vehicleStateValueLabel.setOpaque(true);
        break;
      default:
        vehicleStateValueLabel.setOpaque(false);
    }
  }

  private void updateVehiclePosition() {
    positionValueLabel.setText(fVehicleModel.getPropertyPoint().getText());
  }

  private void updateEnergyLevel() {
    batteryLabel.setText(fVehicleModel.getPropertyEnergyLevel().getValue() + " %");
    Vehicle vehicle = fVehicleModel.getVehicle();

    if (vehicle.isEnergyLevelCritical()) {
      batteryIcon.setIcon(new ImageIcon(getToolkit().getImage(getClass().getClassLoader().
          getResource("org/opentcs/guing/res/symbols/panel/battery-caution-3.png"))));
    }
    else if (vehicle.isEnergyLevelDegraded()) {
      batteryIcon.setIcon(new ImageIcon(getToolkit().getImage(getClass().getClassLoader().
          getResource("org/opentcs/guing/res/symbols/panel/battery-060-2.png"))));
    }
    else if (vehicle.isEnergyLevelGood()) {
      batteryIcon.setIcon(new ImageIcon(getToolkit().getImage(getClass().getClassLoader().
          getResource("org/opentcs/guing/res/symbols/panel/battery-100-2.png"))));
    }
  }

  public VehicleModel getVehicleModel() {
    return fVehicleModel;
  }

  @Override
  public void propertiesChanged(AttributesChangeEvent e) {
    updateVehicle();
  }

  // CHECKSTYLE:OFF
  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    statusPanel = new javax.swing.JPanel();
    vehiclePanel = new javax.swing.JPanel();
    vehicleLabel = new javax.swing.JLabel();
    batteryPanel = new javax.swing.JPanel();
    batteryIcon = new javax.swing.JLabel();
    batteryLabel = new javax.swing.JLabel();
    propertiesPanel = new javax.swing.JPanel();
    integratedLabel = new javax.swing.JLabel();
    integratedStateLabel = new javax.swing.JLabel();
    vehicleStateLabel = new javax.swing.JLabel();
    vehicleStateValueLabel = new javax.swing.JLabel();
    positionLabel = new javax.swing.JLabel();
    positionValueLabel = new javax.swing.JLabel();
    destinationLabel = new javax.swing.JLabel();
    destinationValueLabel = new javax.swing.JLabel();
    fillLabel = new javax.swing.JLabel();

    setMinimumSize(new java.awt.Dimension(200, 59));
    setLayout(new java.awt.GridBagLayout());

    statusPanel.setLayout(new java.awt.BorderLayout());

    vehiclePanel.setLayout(new java.awt.BorderLayout());

    vehicleLabel.setFont(vehicleLabel.getFont());
    vehicleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    vehiclePanel.add(vehicleLabel, java.awt.BorderLayout.NORTH);

    statusPanel.add(vehiclePanel, java.awt.BorderLayout.CENTER);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    add(statusPanel, gridBagConstraints);

    batteryPanel.setMinimumSize(new java.awt.Dimension(20, 14));
    batteryPanel.setPreferredSize(new java.awt.Dimension(45, 14));
    batteryPanel.setLayout(new java.awt.GridBagLayout());
    batteryPanel.add(batteryIcon, new java.awt.GridBagConstraints());

    batteryLabel.setText("battery");
    batteryLabel.setPreferredSize(new java.awt.Dimension(45, 14));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    batteryPanel.add(batteryLabel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(14, 0, 0, 0);
    add(batteryPanel, gridBagConstraints);

    propertiesPanel.setLayout(new java.awt.GridBagLayout());

    integratedLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/plantoverview/operating/panels/vehicleView"); // NOI18N
    integratedLabel.setText(bundle.getString("singleVehicleView.label_integrated.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    propertiesPanel.add(integratedLabel, gridBagConstraints);

    integratedStateLabel.setText(bundle.getString("singleVehicleView.label_integratedState.no.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    propertiesPanel.add(integratedStateLabel, gridBagConstraints);

    vehicleStateLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    vehicleStateLabel.setText(bundle.getString("singleVehicleView.label_state.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
    propertiesPanel.add(vehicleStateLabel, gridBagConstraints);

    vehicleStateValueLabel.setText("UNAVAILABLE");
    vehicleStateValueLabel.setToolTipText("");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
    propertiesPanel.add(vehicleStateValueLabel, gridBagConstraints);

    positionLabel.setText(bundle.getString("singleVehicleView.label_position.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
    propertiesPanel.add(positionLabel, gridBagConstraints);

    positionValueLabel.setText("-");
    positionValueLabel.setMaximumSize(new java.awt.Dimension(68, 14));
    positionValueLabel.setMinimumSize(new java.awt.Dimension(68, 14));
    positionValueLabel.setPreferredSize(new java.awt.Dimension(68, 14));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
    propertiesPanel.add(positionValueLabel, gridBagConstraints);

    destinationLabel.setText(bundle.getString("singleVehicleView.label_destination.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
    propertiesPanel.add(destinationLabel, gridBagConstraints);

    destinationValueLabel.setText("-");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
    propertiesPanel.add(destinationValueLabel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    add(propertiesPanel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.weightx = 1.0;
    add(fillLabel, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel batteryIcon;
  private javax.swing.JLabel batteryLabel;
  private javax.swing.JPanel batteryPanel;
  private javax.swing.JLabel destinationLabel;
  private javax.swing.JLabel destinationValueLabel;
  private javax.swing.JLabel fillLabel;
  private javax.swing.JLabel integratedLabel;
  private javax.swing.JLabel integratedStateLabel;
  private javax.swing.JLabel positionLabel;
  private javax.swing.JLabel positionValueLabel;
  private javax.swing.JPanel propertiesPanel;
  private javax.swing.JPanel statusPanel;
  private javax.swing.JLabel vehicleLabel;
  private javax.swing.JPanel vehiclePanel;
  private javax.swing.JLabel vehicleStateLabel;
  private javax.swing.JLabel vehicleStateValueLabel;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON

  @Override
  public int compareTo(SingleVehicleView o) {
    return fVehicleModel.getName().compareTo(o.getVehicleModel().getName());
  }

  private class VehicleView
      extends JPanel
      implements AttributesChangeListener {

    private final VehicleFigure figure;

    public VehicleView(VehicleModel vehicleModel, VehicleFigure figure) {
      this.figure = requireNonNull(figure, "figure");
      requireNonNull(vehicleModel, "vehicleModel");

      vehicleModel.addAttributesChangeListener(this);

      setBackground(Color.WHITE);
      
      Rectangle r = figure.getBounds().getBounds();
      r.grow(10, 10);
      setPreferredSize(new Dimension(r.width, r.height));

      addMouseListener(new VehicleMouseAdapter(vehicleModel));
    }

    @Override
    public void propertiesChanged(AttributesChangeEvent e) {
      figure.propertiesChanged(e);

      // Because the figure is not part of any drawing it does not automatically redraw itself.
      SwingUtilities.invokeLater(() -> {
        this.repaint();
      });
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      drawVehicle((Graphics2D) g);
    }

    /**
     * Draws the vehicle figure into the dialog.
     *
     * @param g2d The graphics context.
     */
    private void drawVehicle(Graphics2D g2d) {
      figure.setIgnorePrecisePosition(true);
      Point2D.Double posDialog = new Point2D.Double(fVehicleView.getWidth() / 2, fVehicleView.getHeight() / 2);
      figure.setBounds(posDialog, null);
      figure.setAngle(0.0);
      figure.forcedDraw(g2d);
    }
  }

  private class VehicleMouseAdapter
      extends MouseAdapter {

    private final VehicleModel vehicleModel;

    public VehicleMouseAdapter(VehicleModel vehicleModel) {
      this.vehicleModel = requireNonNull(vehicleModel, "vehicleModel");
    }

    @Override
    public void mouseClicked(MouseEvent evt) {
      if (evt.getButton() == MouseEvent.BUTTON1) {
        treeViewManager.selectItem(vehicleModel);
        propertiesComponent.setModel(vehicleModel);
      }

      if (evt.getClickCount() == 2) {
        Figure vehicleFigure = modelManager.getModel().getFigure(vehicleModel);
        drawingEditor.getActiveView().scrollTo(vehicleFigure);
      }

      if (evt.getButton() == MouseEvent.BUTTON3) {
        showPopup(evt.getX(), evt.getY());
      }
    }
  }
}
