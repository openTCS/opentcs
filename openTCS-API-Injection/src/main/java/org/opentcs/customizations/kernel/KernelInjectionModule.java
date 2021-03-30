/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.customizations.kernel;

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.components.kernel.OrderSequenceCleanupApproval;
import org.opentcs.components.kernel.PeripheralJobDispatcher;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.components.kernel.TransportOrderCleanupApproval;
import org.opentcs.components.kernel.routing.EdgeEvaluator;
import org.opentcs.customizations.ConfigurableInjectionModule;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterFactory;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;

/**
 * A base class for Guice modules adding or customizing bindings for the kernel application.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class KernelInjectionModule
    extends ConfigurableInjectionModule {

  /**
   * Sets the scheduler implementation to be used.
   *
   * @param clazz The implementation.
   */
  protected void bindScheduler(Class<? extends Scheduler> clazz) {
    bind(Scheduler.class).to(clazz).in(Singleton.class);
  }

  /**
   * Sets the router implementation to be used.
   *
   * @param clazz The implementation.
   */
  protected void bindRouter(Class<? extends Router> clazz) {
    bind(Router.class).to(clazz).in(Singleton.class);
  }

  /**
   * Sets the dispatcher implementation to be used.
   *
   * @param clazz The implementation.
   */
  protected void bindDispatcher(Class<? extends Dispatcher> clazz) {
    bind(Dispatcher.class).to(clazz).in(Singleton.class);
  }

  /**
   * Sets the peripheral job dispatcher implementation to be used.
   *
   * @param clazz The implementation.
   */
  protected void bindPeripheralJobDispatcher(Class<? extends PeripheralJobDispatcher> clazz) {
    bind(PeripheralJobDispatcher.class).to(clazz).in(Singleton.class);
  }

  /**
   * Returns a multibinder that can be used to register kernel extensions for all kernel states.
   *
   * @return The multibinder.
   */
  protected Multibinder<KernelExtension> extensionsBinderAllModes() {
    return Multibinder.newSetBinder(binder(), KernelExtension.class, ActiveInAllModes.class);
  }

  /**
   * Returns a multibinder that can be used to register kernel extensions for the kernel's modelling
   * state.
   *
   * @return The multibinder.
   */
  protected Multibinder<KernelExtension> extensionsBinderModelling() {
    return Multibinder.newSetBinder(binder(), KernelExtension.class, ActiveInModellingMode.class);
  }

  /**
   * Returns a multibinder that can be used to register kernel extensions for the kernel's operating
   * state.
   *
   * @return The multibinder.
   */
  protected Multibinder<KernelExtension> extensionsBinderOperating() {
    return Multibinder.newSetBinder(binder(), KernelExtension.class, ActiveInOperatingMode.class);
  }

  /**
   * Returns a multibinder that can be used to register vehicle communication adapter factories.
   *
   * @return The multibinder.
   */
  protected Multibinder<VehicleCommAdapterFactory> vehicleCommAdaptersBinder() {
    return Multibinder.newSetBinder(binder(), VehicleCommAdapterFactory.class);
  }

  /**
   * Returns a multibinder that can be used to register peripheral communication adapter factories.
   *
   * @return The multibinder.
   */
  protected Multibinder<PeripheralCommAdapterFactory> peripheralCommAdaptersBinder() {
    return Multibinder.newSetBinder(binder(), PeripheralCommAdapterFactory.class);
  }

  /**
   * Returns a multibinder that can be used to register transport order cleanup approvals.
   *
   * @return The multibinder.
   */
  protected Multibinder<TransportOrderCleanupApproval> transportOrderCleanupApprovalBinder() {
    return Multibinder.newSetBinder(binder(), TransportOrderCleanupApproval.class);
  }

  /**
   * Returns a multibinder that can be used to register order sequence cleanup approvals.
   *
   * @return The multibinder.
   */
  protected Multibinder<OrderSequenceCleanupApproval> orderSequenceCleanupApprovalBinder() {
    return Multibinder.newSetBinder(binder(), OrderSequenceCleanupApproval.class);
  }

  /**
   * Returns a multibinder that can be used to register scheduler modules.
   *
   * @return The multibinder.
   */
  protected Multibinder<Scheduler.Module> schedulerModuleBinder() {
    return Multibinder.newSetBinder(binder(), Scheduler.Module.class);
  }

  /**
   * Returns a mapbinder that can be used to register edge evaluators.
   *
   * @return The mapbinder.
   */
  protected MapBinder<String, EdgeEvaluator> edgeEvaluatorBinder() {
    return MapBinder.newMapBinder(binder(),
                                  TypeLiteral.get(String.class),
                                  TypeLiteral.get(EdgeEvaluator.class));
  }
}
