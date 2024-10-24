// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.rmi.services;

import java.rmi.RemoteException;
import java.util.List;
import java.util.function.Predicate;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.components.kernel.services.NotificationService;
import org.opentcs.data.notification.UserNotification;

/**
 * The default implementation of the notification service.
 * Delegates method invocations to the corresponding remote service.
 */
class RemoteNotificationServiceProxy
    extends
      AbstractRemoteServiceProxy<RemoteNotificationService>
    implements
      NotificationService {

  /**
   * Creates a new instance.
   */
  RemoteNotificationServiceProxy() {
  }

  @Override
  public List<UserNotification> fetchUserNotifications(Predicate<UserNotification> predicate)
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().fetchUserNotifications(getClientId(), predicate);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void publishUserNotification(UserNotification notification)
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().publishUserNotification(getClientId(), notification);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }
}
