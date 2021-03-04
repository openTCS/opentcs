/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.customizations.kernel;

import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import org.opentcs.components.kernel.ControlCenterPanel;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.components.kernel.OrderSequenceCleanupApproval;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.components.kernel.TransportOrderCleanupApproval;
import org.opentcs.customizations.ConfigurableInjectionModule;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;
import org.opentcs.util.annotations.ScheduledApiChange;

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
   * Sets the parking position supplier implementation to be used.
   *
   * @param clazz The implementation.
   * @deprecated Will be removed along with the deprecated supplier interface.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  protected void bindParkingPositionSupplier(
      Class<? extends org.opentcs.components.kernel.ParkingPositionSupplier> clazz) {
    bind(org.opentcs.components.kernel.ParkingPositionSupplier.class).to(clazz).in(Singleton.class);
  }

  /**
   * Sets the recharge position supplier implementation to be used.
   *
   * @param clazz The implementation.
   * @deprecated Will be removed along with the deprecated supplier interface.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  protected void bindRechargePositionSupplier(
      Class<? extends org.opentcs.components.kernel.RechargePositionSupplier> clazz) {
    bind(org.opentcs.components.kernel.RechargePositionSupplier.class).to(clazz).in(Singleton.class);
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
   * @deprecated The definition of <em>recovered</em> is unclear. Unless it is clearly specified,
   * evaluation of a state of recovery should not be part of the API.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  protected void bindRecoveryEvaluator(
      Class<? extends org.opentcs.components.kernel.RecoveryEvaluator> clazz) {
    bind(org.opentcs.components.kernel.RecoveryEvaluator.class).to(clazz).in(Singleton.class);
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
}
