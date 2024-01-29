/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import java.util.Set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;

/**
 * Unit tests for {@link TCSObjectRepository}.
 */
class TCSObjectRepositoryTest {

  private TCSObjectRepository pool;

  @BeforeEach
  void setUp() {
    pool = new TCSObjectRepository();
  }

  @Test
  void returnObjectByClassAndName() {
    Point point1 = new Point("Point-00001");
    Point point2 = new Point("Point-00002");

    pool.addObject(point1);
    pool.addObject(point2);

    assertThat(pool.getObjectOrNull(Point.class, "Point-00001"), is(point1));
    assertThat(pool.getObjectOrNull(Point.class, "Point-00002"), is(point2));
    assertThat(pool.getObject(Point.class, "Point-00001"), is(point1));
    assertThat(pool.getObject(Point.class, "Point-00002"), is(point2));
  }

  @Test
  void returnNullForNonexistentObjectByClassAndName() {
    assertThat(pool.getObjectOrNull(Point.class, "some-name"), is(nullValue()));
  }

  @Test
  void throwOnGetNonexistentObjectByClassAndName() {
    assertThrows(ObjectUnknownException.class, () -> pool.getObject(Point.class, "some-name"));
  }

  @Test
  void returnObjectByName() {
    Point point1 = new Point("Point-00001");
    Point point2 = new Point("Point-00002");

    pool.addObject(point1);
    pool.addObject(point2);

    assertThat(pool.getObjectOrNull("Point-00001"), is(point1));
    assertThat(pool.getObjectOrNull("Point-00002"), is(point2));
    assertThat(pool.getObject("Point-00001"), is(point1));
    assertThat(pool.getObject("Point-00002"), is(point2));
  }

  @Test
  void returnNullForNonexistentObjectByName() {
    assertThat(pool.getObjectOrNull("some-name"), is(nullValue()));
  }

  @Test
  void throwOnGetNonexistentObjectByName() {
    assertThrows(ObjectUnknownException.class, () -> pool.getObject("some-name"));
  }

  @Test
  void returnObjectByClassAndRef() {
    Point point1 = new Point("Point-00001");
    Point point2 = new Point("Point-00002");

    pool.addObject(point1);
    pool.addObject(point2);

    assertThat(pool.getObjectOrNull(Point.class, point1.getReference()), is(point1));
    assertThat(pool.getObjectOrNull(Point.class, point2.getReference()), is(point2));
    assertThat(pool.getObject(Point.class, point1.getReference()), is(point1));
    assertThat(pool.getObject(Point.class, point2.getReference()), is(point2));
  }

  @Test
  void returnNullForNonexistentObjectByClassAndRef() {
    assertThat(pool.getObjectOrNull(Point.class, new Point("some-point").getReference()),
               is(nullValue()));
  }

  @Test
  void throwOnGetNonexistentObjectByClassAndRef() {
    assertThrows(ObjectUnknownException.class,
                 () -> pool.getObject(Point.class, new Point("some-point").getReference()));
  }

  @Test
  void returnObjectByRef() {
    Point point1 = new Point("Point-00001");
    Point point2 = new Point("Point-00002");

    pool.addObject(point1);
    pool.addObject(point2);

    assertThat(pool.getObjectOrNull(point1.getReference()), is(point1));
    assertThat(pool.getObjectOrNull(point2.getReference()), is(point2));
    assertThat(pool.getObject(point1.getReference()), is(point1));
    assertThat(pool.getObject(point2.getReference()), is(point2));
  }

  @Test
  void returnNullForNonexistentObjectByRef() {
    assertThat(pool.getObjectOrNull(new Point("some-point").getReference()), is(nullValue()));
  }

  @Test
  void throwOnGetNonexistentObjectByRef() {
    Point point = new Point("some-point");

    assertThrows(ObjectUnknownException.class, () -> pool.getObject(point.getReference()));
  }

  @Test
  void returnObjectsByClass() {
    Point point1 = new Point("Point-00001");
    Point point2 = new Point("Point-00002");
    Path path1 = new Path("Path-00001", point1.getReference(), point2.getReference());

    pool.addObject(point1);
    pool.addObject(point2);
    pool.addObject(path1);

    Set<Point> points = pool.getObjects(Point.class);
    Set<Path> paths = pool.getObjects(Path.class);

    assertThat(points.size(), is(2));
    assertThat(points, containsInAnyOrder(point1, point2));

    assertThat(paths.size(), is(1));
    assertThat(paths, contains(path1));
  }

  @Test
  void returnObjectsByClassAndPredicate() {
    Point point1 = new Point("Point-00001");
    Point point2 = new Point("Point-00002");
    Path path1 = new Path("Path-00001", point1.getReference(), point2.getReference());

    pool.addObject(point1);
    pool.addObject(point2);
    pool.addObject(path1);

    Set<Point> points = pool.getObjects(Point.class, point -> true);
    Set<Path> paths = pool.getObjects(Path.class, path -> false);

    assertThat(points.size(), is(2));
    assertThat(points, containsInAnyOrder(point1, point2));

    assertThat(paths, is(empty()));
  }

  @Test
  void replaceObjectWithSameName() {
    Point pointV1 = new Point("some-point").withType(Point.Type.HALT_POSITION);
    Point pointV2 = pointV1.withType(Point.Type.PARK_POSITION);

    pool.addObject(pointV1);
    pool.replaceObject(pointV2);

    assertThat(pool.getObjects(Point.class).size(), is(1));
    assertThat(pool.getObjects(Point.class), contains(pointV2));
  }

  @Test
  void throwOnReplaceObjectWithNonexistentName() {
    Point point1 = new Point("some-point").withType(Point.Type.HALT_POSITION);
    Point point2 = new Point("some-other-point").withType(Point.Type.PARK_POSITION);

    pool.addObject(point1);
    assertThrows(IllegalArgumentException.class, () -> pool.replaceObject(point2));
  }

  @Test
  void throwOnReplaceObjectWithDifferentType() {
    Point point = new Point("my-object");
    LocationType locationType = new LocationType("my-object");

    pool.addObject(point);
    assertThrows(IllegalArgumentException.class, () -> pool.replaceObject(locationType));
  }

  @Test
  void removeObjectByRef() {
    Point point1 = new Point("Point-00001");

    pool.addObject(point1);
    pool.removeObject(point1.getReference());

    assertThat(pool.getObjectOrNull(point1.getReference()), is(nullValue()));
  }

  @Test
  void throwOnRemoveNonexistentObjectByRef() {
    assertThrows(ObjectUnknownException.class,
                 () -> pool.removeObject(new Point("some-point").getReference()));
  }

  @Test
  void throwOnAddObjectWithExistingName() {
    pool.addObject(new Point("some-point"));
    // Another object with the same name.
    assertThrows(ObjectExistsException.class, () -> pool.addObject(new Point("some-point")));
  }
}
