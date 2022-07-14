/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.util;

import com.google.inject.AbstractModule;
import javax.inject.Singleton;
import org.opentcs.guing.common.util.PanelRegistry;

/**
 * A default Guice module for this package.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class UtilInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {
    bind(PanelRegistry.class).in(Singleton.class);
  }
}
