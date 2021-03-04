/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import java.io.File;
import java.util.Locale;
import javax.inject.Singleton;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import org.opentcs.access.rmi.factories.NullSocketFactoryProvider;
import org.opentcs.access.rmi.factories.SocketFactoryProvider;
import org.opentcs.access.rmi.factories.AnonSslSocketFactoryProvider;
import org.opentcs.access.rmi.factories.SslSocketFactoryProvider;
import org.opentcs.components.plantoverview.LocationTheme;
import org.opentcs.components.plantoverview.VehicleTheme;
import org.opentcs.customizations.ApplicationHome;
import org.opentcs.customizations.ConfigurableInjectionModule;
import org.opentcs.guing.application.ApplicationInjectionModule;
import org.opentcs.guing.components.ComponentsInjectionModule;
import org.opentcs.guing.exchange.ExchangeInjectionModule;
import org.opentcs.guing.model.ModelInjectionModule;
import org.opentcs.guing.storage.DefaultStorageInjectionModule;
import org.opentcs.guing.transport.TransportInjectionModule;
import org.opentcs.guing.util.PlantOverviewApplicationConfiguration;
import static org.opentcs.guing.util.PlantOverviewApplicationConfiguration.ConnectionEncryption.NONE;
import org.opentcs.guing.util.UtilInjectionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Guice module for the openTCS plant overview application.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultPlantOverviewInjectionModule
    extends ConfigurableInjectionModule {

  /**
   * This class's logger.
   */
  private static final Logger LOG
      = LoggerFactory.getLogger(DefaultPlantOverviewInjectionModule.class);
  /**
   * The application's event bus.
   */
  private final MBassador<Object> eventBus = new MBassador<>(BusConfiguration.Default());

  @Override
  protected void configure() {
    configurePlantOverviewDependencies();
    install(new ApplicationInjectionModule());
    install(new ComponentsInjectionModule());
    install(new ExchangeInjectionModule());
    install(new ModelInjectionModule());
    install(new DefaultStorageInjectionModule());
    install(new TransportInjectionModule());
    install(new UtilInjectionModule());

    configureEventBus();

    bind(File.class)
        .annotatedWith(ApplicationHome.class)
        .toInstance(new File(System.getProperty("opentcs.home", ".")));
  }

  private void configureEventBus() {
    // Bind global event bus and automatically register every created object.
    bind(new TypeLiteral<MBassador<Object>>() {
    })
        .toInstance(eventBus);
    bindListener(Matchers.any(), new TypeListener() {
               @Override
               public <I> void hear(TypeLiteral<I> typeLiteral,
                                    TypeEncounter<I> typeEncounter) {
                 typeEncounter.register(new InjectionListener<I>() {
                   @Override
                   public void afterInjection(I i) {
                     eventBus.subscribe(i);
                   }
                 });
               }
             });
    eventBus.addErrorHandler((error) -> {
      LOG.warn("Event handler caused an error", error.getCause());
    });
  }

  private void configurePlantOverviewDependencies() {
    PlantOverviewApplicationConfiguration configuration
        = getConfigBindingProvider().get(PlantOverviewApplicationConfiguration.PREFIX,
                                         PlantOverviewApplicationConfiguration.class);
    bind(PlantOverviewApplicationConfiguration.class)
        .toInstance(configuration);
    configurePlantOverview(configuration);
    configureThemes(configuration);

    switch (configuration.connectionEncryption()) {
      case NONE:
        bind(SocketFactoryProvider.class).to(NullSocketFactoryProvider.class);
        break;
      case SSL_UNTRUSTED:
        bind(SocketFactoryProvider.class).to(AnonSslSocketFactoryProvider.class);
        break;
      case SSL:
        bind(SocketFactoryProvider.class).to(SslSocketFactoryProvider.class);
        break;
      default:
        LOG.warn("No implementation for '{}' encryption, falling back to '{}'.",
                 configuration.connectionEncryption().name(),
                 NONE.name());
        bind(SocketFactoryProvider.class).to(NullSocketFactoryProvider.class);
    }
  }

  private void configureThemes(PlantOverviewApplicationConfiguration configuration) {
    bind(LocationTheme.class)
        .to(configuration.locationThemeClass())
        .in(Singleton.class);
    bind(VehicleTheme.class)
        .to(configuration.vehicleThemeClass())
        .in(Singleton.class);
  }

  private void configurePlantOverview(PlantOverviewApplicationConfiguration configuration) {
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
