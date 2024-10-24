// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.util;

import com.google.inject.AbstractModule;
import jakarta.inject.Singleton;
import org.opentcs.guing.common.util.PanelRegistry;

/**
 * A default Guice module for this package.
 */
public class UtilInjectionModule
    extends
      AbstractModule {

  /**
   * Creates a new instance.
   */
  public UtilInjectionModule() {
  }

  @Override
  protected void configure() {
    bind(PanelRegistry.class).in(Singleton.class);
  }
}
