/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
/*
 *
 * Created on 23.03.2012 09:53:07
 */
package org.opentcs.kernel.vehicles;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.opentcs.DataObjectFactory;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.InternalVehicleService;
import org.opentcs.components.kernel.services.NotificationService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterEvent;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.strategies.basic.scheduling.DummyScheduler;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.SimpleEventBus;

/**
 * Test cases for StandardVehicleController.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultVehicleControllerTest {

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
   * The (mocked) kernel.
   */
  private LocalKernel localKernel;
  /**
   * The (mocked) vehicle service.
   */
  private InternalVehicleService vehicleService;
  /**
   * A dummy scheduler.
   */
  private Scheduler scheduler;
  /**
   * The instance we're testing.
   */
  private DefaultVehicleController stdVehicleController;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() {
    vehicle = dataObjectFactory.createVehicle();
    vehicleModel = new VehicleProcessModel(vehicle);
    vehicleModelTO = new VehicleProcessModelTO();
    commAdapter = mock(VehicleCommAdapter.class);
    localKernel = mock(LocalKernel.class);
    vehicleService = mock(InternalVehicleService.class);

    doReturn(RECHARGE_OP).when(commAdapter).getRechargeOperation();
    doReturn(vehicleModel).when(commAdapter).getProcessModel();
    doReturn(vehicleModelTO).when(commAdapter).createTransferableProcessModel();

    doReturn(vehicle).when(vehicleService).fetchObject(Vehicle.class, vehicle.getReference());
    doReturn(vehicle).when(vehicleService).fetchObject(Vehicle.class, vehicle.getName());

    scheduler = new DummyScheduler();
    scheduler.initialize();
    stdVehicleController = new DefaultVehicleController(vehicle,
                                                        commAdapter,
                                                        localKernel,
                                                        vehicleService,
                                                        mock(NotificationService.class),
                                                        mock(DispatcherService.class),
                                                        scheduler,
                                                        eventBus);
    stdVehicleController.initialize();
  }

  @After
  public void tearDown() {
    stdVehicleController.terminate();
    scheduler.terminate();
  }

  // Test cases for implementation of interface VehicleManager start here.
  @Test
  public void shouldFowardPositionChangeToKernel() {
    Point point = dataObjectFactory.createPoint();
    doReturn(point).when(vehicleService).fetchObject(Point.class, point.getName());

    vehicleModel.setVehiclePosition(point.getName());

    verify(vehicleService).updateVehiclePosition(vehicle.getReference(),
                                                 point.getReference());
  }

  @Test
  public void shouldForwardPrecisePositionChangeToKernel() {
    Triple newPos = new Triple(211, 391, 0);
    vehicleModel.setVehiclePrecisePosition(newPos);

    verify(vehicleService).updateVehiclePrecisePosition(vehicle.getReference(),
                                                        newPos);
  }

  @Test
  public void shouldForwardAngleChangeToKernel() {
    double newAngle = 7.5;
    vehicleModel.setVehicleOrientationAngle(newAngle);

    verify(vehicleService).updateVehicleOrientationAngle(vehicle.getReference(),
                                                         newAngle);
  }

  @Test
  public void shouldForwardEnergyLevelChangeToKernel() {
    int newLevel = 80;
    vehicleModel.setVehicleEnergyLevel(newLevel);
    verify(vehicleService).updateVehicleEnergyLevel(vehicle.getReference(),
                                                    newLevel);
  }

  @Test
  public void shouldForwardLoadHandlingDevicesChangeToKernel() {
    List<LoadHandlingDevice> devices = new LinkedList<>();
    devices.add(new LoadHandlingDevice("MyLoadHandlingDevice", true));
    vehicleModel.setVehicleLoadHandlingDevices(devices);

    verify(vehicleService).updateVehicleLoadHandlingDevices(vehicle.getReference(),
                                                            devices);
  }

  @Test
  public void shouldForwardVehicleStateChangeToKernel() {
    vehicleModel.setVehicleState(Vehicle.State.EXECUTING);

    verify(vehicleService).updateVehicleState(vehicle.getReference(),
                                              Vehicle.State.EXECUTING);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void shouldForwardAdapterStateChangeToKernel() {
    vehicleModel.setVehicleAdapterState(VehicleCommAdapter.State.UNKNOWN);

    verify(localKernel).setVehicleAdapterState(vehicle.getReference(),
                                               VehicleCommAdapter.State.UNKNOWN);
  }

  @Test
  public void shouldForwardEventToBus() {
    final String adapterName = "myAdapter";
    final String eventString = "myString";
    final List<VehicleCommAdapterEvent> eventsReceived = new LinkedList<>();

    eventBus.subscribe((event) -> {
      if (event instanceof VehicleCommAdapterEvent) {
        eventsReceived.add((VehicleCommAdapterEvent) event);
      }
    });

    vehicleModel.publishEvent(new VehicleCommAdapterEvent(adapterName, eventString));

    assertEquals("Did not receive exactly one event", 1, eventsReceived.size());
    VehicleCommAdapterEvent event = eventsReceived.get(0);
    assertEquals("Received event does not seem to be published event",
                 eventString,
                 event.getAppendix());
  }

  // Test cases for implementation of interface VehicleController start here.
  @Test
  public void shouldHaveIdempotentEnabledState() {
    stdVehicleController.initialize();
    assertTrue(stdVehicleController.isInitialized());
    stdVehicleController.initialize();
    assertTrue(stdVehicleController.isInitialized());
    stdVehicleController.terminate();
    assertFalse(stdVehicleController.isInitialized());
    stdVehicleController.terminate();
    assertFalse(stdVehicleController.isInitialized());
  }

  @Test(expected = IllegalStateException.class)
  public void shouldNotAcceptMultipleDriveOrders() {
    Location location = dataObjectFactory.createLocation();

    Point dstPoint = dataObjectFactory.createPoint();
    Path stepPath = dataObjectFactory.createPath(dstPoint.getReference());
    List<Route.Step> steps = Collections.singletonList(
        new Route.Step(stepPath, null, dstPoint, Vehicle.Orientation.FORWARD, 0));

    DriveOrder driveOrder = new DriveOrder(new DriveOrder.Destination(location.getReference()))
        .withRoute(new Route(steps, 1));

    stdVehicleController.setDriveOrder(driveOrder, new HashMap<>());
    // Should result in an IllegalStateException:
    stdVehicleController.setDriveOrder(driveOrder, new HashMap<>());
  }
}
