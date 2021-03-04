/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.vehicleselection;

import com.google.inject.BindingAnnotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.ProcessabilityChecker;
import org.opentcs.strategies.basic.dispatching.VehicleOrderSelection;
import org.opentcs.strategies.basic.dispatching.VehicleSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks for the closest available vehicle for a given transport order.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class AvailableVehicleSelectionStrategy
    implements OrderVehicleSelectionStrategy,
               Lifecycle {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(VehicleSelector.class);
  /**
   * The local kernel instance.
   */
  private final LocalKernel kernel;
  /**
   * The Router instance calculating route costs.
   */
  private final Router router;
  /**
   * Checks processability of transport orders for vehicles.
   */
  private final ProcessabilityChecker processabilityChecker;
  /**
   * The comparator used for sorting vehicle candidates.
   */
  private final Comparator<VehicleCandidate> candidateComparator;
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  @Inject
  public AvailableVehicleSelectionStrategy(
      @Nonnull LocalKernel kernel,
      @Nonnull Router router,
      @Nonnull ProcessabilityChecker processabilityChecker,
      @Nonnull @VehicleCandidateComparator Comparator<VehicleCandidate> candidateComparator) {
    this.kernel = requireNonNull(kernel, "kernel");
    this.router = requireNonNull(router, "router");
    this.processabilityChecker = requireNonNull(processabilityChecker, "processabilityChecker");
    this.candidateComparator = requireNonNull(candidateComparator, "candidateComparator");
  }

  @Override
  public void initialize() {
    if (initialized) {
      return;
    }
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!initialized) {
      return;
    }
    initialized = false;
  }

  @Nullable
  @Override
  public VehicleOrderSelection selectVehicle(TransportOrder order) {
    requireNonNull(order, "order");

    // Get all vehicle candidates and sort them.
    SortedSet<VehicleCandidate> candidates = new TreeSet<>(candidateComparator);

    for (Vehicle curVehicle : availableVehicles(order)) {
      Point curPosition = kernel.getTCSObject(Point.class, curVehicle.getCurrentPosition());
      // Get a route for the vehicle, starting at it's current position.
      Optional<List<DriveOrder>> tmpDriveOrders = router.getRoute(curVehicle, curPosition, order);
      // If there is a route for this vehicle, remember it and the costs.
      if (tmpDriveOrders.isPresent()
          && processabilityChecker.checkProcessability(curVehicle, order)) {
        long costs = 0;
        for (DriveOrder curDriveOrder : tmpDriveOrders.get()) {
          costs += curDriveOrder.getRoute().getCosts();
        }
        candidates.add(new VehicleCandidate(curVehicle, costs, tmpDriveOrders.get()));
      }
    }

    if (candidates.isEmpty()) {
      return null;
    }

    VehicleCandidate bestCandidate = candidates.first();
    return new VehicleOrderSelection(order,
                                     bestCandidate.getVehicle(),
                                     bestCandidate.getDriveOrders());
  }

  private List<Vehicle> availableVehicles(TransportOrder order) {
    List<Vehicle> result = new LinkedList<>();
    for (Vehicle curVehicle : kernel.getTCSObjects(Vehicle.class)) {
      if (processabilityChecker.availableForTransportOrder(curVehicle, order)) {
        result.add(curVehicle);
      }
    }
    return result;
  }

  /**
   * A binding annotation for the comparator sorting transport orders.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface VehicleCandidateComparator {
  }
}
