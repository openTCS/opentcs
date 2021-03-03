/**
 * (c): IML.
 *
 */
package org.opentcs.guing.application.action.course;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.jhotdraw.draw.Figure;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.dialogs.StandardContentDialog;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.exchange.DefaultKernelProxyManager;
import org.opentcs.guing.exchange.KernelProxyManager;
import org.opentcs.guing.exchange.OpenTCSEventDispatcher;
import org.opentcs.guing.exchange.TransportOrderDispatcher;
import org.opentcs.guing.exchange.adapter.VehicleAdapter;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.transport.LocationActionPanel;
import org.opentcs.guing.transport.PointPanel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Different actions are unified here. One can call this class to get
 * a popup menu containing several vehicle action.
 * 
 * @author Heinz Huber (Fraunhofer IML)
 */
public class VehicleAction
    extends AbstractAction {

  /**
   * Scrolls to a vehicle in the drawing.
   */
  public static final String SCROLL_TO = "course.vehicle.scrollTo";
  /**
   * Automatically moves the drawing so a vehicle is always visible.
   */
  public static final String FOLLOW = "course.vehicle.follow";
  /**
   * Sends a vehicle directly to a point.
   */
  public static final String SEND_TO_POINT = "course.vehicle.sendToPoint";
  /**
   * Sends a vehicle directly to a location.
   */
  public static final String SEND_TO_LOCATION = "course.vehicle.sendToLocation";
  /**
   * Shows the properties of a vehicle.
   */
  public static final String SHOW_PROPERTIES = "course.vehicle.showProperties";
  /**
   * Withdraws the current transport order from a vehicle.
   */
  public static final String WITHDRAW_TRANSPORT_ORDER = "course.vehicle.withdrawTransportOrder";
  /**
   * Withdraws the current transport order from a vehicle and sets its
   * ProcState to UNAVAILABLE.
   */
  public static final String WITHDRAW_TRANSPORT_ORDER_DISABLE_VEHICLE = "course.vehicle.withdrawTransportOrderDisableVehicle";
  /**
   * Dispatches the vehicle.
   */
  public static final String DISPATCH_VEHICLE = "course.vehicle.dispatchVehicle";
  /**
   * This class's logger.
   */
  private static final Logger log
      = Logger.getLogger(VehicleAction.class.getName());
  /**
   * The vehicle.
   */
  private final VehicleModel fVehicle;
  /**
   * The kernel proxy/connection manager to be used.
   */
  private final KernelProxyManager kernelProxyManager;

  /**
   * Creates a new instance.
   *
   * @param actionId
   * @param vehicle
   */
  public VehicleAction(String actionId, VehicleModel vehicle) {
    this.fVehicle = vehicle;
    this.kernelProxyManager = DefaultKernelProxyManager.instance();
    ResourceBundleUtil.getBundle().configureAction(this, actionId);
  }

  /**
   * Creates a popup menu, which contains several vehicle actions.
   * 
   * @param model The <code>VehicleModel</code> to create the menu for.
   * @return The <code>JPopupMenu</code> to show.
   */
  public static JPopupMenu createVehicleMenu(VehicleModel model) {
    Objects.requireNonNull(model, "model is null");

    JPopupMenu menu = new JPopupMenu();
    JMenuItem mi = new JMenuItem(ResourceBundleUtil.getBundle().getString("VehicleAction.vehicle") + model.getName());
    // Disabled, Foreground, Background, ...
    mi.setEnabled(false);
    menu.add(mi);

    menu.addSeparator();

    menu.add(new VehicleAction(VehicleAction.SHOW_PROPERTIES, model));
    menu.add(new VehicleAction(VehicleAction.SCROLL_TO, model));

    JCheckBoxMenuItem followCheckBox = new JCheckBoxMenuItem();
    followCheckBox.setAction(new VehicleAction(VehicleAction.FOLLOW, model));
    followCheckBox.setSelected(model.isViewFollows());
    menu.add(followCheckBox);

    menu.addSeparator();

    VehicleAction vehicleAction;
    vehicleAction = new VehicleAction(VehicleAction.SEND_TO_POINT, model);
    vehicleAction.setEnabled(!pointModels().isEmpty());
    menu.add(vehicleAction);
    vehicleAction = new VehicleAction(VehicleAction.SEND_TO_LOCATION, model);
    vehicleAction.setEnabled(!locationModels().isEmpty());
    menu.add(vehicleAction);

    menu.addSeparator();

    VehicleAdapter vehicleAdapter = (VehicleAdapter) OpenTCSView.instance().getSystemModel().getEventDispatcher().findProcessAdapter(model);
    vehicleAction = new VehicleAction(VehicleAction.WITHDRAW_TRANSPORT_ORDER, model);
    vehicleAction.setEnabled(vehicleAdapter.isVehicleAvailable());
    menu.add(vehicleAction);
    vehicleAction = new VehicleAction(VehicleAction.WITHDRAW_TRANSPORT_ORDER_DISABLE_VEHICLE, model);
    vehicleAction.setEnabled(vehicleAdapter.isVehicleAvailable());
    menu.add(vehicleAction);
    menu.add(new VehicleAction(VehicleAction.DISPATCH_VEHICLE, model));

    return menu;
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();

    if (evt.getActionCommand().equals(labels.getString(SHOW_PROPERTIES + ".text"))) {
      OpenTCSView.instance().getPropertiesComponent().setModel(fVehicle);
    }
    else if (evt.getActionCommand().equals(labels.getString(SCROLL_TO + ".text"))) {
      Figure figure = fVehicle.getFigure();
      OpenTCSDrawingView drawingView = OpenTCSView.instance().getEditor().getActiveView();

      if (drawingView != null && figure != null) {
        drawingView.clearSelection();
        drawingView.addToSelection(figure);
        drawingView.scrollTo(figure);
      }
    }
    else if (evt.getActionCommand().equals(labels.getString(FOLLOW + ".text"))) {
      JCheckBoxMenuItem checkBox = (JCheckBoxMenuItem) evt.getSource();
      OpenTCSDrawingView drawingView = OpenTCSView.instance().getEditor().getActiveView();

      if (drawingView != null) {
        if (checkBox.isSelected()) {
          drawingView.followVehicle(fVehicle);
        }
        else {
          drawingView.stopFollowVehicle();
        }
      }
    }
    else if (evt.getActionCommand().equals(labels.getString(SEND_TO_POINT + ".text"))) {
      List<PointModel> pointModels = pointModels();

      if (!pointModels.isEmpty()) {
        PointPanel contentPanel = new PointPanel(pointModels);
        StandardContentDialog fDialog = new StandardContentDialog(OpenTCSView.instance(), contentPanel);
        fDialog.setTitle(evt.getActionCommand());
        fDialog.setVisible(true);

        if (fDialog.getReturnStatus() == StandardContentDialog.RET_OK) {
          PointModel point = (PointModel) contentPanel.getSelectedItem();
          orderDispatcher().createTransportOrder(point, fVehicle);
        }
      }
    }
    else if (evt.getActionCommand().equals(labels.getString(SEND_TO_LOCATION + ".text"))) {
      List<LocationModel> locModels = locationModels();
      
      if (!locModels.isEmpty()) {
        LocationActionPanel contentPanel = new LocationActionPanel(locModels);
        StandardContentDialog fDialog = new StandardContentDialog(OpenTCSView.instance(), contentPanel);
        fDialog.setTitle(evt.getActionCommand());
        fDialog.setVisible(true);

        if (fDialog.getReturnStatus() == StandardContentDialog.RET_OK) {
          LocationModel location = contentPanel.getSelectedLocation();
          List<LocationModel> locations = new ArrayList<>();
          locations.add(location);
          List<String> actions = new ArrayList<>();
          actions.add(contentPanel.getSelectedAction());
          orderDispatcher().createTransportOrder(locations,
                                                 actions,
                                                 System.currentTimeMillis(),
                                                 fVehicle);
        }
      }
    }
    else if (evt.getActionCommand().equals(labels.getString(WITHDRAW_TRANSPORT_ORDER + ".text"))) {
      try {
        kernel().withdrawTransportOrderByVehicle(vehicleReference(), false);
      }
      catch (ObjectUnknownException | CredentialsException e) {
        log.log(Level.WARNING, "Unexpected exception", e);
      }
    }
    else if (evt.getActionCommand().equals(labels.getString(WITHDRAW_TRANSPORT_ORDER_DISABLE_VEHICLE + ".text"))) {
      try {
        kernel().withdrawTransportOrderByVehicle(vehicleReference(), true);
      }
      catch (ObjectUnknownException | CredentialsException e) {
        log.log(Level.WARNING, "Unexpected exception", e);
      }
    }
    else if (evt.getActionCommand().equals(labels.getString(DISPATCH_VEHICLE + ".text"))) {
      try {
        kernel().dispatchVehicle(vehicleReference(), true);
      }
      catch (ObjectUnknownException | CredentialsException e) {
        log.log(Level.WARNING, "Unexpected exception", e);
      }
    }
  }

  private OpenTCSEventDispatcher eventDispatcher() {
    return (OpenTCSEventDispatcher) OpenTCSView.instance().getSystemModel().getEventDispatcher();
  }

  private TransportOrderDispatcher orderDispatcher() {
    return eventDispatcher().getTransportOrderDispatcher();
  }

  private VehicleAdapter processAdapter() {
    return (VehicleAdapter) eventDispatcher().findProcessAdapter(fVehicle);
  }

  private Kernel kernel() {
    return kernelProxyManager.kernel();
  }

  private TCSObjectReference<Vehicle> vehicleReference() {
    return processAdapter().getProcessObject();
  }

  private static List<LocationModel> locationModels() {
    return OpenTCSView.instance().getSystemModel().getLocationModels();
  }

  private static List<PointModel> pointModels() {
    return OpenTCSView.instance().getSystemModel().getPointModels();
  }
}
