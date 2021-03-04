/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing;

import java.util.HashSet;
import java.util.Set;
import org.junit.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.invocation.InvocationOnMock;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.Vehicle;

/**
 * Test cases for the {@link DefaultRouter}.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public class DefaultRouterTest {

  /**
   * The vehicles which are returned when asking the kernel for vehicles.
   */
  private final Set<Vehicle> vehicles = new HashSet<>();

  /**
   * The class to test.
   */
  private Router router;

  /**
   * The mocked object service to use.
   */
  private TCSObjectService objectService;

  /**
   * The builder for routing tables.
   */
  private PointRouterFactory builder;

  /**
   * The configuration.
   */
  private DefaultRouterConfiguration configuration;

  @Before
  public void setUp() {
    objectService = mock(TCSObjectService.class);
    builder = mock(PointRouterFactory.class);
    when(objectService.fetchObjects(Vehicle.class)).thenReturn(vehicles);
    when(objectService.fetchObject(eq(Vehicle.class), anyString()))
        .then(o -> vehicles.stream()
        .filter(t -> filterByName(o, t))
        .findFirst().orElse(null));
    configuration = mock(DefaultRouterConfiguration.class);
    when(configuration.routeToCurrentPosition()).thenReturn(false);
    router = spy(createRouter());
  }

  @Test
  public void shouldUseDefaultRoutingGroup() {
    createVehicle("Vehicle-000", -1);
    createVehicle("Vehicle-001", -1);
    createVehicle("Vehicle-002", -1);
    router.initialize();

    verify(builder, times(1)).createPointRouter(any());
  }

  @Test
  public void shouldUseDefinedRoutingGroup() {
    createVehicle("Vehicle-000", 1);
    createVehicle("Vehicle-001", 1);
    createVehicle("Vehicle-002", 1);
    router.initialize();

    verify(builder, times(1)).createPointRouter(any());
  }

  @Test
  public void shouldUseDefaultAndSetRoutingGroups() {
    createVehicle("Vehicle-000", 1);
    createVehicle("Vehicle-001", 1);
    createVehicle("Vehicle-002", -1);
    router.initialize();

    verify(builder, times(2)).createPointRouter(any());
  }

  @Test
  public void shouldUseSetRoutingGroups() {
    for (int x = 0; x < 15; x++) {
      vehicles.add(createVehicle("Vehicle-0" + x, x));
    }
    router.initialize();

    verify(builder, times(15)).createPointRouter(any());
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
   * Creates the router with mocked routing table builder to return a new routing table on each
   * request.
   *
   * @return The router
   */
  @SuppressWarnings("unchecked")
  private Router createRouter() {
    when(builder.createPointRouter(any())).thenReturn(mock(PointRouter.class));

    return new DefaultRouter(objectService, builder, configuration);
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
