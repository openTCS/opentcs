/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import java.io.File;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Singleton;
import org.opentcs.access.ApplicationHome;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.algorithms.KernelExtension;
import org.opentcs.kernel.OrderCleanerTask.OrderSweepType;
import org.opentcs.kernel.controlcenter.KernelControlCenter;
import org.opentcs.kernel.module.ModuleInjectionModule;
import org.opentcs.kernel.persistence.ModelPersister;
import org.opentcs.kernel.persistence.OrderPersister;
import org.opentcs.kernel.persistence.XMLFileModelPersister;
import org.opentcs.kernel.persistence.XMLFileOrderPersister;
import org.opentcs.kernel.workingset.MessageBuffer;
import org.opentcs.kernel.workingset.Model;
import org.opentcs.kernel.workingset.TCSObjectPool;
import org.opentcs.kernel.workingset.TransportOrderPool;
import org.opentcs.kernel.xmlorders.XMLOrderInjectionModule;
import org.opentcs.kernel.xmlstatus.XMLStatusInjectionModule;
import org.opentcs.util.configuration.ConfigurationStore;
import org.opentcs.util.eventsystem.CentralEventHub;
import org.opentcs.util.eventsystem.EventHub;
import org.opentcs.util.eventsystem.EventListener;
import org.opentcs.util.eventsystem.EventSource;
import org.opentcs.util.eventsystem.SynchronousEventHub;
import org.opentcs.util.eventsystem.TCSEvent;
import org.opentcs.util.statistics.StatisticsCollector;

/**
 * A Guice module for the openTCS kernel application.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class KernelInjectionModule
    extends AbstractModule {

  private static final Logger log
      = Logger.getLogger(KernelInjectionModule.class.getName());

  @Override
  protected void configure() {
    bind(File.class)
        .annotatedWith(ApplicationHome.class)
        .toInstance(new File(System.getProperty("opentcs.home", ".")));

    // A single global synchronization object for the kernel.
    bind(Object.class)
        .annotatedWith(GlobalKernelSync.class)
        .to(Object.class)
        .in(Singleton.class);

    bind(TCSObjectPool.class).in(Singleton.class);
    bind(Model.class).in(Singleton.class);
    bind(TransportOrderPool.class).in(Singleton.class);
    bind(MessageBuffer.class).in(Singleton.class);

    // A binding for the kernel's one and only central event hub.
    SynchronousEventHub<TCSEvent> kernelEventHub = new SynchronousEventHub<>();
    bind(new TypeLiteral<EventListener<TCSEvent>>() {
    })
        .annotatedWith(CentralEventHub.class)
        .toInstance(kernelEventHub);
    bind(new TypeLiteral<EventSource<TCSEvent>>() {
    })
        .annotatedWith(CentralEventHub.class)
        .toInstance(kernelEventHub);
    bind(new TypeLiteral<EventHub<TCSEvent>>() {
    })
        .annotatedWith(CentralEventHub.class)
        .toInstance(kernelEventHub);

    bind(ModelPersister.class).to(XMLFileModelPersister.class);
    bind(OrderPersister.class).to(XMLFileOrderPersister.class);

    install(new ModuleInjectionModule());

    bind(LocalKernel.class)
        .to(StandardKernel.class)
        .in(Singleton.class);

    configureOrderCleanerTask();
    configureKernelStates();
    configureKernelExtensions();
  }

  private void configureKernelStates() {
    // A map for KernelState instances to be provided at runtime.
    MapBinder<Kernel.State, KernelState> stateMapBinder
        = MapBinder.newMapBinder(binder(), Kernel.State.class, KernelState.class);
    stateMapBinder.addBinding(Kernel.State.SHUTDOWN).to(KernelStateShutdown.class);
    stateMapBinder.addBinding(Kernel.State.MODELLING).to(KernelStateModelling.class);
    stateMapBinder.addBinding(Kernel.State.OPERATING).to(KernelStateOperating.class);
  }

  private void configureKernelExtensions() {
    configureStandardRemoteKernel();
    // Kernel extensions for StandardKernel (permanent extensions)
    Multibinder<KernelExtension> permanentExtBinder
        = Multibinder.newSetBinder(binder(),
                                   KernelExtension.class,
                                   KernelExtension.Permanent.class);
    permanentExtBinder.addBinding()
        .to(KernelControlCenter.class)
        .in(Singleton.class);
    permanentExtBinder.addBinding()
        .to(StandardRemoteKernel.class)
        .in(Singleton.class);
    install(new XMLStatusInjectionModule());

    // Kernel extensions for KernelStateModelling
    Multibinder<KernelExtension> modellingExtBinder
        = Multibinder.newSetBinder(binder(),
                                   KernelExtension.class,
                                   KernelExtension.Modelling.class);
    // No extensions for modelling mode, yet.

    // Kernel extensions for KernelStateOperating
    Multibinder<KernelExtension> operatingExtBinder
        = Multibinder.newSetBinder(binder(),
                                   KernelExtension.class,
                                   KernelExtension.Operating.class);
    install(new XMLOrderInjectionModule());
    operatingExtBinder.addBinding().to(StatisticsCollector.class);
  }

  private void configureOrderCleanerTask() {
    ConfigurationStore cleanerConfigStore
        = ConfigurationStore.getStore(OrderCleanerTask.class.getName());
    String configuredSweepType = cleanerConfigStore.getEnum("orderSweepType",
                                                            "BY_AMOUNT",
                                                            OrderSweepType.class);

    long orderSweepInterval = cleanerConfigStore.getLong("orderSweepInterval",
                                                         10 * 60 * 1000);
    bindConstant()
        .annotatedWith(OrderCleanerTask.SweepInterval.class)
        .to(orderSweepInterval);

    OrderSweepType sweepType;
    try {
      sweepType = OrderSweepType.valueOf(configuredSweepType);
    }
    catch (IllegalArgumentException exc) {
      log.log(Level.WARNING, "Illegal sweep type, using BY_AMOUNT", exc);
      sweepType = OrderSweepType.BY_AMOUNT;
    }
    switch (sweepType) {
      case BY_AGE:
        bindConstant()
            .annotatedWith(OrderCleanerTaskByAge.SweepAge.class)
            .to(cleanerConfigStore.getInt("orderSweepAge", 10 * 60 * 6 * 1000));
        bind(OrderCleanerTask.class).to(OrderCleanerTaskByAge.class);
        break;
      default:
        bindConstant()
            .annotatedWith(OrderCleanerTaskByAmount.SweepThreshold.class)
            .to(cleanerConfigStore.getInt("orderSweepThreshold", 200));
        bind(OrderCleanerTask.class).to(OrderCleanerTaskByAmount.class);
    }
  }

  private void configureStandardRemoteKernel() throws NumberFormatException {
    // Configuration for StandardRemoteKernel
    ConfigurationStore rmiConfigStore
        = ConfigurationStore.getStore(StandardRemoteKernel.class.getName());
    long sweepInterval = rmiConfigStore.getLong("sweepInterval", 5 * 60 * 1000);
    bindConstant()
        .annotatedWith(StandardRemoteKernel.ClientSweepInterval.class)
        .to(sweepInterval);

    String registryHost = rmiConfigStore.getString("rmiRegistryHost",
                                                   "localhost");
    int registryPort = rmiConfigStore.getInt("rmiRegistryPort",
                                             Registry.REGISTRY_PORT);
    bind(StandardRemoteKernel.RegistryAddress.class)
        .toInstance(new StandardRemoteKernel.RegistryAddress(registryHost,
                                                             registryPort));
  }
}
