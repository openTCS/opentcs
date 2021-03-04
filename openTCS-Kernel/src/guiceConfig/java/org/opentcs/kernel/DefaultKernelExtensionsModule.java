/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import java.util.Locale;
import javax.inject.Singleton;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.opentcs.kernel.extensions.adminwebapi.AdminWebApi;
import org.opentcs.kernel.extensions.adminwebapi.AdminWebApiConfiguration;
import org.opentcs.kernel.extensions.controlcenter.ControlCenterConfiguration;
import org.opentcs.kernel.extensions.controlcenter.ControlCenterInfoHandlerFactory;
import org.opentcs.kernel.extensions.controlcenter.KernelControlCenter;
import org.opentcs.kernel.extensions.controlcenter.vehicles.DriverGUI;
import org.opentcs.kernel.extensions.rmi.RmiKernelInterfaceConfiguration;
import org.opentcs.kernel.extensions.rmi.StandardRemoteKernel;
import org.opentcs.kernel.extensions.rmi.StandardRemoteKernelClientPortal;
import org.opentcs.kernel.extensions.servicewebapi.ServiceWebApi;
import org.opentcs.kernel.extensions.servicewebapi.ServiceWebApiConfiguration;
import org.opentcs.kernel.extensions.statistics.StatisticsCollector;
import org.opentcs.kernel.extensions.statistics.StatisticsCollectorConfiguration;
import org.opentcs.kernel.extensions.xmlhost.XMLHostInterfaceConfiguration;
import org.opentcs.kernel.extensions.xmlhost.orders.XMLTelegramOrderReceiver;
import org.opentcs.kernel.extensions.xmlhost.status.StatusMessageDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures the default extensions of the openTCS kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultKernelExtensionsModule
    extends KernelInjectionModule {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultKernelExtensionsModule.class);

  @Override
  protected void configure() {
    // Ensure all kernel extensions binders are initialized.
    extensionsBinderAllModes();
    extensionsBinderModelling();
    extensionsBinderOperating();

    configureAdminInterface();
    configureRestfulOrderInterface();
    configureRmiInterface();
    configureControlCenter();
    configureXmlHostInterface();
    configureStatisticsCollector();
  }

  private void configureAdminInterface() {
    AdminWebApiConfiguration configuration
        = getConfigBindingProvider().get(AdminWebApiConfiguration.PREFIX,
                                         AdminWebApiConfiguration.class);

    if (!configuration.enable()) {
      return;
    }

    bind(AdminWebApiConfiguration.class)
        .toInstance(configuration);

    extensionsBinderAllModes().addBinding()
        .to(AdminWebApi.class)
        .in(Singleton.class);
  }

  private void configureRestfulOrderInterface() {
    ServiceWebApiConfiguration configuration
        = getConfigBindingProvider().get(ServiceWebApiConfiguration.PREFIX,
                                         ServiceWebApiConfiguration.class);

    if (!configuration.enable()) {
      return;
    }

    bind(ServiceWebApiConfiguration.class)
        .toInstance(configuration);

    extensionsBinderOperating().addBinding()
        .to(ServiceWebApi.class)
        .in(Singleton.class);
  }

  private void configureRmiInterface() {
    RmiKernelInterfaceConfiguration configuration
        = getConfigBindingProvider().get(RmiKernelInterfaceConfiguration.PREFIX,
                                         RmiKernelInterfaceConfiguration.class);
    if (configuration.enable()) {
      extensionsBinderAllModes().addBinding()
          .to(StandardRemoteKernelClientPortal.class)
          .in(Singleton.class);
      extensionsBinderAllModes().addBinding()
          .to(StandardRemoteKernel.class)
          .in(Singleton.class);
    }
  }

  private void configureControlCenter() {
    ControlCenterConfiguration configuration
        = getConfigBindingProvider().get(ControlCenterConfiguration.PREFIX,
                                         ControlCenterConfiguration.class);

    if (!configuration.enable()) {
      return;
    }

    bind(ControlCenterConfiguration.class)
        .toInstance(configuration);

    // Bindings for modelling mode panels.
    // No extensions for modelling mode, yet.
    controlCenterPanelBinderModelling();

    // Bindings for operating mode panels.
    controlCenterPanelBinderOperating().addBinding().to(DriverGUI.class);

    install(new FactoryModuleBuilder().build(ControlCenterInfoHandlerFactory.class));

    configureControlCenterLocale(configuration);
    configureControlCenterLookAndFeel();

    extensionsBinderAllModes().addBinding()
        .to(KernelControlCenter.class)
        .in(Singleton.class);
  }

  private void configureXmlHostInterface() {
    XMLHostInterfaceConfiguration configuration
        = getConfigBindingProvider().get(XMLHostInterfaceConfiguration.PREFIX,
                                         XMLHostInterfaceConfiguration.class);
    if (!configuration.enable()) {
      return;
    }

    bind(XMLHostInterfaceConfiguration.class)
        .toInstance(configuration);
    // The status channel is available in all modes.
    extensionsBinderAllModes().addBinding()
        .to(StatusMessageDispatcher.class)
        .in(Singleton.class);

    // The order interface is available only in operating mode.
    extensionsBinderOperating().addBinding()
        .to(XMLTelegramOrderReceiver.class);
  }

  private void configureStatisticsCollector() {
    StatisticsCollectorConfiguration configuration
        = getConfigBindingProvider().get(StatisticsCollectorConfiguration.PREFIX,
                                         StatisticsCollectorConfiguration.class);
    if (!configuration.enable()) {
      return;
    }

    bind(StatisticsCollectorConfiguration.class)
        .toInstance(configuration);
    extensionsBinderOperating().addBinding()
        .to(StatisticsCollector.class);
  }

  private void configureControlCenterLocale(ControlCenterConfiguration configuration) {
    if (configuration.language().toLowerCase().equals("german")) {
      Locale.setDefault(Locale.GERMAN);
    }
    else {
      Locale.setDefault(Locale.ENGLISH);
    }

  }

  private void configureControlCenterLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (ClassNotFoundException | InstantiationException | IllegalAccessException
               | UnsupportedLookAndFeelException ex) {
      LOG.warn("Exception setting look and feel", ex);
    }
  }
}
