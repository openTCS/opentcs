package org.opentcs.guing.plugins.panels.allocation;

import com.google.common.collect.Maps;
import org.junit.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.SchedulerAllocationState;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.components.kernel.services.NotificationService;
import org.opentcs.components.kernel.services.SchedulerService;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.exchange.ApplicationPortal;
import org.opentcs.guing.exchange.ApplicationPortalProvider;
import org.opentcs.util.event.EventSource;
import org.opentcs.util.event.SimpleEventBus;

/**
 *
 * @author Mats Wilhelm
 */
public class ResourceAllocationPanelTest {

  /**
   * The provider for the portal.
   */
  private ApplicationPortalProvider portalProvider;
  /**
   * The mocked event hub.
   */
  private EventSource eventHub;
  /**
   * If the connection to the kernel is established.
   */
  private boolean portalConnected;
  /**
   * The portal to use.
   */
  private KernelServicePortal portal;
  /**
   * The scheduler service to use.
   */
  private SchedulerService schedulerService;
  /**
   * The notification service to use.
   */
  private NotificationService notificationService;
  /**
   * The class to test.
   */
  private ResourceAllocationPanel panel;

  @Before
  public void setUp() {
    portalConnected = true;
    portalProvider = mock(ApplicationPortalProvider.class);
    portal = mock(KernelServicePortal.class);
    eventHub = new SimpleEventBus();
    schedulerService = mock(SchedulerService.class);
    when(portal.getSchedulerService()).thenReturn(schedulerService);
    notificationService = mock(NotificationService.class);
    when(portal.getNotificationService()).thenReturn(notificationService);
    when(portalProvider.portalShared()).thenAnswer((o) -> portalConnected);
    SharedKernelServicePortal sharedPortal = new ApplicationPortal(portal,
                                                                   portalProvider,
                                                                   new Object());
    when(portalProvider.register()).thenReturn(sharedPortal);
    panel = spy(new ResourceAllocationPanel(portalProvider, eventHub));
    panel.initialize();
    panel.allocationTable.setModel(spy(panel.allocationTable.getModel()));
  }

  @After
  public void tearDown() {
    portal = null;
    portalConnected = false;
    panel.terminate();
  }

  @Test
  public void testNoKernelConnection() {
    portalConnected = false;
    Vehicle vehicle = createVehicle("Vehicle-000");

    panel.onEvent(new TCSObjectEvent(vehicle, vehicle, TCSObjectEvent.Type.OBJECT_MODIFIED));

    verify(portal.getSchedulerService(), times(1)).fetchSchedulerAllocations();
  }

  @Test
  public void testWrongKernelMode() {
    when(portal.getState()).thenReturn(Kernel.State.MODELLING);
    Vehicle vehicle = createVehicle("Vehicle-000");

    panel.onEvent(new TCSObjectEvent(vehicle, vehicle, TCSObjectEvent.Type.OBJECT_MODIFIED));

    verify(portal.getSchedulerService(), times(1)).fetchSchedulerAllocations();
  }

  @Test
  public void testKernelQueryNull() {
    when(portal.getState()).thenReturn(Kernel.State.OPERATING);
    Vehicle vehicle = createVehicle("Vehicle-000")
        .withCurrentPosition(createPoint("Point-000").getReference());
    Vehicle vehicle2 = createVehicle("Vehicle-000")
        .withCurrentPosition(createPoint("Point-001").getReference());

    panel.onEvent(new TCSObjectEvent(vehicle, vehicle2, TCSObjectEvent.Type.OBJECT_MODIFIED));

    verify(portal.getSchedulerService(), times(2)).fetchSchedulerAllocations();
    verify((AllocationTreeModel) panel.allocationTable.getModel(), times(0))
        .updateAllocations(any());
  }

  @Test
  public void testVehicleUpdateProcessed() {
    when(portal.getState()).thenReturn(Kernel.State.OPERATING);
    when(portal.getSchedulerService().fetchSchedulerAllocations()).thenReturn(new SchedulerAllocationState(Maps.newHashMap()));
    Vehicle vehicle = createVehicle("Vehicle-000")
        .withCurrentPosition(createPoint("Point-000").getReference());
    Vehicle vehicle2 = createVehicle("Vehicle-000")
        .withCurrentPosition(createPoint("Point-001").getReference());

    panel.onEvent(new TCSObjectEvent(vehicle, vehicle2, TCSObjectEvent.Type.OBJECT_MODIFIED));

    verify(portal.getSchedulerService(), times(2)).fetchSchedulerAllocations();
    verify((AllocationTreeModel) panel.allocationTable.getModel(), times(1))
        .updateAllocations(any());
  }

  @Test
  public void testVehicleCreated() {
    when(portal.getState()).thenReturn(Kernel.State.OPERATING);
    when(portal.getSchedulerService().fetchSchedulerAllocations()).thenReturn(new SchedulerAllocationState(Maps.newHashMap()));
    Vehicle vehicle = createVehicle("Vehicle-000")
        .withCurrentPosition(createPoint("Point-000").getReference());

    panel.onEvent(new TCSObjectEvent(vehicle, null, TCSObjectEvent.Type.OBJECT_CREATED));

    verify(portal.getSchedulerService(), times(2)).fetchSchedulerAllocations();
    verify((AllocationTreeModel) panel.allocationTable.getModel(), times(1))
        .updateAllocations(any());
  }

  @Test
  public void testVehicleRemoved() {
    when(portal.getState()).thenReturn(Kernel.State.OPERATING);
    when(portal.getSchedulerService().fetchSchedulerAllocations()).thenReturn(new SchedulerAllocationState(Maps.newHashMap()));
    Vehicle vehicle = createVehicle("Vehicle-000")
        .withCurrentPosition(createPoint("Point-000").getReference());

    panel.onEvent(new TCSObjectEvent(null, vehicle, TCSObjectEvent.Type.OBJECT_REMOVED));

    verify(portal.getSchedulerService(), times(2)).fetchSchedulerAllocations();
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
