package org.opentcs.guing.plugins.panels.allocation;

import com.google.common.collect.Maps;
import org.junit.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.opentcs.access.Kernel;
import org.opentcs.access.SharedKernelClient;
import org.opentcs.access.queries.QuerySchedulerAllocations;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.exchange.ApplicationKernelClient;
import org.opentcs.guing.exchange.ApplicationKernelProvider;

/**
 *
 * @author Mats Wilhelm
 */
public class ResourceAllocationPanelTest {

  /**
   * The provider for the kernel.
   */
  private ApplicationKernelProvider kernelProvider;

  /**
   * If the connection to the kernel is established.
   */
  private boolean kernelConnected;

  /**
   * The kernel to use.
   */
  private Kernel kernel;

  /**
   * The class to test.
   */
  private ResourceAllocationPanel panel;

  @Before
  public void setUp() {
    kernelConnected = true;
    kernelProvider = mock(ApplicationKernelProvider.class);
    kernel = mock(Kernel.class);
    when(kernelProvider.kernelShared()).thenAnswer((o) -> kernelConnected);
    SharedKernelClient kernelClient = new ApplicationKernelClient(kernel,
                                                                  kernelProvider,
                                                                  new Object());
    when(kernelProvider.register()).thenReturn(kernelClient);
    panel = spy(new ResourceAllocationPanel(kernelProvider));
    panel.initialize();
    panel.allocationTable.setModel(spy(panel.allocationTable.getModel()));
  }

  @After
  public void tearDown() {
    kernel = null;
    kernelConnected = false;
    panel.terminate();
  }

  @Test
  public void testNoKernelConnection() {
    kernelConnected = false;
    Vehicle vehicle = createVehicle("Vehicle-000");

    panel.processEvent(new TCSObjectEvent(vehicle, vehicle, TCSObjectEvent.Type.OBJECT_MODIFIED));

    verify(kernel, never()).query(any());
  }

  @Test
  public void testWrongKernelMode() {
    when(kernel.getState()).thenReturn(Kernel.State.MODELLING);
    Vehicle vehicle = createVehicle("Vehicle-000");

    panel.processEvent(new TCSObjectEvent(vehicle, vehicle, TCSObjectEvent.Type.OBJECT_MODIFIED));

    verify(kernel, never()).query(any());
  }

  @Test
  public void testKernelQueryNull() {
    when(kernel.getState()).thenReturn(Kernel.State.OPERATING);
    Vehicle vehicle = createVehicle("Vehicle-000")
        .withCurrentPosition(createPoint("Point-000").getReference());
    Vehicle vehicle2 = createVehicle("Vehicle-000")
        .withCurrentPosition(createPoint("Point-001").getReference());

    panel.processEvent(new TCSObjectEvent(vehicle, vehicle2, TCSObjectEvent.Type.OBJECT_MODIFIED));

    verify(kernel, times(1)).query(any());
    verify((AllocationTreeModel) panel.allocationTable.getModel(), times(0))
        .updateAllocations(any());
  }

  @Test
  public void testVehicleUpdateProcessed() {
    when(kernel.getState()).thenReturn(Kernel.State.OPERATING);
    when(kernel.query(any())).thenReturn(new QuerySchedulerAllocations(Maps.newHashMap()));
    Vehicle vehicle = createVehicle("Vehicle-000")
        .withCurrentPosition(createPoint("Point-000").getReference());
    Vehicle vehicle2 = createVehicle("Vehicle-000")
        .withCurrentPosition(createPoint("Point-001").getReference());

    panel.processEvent(new TCSObjectEvent(vehicle, vehicle2, TCSObjectEvent.Type.OBJECT_MODIFIED));

    verify(kernel, times(1)).query(any());
    verify((AllocationTreeModel) panel.allocationTable.getModel(), times(1))
        .updateAllocations(any());
  }

  @Test
  public void testVehicleCreated() {
    when(kernel.getState()).thenReturn(Kernel.State.OPERATING);
    when(kernel.query(any())).thenReturn(new QuerySchedulerAllocations(Maps.newHashMap()));
    Vehicle vehicle = createVehicle("Vehicle-000")
        .withCurrentPosition(createPoint("Point-000").getReference());

    panel.processEvent(new TCSObjectEvent(vehicle, null, TCSObjectEvent.Type.OBJECT_CREATED));

    verify(kernel, times(1)).query(any());
    verify((AllocationTreeModel) panel.allocationTable.getModel(), times(1))
        .updateAllocations(any());
  }

  @Test
  public void testVehicleRemoved() {
    when(kernel.getState()).thenReturn(Kernel.State.OPERATING);
    when(kernel.query(any())).thenReturn(new QuerySchedulerAllocations(Maps.newHashMap()));
    Vehicle vehicle = createVehicle("Vehicle-000")
        .withCurrentPosition(createPoint("Point-000").getReference());

    panel.processEvent(new TCSObjectEvent(null, vehicle, TCSObjectEvent.Type.OBJECT_REMOVED));

    verify(kernel, times(1)).query(any());
    verify((AllocationTreeModel) panel.allocationTable.getModel(), times(1))
        .updateAllocations(any());
  }

  /**
   * Creates a vehicle with the given parameters.
   *
   * @param name The vehicle name
   * @return The vehicle
   */
  private Vehicle createVehicle(String name) {
    Vehicle vehicle = new Vehicle(name);

    return vehicle;
  }

  /**
   * Creates a point with the given parameters.
   *
   * @param name The point name
   * @return The point
   */
  private Point createPoint(String name) {
    Point point = new Point(name);

    return point;
  }
}
