/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.vehicleselection;

import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import org.junit.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.Route.Step;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.ProcessabilityChecker;
import org.opentcs.strategies.basic.dispatching.VehicleOrderSelection;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class AvailableVehicleSelectionStrategyTest {

  private LocalKernel kernel;
  private Router router;
  private ProcessabilityChecker processabilityChecker;
  private Comparator<VehicleCandidate> candidateComparator;
  private AvailableVehicleSelectionStrategy strategy;
  private TransportOrder transportOrder;
  private Vehicle vehicle1;
  private Point positionVehicle1;

  @Before
  public void setUp() {
    candidateComparator = new Ordering<VehicleCandidate>() {
      @Override
      public int compare(VehicleCandidate left, VehicleCandidate right) {
        return Ints.saturatedCast(left.getCosts() - right.getCosts());
      }
    };
    kernel = mock(LocalKernel.class);
    router = mock(Router.class);
    processabilityChecker = mock(ProcessabilityChecker.class);
    transportOrder = createPlainTransportOrder("transportOrder");
    positionVehicle1 = new Point("positionVehicle1");
    vehicle1 = new Vehicle("vehicle1").withCurrentPosition(positionVehicle1.getReference());
    strategy = new AvailableVehicleSelectionStrategy(
        kernel,
        router,
        processabilityChecker,
        candidateComparator);
    strategy.initialize();
  }

  @After
  public void tearDown() {
    strategy.terminate();
  }

  @Test
  public void returnsNullForEmptyVehicleSet() {
    when(kernel.getTCSObjects(Vehicle.class)).thenReturn(new HashSet<>());
    VehicleOrderSelection result = strategy.selectVehicle(transportOrder);
    assertThat(result, is(nullValue()));
  }

  @Test
  public void returnsNullForAllVehiclesUnavailableForTransport() {
    when(kernel.getTCSObjects(Vehicle.class)).thenReturn(new HashSet<>(Arrays.asList(vehicle1)));
    when(processabilityChecker.availableForTransportOrder(vehicle1, transportOrder))
        .thenReturn(false);
    VehicleOrderSelection result = strategy.selectVehicle(transportOrder);
    assertThat(result, is(nullValue()));
  }

  @Test
  public void returnsNullForUnprocessableOrder() {
    when(kernel.getTCSObjects(Vehicle.class)).thenReturn(new HashSet<>(Arrays.asList(vehicle1)));
    when(processabilityChecker.availableForTransportOrder(vehicle1, transportOrder))
        .thenReturn(true);
    when(processabilityChecker.checkProcessability(vehicle1, transportOrder))
        .thenReturn(false);
    when(kernel.getTCSObject(Point.class, positionVehicle1.getReference()))
        .thenReturn(positionVehicle1);
    List<DriveOrder> orders = createDriveOrders("destination1");
    when(router.getRoute(vehicle1, positionVehicle1, transportOrder))
        .thenReturn(Optional.of(orders));
    VehicleOrderSelection result = strategy.selectVehicle(transportOrder);
    assertThat(result, is(nullValue()));
  }

  @Test
  public void returnsCheapestVehicleOrder() {

    Point positionVehicle2 = new Point("pointVehicle2");
    Vehicle vehicle2 = new Vehicle("vehicle2").withCurrentPosition(positionVehicle2.getReference());

    Point positionVehicle3 = new Point("pointVehicle3");
    Vehicle vehicle3 = new Vehicle("vehicle3").withCurrentPosition(positionVehicle3.getReference());

    when(kernel.getTCSObjects(Vehicle.class))
        .thenReturn(new HashSet<>(Arrays.asList(vehicle1, vehicle2, vehicle3)));
    when(processabilityChecker.availableForTransportOrder(vehicle1, transportOrder))
        .thenReturn(true);
    when(processabilityChecker.availableForTransportOrder(vehicle2, transportOrder))
        .thenReturn(true);
    when(processabilityChecker.availableForTransportOrder(vehicle3, transportOrder))
        .thenReturn(true);
    when(kernel.getTCSObject(Point.class, positionVehicle1.getReference()))
        .thenReturn(positionVehicle1);
    when(kernel.getTCSObject(Point.class, positionVehicle2.getReference()))
        .thenReturn(positionVehicle2);
    when(kernel.getTCSObject(Point.class, positionVehicle3.getReference()))
        .thenReturn(positionVehicle3);
    when(processabilityChecker.checkProcessability(vehicle1, transportOrder))
        .thenReturn(true);
    when(processabilityChecker.checkProcessability(vehicle2, transportOrder))
        .thenReturn(true);
    when(processabilityChecker.checkProcessability(vehicle3, transportOrder))
        .thenReturn(true);

    List<DriveOrder> orders = createSingleDriveOrderWithCosts("destination1", 8);
    orders.addAll(createSingleDriveOrderWithCosts("destination12", 22));
    when(router.getRoute(vehicle1, positionVehicle1, transportOrder))
        .thenReturn(Optional.of(orders));

    List<DriveOrder> orders2 = createSingleDriveOrderWithCosts("destination2", 8);
    orders2.addAll(createSingleDriveOrderWithCosts("destination22", 2));
    when(router.getRoute(vehicle2, positionVehicle2, transportOrder))
        .thenReturn(Optional.of(orders2));

    List<DriveOrder> orders3 = createSingleDriveOrderWithCosts("destination3", 9);
    orders3.addAll(createSingleDriveOrderWithCosts("destination32", 10));
    when(router.getRoute(vehicle3, positionVehicle3, transportOrder))
        .thenReturn(Optional.of(orders3));

    VehicleOrderSelection result = strategy.selectVehicle(transportOrder);;
    Assert.assertNotNull(result);
    assertTrue(result.isAssignable());
    assertThat(result.getVehicle(), is(equalTo(vehicle2)));
  }

  private TransportOrder createPlainTransportOrder(String transportOrderName) {
    Location destLocation
        = new Location("Some location", new LocationType("Some location type").getReference());
    List<DriveOrder> driveOrders
        = new LinkedList<>(Arrays.asList(
            new DriveOrder(
                new DriveOrder.Destination(destLocation.getReference())
            )
        ));
    return new TransportOrder(transportOrderName, driveOrders);
  }

  private List<DriveOrder> createDriveOrders(String destinationName) {
    Location destLocation
        = new Location(destinationName, new LocationType(destinationName + " location type")
                       .getReference());
    List<DriveOrder> localDriveOrders
        = new LinkedList<>(Arrays.asList(
            new DriveOrder(
                new DriveOrder.Destination(destLocation.getReference())
            )
        ));
    return localDriveOrders;
  }

  private List<DriveOrder> createSingleDriveOrderWithCosts(String destinationName, long cost) {
    Step dummyStep = mock(Step.class);
    Location destLocation
        = new Location(destinationName, new LocationType(destinationName + " location type")
                       .getReference());
    List<DriveOrder> localDriveOrders = new LinkedList<>();
    localDriveOrders.add(
        new DriveOrder(new DriveOrder.Destination(destLocation.getReference()))
        .withRoute(new Route(Arrays.asList(dummyStep), cost))
    );
    return localDriveOrders;
  }
}
