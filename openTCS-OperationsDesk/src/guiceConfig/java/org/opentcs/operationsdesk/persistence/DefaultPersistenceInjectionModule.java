/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.persistence;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.opentcs.guing.common.persistence.ModelFilePersistor;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.guing.common.persistence.OpenTCSModelManager;
import org.opentcs.guing.common.persistence.unified.UnifiedModelPersistor;

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
    bind(ModelManager.class).to(OpenTCSModelManager.class).in(Singleton.class);

    bind(ModelFilePersistor.class).to(UnifiedModelPersistor.class);
  }

}
