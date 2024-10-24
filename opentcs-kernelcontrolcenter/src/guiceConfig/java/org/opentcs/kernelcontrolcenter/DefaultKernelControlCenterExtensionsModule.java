// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernelcontrolcenter;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import jakarta.inject.Singleton;
import org.opentcs.components.kernelcontrolcenter.ControlCenterPanel;
import org.opentcs.customizations.controlcenter.ControlCenterInjectionModule;
import org.opentcs.kernelcontrolcenter.peripherals.PeripheralsPanel;
import org.opentcs.kernelcontrolcenter.util.KernelControlCenterConfiguration;
import org.opentcs.kernelcontrolcenter.vehicles.DriverGUI;

/**
 * Configures the default extensions of the openTCS kernel control center application.
 */
public class DefaultKernelControlCenterExtensionsModule
    extends
      ControlCenterInjectionModule {

  /**
   * Creates a new instance.
   */
  public DefaultKernelControlCenterExtensionsModule() {
  }

  @Override
  protected void configure() {
    configureControlCenterDependencies();
  }

  private void configureControlCenterDependencies() {
    KernelControlCenterConfiguration configuration
        = getConfigBindingProvider().get(
            KernelControlCenterConfiguration.PREFIX,
            KernelControlCenterConfiguration.class
        );
    bind(KernelControlCenterConfiguration.class).toInstance(configuration);

    // Ensure these binders are initialized.
    commAdapterPanelFactoryBinder();
    peripheralCommAdapterPanelFactoryBinder();

    Multibinder<ControlCenterPanel> modellingBinder = controlCenterPanelBinderModelling();
    // No extensions for modelling mode, yet.

    Multibinder<ControlCenterPanel> operatingBinder = controlCenterPanelBinderOperating();
    operatingBinder.addBinding().to(DriverGUI.class);
    if (configuration.enablePeripheralsPanel()) {
      operatingBinder.addBinding().to(PeripheralsPanel.class);
    }

    install(new FactoryModuleBuilder().build(ControlCenterInfoHandlerFactory.class));

    bind(KernelControlCenter.class).in(Singleton.class);
  }
}
