/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application;

import com.google.inject.AbstractModule;
import javax.inject.Singleton;
import javax.swing.JFrame;
import org.jhotdraw.app.Application;
import org.opentcs.guing.components.drawing.DrawingInjectionModule;
import org.opentcs.guing.exchange.ExchangeInjectionModule;
import org.opentcs.guing.exchange.adapter.ProcessAdapterInjectionModule;
import org.opentcs.guing.model.ModelInjectionModule;
import org.opentcs.guing.util.UtilInjectionModule;

/**
 * A Guice module for the openTCS kernel application.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ApplicationInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {

    install(new DrawingInjectionModule());
    install(new ExchangeInjectionModule());
    install(new ProcessAdapterInjectionModule());
    install(new UtilInjectionModule());
    install(new ModelInjectionModule());

    bind(ProgressIndicator.class).to(SplashFrame.class).in(Singleton.class);
    bind(StatusPanel.class).in(Singleton.class);

    bind(JFrame.class).annotatedWith(ApplicationFrame.class).to(JFrame.class).in(Singleton.class);
    bind(Application.class).to(OpenTCSSDIApplication.class).in(Singleton.class);
    bind(OpenTCSView.class).in(Singleton.class);
  }
}
