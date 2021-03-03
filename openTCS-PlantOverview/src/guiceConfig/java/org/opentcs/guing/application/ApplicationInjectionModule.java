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
import java.awt.Component;
import javax.inject.Singleton;
import javax.swing.JFrame;
import org.jhotdraw.app.Application;
import org.opentcs.guing.application.action.ActionInjectionModule;
import org.opentcs.guing.application.action.edit.UndoRedoManager;
import org.opentcs.guing.application.menus.MenusInjectionModule;
import org.opentcs.guing.application.toolbar.ToolBarInjectionModule;
import org.opentcs.guing.util.MessageDisplay;

/**
 * An injection module for this package.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ApplicationInjectionModule
    extends AbstractModule {

  /**
   * The application's main frame.
   */
  private final JFrame applicationFrame = new JFrame();

  @Override
  protected void configure() {
    install(new ActionInjectionModule());
    install(new MenusInjectionModule());
    install(new ToolBarInjectionModule());

    bind(ApplicationState.class).in(Singleton.class);

    bind(MessageDisplay.class).to(KernelStatusPanel.class);
    bind(KernelStatusPanel.class).in(Singleton.class);

    bind(UndoRedoManager.class).in(Singleton.class);

    bind(ProgressIndicator.class)
        .to(SplashFrame.class)
        .in(Singleton.class);
    bind(StatusPanel.class).in(Singleton.class);

    bind(JFrame.class)
        .annotatedWith(ApplicationFrame.class)
        .toInstance(applicationFrame);
    bind(Component.class)
        .annotatedWith(ApplicationFrame.class)
        .toInstance(applicationFrame);

    bind(ViewManager.class)
        .in(Singleton.class);

    bind(Application.class)
        .to(OpenTCSSDIApplication.class)
        .in(Singleton.class);

    bind(OpenTCSView.class).in(Singleton.class);
    bind(GuiManager.class).to(OpenTCSView.class);
  }

}
