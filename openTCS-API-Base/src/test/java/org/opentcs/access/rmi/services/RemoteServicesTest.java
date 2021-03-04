/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.services;

import java.lang.reflect.Method;
import static java.util.Objects.requireNonNull;
import org.junit.*;
import static org.junit.Assert.fail;
import org.opentcs.access.rmi.ClientID;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.NotificationService;
import org.opentcs.components.kernel.services.PlantModelService;
import org.opentcs.components.kernel.services.RouterService;
import org.opentcs.components.kernel.services.SchedulerService;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.components.kernel.services.TransportOrderService;
import org.opentcs.components.kernel.services.VehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the remote service interfaces.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class RemoteServicesTest {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RemoteServicesTest.class);

  @Test
  public void shouldMapAllMethodsInServiceInterfaces() {
    checkMapping(DispatcherService.class, RemoteDispatcherService.class);
    checkMapping(NotificationService.class, RemoteNotificationService.class);
    checkMapping(PlantModelService.class, RemotePlantModelService.class);
    checkMapping(RouterService.class, RemoteRouterService.class);
    checkMapping(SchedulerService.class, RemoteSchedulerService.class);
    checkMapping(TCSObjectService.class, RemoteTCSObjectService.class);
    checkMapping(TransportOrderService.class, RemoteTransportOrderService.class);
    checkMapping(VehicleService.class, RemoteVehicleService.class);
  }

  private void checkMapping(Class<?> serviceInterface, Class<?> remoteServiceInterface) {
    for (Method method : serviceInterface.getDeclaredMethods()) {
      try {
        Method remoteMethod = getRemoteServiceMethod(remoteServiceInterface, method);
        LOG.debug("Found {} corresponding to {}", remoteMethod, method);
      }
      catch (NoSuchMethodException exc) {
        fail("Did not find corresponding method for: " + method);
      }
    }
  }

  private static Method getRemoteServiceMethod(Class<?> remoteServiceInterface, Method method)
      throws NoSuchMethodException {
    requireNonNull(method, "method");

    Class<?>[] paramTypes = method.getParameterTypes();
    Class<?>[] extParamTypes = new Class<?>[paramTypes.length + 1];
    // We're looking for a method with the same parameter types as the called one, but with an 
    // additional client ID as the first parameter.
    extParamTypes[0] = ClientID.class;
    System.arraycopy(paramTypes, 0, extParamTypes, 1, paramTypes.length);
    return remoteServiceInterface.getMethod(method.getName(), extParamTypes);
  }
}
