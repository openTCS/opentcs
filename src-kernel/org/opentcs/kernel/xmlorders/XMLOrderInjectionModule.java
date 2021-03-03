/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.xmlorders;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.opentcs.algorithms.KernelExtension;
import org.opentcs.util.configuration.ConfigurationStore;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class XMLOrderInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder<KernelExtension> operatingExtBinder
        = Multibinder.newSetBinder(binder(),
                                   KernelExtension.class,
                                   KernelExtension.Operating.class);
    ConfigurationStore xmlOrderConfigStore
        = ConfigurationStore.getStore(XMLTelegramOrderReceiver.class.getName());
    bindConstant()
        .annotatedWith(XMLTelegramOrderReceiver.ListenPort.class)
        .to(xmlOrderConfigStore.getInt("listenPort", 55555));
    bindConstant()
        .annotatedWith(XMLTelegramOrderReceiver.InputTimeout.class)
        .to(xmlOrderConfigStore.getInt("inputTimeout", 10000));
    bindConstant()
        .annotatedWith(XMLTelegramOrderReceiver.MaxInputLength.class)
        .to(xmlOrderConfigStore.getInt("maxInputLength", 100 * 1024));
    
    operatingExtBinder.addBinding().to(XMLTelegramOrderReceiver.class);
  }
}
