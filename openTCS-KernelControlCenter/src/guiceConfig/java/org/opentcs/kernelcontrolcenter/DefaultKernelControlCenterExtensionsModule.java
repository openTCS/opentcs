/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import javax.inject.Singleton;
import org.opentcs.components.kernel.ControlCenterPanel;
import org.opentcs.customizations.controlcenter.ControlCenterInjectionModule;
import org.opentcs.kernelcontrolcenter.util.KernelControlCenterConfiguration;
import org.opentcs.kernelcontrolcenter.vehicles.DriverGUI;

/**
 * Configures the default extensions of the openTCS kernel control center application.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DefaultKernelControlCenterExtensionsModule
    extends ControlCenterInjectionModule {

  @Override
  protected void configure() {
    configureControlCenterDependencies();
  }

  private void configureControlCenterDependencies() {
    KernelControlCenterConfiguration configuration
        = getConfigBindingProvider().get(KernelControlCenterConfiguration.PREFIX,
                                         KernelControlCenterConfiguration.class);
    bind(KernelControlCenterConfiguration.class).toInstance(configuration);

    Multibinder<ControlCenterPanel> modellingBinder = controlCenterPanelBinderModelling();
    // No extensions for modelling mode, yet.

    Multibinder<ControlCenterPanel> operatingBinder = controlCenterPanelBinderOperating();
    operatingBinder.addBinding().to(DriverGUI.class);

    install(new FactoryModuleBuilder().build(ControlCenterInfoHandlerFactory.class));

    bind(KernelControlCenter.class).in(Singleton.class);
  }
}
