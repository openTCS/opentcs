/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.toolbar;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.jhotdraw.draw.tool.DragTracker;
import org.jhotdraw.draw.tool.SelectAreaTracker;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ToolBarInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(SelectionToolFactory.class));

    bind(SelectAreaTracker.class).to(OpenTCSSelectAreaTracker.class);
    bind(DragTracker.class).to(OpenTCSDragTracker.class);
  }

}
