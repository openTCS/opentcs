/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.invocation.InvocationOnMock;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.routing.GroupMapper;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.routing.DefaultRoutingGroupMapper;
import org.opentcs.strategies.basic.routing.PointRouter;
import org.opentcs.strategies.basic.routing.PointRouterFactory;
import org.opentcs.strategies.basic.routing.ResourceAvoidanceExtractor;
import org.opentcs.strategies.basic.routing.ResourceAvoidanceExtractor.ResourcesToAvoid;

/**
 * Tests for {@link PointRouterProvider}.
 */
public class PointRouterProviderTest {

  /**
   * The vehicles which are returned when asking the kernel for vehicles.
   */
  private final Set<Vehicle> vehicles = new HashSet<>();

  private TCSObjectService objectService;
  private ResourceAvoidanceExtractor resourceAvoidanceExtractor;
  private GroupMapper routingGroupMapper;
  private PointRouterFactory pointRouterFactory;
  private GraphProvider graphProvider;
  private PointRouterProvider pointRouterProvider;

  @BeforeEach
  public void setUp() {
    objectService = mock();
    when(objectService.fetchObjects(Vehicle.class)).thenReturn(vehicles);
    when(objectService.fetchObject(eq(Vehicle.class), anyString()))
        .then(o -> vehicles.stream()
        .filter(t -> filterByName(o, t))
        .findFirst().orElse(null));
    resourceAvoidanceExtractor = mock();
    when(resourceAvoidanceExtractor.extractResourcesToAvoid(nullable(TransportOrder.class)))
        .thenReturn(ResourcesToAvoid.EMPTY);
    when(resourceAvoidanceExtractor.extractResourcesToAvoid(anySet()))
        .thenReturn(ResourcesToAvoid.EMPTY);
    routingGroupMapper = new DefaultRoutingGroupMapper();
    pointRouterFactory = mock();
    when(pointRouterFactory.createPointRouter(any(Vehicle.class), anySet(), anySet()))
        .thenReturn(mock(PointRouter.class));
    graphProvider = mock();

    pointRouterProvider = new PointRouterProvider(objectService,
                                                  resourceAvoidanceExtractor,
                                                  routingGroupMapper,
                                                  pointRouterFactory,
                                                  graphProvider);
  }

  @Test
  void shouldProvidePointRouterForDefaultRoutingGroup() {
    pointRouterProvider.getPointRouterForVehicle(createVehicle("Vehicle-000", -1),
                                                 (TransportOrder) null);
    pointRouterProvider.getPointRouterForVehicle(createVehicle("Vehicle-001", -1),
                                                 (TransportOrder) null);
    pointRouterProvider.getPointRouterForVehicle(createVehicle("Vehicle-002", -1),
                                                 (TransportOrder) null);
    pointRouterProvider.getPointRouterForVehicle(createVehicle("Vehicle-003", -1),
                                                 Set.of());

    verify(pointRouterFactory, times(1)).createPointRouter(any(Vehicle.class), anySet(), anySet());
  }

  @Test
  void shouldProvidePointRouterForDefinedRoutingGroup() {
    pointRouterProvider.getPointRouterForVehicle(createVehicle("Vehicle-000", 1),
                                                 (TransportOrder) null);
    pointRouterProvider.getPointRouterForVehicle(createVehicle("Vehicle-001", 1),
                                                 (TransportOrder) null);
    pointRouterProvider.getPointRouterForVehicle(createVehicle("Vehicle-002", 1),
                                                 (TransportOrder) null);
    pointRouterProvider.getPointRouterForVehicle(createVehicle("Vehicle-003", 1),
                                                 Set.of());

    verify(pointRouterFactory, times(1)).createPointRouter(any(Vehicle.class), anySet(), anySet());
  }

  @Test
  void shouldProvidePointRouterForDefaultAndDefinedRoutingGroups() {
    pointRouterProvider.getPointRouterForVehicle(createVehicle("Vehicle-000", 1),
                                                 (TransportOrder) null);
    pointRouterProvider.getPointRouterForVehicle(createVehicle("Vehicle-001", 1),
                                                 (TransportOrder) null);
    pointRouterProvider.getPointRouterForVehicle(createVehicle("Vehicle-002", -1),
                                                 (TransportOrder) null);
    pointRouterProvider.getPointRouterForVehicle(createVehicle("Vehicle-003", 1),
                                                 Set.of());

    verify(pointRouterFactory, times(2)).createPointRouter(any(Vehicle.class), anySet(), anySet());
  }

  @Test
  void shouldProvideIndividualPointRouters() {
    for (int x = 0; x < 15; x++) {
      pointRouterProvider.getPointRouterForVehicle(
          createVehicle("Vehicle-0" + x, x), (TransportOrder) null);
    }

    verify(pointRouterFactory, times(15)).createPointRouter(any(Vehicle.class), anySet(), anySet());
  }

  /**
   * Creates a vehicle with a unique id, the given name and the given routing group.
   * If the routing group is negative no property will be added.
   * The vehicle will be added to the kernel objects.
   *
   * @param name The name of the vehicle
   * @param routingGroup The routing group of the vehicle
   * @return The vehicle
   */
  private Vehicle createVehicle(String name, int routingGroup) {
    Vehicle vehicle = new Vehicle(name);
    if (routingGroup >= 0) {
      vehicle = vehicle.withProperty(Router.PROPKEY_ROUTING_GROUP, String.valueOf(routingGroup));
    }
    vehicles.add(vehicle);

    return vehicle;
  }

  /**
   * Stream filter to check if the second argument of the invocation is equal to the object's name.
   *
   * @param mock The method call on the mock
   * @param object The kernel object to compare if the call wants this object
   * @return <code>true</code> if the objects name is equal to the second invocation argument
   */
  private boolean filterByName(InvocationOnMock mock, TCSObject<?> object) {
    Object arg1 = mock.getArguments()[1];
    //Here one can add different types for the getTCSObject method like TCSObjectReference or
    //TCSResourceReference
    if (arg1 instanceof String) {
      return filterByName((String) arg1, object);
    }
    return false;
  }

  /**
   * Stream filter to check if object has the given name.
   *
   * @param name The object name to check
   * @param object The kernel object to compare with the given name
   * @return <code>true</code> if the objects name is equal to the given name
   */
  private boolean filterByName(String name, TCSObject<?> object) {
    return name.equals(object.getName());
  }
}
