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
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Singleton;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.access.rmi.factories.AnonSslSocketFactoryProvider;
import org.opentcs.access.rmi.factories.NullSocketFactoryProvider;
import org.opentcs.access.rmi.factories.SocketFactoryProvider;
import org.opentcs.access.rmi.factories.SslSocketFactoryProvider;
import org.opentcs.common.LoggingScheduledThreadPoolExecutor;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.components.kernel.services.InternalVehicleService;
import org.opentcs.components.kernel.services.NotificationService;
import org.opentcs.components.kernel.services.PlantModelService;
import org.opentcs.components.kernel.services.RouterService;
import org.opentcs.components.kernel.services.SchedulerService;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.components.kernel.services.TransportOrderService;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.customizations.ApplicationHome;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.opentcs.drivers.vehicle.VehicleControllerPool;
import org.opentcs.kernel.extensions.controlcenter.vehicles.AttachmentManager;
import org.opentcs.kernel.extensions.controlcenter.vehicles.VehicleEntryPool;
import org.opentcs.kernel.extensions.rmi.KernelRemoteService;
import org.opentcs.kernel.extensions.rmi.RmiKernelInterfaceConfiguration;
import static org.opentcs.kernel.extensions.rmi.RmiKernelInterfaceConfiguration.ConnectionEncryption.NONE;
import org.opentcs.kernel.extensions.rmi.StandardRemoteDispatcherService;
import org.opentcs.kernel.extensions.rmi.StandardRemoteNotificationService;
import org.opentcs.kernel.extensions.rmi.StandardRemotePlantModelService;
import org.opentcs.kernel.extensions.rmi.StandardRemoteRouterService;
import org.opentcs.kernel.extensions.rmi.StandardRemoteSchedulerService;
import org.opentcs.kernel.extensions.rmi.StandardRemoteTransportOrderService;
import org.opentcs.kernel.extensions.rmi.StandardRemoteVehicleService;
import org.opentcs.kernel.extensions.rmi.UserManager;
import org.opentcs.kernel.persistence.ModelPersister;
import org.opentcs.kernel.persistence.XMLFileModelPersister;
import org.opentcs.kernel.persistence.XMLModel002Builder;
import org.opentcs.kernel.persistence.XMLModelReader;
import org.opentcs.kernel.persistence.XMLModelWriter;
import org.opentcs.kernel.services.StandardDispatcherService;
import org.opentcs.kernel.services.StandardNotificationService;
import org.opentcs.kernel.services.StandardPlantModelService;
import org.opentcs.kernel.services.StandardRouterService;
import org.opentcs.kernel.services.StandardSchedulerService;
import org.opentcs.kernel.services.StandardTCSObjectService;
import org.opentcs.kernel.services.StandardTransportOrderService;
import org.opentcs.kernel.services.StandardVehicleService;
import org.opentcs.kernel.util.RegistryProvider;
import org.opentcs.kernel.vehicles.DefaultVehicleControllerPool;
import org.opentcs.kernel.vehicles.LocalVehicleControllerPool;
import org.opentcs.kernel.vehicles.VehicleCommAdapterRegistry;
import org.opentcs.kernel.vehicles.VehicleControllerFactory;
import org.opentcs.kernel.vehicles.VehiclesConfiguration;
import org.opentcs.kernel.workingset.Model;
import org.opentcs.kernel.workingset.NotificationBuffer;
import org.opentcs.kernel.workingset.TCSObjectPool;
import org.opentcs.kernel.workingset.TransportOrderPool;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.event.SimpleEventBus;
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

  @Override
  protected void configure() {
    configureEventHub();
    configureKernelExecutor();

    // Ensure that the application's home directory can be used everywhere.
    File applicationHome = new File(System.getProperty("opentcs.home", "."));
    bind(File.class)
        .annotatedWith(ApplicationHome.class)
        .toInstance(applicationHome);

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

    bind(UserManager.class)
        .in(Singleton.class);
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
    configureStandardRemoteKernelDependencies(applicationHome);
    configureKernelServicesDependencies();
  }

  private void configureKernelServicesDependencies() {
    bind(StandardPlantModelService.class).in(Singleton.class);
    bind(PlantModelService.class).to(StandardPlantModelService.class);
    bind(InternalPlantModelService.class).to(StandardPlantModelService.class);

    bind(StandardTransportOrderService.class).in(Singleton.class);
    bind(TransportOrderService.class).to(StandardTransportOrderService.class);
    bind(InternalTransportOrderService.class).to(StandardTransportOrderService.class);

    bind(StandardVehicleService.class).in(Singleton.class);
    bind(VehicleService.class).to(StandardVehicleService.class);
    bind(InternalVehicleService.class).to(StandardVehicleService.class);

    bind(StandardTCSObjectService.class).in(Singleton.class);
    bind(TCSObjectService.class).to(StandardTCSObjectService.class);

    bind(StandardNotificationService.class).in(Singleton.class);
    bind(NotificationService.class).to(StandardNotificationService.class);

    bind(StandardRouterService.class).in(Singleton.class);
    bind(RouterService.class).to(StandardRouterService.class);

    bind(StandardDispatcherService.class).in(Singleton.class);
    bind(DispatcherService.class).to(StandardDispatcherService.class);

    bind(StandardSchedulerService.class).in(Singleton.class);
    bind(SchedulerService.class).to(StandardSchedulerService.class);

    Multibinder<KernelRemoteService> remoteServices
        = Multibinder.newSetBinder(binder(), KernelRemoteService.class);
    remoteServices.addBinding().to(StandardRemotePlantModelService.class);
    remoteServices.addBinding().to(StandardRemoteTransportOrderService.class);
    remoteServices.addBinding().to(StandardRemoteVehicleService.class);
    remoteServices.addBinding().to(StandardRemoteNotificationService.class);
    remoteServices.addBinding().to(StandardRemoteRouterService.class);
    remoteServices.addBinding().to(StandardRemoteDispatcherService.class);
    remoteServices.addBinding().to(StandardRemoteSchedulerService.class);
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

  @SuppressWarnings("deprecation")
  private void configureEventHub() {
    EventBus newEventBus = new SimpleEventBus();
    bind(EventHandler.class)
        .annotatedWith(ApplicationEventBus.class)
        .toInstance(newEventBus);
    bind(org.opentcs.util.event.EventSource.class)
        .annotatedWith(ApplicationEventBus.class)
        .toInstance(newEventBus);
    bind(EventBus.class)
        .annotatedWith(ApplicationEventBus.class)
        .toInstance(newEventBus);

    // A binding for the kernel's one and only central event hub.
    BusBackedEventHub<org.opentcs.util.eventsystem.TCSEvent> busBackedHub
        = new BusBackedEventHub<>(newEventBus, org.opentcs.util.eventsystem.TCSEvent.class);
    busBackedHub.initialize();
    bind(new TypeLiteral<org.opentcs.util.eventsystem.EventListener<org.opentcs.util.eventsystem.TCSEvent>>() {
    })
        .annotatedWith(org.opentcs.customizations.kernel.CentralEventHub.class)
        .toInstance(busBackedHub);
    bind(new TypeLiteral<org.opentcs.util.eventsystem.EventSource<org.opentcs.util.eventsystem.TCSEvent>>() {
    })
        .annotatedWith(org.opentcs.customizations.kernel.CentralEventHub.class)
        .toInstance(busBackedHub);
    bind(new TypeLiteral<org.opentcs.util.eventsystem.EventHub<org.opentcs.util.eventsystem.TCSEvent>>() {
    })
        .annotatedWith(org.opentcs.customizations.kernel.CentralEventHub.class)
        .toInstance(busBackedHub);
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

  private void configureStandardRemoteKernelDependencies(File applicationHome) {
    bind(RegistryProvider.class).in(Singleton.class);

    RmiKernelInterfaceConfiguration configuration
        = getConfigBindingProvider().get(RmiKernelInterfaceConfiguration.PREFIX,
                                         RmiKernelInterfaceConfiguration.class);
    bind(RmiKernelInterfaceConfiguration.class)
        .toInstance(configuration);

    SocketFactoryProvider socketFactoryProvider;
    switch (configuration.connectionEncryption()) {
      case NONE:
        socketFactoryProvider = new NullSocketFactoryProvider();
        break;
      case SSL_UNTRUSTED:
        socketFactoryProvider = new AnonSslSocketFactoryProvider();
        break;
      case SSL:
        socketFactoryProvider = new SslSocketFactoryProvider(applicationHome.getPath(),
                                                             configuration.keystorePassword(),
                                                             configuration.truststorePassword());
        break;
      default:
        LOG.warn("No implementation for '{}' encryption, falling back to '{}'.",
                 configuration.connectionEncryption().name(),
                 NONE.name());
        socketFactoryProvider = new NullSocketFactoryProvider();
    }
    bind(SocketFactoryProvider.class).toInstance(socketFactoryProvider);
  }

  private void configureKernelExecutor() {
    ScheduledExecutorService executor = new LoggingScheduledThreadPoolExecutor(
        1, runnable -> new Thread(runnable, "kernelExecutor"));
    bind(ScheduledExecutorService.class)
        .annotatedWith(KernelExecutor.class)
        .toInstance(executor);
    bind(ExecutorService.class)
        .annotatedWith(KernelExecutor.class)
        .toInstance(executor);
    bind(Executor.class)
        .annotatedWith(KernelExecutor.class)
        .toInstance(executor);
  }
}
