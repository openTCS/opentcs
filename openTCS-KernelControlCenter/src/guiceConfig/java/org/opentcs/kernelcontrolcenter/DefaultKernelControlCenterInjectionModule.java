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
import org.opentcs.access.SslParameterSet;
import org.opentcs.access.rmi.KernelServicePortalBuilder;
import org.opentcs.access.rmi.factories.NullSocketFactoryProvider;
import org.opentcs.access.rmi.factories.SecureSocketFactoryProvider;
import org.opentcs.access.rmi.factories.SocketFactoryProvider;
import org.opentcs.common.DefaultPortalManager;
import org.opentcs.common.KernelClientApplication;
import org.opentcs.common.PortalManager;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.customizations.ApplicationHome;
import org.opentcs.customizations.ConfigurableInjectionModule;
import org.opentcs.customizations.ServiceCallWrapper;
import org.opentcs.kernelcontrolcenter.exchange.DefaultServiceCallWrapper;
import org.opentcs.kernelcontrolcenter.exchange.SslConfiguration;
import org.opentcs.kernelcontrolcenter.util.KernelControlCenterConfiguration;
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
    configureKernelControlCenterDependencies();
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

  private void configureKernelControlCenterDependencies() {
    KernelControlCenterConfiguration configuration
        = getConfigBindingProvider().get(KernelControlCenterConfiguration.PREFIX,
                                         KernelControlCenterConfiguration.class);
    bind(KernelControlCenterConfiguration.class)
        .toInstance(configuration);
    configureKernelControlCenter(configuration);
    configureSocketConnections();

    bind(CallWrapper.class)
        .annotatedWith(ServiceCallWrapper.class)
        .to(DefaultServiceCallWrapper.class)
        .in(Singleton.class);

    bind(new TypeLiteral<List<ConnectionParamSet>>() {
    })
        .toInstance(configuration.connectionBookmarks());
  }

  private void configureSocketConnections() {
    SslConfiguration sslConfiguration = getConfigBindingProvider().get(SslConfiguration.PREFIX,
                                                                       SslConfiguration.class);

    //Create the data object for the ssl configuration
    SslParameterSet sslParamSet = new SslParameterSet(SslParameterSet.DEFAULT_KEYSTORE_TYPE,
                                                      null,
                                                      null,
                                                      new File(sslConfiguration.truststoreFile()),
                                                      sslConfiguration.truststorePassword());
    bind(SslParameterSet.class).toInstance(sslParamSet);

    SocketFactoryProvider socketFactoryProvider;
    if (sslConfiguration.enable()) {
      socketFactoryProvider = new SecureSocketFactoryProvider(sslParamSet);
    }
    else {
      LOG.warn("SSL encryption disabled, connections will not be secured!");
      socketFactoryProvider = new NullSocketFactoryProvider();
    }

    //Bind socket provider to the kernel portal
    bind(KernelServicePortal.class)
        .toInstance(new KernelServicePortalBuilder()
            .setSocketFactoryProvider(socketFactoryProvider)
            .build());
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
