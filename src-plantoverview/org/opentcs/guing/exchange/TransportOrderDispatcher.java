/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.exchange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.DriveOrder.Destination;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.exchange.adapter.LocationAdapter;
import org.opentcs.guing.exchange.adapter.VehicleAdapter;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.transport.TransportOrderListener;
import org.opentcs.util.eventsystem.EventListener;
import org.opentcs.util.eventsystem.TCSEvent;

/**
 * A special event dispatcher for transport orders.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class TransportOrderDispatcher
    implements EventListener<TCSEvent> {

  /**
   * This class's logger.
   */
  private static final Logger log
      = Logger.getLogger(TransportOrderDispatcher.class.getName());
  /**
   * The central dispatcher to which incoming events are forwarded.
   */
  private final OpenTCSEventDispatcher fDispatcher;
  /**
   * The listeners.
   */
  private final List<TransportOrderListener> fListeners = new ArrayList<>();

  /**
   * Creates a new instance of TransportOrderEventDispatcher.
   *
   * @param dispatcher The injected {@link OpenTCSEventDispatcher}.
   */
  public TransportOrderDispatcher(OpenTCSEventDispatcher dispatcher) {
    fDispatcher = requireNonNull(dispatcher, "dispatcher");
  }

  /**
   * Adds a new {@link TransportOrderListener}.
   *
   * @param l The new listener.
   */
  public void addListener(TransportOrderListener l) {
    fListeners.add(l);
  }

  @Override
  public void processEvent(TCSEvent event) {
    TCSObjectEvent objEvent = (TCSObjectEvent) event;
    TransportOrder t = (TransportOrder) objEvent.getCurrentOrPreviousObjectState();

    switch (objEvent.getType()) {
      case OBJECT_CREATED:
        transportOrderCreated(t);
        break;

      case OBJECT_MODIFIED:
        transportOrderModified(t);
        break;

      case OBJECT_REMOVED:
        transportOrderRemoved(t);
        break;
    }
  }

  /**
   * Returns all transport orders.
   *
   * @return All {@link org.opentcs.data.order.TransportOrder}s.
   */
  public Set<TransportOrder> getTransportOrders() {
    try {
      return getKernel().getTCSObjects(TransportOrder.class);
    }
    catch (CredentialsException e) {
      log.log(Level.WARNING, "Unexpected exception", e);
      return null;
    }
  }

  /**
   * Creates a new transport order.
   *
   * @param locations The locations to visit.
   * @param actions The actions to execute.
   * @param deadline The deadline.
   * @param vehicle The vehicle that shall execute this order. Pass
   * <code>null</code> to let the kernel determine one.
   */
  @SuppressWarnings("unchecked")
  public void createTransportOrder(List<LocationModel> locations, List<String> actions, long deadline, VehicleModel vehicle) {
    requireNonNull(locations, "locations");
    requireNonNull(actions, "actions");

    List<Destination> destinations = new ArrayList<>();
    for (int i = 0; i < locations.size(); i++) {
      LocationModel locModel = locations.get(i);
      String action = actions.get(i);
      LocationAdapter locAdapter
          = (LocationAdapter) fDispatcher.findProcessAdapter(locModel);
      destinations.add(new Destination(locAdapter.getProcessObject(), action));
    }

    try {
      TransportOrder tOrder = getKernel().createTransportOrder(destinations);
      getKernel().setTransportOrderDeadline(tOrder.getReference(), deadline);

      if (vehicle != null) {
        VehicleAdapter vAdapter = (VehicleAdapter) fDispatcher.findProcessAdapter(vehicle);
        getKernel().setTransportOrderIntendedVehicle(tOrder.getReference(),
                                                     vAdapter.getProcessObject());
      }

      getKernel().activateTransportOrder(tOrder.getReference());
    }
    catch (CredentialsException | ObjectUnknownException e) {
      log.log(Level.WARNING, "Unexpected exception", e);
    }
  }

  /**
   * Creates a new transport order by copying the given one.
   *
   * @param pattern The transport order that server as a pattern.
   */
  public void createTransportOrder(TransportOrder pattern) {
    List<Destination> destinations = new ArrayList<>();
    List<DriveOrder> driveOrders = new ArrayList<>();
    driveOrders.addAll(pattern.getPastDriveOrders());

    if (pattern.getCurrentDriveOrder() != null) {
      driveOrders.add(pattern.getCurrentDriveOrder());
    }

    driveOrders.addAll(pattern.getFutureDriveOrders());

    for (DriveOrder dOrder : driveOrders) {
      destinations.add(dOrder.getDestination());
    }

    try {
      TransportOrder tOrder = getKernel().createTransportOrder(destinations);
      getKernel().setTransportOrderDeadline(tOrder.getReference(),
                                            pattern.getDeadline());

      if (pattern.getIntendedVehicle() != null) {
        getKernel().setTransportOrderIntendedVehicle(tOrder.getReference(),
                                                     pattern.getIntendedVehicle());
      }

      getKernel().activateTransportOrder(tOrder.getReference());
    }
    catch (CredentialsException | ObjectUnknownException e) {
      log.log(Level.WARNING, "Unexpected exception", e);
    }
  }

  /**
   * Creates a new transport order for the purpose to drive to a point.
   *
   * @param point The point that shall be driven to.
   * @param vehicle The vehicle to execute this order.
   */
  @SuppressWarnings("unchecked")
  public void createTransportOrder(PointModel point, VehicleModel vehicle) {
    requireNonNull(point, "point");
    requireNonNull(vehicle, "vehicle");

    // This is only allowed in operating mode.
    if (getKernel().getState() != Kernel.State.OPERATING) {
      return;
    }

    TCSObjectReference<Location> locRef
        = TCSObjectReference.getDummyReference(Location.class, point.getName());
    List<Destination> dest
        = Collections.singletonList(new Destination(locRef, Destination.OP_MOVE));

    try {
      TransportOrder t = getKernel().createTransportOrder(dest);
      getKernel().setTransportOrderDeadline(t.getReference(),
                                            System.currentTimeMillis());

      VehicleAdapter vAdapter
          = (VehicleAdapter) fDispatcher.findProcessAdapter(vehicle);
      getKernel().setTransportOrderIntendedVehicle(t.getReference(),
                                                   vAdapter.getProcessObject());

      getKernel().activateTransportOrder(t.getReference());
    }
    catch (CredentialsException | ObjectUnknownException e) {
      log.log(Level.WARNING, "Unexpected exception", e);
    }
  }

  /**
   * Requests the removal of a transport order.
   *
   * @param t The {@link org.opentcs.data.order.TransportOrder} to remove.
   */
  public void requestRemoveTransportOrder(TransportOrder t) {
    try {
      getKernel().removeTCSObject(t.getReference());
    }
    catch (CredentialsException | ObjectUnknownException e) {
      log.log(Level.WARNING, "Unexpected exception", e);
    }
  }

  /**
   * Returns the kernel.
   *
   * @return The kernel.
   */
  private Kernel getKernel() {
    return fDispatcher.getKernel();
  }

  /**
   * Informs the listeners that a transport order was created.
   *
   * @param t The newly created {@link org.opentcs.data.order.TransportOrder}.
   */
  private void transportOrderCreated(TransportOrder t) {
    for (TransportOrderListener l : fListeners) {
      l.transportOrderAdded(t);
    }
  }

  /**
   * Informs the listeners that a transport order has changed.
   *
   * @param t The changed {@link org.opentcs.data.order.TransportOrder}.
   */
  private void transportOrderModified(TransportOrder t) {
    for (TransportOrderListener l : fListeners) {
      l.transportOrderChanged(t);
    }
  }

  /**
   * Informs the listeners that a transport order was removed.
   *
   * @param t The removed {@link org.opentcs.data.order.TransportOrder}.
   */
  private void transportOrderRemoved(TransportOrder t) {
    for (TransportOrderListener l : fListeners) {
      l.transportOrderRemoved(t);
    }
  }
}
