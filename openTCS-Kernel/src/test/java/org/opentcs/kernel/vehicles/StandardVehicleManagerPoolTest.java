package org.opentcs.kernel.vehicles;

import org.junit.*;
import static org.mockito.Mockito.mock;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

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
  private VehicleCommAdapter commAdapter;
  /**
   * The VehicleManagerpool we're testing
   */
  private LocalVehicleControllerPool vehManagerPool;

  @Before
  public void setUp() {
    localKernel = mock(LocalKernel.class);
    commAdapter = mock(VehicleCommAdapter.class);
    vehManagerPool = new DefaultVehicleControllerPool(
        localKernel, new MockedVehicleManagerFactory());
  }

  @Test(expected = NullPointerException.class)
  public void testThrowsNPEIfVehicleNameIsNull() {
    vehManagerPool.attachVehicleController(null, commAdapter);
  }

  @Test(expected = NullPointerException.class)
  public void testThrowsNPEIfCommAdapterIsNull() {
    vehManagerPool.attachVehicleController(A_VEHICLE_NAME, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testThrowsExceptionForUnknownVehicleName() {
    vehManagerPool.attachVehicleController(UNKNOWN_VEHICLE_NAME, commAdapter);
  }

  @Test(expected = NullPointerException.class)
  public void testThrowsNPEIfDetachingNullVehicleName() {
    vehManagerPool.detachVehicleController(null);
  }

  /**
   * A factory delivering vehicle manager mocks.
   */
  private static class MockedVehicleManagerFactory
      implements VehicleControllerFactory {

    @Override
    public DefaultVehicleController createVehicleController(Vehicle vehicle,
                                                     VehicleCommAdapter commAdapter) {
      return mock(DefaultVehicleController.class);
    }
  }
}
