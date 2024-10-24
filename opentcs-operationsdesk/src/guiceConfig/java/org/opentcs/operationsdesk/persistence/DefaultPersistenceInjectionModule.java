// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.persistence;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.opentcs.guing.common.persistence.ModelFilePersistor;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.guing.common.persistence.OpenTCSModelManager;
import org.opentcs.guing.common.persistence.unified.UnifiedModelPersistor;

/**
 * Default bindings for model readers and persistors.
 */
public class DefaultPersistenceInjectionModule
    extends
      AbstractModule {

  /**
   * Creates a new instance.
   */
  public DefaultPersistenceInjectionModule() {
  }

  @Override
  protected void configure() {
    bind(ModelManager.class).to(OpenTCSModelManager.class).in(Singleton.class);

    bind(ModelFilePersistor.class).to(UnifiedModelPersistor.class);
  }

}
