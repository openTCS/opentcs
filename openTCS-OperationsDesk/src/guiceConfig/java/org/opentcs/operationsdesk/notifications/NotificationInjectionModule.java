/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.notifications;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import javax.inject.Singleton;

/**
 * A Guice module for this package.
 */
public class NotificationInjectionModule
    extends AbstractModule {

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
