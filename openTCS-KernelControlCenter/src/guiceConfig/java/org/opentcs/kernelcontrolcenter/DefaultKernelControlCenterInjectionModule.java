/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter;

import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import java.io.File;
import java.util.List;
import java.util.Locale;
import javax.inject.Singleton;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.rmi.KernelServicePortalBuilder;
import org.opentcs.access.rmi.factories.AnonSslSocketFactoryProvider;
import org.opentcs.access.rmi.factories.NullSocketFactoryProvider;
import org.opentcs.access.rmi.factories.SocketFactoryProvider;
import org.opentcs.access.rmi.factories.SslSocketFactoryProvider;
import org.opentcs.common.DefaultPortalManager;
import org.opentcs.common.KernelClientApplication;
import org.opentcs.common.PortalManager;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.customizations.ApplicationHome;
import org.opentcs.customizations.ConfigurableInjectionModule;
import org.opentcs.customizations.ServiceCallWrapper;
import org.opentcs.kernelcontrolcenter.exchange.DefaultServiceCallWrapper;
import org.opentcs.kernelcontrolcenter.util.KernelControlCenterConfiguration;
import static org.opentcs.kernelcontrolcenter.util.KernelControlCenterConfiguration.ConnectionEncryption.NONE;
import static org.opentcs.kernelcontrolcenter.util.KernelControlCenterConfiguration.ConnectionEncryption.SSL;
import static org.opentcs.kernelcontrolcenter.util.KernelControlCenterConfiguration.ConnectionEncryption.SSL_UNTRUSTED;
import org.opentcs.kernelcontrolcenter.vehicles.LocalVehicleEntryPool;
import org.opentcs.util.CallWrapper;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.event.EventSource;
import org.opentcs.util.event.SimpleEventBus;
import org.opentcs.util.gui.dialog.ConnectionParamSet;
import org.opentcs.virtualvehicle.AdapterPanelComponentsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Guice module for the openTCS kernel control center application.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DefaultKernelControlCenterInjectionModule
    extends ConfigurableInjectionModule {

  /**
   * This class' logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultKernelControlCenterInjectionModule.class);

  @Override
  protected void configure() {
    File applicationHome = new File(System.getProperty("opentcs.home", "."));
    bind(File.class)
        .annotatedWith(ApplicationHome.class)
        .toInstance(applicationHome);

    bind(LocalVehicleEntryPool.class)
        .in(Singleton.class);

    bind(KernelClientApplication.class)
        .to(KernelControlCenterApplication.class)
        .in(Singleton.class);

    install(new FactoryModuleBuilder().build(AdapterPanelComponentsFactory.class));

    configureEventBus();
    configureKernelControlCenterDependencies(applicationHome);
    configureExchangeInjectionModules();
  }

  private void configureEventBus() {
    EventBus newEventBus = new SimpleEventBus();
    bind(EventHandler.class)
        .annotatedWith(ApplicationEventBus.class)
        .toInstance(newEventBus);
    bind(EventSource.class)
        .annotatedWith(ApplicationEventBus.class)
        .toInstance(newEventBus);
    bind(EventBus.class)
        .annotatedWith(ApplicationEventBus.class)
        .toInstance(newEventBus);
  }

  private void configureExchangeInjectionModules() {
    bind(PortalManager.class)
        .to(DefaultPortalManager.class)
        .in(Singleton.class);
  }

  private void configureKernelControlCenterDependencies(File applicationHome) {
    KernelControlCenterConfiguration configuration
        = getConfigBindingProvider().get(KernelControlCenterConfiguration.PREFIX,
                                         KernelControlCenterConfiguration.class);
    bind(KernelControlCenterConfiguration.class)
        .toInstance(configuration);
    configureKernelControlCenter(configuration);

    SocketFactoryProvider socketFactoryProvider;
    switch (configuration.connectionEncryption()) {
      case NONE:
        socketFactoryProvider = new NullSocketFactoryProvider();
        break;
      case SSL_UNTRUSTED:
        socketFactoryProvider = new AnonSslSocketFactoryProvider();
        break;
      case SSL:
        socketFactoryProvider = new SslSocketFactoryProvider(applicationHome.getPath(),
                                                             configuration.truststorePassword());
        break;
      default:
        LOG.warn("No implementation for '{}' encryption, falling back to '{}'.",
                 configuration.connectionEncryption().name(),
                 NONE.name());
        socketFactoryProvider = new NullSocketFactoryProvider();
    }

    bind(KernelServicePortal.class)
        .toInstance(new KernelServicePortalBuilder()
            .setSocketFactoryProvider(socketFactoryProvider)
            .build());

    bind(CallWrapper.class)
        .annotatedWith(ServiceCallWrapper.class)
        .to(DefaultServiceCallWrapper.class)
        .in(Singleton.class);

    bind(new TypeLiteral<List<ConnectionParamSet>>() {
    })
        .toInstance(configuration.connectionBookmarks());
  }

  private void configureKernelControlCenter(KernelControlCenterConfiguration configuration) {
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
      LOG.warn("Could not set look-and-feel", ex);
    }
    // Show tooltips for 30 seconds (Default: 4 sec)
    ToolTipManager.sharedInstance().setDismissDelay(30 * 1000);
  }
}
