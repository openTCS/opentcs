/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import java.io.File;
import javax.inject.Singleton;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.customizations.ApplicationHome;
import org.opentcs.customizations.kernel.CentralEventHub;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.opentcs.drivers.vehicle.VehicleControllerPool;
import org.opentcs.kernel.controlcenter.vehicles.AttachmentManager;
import org.opentcs.kernel.controlcenter.vehicles.VehicleEntryPool;
import org.opentcs.kernel.persistence.ModelPersister;
import org.opentcs.kernel.persistence.XMLFileModelPersister;
import org.opentcs.kernel.persistence.XMLModel002Builder;
import org.opentcs.kernel.persistence.XMLModelReader;
import org.opentcs.kernel.persistence.XMLModelWriter;
import org.opentcs.kernel.vehicles.DefaultVehicleControllerPool;
import org.opentcs.kernel.vehicles.LocalVehicleControllerPool;
import org.opentcs.kernel.vehicles.VehicleCommAdapterRegistry;
import org.opentcs.kernel.vehicles.VehicleControllerFactory;
import org.opentcs.kernel.vehicles.VehiclesConfiguration;
import org.opentcs.kernel.workingset.Model;
import org.opentcs.kernel.workingset.NotificationBuffer;
import org.opentcs.kernel.workingset.TCSObjectPool;
import org.opentcs.kernel.workingset.TransportOrderPool;
import org.opentcs.util.eventsystem.EventHub;
import org.opentcs.util.eventsystem.EventListener;
import org.opentcs.util.eventsystem.EventSource;
import org.opentcs.util.eventsystem.SynchronousEventHub;
import org.opentcs.util.eventsystem.TCSEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Guice module for the openTCS kernel application.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultKernelInjectionModule
    extends KernelInjectionModule {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultKernelInjectionModule.class);
  /**
   * The kernel application's event bus.
   */
  private final MBassador<Object> eventBus = new MBassador<>(BusConfiguration.Default());

  @Override
  protected void configure() {
    configureEventBus();
    configureEventHub();

    // Ensure that the application's home directory can be used everywhere.
    bind(File.class)
        .annotatedWith(ApplicationHome.class)
        .toInstance(new File(System.getProperty("opentcs.home", ".")));

    // A single global synchronization object for the kernel.
    bind(Object.class)
        .annotatedWith(GlobalKernelSync.class)
        .to(Object.class)
        .in(Singleton.class);

    // The kernel's data pool structures.
    bind(TCSObjectPool.class).in(Singleton.class);
    bind(Model.class).in(Singleton.class);
    bind(TransportOrderPool.class).in(Singleton.class);
    bind(NotificationBuffer.class).in(Singleton.class);

    configurePersistence();

    bind(VehicleCommAdapterRegistry.class)
        .in(Singleton.class);

    configureVehicleControllers();

    bind(AttachmentManager.class)
        .in(Singleton.class);
    bind(VehicleEntryPool.class)
        .in(Singleton.class);

    bind(StandardKernel.class)
        .in(Singleton.class);
    bind(LocalKernel.class)
        .to(StandardKernel.class);

    configureKernelStatesDependencies();
    configureKernelStarterDependencies();
    configureStandardRemoteKernelDependencies();
  }

  private void configureVehicleControllers() {
    bind(VehiclesConfiguration.class)
        .toInstance(getConfigBindingProvider().get(VehiclesConfiguration.PREFIX,
                                                   VehiclesConfiguration.class));

    install(new FactoryModuleBuilder().build(VehicleControllerFactory.class));

    bind(DefaultVehicleControllerPool.class)
        .in(Singleton.class);
    bind(VehicleControllerPool.class)
        .to(DefaultVehicleControllerPool.class);
    bind(LocalVehicleControllerPool.class)
        .to(DefaultVehicleControllerPool.class);
  }

  private void configurePersistence() {
    bind(ModelPersister.class).to(XMLFileModelPersister.class);
    bind(XMLModelReader.class).to(XMLModel002Builder.class);
    bind(XMLModelWriter.class).to(XMLModel002Builder.class);
  }

  private void configureEventBus() {
    // Bind global event bus and automatically register every created object.
    bind(new TypeLiteral<MBassador<Object>>() {
    })
        .toInstance(eventBus);
    bindListener(Matchers.any(), new TypeListener() {
               @Override
               public <I> void hear(TypeLiteral<I> typeLiteral,
                                    TypeEncounter<I> typeEncounter) {
                 typeEncounter.register(new InjectionListener<I>() {
                   @Override
                   public void afterInjection(I i) {
                     eventBus.subscribe(i);
                   }
                 });
               }
             });
    eventBus.addErrorHandler((error) -> {
      LOG.warn("Event handler caused an error", error.getCause());
    });
  }

  private void configureEventHub() {
    // A binding for the kernel's one and only central event hub.
    SynchronousEventHub<TCSEvent> kernelEventHub
        = new BusBackedEventHub<>(eventBus, TCSEvent.class);
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
  }

  private void configureKernelStatesDependencies() {
    // A map for KernelState instances to be provided at runtime.
    MapBinder<Kernel.State, KernelState> stateMapBinder
        = MapBinder.newMapBinder(binder(), Kernel.State.class, KernelState.class);
    stateMapBinder.addBinding(Kernel.State.SHUTDOWN).to(KernelStateShutdown.class);
    stateMapBinder.addBinding(Kernel.State.MODELLING).to(KernelStateModelling.class);
    stateMapBinder.addBinding(Kernel.State.OPERATING).to(KernelStateOperating.class);

    bind(OrderPoolConfiguration.class)
        .toInstance(getConfigBindingProvider().get(OrderPoolConfiguration.PREFIX,
                                                   OrderPoolConfiguration.class));

    transportOrderCleanupApprovalBinder();
    orderSequenceCleanupApprovalBinder();
  }

  private void configureKernelStarterDependencies() {
    bind(KernelApplicationConfiguration.class)
        .toInstance(getConfigBindingProvider().get(KernelApplicationConfiguration.PREFIX,
                                                   KernelApplicationConfiguration.class));
  }

  private void configureStandardRemoteKernelDependencies() {
    bind(RmiKernelInterfaceConfiguration.class)
        .toInstance(getConfigBindingProvider().get(RmiKernelInterfaceConfiguration.PREFIX,
                                                   RmiKernelInterfaceConfiguration.class));
  }
}
