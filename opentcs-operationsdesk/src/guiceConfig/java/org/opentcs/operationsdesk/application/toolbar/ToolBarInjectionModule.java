// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.application.toolbar;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import jakarta.inject.Singleton;
import org.jhotdraw.draw.tool.DragTracker;
import org.jhotdraw.draw.tool.SelectAreaTracker;
import org.opentcs.operationsdesk.application.action.ToolBarManager;
import org.opentcs.thirdparty.guing.common.jhotdraw.application.toolbar.OpenTCSDragTracker;
import org.opentcs.thirdparty.guing.common.jhotdraw.application.toolbar.OpenTCSSelectAreaTracker;

/**
 */
public class ToolBarInjectionModule
    extends
      AbstractModule {

  /**
   * Creates a new instance.
   */
  public ToolBarInjectionModule() {
  }

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(SelectionToolFactory.class));

    bind(ToolBarManager.class).in(Singleton.class);

    bind(SelectAreaTracker.class).to(OpenTCSSelectAreaTracker.class);
    bind(DragTracker.class).to(OpenTCSDragTracker.class);
  }

}
