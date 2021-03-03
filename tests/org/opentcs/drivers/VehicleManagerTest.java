/*
 *
 * Created on 23.03.2012 09:53:07
 */
package org.opentcs.drivers;

import java.util.LinkedList;
import java.util.List;
import org.junit.*;
import static org.mockito.Mockito.*;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.kernel.module.scheduling.DummyScheduler;
import org.opentcs.kernel.vehicles.StandardVehicleController;

/**
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VehicleManagerTest {

  /**
   * A name for a vehicle.
   */
  private static final String A_VEHICLE_NAME = "MyVehicle";
  /**
   * A name for a point.
   */
  private static final String A_POINT_NAME = "SomePointName";
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
   * The VehicleManager we're testing.
   */
  private VehicleManager vehicleManager;

  @Before
  public void setUp() {
    vehicle = new Vehicle(1, A_VEHICLE_NAME);
    commAdapter = mock(CommunicationAdapter.class);
    localKernel = mock(LocalKernel.class);

    doReturn(new DummyScheduler()).when(localKernel).getScheduler();
    doReturn(vehicle).when(localKernel).getTCSObject(Vehicle.class,
                                                     vehicle.getReference());
    doReturn(vehicle).when(localKernel).getTCSObject(Vehicle.class,
                                                     vehicle.getName());

    vehicleManager = new StandardVehicleController(vehicle, commAdapter, localKernel);
  }

  @After
  public void tearDown() {
    vehicle = null;
    commAdapter = null;
    localKernel = null;
    vehicleManager = null;
  }

  @Test
  public void testSetVehiclePositionResultsInCallToKernel() {
    Point point = new Point(1, A_POINT_NAME);
    doReturn(point).when(localKernel).getTCSObject(Point.class, A_POINT_NAME);

    vehicleManager.setVehiclePosition(A_POINT_NAME);

    verify(localKernel, times(1)).setVehiclePosition(vehicle.getReference(),
                                                     point.getReference());
  }

  @Test
  public void testSetVehiclePrecisePosition() {
    Triple newPos = new Triple(211, 391, 0);
    vehicleManager.setVehiclePrecisePosition(newPos);

    verify(localKernel, times(1)).setVehiclePrecisePosition(vehicle.getReference(),
                                                            newPos);
  }

  @Test
  public void testSetVehicleOrientationAngle() {
    double newAngle = 7.5;
    vehicleManager.setVehicleOrientationAngle(newAngle);

    verify(localKernel, times(1)).setVehicleOrientationAngle(vehicle.getReference(),
                                                             newAngle);
  }

  @Test
  public void testSetVehicleEnergyLevel() {
    int newLevel = 80;
    vehicleManager.setVehicleEnergyLevel(newLevel);
    verify(localKernel, times(1)).setVehicleEnergyLevel(vehicle.getReference(),
                                                        newLevel);
  }

  @Test
  public void testSetVehicleLoadHandlingDevices() {
    List<LoadHandlingDevice> devices = new LinkedList<>();
    devices.add(new LoadHandlingDevice("MyLoadHandlingDevice", true));
    vehicleManager.setVehicleLoadHandlingDevices(devices);

    verify(localKernel, times(1)).setVehicleLoadHandlingDevices(vehicle.getReference(),
                                                                devices);
  }

  @Test
  public void testSetVehicleState() {
    vehicleManager.setVehicleState(Vehicle.State.EXECUTING);

    verify(localKernel, times(1)).setVehicleState(vehicle.getReference(),
                                                  Vehicle.State.EXECUTING);
  }

  @Test
  public void testSetAdapterState() {
    vehicleManager.setAdapterState(CommunicationAdapter.State.UNKNOWN);

    verify(localKernel, times(1)).setVehicleAdapterState(vehicle.getReference(),
                                                         CommunicationAdapter.State.UNKNOWN);
  }
}
