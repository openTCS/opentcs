/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
/*
 *
 * Created on June 12, 2006, 8:17 AM
 */
package org.opentcs.kernel.workingset;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.opentcs.access.to.model.PathCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.util.event.SimpleEventBus;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ModelTest {

  /**
   * The model to be tested here.
   */
  private Model model;
  /**
   * The object pool backing the test model.
   */
  private TCSObjectPool globalPool;

  @Before
  public void setUp() {
    globalPool = new TCSObjectPool(new SimpleEventBus());
    model = new Model(globalPool);
  }

  @After
  public void tearDown() {
    model = null;
    globalPool = null;
  }

  /**
   * Verify that the contents of the model and the global object pool do not
   * differ.
   */
  @Test
  public void testGlobalPoolSynchronicity()
      throws ObjectUnknownException {
    Set<String> objectNames = new HashSet<>();
    // Add some objects to the model.
    for (int i = 0; i < 100; i++) {
      Point srcPoint = model.createPoint(new PointCreationTO("srcPoint-" + UUID.randomUUID()));
      objectNames.add(srcPoint.getName());
      Point destPoint = model.createPoint(new PointCreationTO("destPoint-" + UUID.randomUUID()));
      objectNames.add(destPoint.getName());
      Path newPath = model.createPath(new PathCreationTO("newPath-" + UUID.randomUUID(),
                                                         srcPoint.getName(),
                                                         destPoint.getName()));
      objectNames.add(newPath.getName());
    }
    // Verify that the global object pool has the correct number of objects.
    assertEquals(objectNames.size(), globalPool.size());
    // Verify that the model contains only the objects just created.
    for (Point curPoint : model.getObjectPool().getObjects(Point.class)) {
      assertTrue("objectNames does not contain '" + curPoint.getName() + "'",
                 objectNames.contains(curPoint.getName()));
    }
    for (Path curPath : model.getObjectPool().getObjects(Path.class)) {
      assertTrue("objectNames does not contain '" + curPath.getName() + "'",
                 objectNames.contains(curPath.getName()));
    }
    // Verify that, after removing all objects from the model, the global pool
    // is empty.
    model.clear();
    assertTrue("globalPool is not empty after removing all objects",
               globalPool.isEmpty());
  }
}
