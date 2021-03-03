/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import java.io.File;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import org.opentcs.customizations.ApplicationHome;
import org.opentcs.guing.application.ApplicationInjectionModule;
import org.opentcs.guing.components.ComponentsInjectionModule;
import org.opentcs.guing.exchange.ExchangeInjectionModule;
import org.opentcs.guing.model.ModelInjectionModule;
import org.opentcs.guing.transport.TransportInjectionModule;
import org.opentcs.guing.util.UtilInjectionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Guice module for the openTCS kernel application.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class FixedPlantOverviewInjectionModule
    extends AbstractModule {

  /**
   * This class's logger.
   */
  private static final Logger log
      = LoggerFactory.getLogger(FixedPlantOverviewInjectionModule.class);
  /**
   * The application's event bus.
   */
  private final MBassador<Object> eventBus = new MBassador<>(BusConfiguration.Default());

  @Override
  protected void configure() {

    install(new ApplicationInjectionModule());
    install(new ComponentsInjectionModule());
    install(new ExchangeInjectionModule());
    install(new ModelInjectionModule());
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
      log.warn("Event handler caused an error", error.getCause());
    });
  }

}
