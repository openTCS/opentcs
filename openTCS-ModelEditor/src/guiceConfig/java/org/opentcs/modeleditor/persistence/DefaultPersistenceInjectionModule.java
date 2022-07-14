/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
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
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DefaultPersistenceInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {
    bind(OpenTCSModelManagerModeling.class).in(Singleton.class);
    bind(ModelManager.class).to(OpenTCSModelManagerModeling.class);
    bind(ModelManagerModeling.class).to(OpenTCSModelManagerModeling.class);

    bind(ModelFileReader.class).to(UnifiedModelReader.class);
    bind(ModelFilePersistor.class).to(UnifiedModelPersistor.class);
  }

}
