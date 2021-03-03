package org.opentcs.guing.plugins.panels.allocation;

import com.google.common.collect.Maps;
import org.junit.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.opentcs.access.Kernel;
import org.opentcs.access.SharedKernelProvider;
import org.opentcs.access.queries.QuerySchedulerAllocations;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 *
 * @author Mats Wilhelm
 */
public class ResourceAllocationPanelTest {

  /**
   * Provides a unique object id.
   */
  private int nextObjectId;

  /**
   * The provider for the kernel.
   */
  private SharedKernelProvider kernelProvider;

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
    kernelProvider = mock(SharedKernelProvider.class);
    kernel = mock(Kernel.class);
    when(kernelProvider.kernelShared()).thenAnswer((o) -> kernelConnected);
    when(kernelProvider.getKernel()).thenAnswer((o) -> kernelConnected ? kernel : null);
    panel = spy(new ResourceAllocationPanel(kernelProvider));
    panel.allocationTable.setModel(spy(panel.allocationTable.getModel()));
  }

  @After
  public void tearDown() {
    kernel = null;
    kernelConnected = false;
  }

  @Test
  public void testNoKernelConnection() {
    Vehicle vehicle = createVehicle("Vehicle-000");
    TCSObjectEvent event = new TCSObjectEvent(vehicle, vehicle, TCSObjectEvent.Type.OBJECT_MODIFIED);
    panel.processEvent(event);
    verify(kernel, times(0)).query(any());
  }

  @Test
  public void testWrongKernelMode() {
    kernelConnected = true;
    when(kernel.getState()).thenReturn(Kernel.State.MODELLING);
    Vehicle vehicle = createVehicle("Vehicle-000");
    TCSObjectEvent event = new TCSObjectEvent(vehicle, vehicle, TCSObjectEvent.Type.OBJECT_MODIFIED);
    panel.processEvent(event);
    verify(kernel, times(0)).query(any());
  }

  @Test
  public void testKernelQueryNull() {
    kernelConnected = true;
    when(kernel.getState()).thenReturn(Kernel.State.OPERATING);
    Vehicle vehicle = createVehicle("Vehicle-000");
    vehicle.setCurrentPosition(createPoint("Point-000").getReference());
    Vehicle vehicle2 = createVehicle("Vehicle-000");
    vehicle2.setCurrentPosition(createPoint("Point-001").getReference());
    TCSObjectEvent event = new TCSObjectEvent(vehicle, vehicle2, TCSObjectEvent.Type.OBJECT_MODIFIED);
    panel.processEvent(event);
    verify(kernel, times(1)).query(any());
    verify((AllocationTreeModel) panel.allocationTable.getModel(), times(0)).updateAllocations(any());
  }

  @Test
  public void testVehicleUpdateProcessed() {
    kernelConnected = true;
    when(kernel.getState()).thenReturn(Kernel.State.OPERATING);
    when(kernel.query(any())).thenReturn(new QuerySchedulerAllocations(Maps.newHashMap()));
    Vehicle vehicle = createVehicle("Vehicle-000");
    vehicle.setCurrentPosition(createPoint("Point-000").getReference());
    Vehicle vehicle2 = createVehicle("Vehicle-000");
    vehicle2.setCurrentPosition(createPoint("Point-001").getReference());
    TCSObjectEvent event = new TCSObjectEvent(vehicle, vehicle2, TCSObjectEvent.Type.OBJECT_MODIFIED);
    panel.processEvent(event);
    verify(kernel, times(1)).query(any());
    verify((AllocationTreeModel) panel.allocationTable.getModel(), times(1)).updateAllocations(any());
  }

  @Test
  public void testVehicleCreated() {
    kernelConnected = true;
    when(kernel.getState()).thenReturn(Kernel.State.OPERATING);
    when(kernel.query(any())).thenReturn(new QuerySchedulerAllocations(Maps.newHashMap()));
    Vehicle vehicle = createVehicle("Vehicle-000");
    vehicle.setCurrentPosition(createPoint("Point-000").getReference());
    TCSObjectEvent event = new TCSObjectEvent(vehicle, null, TCSObjectEvent.Type.OBJECT_CREATED);
    panel.processEvent(event);
    verify(kernel, times(1)).query(any());
    verify((AllocationTreeModel) panel.allocationTable.getModel(), times(1)).updateAllocations(any());
  }

  @Test
  public void testVehicleRemoved() {
    kernelConnected = true;
    when(kernel.getState()).thenReturn(Kernel.State.OPERATING);
    when(kernel.query(any())).thenReturn(new QuerySchedulerAllocations(Maps.newHashMap()));
    Vehicle vehicle = createVehicle("Vehicle-000");
    vehicle.setCurrentPosition(createPoint("Point-000").getReference());
    TCSObjectEvent event = new TCSObjectEvent(null, vehicle, TCSObjectEvent.Type.OBJECT_REMOVED);
    panel.processEvent(event);
    verify(kernel, times(1)).query(any());
    verify((AllocationTreeModel) panel.allocationTable.getModel(), times(1)).updateAllocations(any());
  }

  /**
   * Creates a vehicle with the given parameters.
   *
   * @param name The vehicle name
   * @return The vehicle
   */
  private Vehicle createVehicle(String name) {
    Vehicle vehicle = new Vehicle(nextObjectId++, name);

    return vehicle;
  }

  /**
   * Creates a point with the given parameters.
   *
   * @param name The point name
   * @return The point
   */
  private Point createPoint(String name) {
    Point point = new Point(nextObjectId++, name);

    return point;
  }
}
