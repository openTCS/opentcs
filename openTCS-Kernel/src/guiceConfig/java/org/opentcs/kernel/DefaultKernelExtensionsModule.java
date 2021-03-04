/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import java.util.Locale;
import javax.inject.Singleton;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.opentcs.components.kernel.ControlCenterPanel;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.opentcs.kernel.controlcenter.ControlCenterConfiguration;
import org.opentcs.kernel.controlcenter.ControlCenterInfoHandlerFactory;
import org.opentcs.kernel.controlcenter.KernelControlCenter;
import org.opentcs.kernel.controlcenter.vehicles.DriverGUI;
import org.opentcs.kernel.statistics.StatisticsCollector;
import org.opentcs.kernel.xmlhost.XMLHostInterfaceConfiguration;
import org.opentcs.kernel.xmlhost.orders.XMLTelegramOrderReceiver;
import org.opentcs.kernel.xmlhost.status.StatusMessageDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures the default extensions of the openTCS kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultKernelExtensionsModule
    extends KernelInjectionModule {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultKernelExtensionsModule.class);

  @Override
  protected void configure() {
    configureControlCenterDependencies();
    configureKernelExtensionsDependencies();
    // Kernel extensions for StandardKernel (permanent extensions)
    Multibinder<KernelExtension> allStatesBinder = extensionsBinderAllModes();
    allStatesBinder.addBinding()
        .to(KernelControlCenter.class)
        .in(Singleton.class);
    allStatesBinder.addBinding()
        .to(StandardRemoteKernel.class)
        .in(Singleton.class);
    allStatesBinder.addBinding()
        .to(StatusMessageDispatcher.class)
        .in(Singleton.class);

    // Kernel extensions for KernelStateModelling
    Multibinder<KernelExtension> modellingBinder = extensionsBinderModelling();
    // No extensions for modelling mode, yet.

    // Kernel extensions for KernelStateOperating
    Multibinder<KernelExtension> operatingBinder = extensionsBinderOperating();
    operatingBinder.addBinding().to(XMLTelegramOrderReceiver.class);
    operatingBinder.addBinding().to(StatisticsCollector.class);
  }

  private void configureControlCenterDependencies() {
    Multibinder<ControlCenterPanel> modellingBinder = controlCenterPanelBinderModelling();
    // No extensions for modelling mode, yet.

    Multibinder<ControlCenterPanel> operatingBinder = controlCenterPanelBinderOperating();
    operatingBinder.addBinding().to(DriverGUI.class);

    install(new FactoryModuleBuilder().build(ControlCenterInfoHandlerFactory.class));

    ControlCenterConfiguration configuration
        = getConfigBindingProvider().get(ControlCenterConfiguration.PREFIX,
                                         ControlCenterConfiguration.class);
    bind(ControlCenterConfiguration.class)
        .toInstance(configuration);
    configureKernelControlCenter(configuration);
  }

  private void configureKernelExtensionsDependencies() {
    bind(XMLHostInterfaceConfiguration.class)
        .toInstance(getConfigBindingProvider().get(XMLHostInterfaceConfiguration.PREFIX,
                                                   XMLHostInterfaceConfiguration.class));
  }

  private void configureKernelControlCenter(ControlCenterConfiguration configuration) {
    if (configuration.language().toLowerCase().equals("german")) {
      Locale.setDefault(Locale.GERMAN);
    }
    else {
      Locale.setDefault(Locale.ENGLISH);
    }

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (ClassNotFoundException | InstantiationException | IllegalAccessException
               | UnsupportedLookAndFeelException ex) {
      LOG.warn("Exception setting look and feel", ex);
    }
  }
}
