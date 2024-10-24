// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.modeleditor.application.action;

import com.google.inject.AbstractModule;
import jakarta.inject.Singleton;

/**
 * An injection module for this package.
 */
public class ActionInjectionModule
    extends
      AbstractModule {

  /**
   * Creates a new instance.
   */
  public ActionInjectionModule() {
  }

  @Override
  protected void configure() {
    bind(ViewActionMap.class).in(Singleton.class);
  }

}
