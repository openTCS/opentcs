/*
 *
 * Created on June 8, 2006, 9:15 AM
 */
package org.opentcs.kernel.workingset;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.listener.Handler;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.util.eventsystem.TCSEvent;

/**
 * A test class for TCSObjectPool.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TCSObjectPoolTest {

  /**
   * The pool to be tested here.
   */
  private TCSObjectPool pool;

  @Before
  public void setUp() {
    pool = new TCSObjectPool(new MBassador<>(BusConfiguration.Default()));
  }

  @After
  public void tearDown() {
    pool = null;
  }

  /**
   * Test for method getObject(Class<T> clazz, String name)
   */
  @Test
  public void testGetObjectByClassAndName() {
    Point point1 = new Point(pool.getUniqueObjectId(), "Point-00001");
    pool.addObject(point1);
    Point point2 = new Point(pool.getUniqueObjectId(), "Point-00002");
    pool.addObject(point2);

    assertEquals(point1, pool.getObject(Point.class, "Point-00001"));
    assertEquals(point2, pool.getObject(Point.class, "Point-00002"));
  }

  /**
   * Test for method getObject(String name)
   */
  @Test
  public void testGetObjectByName() {
    Point point1 = new Point(pool.getUniqueObjectId(), "Point-00001");
    pool.addObject(point1);
    Point point2 = new Point(pool.getUniqueObjectId(), "Point-00002");
    pool.addObject(point2);

    assertEquals(point1, pool.getObject("Point-00001"));
    assertEquals(point2, pool.getObject("Point-00002"));
  }

  /**
   * Test for method getObject(Class<T> clazz, TCSObjectReference<T> ref)
   */
  @Test
  public void testGetObjectByClassAndRef() {
    Point point1 = new Point(pool.getUniqueObjectId(), "Point-00001");
    pool.addObject(point1);
    Point point2 = new Point(pool.getUniqueObjectId(), "Point-00002");
    pool.addObject(point2);

    assertEquals(point1, pool.getObject(Point.class, point1.getReference()));
    assertEquals(point2, pool.getObject(Point.class, point2.getReference()));
  }

  /**
   * Test for method getObject(TCSObjectReference<?> ref)
   */
  @Test
  public void testGetObjectByRef() {
    Point point1 = new Point(pool.getUniqueObjectId(), "Point-00001");
    pool.addObject(point1);
    Point point2 = new Point(pool.getUniqueObjectId(), "Point-00002");
    pool.addObject(point2);

    assertEquals(point1, pool.getObject(point1.getReference()));
    assertEquals(point2, pool.getObject(point2.getReference()));
  }

  /**
   * Test for method getObjects(Class<T> clazz, Pattern regexp)
   */
  @Test
  public void testGetObjectsByClassAndPattern() {
    Point point1 = new Point(pool.getUniqueObjectId(), "Point-00001");
    pool.addObject(point1);
    Point point2 = new Point(pool.getUniqueObjectId(), "Point-00002");
    pool.addObject(point2);
    Path path1 = new Path(pool.getUniqueObjectId(), "Point-00003",
                          point1.getReference(), point2.getReference());
    pool.addObject(path1);

    Set<Point> points = pool.getObjects(Point.class, Pattern.compile("Point.*"));

    Iterator<Point> it = points.iterator();
    assertEquals(2, points.size());
    assertEquals(point1, it.next());
    assertEquals(point2, it.next());
  }

  /**
   * Test for method getObjects(Class<T> clazz)
   */
  @Test
  public void testGetObjectsByClass() {
    Point point1 = new Point(pool.getUniqueObjectId(), "Point-00001");
    pool.addObject(point1);
    Point point2 = new Point(pool.getUniqueObjectId(), "Point-00002");
    pool.addObject(point2);
    Path path1 = new Path(pool.getUniqueObjectId(), "Path-00001",
                          point1.getReference(), point2.getReference());
    pool.addObject(path1);

    Set<Point> points = pool.getObjects(Point.class);

    Iterator<Point> it = points.iterator();
    assertEquals(2, points.size());
    assertEquals(point1, it.next());
    assertEquals(point2, it.next());
  }

  /**
   * Test for method getObjects(Pattern regexp)
   */
  @Test
  public void testGetObjectsByPattern() {
    Point point1 = new Point(pool.getUniqueObjectId(), "Point-00001");
    pool.addObject(point1);
    Point point2 = new Point(pool.getUniqueObjectId(), "Point-00002");
    pool.addObject(point2);
    Point point3 = new Point(pool.getUniqueObjectId(), "Punkt-00003");
    pool.addObject(point3);

    Set<Point> points = pool.getObjects(Point.class, Pattern.compile("Point.*"));

    Iterator<Point> it = points.iterator();
    assertEquals(2, points.size());
    assertEquals(point1, it.next());
    assertEquals(point2, it.next());
  }

  /**
   * Test for method removeObject(TCSObjectReference<?> ref)
   */
  @Test
  public void testRemoveObjectByRef() {
    Point point1 = new Point(pool.getUniqueObjectId(), "Point-00001");
    pool.addObject(point1);
    assertEquals(point1, pool.getObject("Point-00001"));
    pool.removeObject(point1.getReference());
    assertNull(pool.getObject("Point-00001"));
  }

  /**
   * Test for method removeObjects(Set<String> objectNames)
   */
  @Test
  public void testRemoveObjectsByName() {
    Point point1 = new Point(pool.getUniqueObjectId(), "Point-00001");
    pool.addObject(point1);
    Point point2 = new Point(pool.getUniqueObjectId(), "Point-00002");
    pool.addObject(point2);
    assertEquals(point1, pool.getObject(point1.getReference()));
    assertEquals(point2, pool.getObject(point2.getReference()));

    Set<String> names = new HashSet<>();
    names.add("Point-00001");
    names.add("Point-00002");
    pool.removeObjects(names);

    assertNull(pool.getObject("Point-00001"));
    assertNull(pool.getObject("Point-00002"));
  }

  /**
   * Test for method emitObjectEvent()
   */
  @Test
  public void shouldEmitEventForCreatedObject() {
    MBassador<Object> eventBus = new MBassador<>(BusConfiguration.Default());

    List<TCSEvent> receivedEvents = new LinkedList<>();
    Object eventHandler = new Object() {
      @Handler
      public void handleEvent(TCSEvent event) {
        receivedEvents.add(event);
      }
    };
    eventBus.subscribe(eventHandler);
    pool = new TCSObjectPool(eventBus);
    Point point1 = new Point(pool.getUniqueObjectId(), "Point-00001");
    pool.addObject(point1);

    assertEquals(0, receivedEvents.size());
    pool.emitObjectEvent(point1, point1, TCSObjectEvent.Type.OBJECT_CREATED);
    assertEquals(1, receivedEvents.size());
  }

  /**
   * Verify that adding objects with duplicate names is refused by the pool.
   */
  @Test(expected = ObjectExistsException.class)
  public void testAddingDuplicateNames() {
    // A few initial objects
    Point point1 = new Point(pool.getUniqueObjectId(), "Point-00001");
    pool.addObject(point1);
    Point point2 = new Point(pool.getUniqueObjectId(), "Point-00002");
    pool.addObject(point2);
    Path path1 = new Path(pool.getUniqueObjectId(), "Path-00001",
                          point1.getReference(), point2.getReference());
    pool.addObject(path1);
    Path path2 = new Path(pool.getUniqueObjectId(), "Path-00002",
                          point2.getReference(), point1.getReference());
    pool.addObject(path2);
    // A misnamed/duplicate object
    pool.addObject(new Point(pool.getUniqueObjectId(), "Path-00002"));
  }

  /**
   * Verify that the pool generates unique object names.
   */
  @Test(expected = ObjectExistsException.class)
  public void testUniqueNameGenerator() {
    String prefix = "ABC";
    String suffixPattern = "000";
    for (int i = 1; i <= 100; i++) {
      String curName = pool.getUniqueObjectName(prefix, suffixPattern);
      pool.addObject(new Point(pool.getUniqueObjectId(), curName));
    }
    // Add a name that should already exist in the pool, and expect an exception.
    pool.addObject(new Point(pool.getUniqueObjectId(), "ABC050"));
  }

  @Test(expected = ObjectUnknownException.class)
  public void testRenamingUnknownObjectThrowsException() {
    // A few initial objects
    Point point1 = new Point(pool.getUniqueObjectId(), "Point-00001");
    pool.addObject(point1);
    Point point2 = new Point(pool.getUniqueObjectId(), "Point-00002");
    pool.addObject(point2);
    Point point3 = new Point(pool.getUniqueObjectId(), "Point-00003");
    pool.addObject(point3);
    Path path1 = new Path(pool.getUniqueObjectId(), "Path-00001",
                          point1.getReference(), point2.getReference());
    pool.addObject(path1);
    Path path2 = new Path(pool.getUniqueObjectId(), "Path-00002",
                          point2.getReference(), point1.getReference());
    pool.addObject(path2);
    // Try to rename an object that does not exist.
    TCSObjectReference<Point> point3Ref = point3.getReference();
    pool.removeObject(point3Ref);
    pool.renameObject(point3Ref, "Path-00002");
  }

  @Test(expected = ObjectExistsException.class)
  public void testRenamingToExistingNameThrowsException() {
    // A few initial objects
    Point point1 = new Point(pool.getUniqueObjectId(), "Point-00001");
    pool.addObject(point1);
    Point point2 = new Point(pool.getUniqueObjectId(), "Point-00002");
    pool.addObject(point2);
    Point point3 = new Point(pool.getUniqueObjectId(), "Point-00003");
    pool.addObject(point3);
    Path path1 = new Path(pool.getUniqueObjectId(), "Path-00001",
                          point1.getReference(), point2.getReference());
    pool.addObject(path1);
    Path path2 = new Path(pool.getUniqueObjectId(), "Path-00002",
                          point2.getReference(), point1.getReference());
    pool.addObject(path2);
    // Try to change an object's name to one that exists already.
    pool.renameObject(point1.getReference(), "Path-00002");
  }

}
