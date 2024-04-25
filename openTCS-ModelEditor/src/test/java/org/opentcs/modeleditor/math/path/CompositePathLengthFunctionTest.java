/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.math.path;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.opentcs.guing.base.model.elements.PathModel;

/**
 * Test for {@link CompositePathlengthFunction}.
 */
class CompositePathLengthFunctionTest {

  private CompositePathLengthFunction compPLF;
  private PathLengthFunction directFunction;
  private PathLengthFunction bezierFunction;
  private EuclideanDistance defaultFunction;

  @BeforeEach
  void setUp() {
    directFunction = mock();
    bezierFunction = mock();
    defaultFunction = mock();

    Map<PathModel.Type, PathLengthFunction> pathlengthfunctions = new HashMap<>();
    pathlengthfunctions.put(PathModel.Type.DIRECT, directFunction);
    pathlengthfunctions.put(PathModel.Type.BEZIER, bezierFunction);

    compPLF = new CompositePathLengthFunction(pathlengthfunctions, defaultFunction);
  }

  @Test
  void shouldUseDirectFunction() {
    PathModel pathModel = new PathModel();
    pathModel.getPropertyPathConnType().setValue(PathModel.Type.DIRECT);

    compPLF.applyAsDouble(pathModel);

    verify(directFunction).applyAsDouble(pathModel);
  }

  @Test
  void shouldUseBezierFunction() {
    PathModel pathModel = new PathModel();
    pathModel.getPropertyPathConnType().setValue(PathModel.Type.BEZIER);

    compPLF.applyAsDouble(pathModel);

    verify(bezierFunction).applyAsDouble(pathModel);
  }

  @Test
  void shouldUseDefaultFunctionAsFallback() {
    PathModel pathModel = new PathModel();
    pathModel.getPropertyPathConnType().setValue(PathModel.Type.POLYPATH);

    compPLF.applyAsDouble(pathModel);

    verify(defaultFunction).applyAsDouble(pathModel);
  }
}
