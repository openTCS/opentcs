/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import org.opentcs.DataObjectFactory;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.components.kernel.services.InternalVehicleService;
import org.opentcs.components.kernel.services.NotificationService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterEvent;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.kernel.KernelApplicationConfiguration;
import org.opentcs.strategies.basic.scheduling.DummyScheduler;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.SimpleEventBus;

/**
 * Test cases for StandardVehicleController.
 */
class DefaultVehicleControllerTest {

  private static final String RECHARGE_OP = "recharge";
  /**
   * Creates model objects for us.
   */
  private final DataObjectFactory dataObjectFactory = new DataObjectFactory();
  /**
   * The kernel application's event bus.
   */
  private final EventBus eventBus = new SimpleEventBus();
  /**
   * A vehicle.
   */
  private Vehicle vehicle;
  /**
   * A vehicle model.
   */
  private VehicleProcessModel vehicleModel;
  /**
   * A serializable representation of the vehicle model.
   */
  private VehicleProcessModelTO vehicleModelTO;
  /**
   * A (mocked) communication adapter.
   */
  private VehicleCommAdapter commAdapter;
  /**
   * The (mocked) vehicle service.
   */
  private InternalVehicleService vehicleService;
  /**
   * A dummy scheduler.
   */
  private Scheduler scheduler;
  /**
   * A (mocked) components factory.
   */
  private VehicleControllerComponentsFactory componentsFactory;
  /**
   * A (mocked) peripheral interactor.
   */
  private PeripheralInteractor peripheralInteractor;
  /**
   * The instance we're testing.
   */
  private DefaultVehicleController stdVehicleController;

  @BeforeEach
  void setUp() {
    vehicle = dataObjectFactory.createVehicle();
    vehicleModel = new VehicleProcessModel(vehicle);
    vehicleModelTO = new VehicleProcessModelTO();
    commAdapter = mock(VehicleCommAdapter.class);
    vehicleService = mock(InternalVehicleService.class);
    componentsFactory = mock(VehicleControllerComponentsFactory.class);
    peripheralInteractor = mock(PeripheralInteractor.class);

    doReturn(RECHARGE_OP).when(commAdapter).getRechargeOperation();
    doReturn(vehicleModel).when(commAdapter).getProcessModel();
    doReturn(vehicleModelTO).when(commAdapter).createTransferableProcessModel();

    doReturn(vehicle).when(vehicleService).fetchObject(Vehicle.class, vehicle.getReference());
    doReturn(vehicle).when(vehicleService).fetchObject(Vehicle.class, vehicle.getName());

    doReturn(peripheralInteractor).when(componentsFactory)
        .createPeripheralInteractor(vehicle.getReference());

    scheduler = spy(new DummyScheduler());
    scheduler.initialize();
    stdVehicleController = new DefaultVehicleController(vehicle,
                                                        commAdapter,
                                                        vehicleService,
                                                        mock(InternalTransportOrderService.class),
                                                        mock(NotificationService.class),
                                                        mock(DispatcherService.class),
                                                        scheduler,
                                                        eventBus,
                                                        componentsFactory,
                                                        mock(MovementCommandMapper.class),
                                                        mock(KernelApplicationConfiguration.class));
    stdVehicleController.initialize();
  }

  @AfterEach
  void tearDown() {
    stdVehicleController.terminate();
    scheduler.terminate();
  }

  // Test cases for implementation of interface VehicleManager start here.
  @Test
  void shouldFowardPositionChangeToKernel() {
    Point point = dataObjectFactory.createPoint();
    doReturn(point).when(vehicleService).fetchObject(Point.class, point.getName());

    vehicleModel.setPosition(point.getName());

    verify(vehicleService).updateVehiclePosition(vehicle.getReference(),
                                                 point.getReference());
  }

  @Test
  void shouldForwardPrecisePositionChangeToKernel() {
    Triple newPos = new Triple(211, 391, 0);
    vehicleModel.setPrecisePosition(newPos);

    verify(vehicleService).updateVehiclePrecisePosition(vehicle.getReference(),
                                                        newPos);
  }

  @Test
  void shouldForwardAngleChangeToKernel() {
    double newAngle = 7.5;
    vehicleModel.setOrientationAngle(newAngle);

    verify(vehicleService).updateVehicleOrientationAngle(vehicle.getReference(),
                                                         newAngle);
  }

  @Test
  void shouldForwardEnergyLevelChangeToKernel() {
    int newLevel = 80;
    vehicleModel.setEnergyLevel(newLevel);
    verify(vehicleService).updateVehicleEnergyLevel(vehicle.getReference(),
                                                    newLevel);
  }

  @Test
  void shouldForwardLoadHandlingDevicesChangeToKernel() {
    List<LoadHandlingDevice> devices
        = List.of(new LoadHandlingDevice("MyLoadHandlingDevice", true));
    vehicleModel.setLoadHandlingDevices(devices);

    verify(vehicleService).updateVehicleLoadHandlingDevices(vehicle.getReference(),
                                                            devices);
  }

  @Test
  void shouldForwardVehicleStateChangeToKernel() {
    vehicleModel.setState(Vehicle.State.EXECUTING);

    verify(vehicleService).updateVehicleState(vehicle.getReference(),
                                              Vehicle.State.EXECUTING);
  }

  @Test
  void shouldForwardEventToBus() {
    final String adapterName = "myAdapter";
    final String eventString = "myString";
    final List<VehicleCommAdapterEvent> eventsReceived = new ArrayList<>();

    eventBus.subscribe(event -> {
      if (event instanceof VehicleCommAdapterEvent) {
        eventsReceived.add((VehicleCommAdapterEvent) event);
      }
    });

    vehicleModel.publishEvent(new VehicleCommAdapterEvent(adapterName, eventString));

    assertEquals(1, eventsReceived.size());
    VehicleCommAdapterEvent event = eventsReceived.get(0);
    assertEquals(eventString, event.getAppendix());
  }

  // Test cases for implementation of interface VehicleController start here.
  @Test
  void shouldHaveIdempotentEnabledState() {
    stdVehicleController.initialize();
    assertTrue(stdVehicleController.isInitialized());
    stdVehicleController.initialize();
    assertTrue(stdVehicleController.isInitialized());
    stdVehicleController.terminate();
    assertFalse(stdVehicleController.isInitialized());
    stdVehicleController.terminate();
    assertFalse(stdVehicleController.isInitialized());
  }

  @Test
  void shouldSetClaimOnNewTransportOrder() {
    Location location = dataObjectFactory.createLocation();

    Point dstPoint = dataObjectFactory.createPoint();
    Path stepPath = dataObjectFactory.createPath(dstPoint.getReference());
    List<Route.Step> steps = List.of(
        new Route.Step(stepPath, null, dstPoint, Vehicle.Orientation.FORWARD, 0)
    );

    DriveOrder driveOrder = new DriveOrder(new DriveOrder.Destination(location.getReference()))
        .withRoute(new Route(steps, 1));

    TransportOrder transportOrder
        = new TransportOrder("some-transport-order", List.of(driveOrder))
            .withCurrentDriveOrderIndex(0);

    stdVehicleController.setTransportOrder(transportOrder);

    verify(scheduler).claim(eq(stdVehicleController), Mockito.any());
  }
}
