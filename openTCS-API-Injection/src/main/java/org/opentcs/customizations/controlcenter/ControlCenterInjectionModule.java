/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.customizations.controlcenter;

import com.google.inject.multibindings.Multibinder;
import org.opentcs.components.kernel.ControlCenterPanel;
import org.opentcs.customizations.ConfigurableInjectionModule;
import org.opentcs.drivers.vehicle.management.VehicleCommAdapterPanelFactory;

/**
 * A base class for Guice modules adding or customizing bindings for the kernel control center
 * application.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public abstract class ControlCenterInjectionModule
    extends ConfigurableInjectionModule {

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
   * Returns a multibinder that can be used to register {@link VehicleCommAdapterPanelFactory}
   * implementations.
   *
   * @return The multibinder.
   */
  protected Multibinder<VehicleCommAdapterPanelFactory> commAdapterPanelFactoryBinder() {
    return Multibinder.newSetBinder(binder(), VehicleCommAdapterPanelFactory.class);
  }
}
