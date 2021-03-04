/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.orderselection;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.when;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.ProcessabilityChecker;
import org.opentcs.strategies.basic.dispatching.VehicleOrderSelection;
import org.mockito.Mockito;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.orderselection.parking.ParkingPositionSupplier;
import static org.mockito.Mockito.mock;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class ParkingOrderSelectionStrategyTest {

  private Vehicle vehicle;
  private LocalKernel kernel;
  private Router router;
  private ParkingPositionSupplier parkingPosSupplier;
  private ProcessabilityChecker processabilityChecker;
  private DefaultDispatcherConfiguration configuration;
  private ParkingOrderSelectionStrategy parkingOrderSelectionStrategy;
  private TransportOrder transportOrder;

  @Before
  public void setUp() {
    vehicle = new Vehicle("vehicle1");
    kernel = mock(LocalKernel.class);
    router = mock(Router.class);
    parkingPosSupplier = mock(ParkingPositionSupplier.class);
    processabilityChecker = mock(ProcessabilityChecker.class);
    configuration = mock(DefaultDispatcherConfiguration.class);

  }

  @After
  public void tearDown() {
    if (parkingOrderSelectionStrategy != null) {
      parkingOrderSelectionStrategy.terminate();
    }
  }

  @Test
  public void returnsNullforDontParkConfiguration() {
    when(configuration.parkIdleVehicles())
        .thenReturn(false);
    VehicleOrderSelection result = runSelectOrder();
    assertThat(result, is(nullValue()));
  }

  @Test
  public void returnsNoActionForVehicleInParkingPosition() {
    when(configuration.parkIdleVehicles())
        .thenReturn(true);
    Point vehicle1Point = new Point("vehicle1point1").withType(Point.Type.PARK_POSITION);
    vehicle = vehicle.withCurrentPosition(vehicle1Point.getReference());
    when(kernel.getTCSObject(Point.class, vehicle1Point.getReference()))
        .thenReturn(vehicle1Point);
    VehicleOrderSelection result = runSelectOrder();
    assertThat(result, is(notNullValue()));
    assertThat(result.isAssignable(), is(false));
  }

  @Test
  public void returnsNullForUnavailableParkingPosition() {
    Point vehicle1Point = new Point("vehicle1point1").withType(Point.Type.HALT_POSITION);
    when(configuration.parkIdleVehicles())
        .thenReturn(true);
    vehicle = vehicle.withCurrentPosition(vehicle1Point.getReference());
    when(kernel.getTCSObject(Point.class, vehicle1Point.getReference()))
        .thenReturn(vehicle1Point);
    when(parkingPosSupplier.findParkingPosition(vehicle))
        .thenReturn(Optional.empty());
    VehicleOrderSelection result = runSelectOrder();
    assertThat(result, is(nullValue()));
  }

  @Test
  public void returnsParkOrder() {
    configureMocksandBeans();
    when(processabilityChecker.checkProcessability(vehicle, transportOrder))
        .thenReturn(true);
    VehicleOrderSelection result = runSelectOrder();
    assertThat(result.isAssignable(), is(true));
  }

  @Test
  public void returnsNullForUnprocessableParkOrder() {
    configureMocksandBeans();
    when(processabilityChecker.checkProcessability(vehicle, transportOrder))
        .thenReturn(false);
    VehicleOrderSelection result = runSelectOrder();
    assertThat(result, is(nullValue()));
  }

  private void configureMocksandBeans() {
    Point vehicle1ParkPos = new Point("vehicle1ParkPos");
    Point vehicle1Point = new Point("vehicle1point1").withType(Point.Type.HALT_POSITION);
    when(configuration.parkIdleVehicles())
        .thenReturn(true);
    vehicle = vehicle.withCurrentPosition(vehicle1Point.getReference());
    when(kernel.getTCSObject(Point.class, vehicle1Point.getReference()))
        .thenReturn(vehicle1Point);
    when(parkingPosSupplier.findParkingPosition(vehicle))
        .thenReturn(Optional.of(vehicle1ParkPos));
    Optional<List<DriveOrder>> optionalDriveOrders = Optional.of(createDriveOrders("toParkPosition"));
    transportOrder = createPlainTransportOrder("Parkorder");
    when(kernel.createTransportOrder(Mockito.any(TransportOrderCreationTO.class)))
        .thenReturn(transportOrder);
    when(router.getRoute(vehicle, vehicle1Point, transportOrder))
        .thenReturn(optionalDriveOrders);

  }

  private VehicleOrderSelection runSelectOrder() {
    parkingOrderSelectionStrategy = new ParkingOrderSelectionStrategy(kernel, router, parkingPosSupplier, processabilityChecker, configuration);
    parkingOrderSelectionStrategy.initialize();
    return parkingOrderSelectionStrategy.selectOrder(vehicle);
  }

  private List<DriveOrder> createDriveOrders(String destinationName) {
    Location destLocation
        = new Location(destinationName, new LocationType("Some location type").getReference());
    List<DriveOrder> driveOrders
        = Arrays.asList(new DriveOrder(new DriveOrder.Destination(destLocation.getReference())));
    return driveOrders;
  }

  private TransportOrder createPlainTransportOrder(String transportOrderName) {
    Location destLocation
        = new Location("Some location", new LocationType("Some location type").getReference());
    List<DriveOrder> driveOrders
        = Arrays.asList(new DriveOrder(new DriveOrder.Destination(destLocation.getReference())));
    return new TransportOrder(transportOrderName, driveOrders)
        .withDispensable(true)
        .withIntendedVehicle(vehicle.getReference());
  }
}
