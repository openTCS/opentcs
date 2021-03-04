/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.orderselection;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.*;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.access.LocalKernel;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.ProcessabilityChecker;
import org.opentcs.strategies.basic.dispatching.VehicleOrderSelection;
import org.opentcs.strategies.basic.dispatching.orderselection.recharging.RechargePositionSupplier;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class RechargeOrderSelectionStrategyTest {

  private LocalKernel kernel;
  private Router router;
  private ProcessabilityChecker processabilityChecker;
  private RechargePositionSupplier rechargePosSupplier;
  private DefaultDispatcherConfiguration configuration;
  private Vehicle vehicle;
  private List<DriveOrder> driveOrders;
  private TransportOrder plainOrder;
  private RechargeOrderSelectionStrategy strategy;

  @Before
  public void setUp() {
    kernel = mock(LocalKernel.class);
    router = mock(Router.class);
    processabilityChecker = mock(ProcessabilityChecker.class);
    rechargePosSupplier = mock(RechargePositionSupplier.class);
    configuration = mock(DefaultDispatcherConfiguration.class);
    vehicle = new Vehicle("vehicle");
    strategy = new RechargeOrderSelectionStrategy(
        kernel,
        router,
        processabilityChecker,
        rechargePosSupplier,
        configuration);
    strategy.initialize();
  }

  @After
  public void tearDown() {
    strategy.terminate();
  }

  @Test
  public void returnsNullForDontRecharge() {
    when(configuration.rechargeIdleVehicles()).thenReturn(false);
    VehicleOrderSelection result = strategy.selectOrder(vehicle);
    assertThat(result, is(nullValue()));
  }

  @Test
  public void returnsNullForUndegradedEnergyLevel() {
    vehicle = vehicle.withEnergyLevel(80).withEnergyLevelGood(70).withEnergyLevelCritical(20);
    when(configuration.rechargeIdleVehicles()).thenReturn(true);
    VehicleOrderSelection result = strategy.selectOrder(vehicle);
    assertThat(result, is(nullValue()));
  }

  @Test
  public void returnsNoActionForChargingVehicle() {
    vehicle = vehicle
        .withEnergyLevel(60)
        .withEnergyLevelGood(70)
        .withEnergyLevelCritical(20)
        .withState(Vehicle.State.CHARGING);
    when(configuration.rechargeIdleVehicles()).thenReturn(true);
    VehicleOrderSelection result = strategy.selectOrder(vehicle);
    assertNotNull(result);
    assertThat(result.isAssignable(), is(false));
  }

  @Test
  public void returnsNullForNoSuitableRechargeLocation() {
    when(rechargePosSupplier.findRechargeSequence(vehicle)).thenReturn(new LinkedList<>());
    vehicle = vehicle
        .withEnergyLevel(60)
        .withEnergyLevelGood(70)
        .withEnergyLevelCritical(20)
        .withState(Vehicle.State.IDLE);
    when(configuration.rechargeIdleVehicles()).thenReturn(true);
    VehicleOrderSelection result = strategy.selectOrder(vehicle);
    assertThat(result, is(nullValue()));
  }

  @Test
  public void returnsOrderForRechargableProcessableVehicle() {
    configureMocksBeansForSelectOrderWithCorrectPrecond();
    when(processabilityChecker.checkProcessability(vehicle, plainOrder))
        .thenReturn(true);
    VehicleOrderSelection result = strategy.selectOrder(vehicle);
    assertNotNull(result);
    assertTrue(result.isAssignable());
    assertThat(result.getTransportOrder(), equalTo(plainOrder));
    assertThat(result.getDriveOrders(), equalTo(driveOrders));
    assertThat(result.getVehicle(), equalTo(vehicle));
  }

  @Test
  public void returnsNullForUnprocessableOrder() {
    configureMocksBeansForSelectOrderWithCorrectPrecond();
    when(processabilityChecker.checkProcessability(vehicle, plainOrder))
        .thenReturn(false);
    VehicleOrderSelection result = strategy.selectOrder(vehicle);
    assertThat(result, is(nullValue()));
  }

  private void configureMocksBeansForSelectOrderWithCorrectPrecond() {
    Point vehiclePosition = new Point("VehiclePosition");
    vehicle = vehicle
        .withEnergyLevel(60)
        .withEnergyLevelGood(70)
        .withEnergyLevelCritical(20)
        .withState(Vehicle.State.IDLE)
        .withCurrentPosition(vehiclePosition.getReference());
    when(configuration.rechargeIdleVehicles()).thenReturn(true);
    List<DriveOrder.Destination> destinations = createDestinations("Detination1");
    driveOrders = createDriveOrders(destinations);
    when(rechargePosSupplier.findRechargeSequence(vehicle)).thenReturn(destinations);
    plainOrder = createPlainTransportOrder("TransportOrder");
    when(kernel.createTransportOrder(Mockito.any(TransportOrderCreationTO.class)))
        .thenReturn(plainOrder);
    when(kernel.getTCSObject(Point.class, vehiclePosition.getReference()))
        .thenReturn(vehiclePosition);
    when(router.getRoute(vehicle, vehiclePosition, plainOrder))
        .thenReturn(Optional.of(driveOrders));
  }
  
  private List<DriveOrder.Destination> createDestinations(String destinationName) {
    Location destLocation
        = new Location(destinationName, new LocationType(destinationName + " location type")
            .getReference());

    List<DriveOrder.Destination> destinations = new LinkedList<>();
    destinations.add(new DriveOrder.Destination(destLocation.getReference()));
    return destinations;
  }

  private List<DriveOrder> createDriveOrders(List<DriveOrder.Destination> destinations) {
    List<DriveOrder> localDriveOrders = new LinkedList<>();
    for (DriveOrder.Destination dest : destinations) {
      localDriveOrders.add(new DriveOrder(dest));
    }
    return localDriveOrders;
  }

  private TransportOrder createPlainTransportOrder(String transportOrderName) {
    Location destLocation
        = new Location("Some location", new LocationType("Some location type").getReference());
    List<DriveOrder> localDriveOrders
        = Arrays.asList(new DriveOrder(new DriveOrder.Destination(destLocation.getReference())));
    return new TransportOrder(transportOrderName, localDriveOrders)
        .withDispensable(true)
        .withIntendedVehicle(vehicle.getReference());
  }
}
