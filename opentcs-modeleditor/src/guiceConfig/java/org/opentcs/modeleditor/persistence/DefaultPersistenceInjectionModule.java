// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.modeleditor.persistence;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.opentcs.guing.common.persistence.ModelFilePersistor;
import org.opentcs.guing.common.persistence.ModelFileReader;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.guing.common.persistence.unified.UnifiedModelPersistor;
import org.opentcs.modeleditor.persistence.unified.UnifiedModelReader;

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
    bind(OpenTCSModelManagerModeling.class).in(Singleton.class);
    bind(ModelManager.class).to(OpenTCSModelManagerModeling.class);
    bind(ModelManagerModeling.class).to(OpenTCSModelManagerModeling.class);

    bind(ModelFileReader.class).to(UnifiedModelReader.class);
    bind(ModelFilePersistor.class).to(UnifiedModelPersistor.class);
  }

}
