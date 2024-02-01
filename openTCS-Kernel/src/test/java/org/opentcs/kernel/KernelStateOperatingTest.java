/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import org.opentcs.kernel.workingset.WorkingSetCleanupTask;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import javax.annotation.Nonnull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.components.kernel.PeripheralJobDispatcher;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.components.kernel.services.InternalVehicleService;
import org.opentcs.data.model.Vehicle;
import org.opentcs.kernel.extensions.controlcenter.vehicles.AttachmentManager;
import org.opentcs.kernel.peripherals.LocalPeripheralControllerPool;
import org.opentcs.kernel.peripherals.PeripheralAttachmentManager;
import org.opentcs.kernel.persistence.ModelPersister;
import org.opentcs.kernel.vehicles.LocalVehicleControllerPool;
import org.opentcs.kernel.workingset.PlantModelManager;
import org.opentcs.kernel.workingset.PeripheralJobPoolManager;
import org.opentcs.kernel.workingset.PrefixedUlidObjectNameProvider;
import org.opentcs.kernel.workingset.TCSObjectRepository;
import org.opentcs.kernel.workingset.TransportOrderPoolManager;
import org.opentcs.util.event.SimpleEventBus;

/**
 * Tests the operating state of the kernel.
 */
class KernelStateOperatingTest {

  private final Set<Vehicle> vehicles = new HashSet<>();

  private int objectID;

  private KernelState operating;

  private KernelApplicationConfiguration configuration;

  private TCSObjectRepository objectPool;

  private Router router;

  private Scheduler scheduler;

  private Dispatcher dispatcher;

  private PeripheralJobDispatcher peripheralJobDispatcher;

  private LocalVehicleControllerPool controllerPool;

  private AttachmentManager attachmentManager;

  private InternalVehicleService vehicleService;

  @BeforeEach
  void setUp() {
    objectID = 0;
    objectPool = mock(TCSObjectRepository.class);
    configuration = mock(KernelApplicationConfiguration.class);
    router = mock(Router.class);
    scheduler = mock(Scheduler.class);
    dispatcher = mock(Dispatcher.class);
    peripheralJobDispatcher = mock(PeripheralJobDispatcher.class);
    controllerPool = mock(LocalVehicleControllerPool.class);
    attachmentManager = mock(AttachmentManager.class);
    vehicleService = mock(InternalVehicleService.class);
    when(vehicleService.fetchObjects(Vehicle.class)).thenReturn(vehicles);
  }

  @Test
  void shouldInitializeExtensionsAndComponents() {
    KernelExtension extension = mock(KernelExtension.class);
    operating = createKernel(Collections.singleton(extension));
    operating.initialize();
    verify(router, times(1)).initialize();
    verify(scheduler, times(1)).initialize();
    verify(dispatcher, times(1)).initialize();
    verify(peripheralJobDispatcher, times(1)).initialize();
    verify(controllerPool, times(1)).initialize();
    verify(extension, times(1)).initialize();
  }

  @Test
  void shouldTerminateExtensionsAndComponents() {
    KernelExtension extension = mock(KernelExtension.class);
    operating = createKernel(Collections.singleton(extension));
    operating.initialize();
    operating.terminate();
    verify(router, times(1)).terminate();
    verify(scheduler, times(1)).terminate();
    verify(dispatcher, times(1)).terminate();
    verify(peripheralJobDispatcher, times(1)).terminate();
    verify(controllerPool, times(1)).terminate();
    verify(extension, times(1)).terminate();
  }

  @Test
  void initializeKernelWithVehiclesAsUnavailable() {
    Vehicle vehicle = new Vehicle("Vehicle-" + objectID++);
    vehicles.add(vehicle);
    operating = createKernel(new HashSet<>());
    operating.initialize();
    verify(vehicleService, times(1)).updateVehicleProcState(vehicle.getReference(),
                                                            Vehicle.ProcState.IDLE);
    verify(vehicleService, times(1)).updateVehicleState(vehicle.getReference(),
                                                        Vehicle.State.UNKNOWN);
    verify(vehicleService, times(1)).updateVehicleTransportOrder(vehicle.getReference(), null);
    verify(vehicleService, times(1)).updateVehicleOrderSequence(vehicle.getReference(), null);
  }

  @Test
  void terminateKernelWithVehiclesAsUnavailable() {
    Vehicle vehicle = new Vehicle("Vehicle-" + objectID++);
    vehicles.add(vehicle);
    operating = createKernel(new HashSet<>());
    operating.initialize();
    operating.terminate();
    verify(vehicleService, times(2)).updateVehicleProcState(vehicle.getReference(),
                                                            Vehicle.ProcState.IDLE);
    verify(vehicleService, times(2)).updateVehicleState(vehicle.getReference(),
                                                        Vehicle.State.UNKNOWN);
    verify(vehicleService, times(2)).updateVehicleTransportOrder(vehicle.getReference(), null);
    verify(vehicleService, times(2)).updateVehicleOrderSequence(vehicle.getReference(), null);
  }

  /**
   * Creates the kernel to test.
   *
   * @param extensions The kernel extensions
   * @return The kernel to test
   */
  @SuppressWarnings("unchecked")
  private KernelStateOperating createKernel(@Nonnull Set<KernelExtension> extensions) {
    ScheduledExecutorService executorMock = mock(ScheduledExecutorService.class);
    when(executorMock.scheduleAtFixedRate(any(), anyLong(), anyLong(), any()))
        .thenReturn(mock(ScheduledFuture.class));

    return spy(new KernelStateOperating(
            new Object(),
            mock(PlantModelManager.class),
            new TransportOrderPoolManager(objectPool,
                                          new SimpleEventBus(),
                                          new PrefixedUlidObjectNameProvider()),
            new PeripheralJobPoolManager(objectPool,
                                         new SimpleEventBus(),
                                         new PrefixedUlidObjectNameProvider()),
            mock(ModelPersister.class),
            configuration,
            router,
            scheduler,
            dispatcher,
            peripheralJobDispatcher,
            controllerPool,
            mock(LocalPeripheralControllerPool.class),
            executorMock,
            mock(WorkingSetCleanupTask.class),
            extensions,
            attachmentManager,
            mock(PeripheralAttachmentManager.class),
            vehicleService,
            mock(PathLockEventListener.class),
            mock(VehicleDispatchTrigger.class)
        )
    );
  }
}
