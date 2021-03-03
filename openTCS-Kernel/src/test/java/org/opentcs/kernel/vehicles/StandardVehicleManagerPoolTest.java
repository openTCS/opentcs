package org.opentcs.kernel.vehicles;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import org.junit.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.CommunicationAdapter;
import org.opentcs.drivers.VehicleManager;
import org.opentcs.drivers.VehicleManagerPool;

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
    vehManagerPool = new StandardVehicleManagerPool(
        localKernel, new MockedVehicleManagerFactory());
  }

  @Test
  public void should_return_same_object_for_equivalent_calls() {
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

  public void should_return_different_object_after_detaching() {
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

  /**
   * A factory delivering vehicle manager mocks.
   */
  private static class MockedVehicleManagerFactory
      implements VehicleManagerFactory {

    @Override
    public StandardVehicleController createStandardVehicleController(
        Vehicle vehicle,
        CommunicationAdapter commAdapter) {
      return mock(StandardVehicleController.class);
    }
  }
}
