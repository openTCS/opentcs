// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opentcs.DataObjectFactory;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.components.kernel.services.InternalVehicleService;
import org.opentcs.components.kernel.services.NotificationService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.IncomingPoseTransformer;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.MovementCommandTransformer;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterEvent;
import org.opentcs.drivers.vehicle.VehicleDataTransformerFactory;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.kernel.KernelApplicationConfiguration;
import org.opentcs.kernel.vehicles.transformers.VehicleDataTransformerRegistry;
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
   * A (mocked) vehicle data transformer registry.
   */
  private VehicleDataTransformerRegistry dataTransformerRegistry;
  /**
   * A (mocked) vehicle data transformer factory.
   */
  private VehicleDataTransformerFactory dataTransformerFactory;
  /**
   * A (mocked) incoming pose transformer.
   */
  private IncomingPoseTransformer poseTransformer;
  /**
   * A (mocked) movement command transformer.
   */
  private MovementCommandTransformer movementCommandTransformer;
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
    vehicle = dataObjectFactory
        .createVehicle()
        .withProperties(Map.of("tcs:vehicleDataTransformer", "dummyFactory"));
    vehicleModel = new VehicleProcessModel(vehicle);
    vehicleModelTO = new VehicleProcessModelTO();
    commAdapter = mock(VehicleCommAdapter.class);
    vehicleService = mock(InternalVehicleService.class);
    componentsFactory = mock(VehicleControllerComponentsFactory.class);
    peripheralInteractor = mock(PeripheralInteractor.class);
    dataTransformerFactory = mock(VehicleDataTransformerFactory.class);
    poseTransformer = mock(IncomingPoseTransformer.class);
    movementCommandTransformer = mock(MovementCommandTransformer.class);
    dataTransformerRegistry = new VehicleDataTransformerRegistry(Set.of(dataTransformerFactory));

    doReturn("dummyFactory").when(dataTransformerFactory).getName();
    doReturn(poseTransformer).when(dataTransformerFactory).createIncomingPoseTransformer(vehicle);
    doReturn(movementCommandTransformer)
        .when(dataTransformerFactory).createMovementCommandTransformer(vehicle);
    doReturn(true).when(dataTransformerFactory).providesTransformersFor(vehicle);
    when(poseTransformer.apply(any(Pose.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(movementCommandTransformer.apply(any(MovementCommand.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    doReturn(RECHARGE_OP).when(commAdapter).getRechargeOperation();
    doReturn(vehicleModel).when(commAdapter).getProcessModel();
    doReturn(vehicleModelTO).when(commAdapter).createTransferableProcessModel();

    doReturn(Optional.of(vehicle)).when(vehicleService).fetch(
        Vehicle.class, vehicle.getReference()
    );
    doReturn(Optional.of(vehicle)).when(vehicleService).fetch(Vehicle.class, vehicle.getName());

    doReturn(peripheralInteractor).when(componentsFactory)
        .createPeripheralInteractor(vehicle.getReference());

    scheduler = spy(new DummyScheduler());
    scheduler.initialize();
    stdVehicleController = new DefaultVehicleController(
        vehicle,
        commAdapter,
        vehicleService,
        mock(InternalTransportOrderService.class),
        mock(NotificationService.class),
        mock(DispatcherService.class),
        scheduler,
        eventBus,
        componentsFactory,
        mock(MovementCommandMapper.class),
        mock(KernelApplicationConfiguration.class),
        new CommandProcessingTracker(),
        dataTransformerRegistry,
        mock(VehiclePositionResolver.class)
    );
    stdVehicleController.initialize();
  }

  @AfterEach
  void tearDown() {
    stdVehicleController.terminate();
    scheduler.terminate();
  }

  // Test cases for implementation of interface VehicleManager start here.
  @Test
  void shouldForwardPositionChangeToKernel() {
    Point point = dataObjectFactory.createPoint();
    doReturn(Optional.of(point)).when(vehicleService).fetch(Point.class, point.getName());

    vehicleModel.setPosition(point.getName());

    verify(vehicleService).updateVehiclePosition(
        vehicle.getReference(),
        point.getReference()
    );
  }

  @Test
  void shouldForwardPoseChangeToKernel() {
    Pose newPose = new Pose(new Triple(211, 391, 0), 7.5);
    vehicleModel.setPose(newPose);

    verify(vehicleService).updateVehiclePose(
        vehicle.getReference(),
        newPose
    );
  }

  @Test
  void shouldTransformPoseWhenUsingDifferentCoordinateSystems() {
    // The initial call to the transformer should have already been made during initialization.
    verify(poseTransformer, times(1)).apply(any(Pose.class));

    vehicleModel.setPose(new Pose(new Triple(211, 391, 0), Double.NaN));
    verify(poseTransformer, times(2)).apply(any(Pose.class));

    vehicleModel.setPose(new Pose(null, 33.0));
    verify(poseTransformer, times(3)).apply(any(Pose.class));
  }

  @Test
  void shouldForwardEnergyLevelChangeToKernel() {
    int newLevel = 80;
    vehicleModel.setEnergyLevel(newLevel);
    verify(vehicleService).updateVehicleEnergyLevel(
        vehicle.getReference(),
        newLevel
    );
  }

  @Test
  void shouldForwardLoadHandlingDevicesChangeToKernel() {
    List<LoadHandlingDevice> devices
        = List.of(new LoadHandlingDevice("MyLoadHandlingDevice", true));
    vehicleModel.setLoadHandlingDevices(devices);

    verify(vehicleService).updateVehicleLoadHandlingDevices(
        vehicle.getReference(),
        devices
    );
  }

  @Test
  void shouldForwardVehicleStateChangeToKernel() {
    vehicleModel.setState(Vehicle.State.EXECUTING);

    verify(vehicleService).updateVehicleState(
        vehicle.getReference(),
        Vehicle.State.EXECUTING
    );
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
        new Route.Step(stepPath, null, dstPoint, Vehicle.Orientation.FORWARD, 0, 1)
    );

    DriveOrder driveOrder = new DriveOrder(
        "some-order",
        new DriveOrder.Destination(location.getReference())
    )
        .withRoute(new Route(steps));

    TransportOrder transportOrder
        = new TransportOrder("some-transport-order", List.of(driveOrder))
            .withCurrentDriveOrderIndex(0);

    stdVehicleController.setTransportOrder(transportOrder);

    verify(scheduler).claim(eq(stdVehicleController), Mockito.any());
  }
}
