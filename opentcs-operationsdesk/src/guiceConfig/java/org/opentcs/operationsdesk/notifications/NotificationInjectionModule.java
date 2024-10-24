// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.notifications;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import jakarta.inject.Singleton;

/**
 * A Guice module for this package.
 */
public class NotificationInjectionModule
    extends
      AbstractModule {

  /**
   * Creates a new instance.
   */
  public NotificationInjectionModule() {
  }

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(UserNotificationViewFactory.class));

    bind(UserNotificationsContainer.class)
        .in(Singleton.class);
  }

}
