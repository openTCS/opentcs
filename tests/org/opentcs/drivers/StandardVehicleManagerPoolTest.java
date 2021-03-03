/*
 *
 * Created on 23.03.2012 11:14:44
 */
package org.opentcs.drivers;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.model.Vehicle;
import org.opentcs.kernel.vehicles.StandardVehicleManagerPool;

/**
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StandardVehicleManagerPoolTest {

  /**
   * A name for a vehicle.
   */
  private static final String A_VEHICLE_NAME = "MyVehicle";
  /**
   * An name for a vehicle that does not exist.
   */
  private static final String UNKNOWN_VEHICLE_NAME = "SomeUnknownVehicle";
  /**
   * The (mocked) kernel.
   */
  private LocalKernel localKernel;
  /**
   * A (mocked) communication adapter.
   */
  private CommunicationAdapter commAdapter;
  /**
   * The VehicleManagerpool we're testing
   */
  private VehicleManagerPool vehManagerPool;

  @Before
  public void setUp() {
    localKernel = mock(LocalKernel.class);
    commAdapter = mock(CommunicationAdapter.class);
    vehManagerPool = new StandardVehicleManagerPool(localKernel);
  }

  @After
  public void tearDown() {
    vehManagerPool = null;
    commAdapter = null;
    localKernel = null;
  }

  @Test
  public void testThatManagerReturnsSameObjectForMultipleEquivalentCalls() {
    Vehicle vehicle = new Vehicle(1, A_VEHICLE_NAME);
    doReturn(vehicle).when(localKernel).getTCSObject(Vehicle.class,
                                                     A_VEHICLE_NAME);

    // Does the pool return the same VehicleManager for multiple equivalent calls?
    VehicleManager manager1 = vehManagerPool.getVehicleManager(A_VEHICLE_NAME,
                                                               commAdapter);
    VehicleManager manager2 = vehManagerPool.getVehicleManager(A_VEHICLE_NAME,
                                                               commAdapter);
    assertSame(manager1, manager2);
    verify(localKernel, atLeastOnce()).getTCSObject(Vehicle.class, A_VEHICLE_NAME);

  }

  public void testThatDetachingResultsInDifferentManager() {
    Vehicle vehicle = new Vehicle(1, A_VEHICLE_NAME);
    doReturn(vehicle).when(localKernel).getTCSObject(Vehicle.class,
                                                     A_VEHICLE_NAME);

    VehicleManager manager1 = vehManagerPool.getVehicleManager(A_VEHICLE_NAME,
                                                               commAdapter);
    // Does the pool return a different VehicleManager after detaching the
    // previous one?
    vehManagerPool.detachVehicleManager(A_VEHICLE_NAME);
    VehicleManager manager2 = vehManagerPool.getVehicleManager(A_VEHICLE_NAME,
                                                               commAdapter);
    assertNotSame(manager1, manager2);
  }

  @Test(expected = NullPointerException.class)
  public void testThrowsNPEIfVehicleNameIsNull() {
    VehicleManager manager = vehManagerPool.getVehicleManager(null, commAdapter);
  }

  @Test(expected = NullPointerException.class)
  public void testThrowsNPEIfCommAdapterIsNull() {
    VehicleManager manager = vehManagerPool.getVehicleManager(A_VEHICLE_NAME,
                                                              null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testThrowsExceptionForUnknownVehicleName() {
    VehicleManager manager = vehManagerPool.getVehicleManager(UNKNOWN_VEHICLE_NAME,
                                                              commAdapter);
  }

  @Test(expected = NullPointerException.class)
  public void testThrowsNPEIfDetachingNullVehicleName() {
    vehManagerPool.detachVehicleManager(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDetachingVehicleNameIsUnknown() {
    vehManagerPool.detachVehicleManager(UNKNOWN_VEHICLE_NAME);
  }
}
