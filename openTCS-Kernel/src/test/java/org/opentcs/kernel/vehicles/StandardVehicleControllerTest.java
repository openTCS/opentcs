/*
 *
 * Created on 23.03.2012 09:53:07
 */
package org.opentcs.kernel.vehicles;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.listener.Handler;
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
import org.opentcs.strategies.basic.scheduling.DummyScheduler;

/**
 * Test cases for StandardVehicleController.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StandardVehicleControllerTest {

  private static final String RECHARGE_OP = "recharge";
  /**
   * Creates model objects for us.
   */
  private final DataObjectFactory dataObjectFactory = new DataObjectFactory();
  /**
   * Represents the kernel application's event bus.
   */
  private final MBassador<Object> eventBus = new MBassador<>(BusConfiguration.Default());
  /**
   * A vehicle.
   */
  private Vehicle vehicle;
  /**
   * A vehicle model.
   */
  private VehicleProcessModel vehicleModel;
  /**
   * A (mocked) communication adapter.
   */
  private VehicleCommAdapter commAdapter;
  /**
   * The (mocked) kernel.
   */
  private LocalKernel localKernel;
  /**
   * A dummy scheduler.
   */
  private Scheduler scheduler;
  /**
   * The instance we're testing.
   */
  private DefaultVehicleController stdVehicleController;

  @Before
  public void setUp() {
    vehicle = dataObjectFactory.createVehicle();
    vehicleModel = new VehicleProcessModel(vehicle);
    commAdapter = mock(VehicleCommAdapter.class);
    localKernel = mock(LocalKernel.class);

    doReturn(RECHARGE_OP).when(commAdapter).getRechargeOperation();
    doReturn(vehicleModel).when(commAdapter).getProcessModel();

    doReturn(vehicle).when(localKernel).getTCSObject(Vehicle.class, vehicle.getReference());
    doReturn(vehicle).when(localKernel).getTCSObject(Vehicle.class, vehicle.getName());

    scheduler = new DummyScheduler();
    scheduler.initialize();

    stdVehicleController = new DefaultVehicleController(vehicle,
                                                        commAdapter,
                                                        localKernel,
                                                        scheduler,
                                                        eventBus,
                                                        true);
    stdVehicleController.initialize();
  }

  @After
  public void tearDown() {
    stdVehicleController.terminate();
    scheduler.terminate();
  }

  // Test cases for implementation of interface VehicleManager start here.
  @Test
  public void should_foward_position_change_to_kernel() {
    Point point = dataObjectFactory.createPoint();
    doReturn(point).when(localKernel).getTCSObject(Point.class, point.getName());

    vehicleModel.setVehiclePosition(point.getName());

    verify(localKernel).setVehiclePosition(vehicle.getReference(),
                                           point.getReference());
  }

  @Test
  public void should_forward_precise_position_change_to_kernel() {
    Triple newPos = new Triple(211, 391, 0);
    vehicleModel.setVehiclePrecisePosition(newPos);

    verify(localKernel).setVehiclePrecisePosition(vehicle.getReference(),
                                                  newPos);
  }

  @Test
  public void should_forward_angle_change_to_kernel() {
    double newAngle = 7.5;
    vehicleModel.setVehicleOrientationAngle(newAngle);

    verify(localKernel).setVehicleOrientationAngle(vehicle.getReference(),
                                                   newAngle);
  }

  @Test
  public void should_forward_energy_level_change_to_kernel() {
    int newLevel = 80;
    vehicleModel.setVehicleEnergyLevel(newLevel);
    verify(localKernel).setVehicleEnergyLevel(vehicle.getReference(),
                                              newLevel);
  }

  @Test
  public void should_forward_load_handling_devices_change_to_kernel() {
    List<LoadHandlingDevice> devices = new LinkedList<>();
    devices.add(new LoadHandlingDevice("MyLoadHandlingDevice", true));
    vehicleModel.setVehicleLoadHandlingDevices(devices);

    verify(localKernel).setVehicleLoadHandlingDevices(vehicle.getReference(),
                                                      devices);
  }

  @Test
  public void should_forward_vehicle_state_change_to_kernel() {
    vehicleModel.setVehicleState(Vehicle.State.EXECUTING);

    verify(localKernel).setVehicleState(vehicle.getReference(),
                                        Vehicle.State.EXECUTING);
  }

  @Test
  public void should_forward_adapter_state_change_to_kernel() {
    vehicleModel.setVehicleAdapterState(VehicleCommAdapter.State.UNKNOWN);

    verify(localKernel).setVehicleAdapterState(vehicle.getReference(),
                                               VehicleCommAdapter.State.UNKNOWN);
  }

  @Test
  public void should_forward_event_to_bus() {
    final String adapterName = "myAdapter";
    final String eventString = "myString";
    final List<VehicleCommAdapterEvent> eventsReceived = new LinkedList<>();
    Object eventHandler = new Object() {

      @Handler
      public void handleEvent(VehicleCommAdapterEvent event) {
        eventsReceived.add(event);
      }
    };
    eventBus.subscribe(eventHandler);

    vehicleModel.publishEvent(new VehicleCommAdapterEvent(adapterName, eventString));

    assertEquals("Did not receive exactly one event", 1, eventsReceived.size());
    VehicleCommAdapterEvent event = eventsReceived.get(0);
    assertEquals("Received event does not seem to be published event",
                 eventString,
                 event.getAppendix());
  }

  // Test cases for implementation of interface VehicleController start here.
  @Test
  public void should_have_idempotent_enabled_state() {
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
  public void should_not_accept_multiple_drive_orders() {
    Location location = dataObjectFactory.createLocation();
    DriveOrder driveOrder = new DriveOrder(new DriveOrder.Destination(
        location.getReference(), DriveOrder.Destination.OP_NOP));

    Point dstPoint = dataObjectFactory.createPoint();
    Path stepPath = dataObjectFactory.createPath(dstPoint.getReference());
    List<Route.Step> steps = Collections.singletonList(
        new Route.Step(stepPath, dstPoint, Vehicle.Orientation.FORWARD, 0));

    driveOrder.setRoute(new Route(steps, 1));

    stdVehicleController.setDriveOrder(driveOrder, new HashMap<>());
    // Should result in an IllegalStateException:
    stdVehicleController.setDriveOrder(driveOrder, new HashMap<>());
  }
}
