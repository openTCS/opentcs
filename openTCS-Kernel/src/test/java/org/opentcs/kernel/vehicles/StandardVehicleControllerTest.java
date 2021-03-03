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
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.drivers.CommunicationAdapter;
import org.opentcs.drivers.CommunicationAdapterEvent;
import org.opentcs.drivers.LoadHandlingDevice;
import org.opentcs.strategies.basic.scheduling.DummyScheduler;

/**
 * Test cases for StandardVehicleController.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StandardVehicleControllerTest {

  /**
   * Creates model objects for us.
   */
  private final DataObjectFactory dataObjectFactory = new DataObjectFactory();
  /**
   * Represents the kernel application's event bus.
   */
  private final MBassador<Object> eventBus
      = new MBassador<>(BusConfiguration.Default());
  /**
   * A vehicle.
   */
  private Vehicle vehicle;
  /**
   * A (mocked) communication adapter.
   */
  private CommunicationAdapter commAdapter;
  /**
   * The (mocked) kernel.
   */
  private LocalKernel localKernel;
  /**
   * The instance we're testing.
   */
  private StandardVehicleController stdVehicleController;

  @Before
  public void setUp() {
    vehicle = dataObjectFactory.createVehicle();
    commAdapter = mock(CommunicationAdapter.class);
    localKernel = mock(LocalKernel.class);

    doReturn(vehicle).when(localKernel).getTCSObject(Vehicle.class,
                                                     vehicle.getReference());
    doReturn(vehicle).when(localKernel).getTCSObject(Vehicle.class,
                                                     vehicle.getName());

    stdVehicleController = new StandardVehicleController(vehicle,
                                                         commAdapter,
                                                         localKernel,
                                                         new DummyScheduler(),
                                                         eventBus);
  }

  // Test cases for implementation of interface VehicleManager start here.
  @Test
  public void should_foward_position_change_to_kernel() {
    Point point = dataObjectFactory.createPoint();
    doReturn(point).when(localKernel).getTCSObject(Point.class, point.getName());

    stdVehicleController.setVehiclePosition(point.getName());

    verify(localKernel).setVehiclePosition(vehicle.getReference(),
                                           point.getReference());
  }

  @Test
  public void should_forward_precise_position_change_to_kernel() {
    Triple newPos = new Triple(211, 391, 0);
    stdVehicleController.setVehiclePrecisePosition(newPos);

    verify(localKernel).setVehiclePrecisePosition(vehicle.getReference(),
                                                  newPos);
  }

  @Test
  public void should_forward_angle_change_to_kernel() {
    double newAngle = 7.5;
    stdVehicleController.setVehicleOrientationAngle(newAngle);

    verify(localKernel).setVehicleOrientationAngle(vehicle.getReference(),
                                                   newAngle);
  }

  @Test
  public void should_forward_energy_level_change_to_kernel() {
    int newLevel = 80;
    stdVehicleController.setVehicleEnergyLevel(newLevel);
    verify(localKernel).setVehicleEnergyLevel(vehicle.getReference(),
                                              newLevel);
  }

  @Test
  public void should_forward_load_handling_devices_change_to_kernel() {
    List<LoadHandlingDevice> devices = new LinkedList<>();
    devices.add(new LoadHandlingDevice("MyLoadHandlingDevice", true));
    stdVehicleController.setVehicleLoadHandlingDevices(devices);

    verify(localKernel).setVehicleLoadHandlingDevices(vehicle.getReference(),
                                                      devices);
  }

  @Test
  public void should_forward_vehicle_state_change_to_kernel() {
    stdVehicleController.setVehicleState(Vehicle.State.EXECUTING);

    verify(localKernel).setVehicleState(vehicle.getReference(),
                                        Vehicle.State.EXECUTING);
  }

  @Test
  public void should_forward_adapter_state_change_to_kernel() {
    stdVehicleController.setAdapterState(CommunicationAdapter.State.UNKNOWN);

    verify(localKernel).setVehicleAdapterState(vehicle.getReference(),
                                               CommunicationAdapter.State.UNKNOWN);
  }

  @Test
  public void should_forward_event_to_bus() {
    final String adapterName = "myAdapter";
    final String eventString = "myString";
    final List<CommunicationAdapterEvent> eventsReceived = new LinkedList<>();
    Object eventHandler = new Object() {

      @Handler
      public void handleEvent(CommunicationAdapterEvent event) {
        eventsReceived.add(event);
      }
    };
    eventBus.subscribe(eventHandler);

    stdVehicleController.publishEvent(
        new CommunicationAdapterEvent(adapterName, eventString));

    assertEquals("Did not receive exactly one event", 1, eventsReceived.size());
    CommunicationAdapterEvent event = eventsReceived.get(0);
    assertEquals("Received event does not seem to be published event",
                 eventString,
                 event.getAppendix());
  }

  // Test cases for implementation of interface VehicleController start here.
  @Test
  public void should_have_idempotent_enabled_state() {
    stdVehicleController.enable();
    assertTrue(stdVehicleController.isEnabled());
    stdVehicleController.enable();
    assertTrue(stdVehicleController.isEnabled());
    stdVehicleController.disable();
    assertFalse(stdVehicleController.isEnabled());
    stdVehicleController.disable();
    assertFalse(stdVehicleController.isEnabled());
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
