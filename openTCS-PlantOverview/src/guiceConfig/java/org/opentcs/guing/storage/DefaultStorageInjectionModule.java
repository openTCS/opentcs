package org.opentcs.guing.storage;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * Default bindings for model readers and persistors.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DefaultStorageInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), ModelReader.class)
        .addBinding().to(UnifiedModelReader.class);
    Multibinder.newSetBinder(binder(), ModelReader.class)
        .addBinding().to(ModelJAXBReader.class);

    Multibinder.newSetBinder(binder(), ModelFilePersistor.class)
        .addBinding().to(UnifiedModelPersistor.class);
    Multibinder.newSetBinder(binder(), ModelFilePersistor.class)
        .addBinding().to(ModelJAXBPersistor.class);
  }

}
