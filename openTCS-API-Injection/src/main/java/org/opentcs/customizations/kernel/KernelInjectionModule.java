/*
 * openTCS copyright information:
 * Copyright (c) 2016 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.customizations.kernel;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import org.opentcs.components.kernel.ControlCenterPanel;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.components.kernel.ParkingPositionSupplier;
import org.opentcs.components.kernel.RechargePositionSupplier;
import org.opentcs.components.kernel.RecoveryEvaluator;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;

/**
 * A base class for Guice modules adding or customizing bindings for the kernel application.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class KernelInjectionModule
    extends AbstractModule {

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
   * Sets the parking position supplier implementation to be used.
   *
   * @param clazz The implementation.
   */
  protected void bindParkingPositionSupplier(Class<? extends ParkingPositionSupplier> clazz) {
    bind(ParkingPositionSupplier.class).to(clazz).in(Singleton.class);
  }

  /**
   * Sets the recharge position supplier implementation to be used.
   *
   * @param clazz The implementation.
   */
  protected void bindRechargePositionSupplier(Class<? extends RechargePositionSupplier> clazz) {
    bind(RechargePositionSupplier.class).to(clazz).in(Singleton.class);
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
   * Sets the recovery evaluator implementation to be used.
   *
   * @param clazz The implementation.
   */
  protected void bindRecoveryEvaluator(Class<? extends RecoveryEvaluator> clazz) {
    bind(RecoveryEvaluator.class).to(clazz).in(Singleton.class);
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
   * Returns a multibinder that can be used to register {@link ControlCenterPanel} implementations
   * for the kernel's modelling mode.
   *
   * @return The multibinder.
   */
  protected Multibinder<ControlCenterPanel> controlCenterPanelBinderModelling() {
    return Multibinder.newSetBinder(binder(),
                                    ControlCenterPanel.class,
                                    ActiveInModellingMode.class);
  }

  /**
   * Returns a multibinder that can be used to register {@link ControlCenterPanel} implementations
   * for the kernel's operating mode.
   *
   * @return The multibinder.
   */
  protected Multibinder<ControlCenterPanel> controlCenterPanelBinderOperating() {
    return Multibinder.newSetBinder(binder(),
                                    ControlCenterPanel.class,
                                    ActiveInOperatingMode.class);
  }

  /**
   * Returns a multibinder that can be used to register vehicle communication adapter factories.
   *
   * @return The multibinder.
   */
  protected Multibinder<VehicleCommAdapterFactory> vehicleCommAdaptersBinder() {
    return Multibinder.newSetBinder(binder(), VehicleCommAdapterFactory.class);
  }
}
