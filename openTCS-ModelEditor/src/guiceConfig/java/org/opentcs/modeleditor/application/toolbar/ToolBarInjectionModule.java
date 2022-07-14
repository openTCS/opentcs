/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.application.toolbar;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import javax.inject.Singleton;
import org.jhotdraw.draw.tool.DragTracker;
import org.jhotdraw.draw.tool.SelectAreaTracker;
import org.opentcs.modeleditor.application.action.ToolBarManager;
import org.opentcs.thirdparty.guing.common.jhotdraw.application.toolbar.OpenTCSDragTracker;
import org.opentcs.thirdparty.guing.common.jhotdraw.application.toolbar.OpenTCSSelectAreaTracker;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ToolBarInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(SelectionToolFactory.class));
    install(new FactoryModuleBuilder().build(CreationToolFactory.class));
    
    bind(ToolBarManager.class).in(Singleton.class);

    bind(SelectAreaTracker.class).to(OpenTCSSelectAreaTracker.class);
    bind(DragTracker.class).to(OpenTCSDragTracker.class);
  }

}
