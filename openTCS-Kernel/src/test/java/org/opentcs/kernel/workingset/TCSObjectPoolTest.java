/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
/*
 *
 * Created on June 8, 2006, 9:15 AM
 */
package org.opentcs.kernel.workingset;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.SimpleEventBus;

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
    pool = new TCSObjectPool(new SimpleEventBus());
  }

  @After
  public void tearDown() {
    pool = null;
  }

  @Test
  public void shouldReturnObjectByClassAndName() {
    Point point1 = new Point("Point-00001");
    pool.addObject(point1);
    Point point2 = new Point("Point-00002");
    pool.addObject(point2);

    assertEquals(point1, pool.getObjectOrNull(Point.class, "Point-00001"));
    assertEquals(point2, pool.getObjectOrNull(Point.class, "Point-00002"));
  }

  @Test
  public void shouldReturnObjectByName() {
    Point point1 = new Point("Point-00001");
    pool.addObject(point1);
    Point point2 = new Point("Point-00002");
    pool.addObject(point2);

    assertEquals(point1, pool.getObjectOrNull("Point-00001"));
    assertEquals(point2, pool.getObjectOrNull("Point-00002"));
  }

  @Test
  public void shouldReturnObjectByClassAndRef() {
    Point point1 = new Point("Point-00001");
    pool.addObject(point1);
    Point point2 = new Point("Point-00002");
    pool.addObject(point2);

    assertEquals(point1, pool.getObjectOrNull(Point.class, point1.getReference()));
    assertEquals(point2, pool.getObjectOrNull(Point.class, point2.getReference()));
  }

  @Test
  public void shouldReturnObjectByRef() {
    Point point1 = new Point("Point-00001");
    pool.addObject(point1);
    Point point2 = new Point("Point-00002");
    pool.addObject(point2);

    assertEquals(point1, pool.getObjectOrNull(point1.getReference()));
    assertEquals(point2, pool.getObjectOrNull(point2.getReference()));
  }

  @Test
  public void shouldReturnObjectsByClassAndPattern() {
    Point point1 = new Point("Point-00001");
    pool.addObject(point1);
    Point point2 = new Point("Point-00002");
    pool.addObject(point2);
    Path path1 = new Path("Point-00003", point1.getReference(), point2.getReference());
    pool.addObject(path1);

    Set<Point> points = pool.getObjects(Point.class, Pattern.compile("Point.*"));

    assertEquals(2, points.size());
    assertTrue(points.contains(point1));
    assertTrue(points.contains(point2));
  }

  @Test
  public void shouldReturnObjectsByClass() {
    Point point1 = new Point("Point-00001");
    pool.addObject(point1);
    Point point2 = new Point("Point-00002");
    pool.addObject(point2);
    Path path1 = new Path("Path-00001", point1.getReference(), point2.getReference());
    pool.addObject(path1);

    Set<Point> points = pool.getObjects(Point.class);

    assertEquals(2, points.size());
    assertTrue(points.contains(point1));
    assertTrue(points.contains(point2));
  }

  @Test
  public void shouldReturnObjectsByPattern() {
    Point point1 = new Point("Point-00001");
    pool.addObject(point1);
    Point point2 = new Point("Point-00002");
    pool.addObject(point2);
    Point point3 = new Point("Punkt-00003");
    pool.addObject(point3);

    Set<Point> points = pool.getObjects(Point.class, Pattern.compile("Point.*"));

    assertEquals(2, points.size());
    assertTrue(points.contains(point1));
    assertTrue(points.contains(point2));
  }

  @Test
  public void shouldRemoveObjectByRef() {
    Point point1 = new Point("Point-00001");
    pool.addObject(point1);
    assertEquals(point1, pool.getObjectOrNull("Point-00001"));
    pool.removeObject(point1.getReference());
    assertNull(pool.getObjectOrNull("Point-00001"));
  }

  @Test
  public void shouldRemoveObjectsByName() {
    Point point1 = new Point("Point-00001");
    pool.addObject(point1);
    Point point2 = new Point("Point-00002");
    pool.addObject(point2);
    assertEquals(point1, pool.getObjectOrNull(point1.getReference()));
    assertEquals(point2, pool.getObjectOrNull(point2.getReference()));

    Set<String> names = new HashSet<>();
    names.add("Point-00001");
    names.add("Point-00002");
    pool.removeObjects(names);

    assertNull(pool.getObjectOrNull("Point-00001"));
    assertNull(pool.getObjectOrNull("Point-00002"));
  }

  @Test
  public void shouldEmitEventForCreatedObject() {
    EventBus eventBus = new SimpleEventBus();

    List<Object> receivedEvents = new LinkedList<>();

    eventBus.subscribe((event) -> {
      receivedEvents.add(event);
    });

    pool = new TCSObjectPool(eventBus);
    Point point1 = new Point("Point-00001");
    pool.addObject(point1);

    assertEquals(0, receivedEvents.size());
    pool.emitObjectEvent(point1, point1, TCSObjectEvent.Type.OBJECT_CREATED);
    assertEquals(1, receivedEvents.size());
  }

  @Test(expected = ObjectExistsException.class)
  public void shouldThrowIfAddingExistingName() {
    // A few initial objects
    Point point1 = new Point("Point-00001");
    pool.addObject(point1);
    Point point2 = new Point("Point-00002");
    pool.addObject(point2);
    Path path1 = new Path("Path-00001", point1.getReference(), point2.getReference());
    pool.addObject(path1);
    Path path2 = new Path("Path-00002", point2.getReference(), point1.getReference());
    pool.addObject(path2);
    // A misnamed/duplicate object
    pool.addObject(new Point("Path-00002"));
  }

  /**
   * Verify that the pool generates unique object names.
   */
  @Test(expected = ObjectExistsException.class)
  @SuppressWarnings("deprecation")
  public void testUniqueNameGenerator() {
    String prefix = "ABC";
    String suffixPattern = "000";
    for (int i = 1; i <= 100; i++) {
      String curName = pool.getUniqueObjectName(prefix, suffixPattern);
      pool.addObject(new Point(curName));
    }
    // Add a name that should already exist in the pool, and expect an exception.
    pool.addObject(new Point("ABC050"));
  }
}
