// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import jakarta.inject.Singleton;
import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.access.SslParameterSet;
import org.opentcs.common.LoggingScheduledThreadPoolExecutor;
import org.opentcs.components.kernel.ObjectNameProvider;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.InternalPeripheralJobService;
import org.opentcs.components.kernel.services.InternalPeripheralService;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.components.kernel.services.InternalQueryService;
import org.opentcs.components.kernel.services.InternalTCSObjectService;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.components.kernel.services.InternalVehicleService;
import org.opentcs.components.kernel.services.NotificationService;
import org.opentcs.components.kernel.services.PeripheralDispatcherService;
import org.opentcs.components.kernel.services.PeripheralJobService;
import org.opentcs.components.kernel.services.PeripheralService;
import org.opentcs.components.kernel.services.PlantModelService;
import org.opentcs.components.kernel.services.QueryService;
import org.opentcs.components.kernel.services.RouterService;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.components.kernel.services.TransportOrderService;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.customizations.ApplicationHome;
import org.opentcs.customizations.kernel.GlobalSyncObject;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.opentcs.drivers.peripherals.PeripheralControllerPool;
import org.opentcs.drivers.vehicle.VehicleControllerPool;
import org.opentcs.kernel.extensions.controlcenter.vehicles.AttachmentManager;
import org.opentcs.kernel.extensions.controlcenter.vehicles.VehicleEntryPool;
import org.opentcs.kernel.extensions.watchdog.Watchdog;
import org.opentcs.kernel.extensions.watchdog.WatchdogConfiguration;
import org.opentcs.kernel.peripherals.DefaultPeripheralControllerPool;
import org.opentcs.kernel.peripherals.LocalPeripheralControllerPool;
import org.opentcs.kernel.peripherals.PeripheralAttachmentManager;
import org.opentcs.kernel.peripherals.PeripheralCommAdapterRegistry;
import org.opentcs.kernel.peripherals.PeripheralControllerFactory;
import org.opentcs.kernel.peripherals.PeripheralEntryPool;
import org.opentcs.kernel.persistence.ModelPersister;
import org.opentcs.kernel.persistence.XMLFileModelPersister;
import org.opentcs.kernel.services.StandardDispatcherService;
import org.opentcs.kernel.services.StandardNotificationService;
import org.opentcs.kernel.services.StandardPeripheralDispatcherService;
import org.opentcs.kernel.services.StandardPeripheralJobService;
import org.opentcs.kernel.services.StandardPeripheralService;
import org.opentcs.kernel.services.StandardPlantModelService;
import org.opentcs.kernel.services.StandardQueryService;
import org.opentcs.kernel.services.StandardRouterService;
import org.opentcs.kernel.services.StandardTCSObjectService;
import org.opentcs.kernel.services.StandardTransportOrderService;
import org.opentcs.kernel.services.StandardVehicleService;
import org.opentcs.kernel.vehicles.DefaultVehicleControllerPool;
import org.opentcs.kernel.vehicles.LocalVehicleControllerPool;
import org.opentcs.kernel.vehicles.VehicleCommAdapterRegistry;
import org.opentcs.kernel.vehicles.VehicleControllerComponentsFactory;
import org.opentcs.kernel.vehicles.VehicleControllerFactory;
import org.opentcs.kernel.vehicles.VehiclePositionResolverConfiguration;
import org.opentcs.kernel.vehicles.transformers.CoordinateSystemMapperFactory;
import org.opentcs.kernel.vehicles.transformers.DefaultVehicleDataTransformerFactory;
import org.opentcs.kernel.workingset.CreationTimeThreshold;
import org.opentcs.kernel.workingset.NotificationBuffer;
import org.opentcs.kernel.workingset.PeripheralJobPoolManager;
import org.opentcs.kernel.workingset.PlantModelManager;
import org.opentcs.kernel.workingset.PrefixedUlidObjectNameProvider;
import org.opentcs.kernel.workingset.TCSObjectManager;
import org.opentcs.kernel.workingset.TCSObjectRepository;
import org.opentcs.kernel.workingset.TransportOrderPoolManager;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.event.SimpleEventBus;
import org.opentcs.util.logging.UncaughtExceptionLogger;

