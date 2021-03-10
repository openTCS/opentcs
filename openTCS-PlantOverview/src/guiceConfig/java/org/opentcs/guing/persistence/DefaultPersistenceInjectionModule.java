package org.opentcs.guing.persistence;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.opentcs.guing.persistence.unified.UnifiedModelPersistor;
import org.opentcs.guing.persistence.unified.UnifiedModelReader;

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

    bind(ModelFileReader.class).to(UnifiedModelReader.class);
    bind(ModelFilePersistor.class).to(UnifiedModelPersistor.class);
  }

}
