package org.opentcs.guing.persistence;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import org.opentcs.guing.persistence.legacy.LegacyModelPersistor;
import org.opentcs.guing.persistence.legacy.LegacyModelReader;
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

    Multibinder.newSetBinder(binder(), ModelFileReader.class)
        .addBinding().to(UnifiedModelReader.class);
    Multibinder.newSetBinder(binder(), ModelFileReader.class)
        .addBinding().to(LegacyModelReader.class);

    Multibinder.newSetBinder(binder(), ModelFilePersistor.class)
        .addBinding().to(UnifiedModelPersistor.class);
    Multibinder.newSetBinder(binder(), ModelFilePersistor.class)
        .addBinding().to(LegacyModelPersistor.class);
  }

}
