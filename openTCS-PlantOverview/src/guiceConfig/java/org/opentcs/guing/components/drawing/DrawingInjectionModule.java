/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.drawing;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import javax.inject.Singleton;
import org.jhotdraw.draw.DrawingEditor;
import org.opentcs.guing.components.drawing.figures.FigureFactory;

/**
 * A Guice module for this package.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DrawingInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(FigureFactory.class));

    bind(OpenTCSDrawingEditor.class).in(Singleton.class);
    bind(DrawingEditor.class).to(OpenTCSDrawingEditor.class);

    bind(OpenTCSDrawingView.class).to(OpenTCSDrawingViewBuffered.class);
  }
}
