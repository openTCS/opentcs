/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.xmlstatus;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import javax.inject.Singleton;
import org.opentcs.algorithms.KernelExtension;
import org.opentcs.util.configuration.ConfigurationStore;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class XMLStatusInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder<KernelExtension> permanentExtBinder
        = Multibinder.newSetBinder(binder(),
                                   KernelExtension.class,
                                   KernelExtension.Permanent.class);
    ConfigurationStore xmlStatusConfigStore
        = ConfigurationStore.getStore(StatusMessageDispatcher.class.getName());
    bindConstant()
        .annotatedWith(StatusMessageDispatcher.ListenPort.class)
        .to(xmlStatusConfigStore.getInt("listenPort", 44444));
    bindConstant()
        .annotatedWith(StatusMessageDispatcher.MessageSeparator.class)
        .to(xmlStatusConfigStore.getString("messageSeparator", "|"));

    permanentExtBinder.addBinding()
        .to(StatusMessageDispatcher.class)
        .in(Singleton.class);
  }
}
