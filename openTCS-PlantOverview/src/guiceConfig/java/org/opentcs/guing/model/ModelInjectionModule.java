/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.model.elements.LayoutModel;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.StaticRouteModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.storage.OpenTCSModelManager;
import org.opentcs.util.UniqueStringGenerator;

/**
 * A Guice module for the model package.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ModelInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {
    bind(SystemModel.class).to(StandardSystemModel.class);
    
    bind(ModelManager.class).to(OpenTCSModelManager.class).in(Singleton.class);
  }

  @Provides
  public UniqueStringGenerator<Class<? extends ModelComponent>> modelNameGenerator() {
    UniqueStringGenerator<Class<? extends ModelComponent>> generator
        = new UniqueStringGenerator<>();

    generator.registerNamePattern(PointModel.class, "Point-", "0000");
    generator.registerNamePattern(PathModel.class, "Path-", "0000");
    generator.registerNamePattern(LocationTypeModel.class, "LType-", "0000");
    generator.registerNamePattern(LocationModel.class, "Location-", "0000");
    generator.registerNamePattern(LinkModel.class, "Link-", "0000");
    generator.registerNamePattern(BlockModel.class, "Block-", "0000");
    generator.registerNamePattern(GroupModel.class, "Group-", "0000");
    generator.registerNamePattern(StaticRouteModel.class, "StaticRoute-", "0000");
    generator.registerNamePattern(LayoutModel.class, "Layout-", "0000");
    generator.registerNamePattern(VehicleModel.class, "Vehicle-", "0000");

    return generator;
  }
}