/**
 * A Guice module for the openTCS kernel application.
 */
public class DefaultKernelInjectionModule
    extends
      KernelInjectionModule {

  /**
   * Creates a new instance.
   */
  public DefaultKernelInjectionModule() {
  }

  @Override
  @SuppressWarnings("deprecation")
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
        .annotatedWith(GlobalSyncObject.class)
        .to(Object.class)
        .in(Singleton.class);

    // The kernel's data pool structures.
    bind(TCSObjectRepository.class).in(Singleton.class);
    bind(TCSObjectManager.class).in(Singleton.class);
    bind(PlantModelManager.class).in(Singleton.class);
    bind(TransportOrderPoolManager.class).in(Singleton.class);
    bind(PeripheralJobPoolManager.class).in(Singleton.class);
    bind(NotificationBuffer.class).in(Singleton.class);

    bind(ObjectNameProvider.class)
        .to(PrefixedUlidObjectNameProvider.class)
        .in(Singleton.class);

    configurePersistence();

    bind(VehicleCommAdapterRegistry.class)
        .in(Singleton.class);

    configureVehicleControllers();

    bind(AttachmentManager.class)
        .in(Singleton.class);
    bind(VehicleEntryPool.class)
        .in(Singleton.class);

    configurePeripheralControllers();

    bind(PeripheralCommAdapterRegistry.class)
        .in(Singleton.class);
    bind(PeripheralAttachmentManager.class)
        .in(Singleton.class);
    bind(PeripheralEntryPool.class)
        .in(Singleton.class);

    bind(StandardKernel.class)
        .in(Singleton.class);
    bind(LocalKernel.class)
        .to(StandardKernel.class);

    vehicleDataTransformersBinder().addBinding().to(DefaultVehicleDataTransformerFactory.class);
    // tag::documentation_registerTransformerFactory[]
    vehicleDataTransformersBinder().addBinding().to(CoordinateSystemMapperFactory.class);
    // end::documentation_registerTransformerFactory[]

    configureKernelStatesDependencies();
    configureKernelStarterDependencies();
    configureSslParameters();
    configureKernelServicesDependencies();

    // Ensure all of these binders are initialized.
    extensionsBinderAllModes();
    extensionsBinderModelling();
    extensionsBinderOperating();
    vehicleCommAdaptersBinder();
    peripheralCommAdaptersBinder();

    configureWatchdogExtension();
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
    bind(InternalTCSObjectService.class).to(StandardTCSObjectService.class);

    bind(StandardNotificationService.class).in(Singleton.class);
    bind(NotificationService.class).to(StandardNotificationService.class);

    bind(StandardRouterService.class).in(Singleton.class);
    bind(RouterService.class).to(StandardRouterService.class);

    bind(StandardDispatcherService.class).in(Singleton.class);
    bind(DispatcherService.class).to(StandardDispatcherService.class);

    bind(StandardQueryService.class).in(Singleton.class);
    bind(QueryService.class).to(StandardQueryService.class);
    bind(InternalQueryService.class).to(StandardQueryService.class);

    bind(StandardPeripheralService.class).in(Singleton.class);
    bind(PeripheralService.class).to(StandardPeripheralService.class);
    bind(InternalPeripheralService.class).to(StandardPeripheralService.class);

    bind(StandardPeripheralJobService.class).in(Singleton.class);
    bind(PeripheralJobService.class).to(StandardPeripheralJobService.class);
    bind(InternalPeripheralJobService.class).to(StandardPeripheralJobService.class);

    bind(StandardPeripheralDispatcherService.class).in(Singleton.class);
    bind(PeripheralDispatcherService.class).to(StandardPeripheralDispatcherService.class);
  }

  private void configureVehicleControllers() {
    install(new FactoryModuleBuilder().build(VehicleControllerFactory.class));
    install(new FactoryModuleBuilder().build(VehicleControllerComponentsFactory.class));

    bind(DefaultVehicleControllerPool.class)
        .in(Singleton.class);
    bind(VehicleControllerPool.class)
        .to(DefaultVehicleControllerPool.class);
    bind(LocalVehicleControllerPool.class)
        .to(DefaultVehicleControllerPool.class);
    bind(VehiclePositionResolverConfiguration.class)
        .toInstance(
            getConfigBindingProvider().get(
                VehiclePositionResolverConfiguration.PREFIX,
                VehiclePositionResolverConfiguration.class
            )
        );
  }

  private void configurePeripheralControllers() {
    install(new FactoryModuleBuilder().build(PeripheralControllerFactory.class));

    bind(DefaultPeripheralControllerPool.class)
        .in(Singleton.class);
    bind(PeripheralControllerPool.class)
        .to(DefaultPeripheralControllerPool.class);
    bind(LocalPeripheralControllerPool.class)
        .to(DefaultPeripheralControllerPool.class);
  }

  private void configurePersistence() {
    bind(ModelPersister.class).to(XMLFileModelPersister.class);
  }

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
  }

  private void configureKernelStatesDependencies() {
    // A map for KernelState instances to be provided at runtime.
    MapBinder<Kernel.State, KernelState> stateMapBinder
        = MapBinder.newMapBinder(binder(), Kernel.State.class, KernelState.class);
    stateMapBinder.addBinding(Kernel.State.SHUTDOWN).to(KernelStateShutdown.class);
    stateMapBinder.addBinding(Kernel.State.MODELLING).to(KernelStateModelling.class);
    stateMapBinder.addBinding(Kernel.State.OPERATING).to(KernelStateOperating.class);

    bind(OrderPoolConfiguration.class)
        .toInstance(
            getConfigBindingProvider().get(
                OrderPoolConfiguration.PREFIX,
                OrderPoolConfiguration.class
            )
        );

    bind(CreationTimeThreshold.class)
        .in(Singleton.class);

    transportOrderCleanupApprovalBinder();
    orderSequenceCleanupApprovalBinder();
    peripheralJobCleanupApprovalBinder();
  }

  private void configureKernelStarterDependencies() {
    bind(KernelApplicationConfiguration.class)
        .toInstance(
            getConfigBindingProvider().get(
                KernelApplicationConfiguration.PREFIX,
                KernelApplicationConfiguration.class
            )
        );
  }

  private void configureSslParameters() {
    SslConfiguration configuration
        = getConfigBindingProvider().get(
            SslConfiguration.PREFIX,
            SslConfiguration.class
        );
    SslParameterSet sslParamSet = new SslParameterSet(
        SslParameterSet.DEFAULT_KEYSTORE_TYPE,
        new File(configuration.keystoreFile()),
        configuration.keystorePassword(),
        new File(configuration.truststoreFile()),
        configuration.truststorePassword()
    );
    bind(SslParameterSet.class).toInstance(sslParamSet);
  }

  private void configureKernelExecutor() {
    ScheduledExecutorService executor
        = new LoggingScheduledThreadPoolExecutor(
            1,
            runnable -> {
              Thread thread = new Thread(runnable, "kernelExecutor");
              thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(false));
              return thread;
            }
        );
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

  private void configureWatchdogExtension() {
    extensionsBinderOperating().addBinding()
        .to(Watchdog.class)
        .in(Singleton.class);

    bind(WatchdogConfiguration.class)
        .toInstance(
            getConfigBindingProvider().get(
                WatchdogConfiguration.PREFIX,
                WatchdogConfiguration.class
            )
        );

  }
}
