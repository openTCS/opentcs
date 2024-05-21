/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.math.path;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import org.opentcs.guing.base.model.elements.PathModel;

/**
 * A Guice configuration module for this package.
 */
public class PathLengthFunctionInjectionModule
    extends AbstractModule {

  /**
   * Creates a new instance.
   */
  public PathLengthFunctionInjectionModule() {
  }

  @Override
  protected void configure() {
    MapBinder<PathModel.Type, PathLengthFunction> pathLengthFunctionBinder
        = MapBinder.newMapBinder(binder(),
                                 new TypeLiteral<PathModel.Type>() {
                             },
                                 new TypeLiteral<PathLengthFunction>() {
                             });
    pathLengthFunctionBinder
        .addBinding(PathModel.Type.DIRECT)
        .to(EuclideanDistance.class);
    pathLengthFunctionBinder
        .addBinding(PathModel.Type.ELBOW)
        .to(EuclideanDistance.class);
    pathLengthFunctionBinder
        .addBinding(PathModel.Type.SLANTED)
        .to(EuclideanDistance.class);
    pathLengthFunctionBinder
        .addBinding(PathModel.Type.POLYPATH)
        .to(PolyPathLength.class);
    pathLengthFunctionBinder
        .addBinding(PathModel.Type.BEZIER)
        .to(BezierLength.class);
    pathLengthFunctionBinder
        .addBinding(PathModel.Type.BEZIER_3)
        .to(BezierThreeLength.class);

    bind(PathLengthFunction.class)
        .to(CompositePathLengthFunction.class);
  }
}
