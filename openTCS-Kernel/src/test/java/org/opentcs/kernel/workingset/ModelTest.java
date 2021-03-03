/*
 *
 * Created on June 12, 2006, 8:17 AM
 */
package org.opentcs.kernel.workingset;

import java.util.HashSet;
import java.util.Set;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;

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
    globalPool = new TCSObjectPool(new MBassador<>(BusConfiguration.Default()));
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
      Point srcPoint = model.createPoint(null);
      objectNames.add(srcPoint.getName());
      Point destPoint = model.createPoint(null);
      objectNames.add(destPoint.getName());
      Path newPath = model.createPath(
          null, srcPoint.getReference(), destPoint.getReference());
      objectNames.add(newPath.getName());
    }
    // Verify that the global object pool has the correct number of objects.
    assertEquals(objectNames.size(), globalPool.size());
    // Verify that the model contains only the objects just created.
    for (Point curPoint : model.getPoints(null)) {
      assertTrue("objectNames does not contain '" + curPoint.getName() + "'",
                 objectNames.contains(curPoint.getName()));
    }
    for (Path curPath : model.getPaths(null)) {
      assertTrue("objectNames does not contain '" + curPath.getName() + "'",
                 objectNames.contains(curPath.getName()));
    }
    // Verify that, after removing all objects from the model, the global pool
    // is empty.
    for (Path curPath : model.getPaths(null)) {
      model.removePath(curPath.getReference());
    }
    for (Point curPoint : model.getPoints(null)) {
      model.removePoint(curPoint.getReference());
    }
    assertTrue("globalPool is not empty after removing all objects",
               globalPool.isEmpty());
  }

  /**
   * Test integrity/uniqueness of point names.
   */
  @Test
  public void testPointNameIntegrity()
      throws ObjectUnknownException, ObjectExistsException {
    // Add some points to the model.
    Point point1 = model.createPoint(null);
    Point point2 = model.createPoint(null);
    Point point3 = model.createPoint(null);
    Point point4 = model.createPoint(null);
    Point point5 = model.createPoint(null);
    // Add some paths, too.
    Path path1
        = model.createPath(null, point1.getReference(), point2.getReference());
    Path path2
        = model.createPath(null, point2.getReference(), point3.getReference());
    Path path3
        = model.createPath(null, point3.getReference(), point4.getReference());
    // Try changing a non-existent point's name
    try {
      model.removePoint(point5.getReference());
      globalPool.renameObject(point5.getReference(), "pointY");
      fail("Should raise an ObjectUnknownException for non-existent names");
    }
    catch (ObjectUnknownException exc) {
      assertTrue(true);
    }
    // Try changing a point's name to one that exists already.
    try {
      globalPool.renameObject(point3.getReference(), point1.getName());
      fail("Should raise an ObjectExistsException for duplicate names");
    }
    catch (ObjectExistsException exc) {
      assertTrue(true);
    }
    // Try changing a point's name to that of an existing path.
    try {
      globalPool.renameObject(point2.getReference(), path1.getName());
      fail("Should raise an ObjectExistsException for duplicate names");
    }
    catch (ObjectExistsException exc) {
      assertTrue(true);
    }
  }

  @Test
  public void testPointOrientationAngle() {
    Point myPoint = model.createPoint(null);

    model.setPointVehicleOrientationAngle(myPoint.getReference(), 0.0);
    model.setPointVehicleOrientationAngle(myPoint.getReference(), -360.0);
    model.setPointVehicleOrientationAngle(myPoint.getReference(), 360.0);
    model.setPointVehicleOrientationAngle(myPoint.getReference(), Double.NaN);

    try {
      model.setPointVehicleOrientationAngle(myPoint.getReference(), -360.1);
      fail("Should raise an IllegalArgumentException for value -360.1");
    }
    catch (IllegalArgumentException exc) {
      assertTrue(true);
    }
    try {
      model.setPointVehicleOrientationAngle(myPoint.getReference(), 360.1);
      fail("Should raise an IllegalArgumentException for value 360.1");
    }
    catch (IllegalArgumentException exc) {
      assertTrue(true);
    }
    try {
      model.setPointVehicleOrientationAngle(myPoint.getReference(), 1000);
      fail("Should raise an IllegalArgumentException for value 1000");
    }
    catch (IllegalArgumentException exc) {
      assertTrue(true);
    }
    myPoint = model.getPoint(myPoint.getReference());
    assertTrue(Double.isNaN(myPoint.getVehicleOrientationAngle()));
    // Verify that, after these exceptions, we can set an acceptable value.
    model.setPointVehicleOrientationAngle(myPoint.getReference(), 360.0);
  }

  /**
   * Test integrity/uniqueness of path names.
   */
  @Test
  public void testPathNameIntegrity()
      throws ObjectUnknownException, ObjectExistsException {
    // Add some points to the model.
    Point point1 = model.createPoint(null);
    Point point2 = model.createPoint(null);
    Point point3 = model.createPoint(null);
    Point point4 = model.createPoint(null);
    // Add some paths, too.
    Path path1
        = model.createPath(null, point1.getReference(), point2.getReference());
    Path path2
        = model.createPath(null, point2.getReference(), point3.getReference());
    Path path3
        = model.createPath(null, point3.getReference(), point4.getReference());
    Path path4
        = model.createPath(null, point4.getReference(), point1.getReference());
    // Try changing a non-existent path's name.
    try {
      model.removePath(path4.getReference());
      globalPool.renameObject(path4.getReference(), "pathY");
      fail("Should raise an ObjectUnknownException for non-existent names");
    }
    catch (ObjectUnknownException exc) {
      assertTrue(true);
    }
    // Try changing a path's name to one that exists already.
    try {
      globalPool.renameObject(path1.getReference(), path2.getName());
      fail("Should raise an ObjectExistsException for duplicate names");
    }
    catch (ObjectExistsException exc) {
      assertTrue(true);
    }
    // Try changing a path's name to that of an existing point.
    try {
      globalPool.renameObject(path3.getReference(), point1.getName());
      fail("Should raise an ObjectExistsException for duplicate names");
    }
    catch (ObjectExistsException exc) {
      assertTrue(true);
    }
  }
}
