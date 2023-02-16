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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;

/**
 * Unit tests for {@link TCSObjectRepository}.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TCSObjectRepositoryTest {

  private TCSObjectRepository pool;

  @BeforeEach
  public void setUp() {
    pool = new TCSObjectRepository();
  }

  @Test
  public void returnObjectByClassAndName() {
    Point point1 = new Point("Point-00001");
    Point point2 = new Point("Point-00002");

    pool.addObject(point1);
    pool.addObject(point2);

    assertThat(pool.getObjectOrNull(Point.class, "Point-00001"), is(point1));
    assertThat(pool.getObjectOrNull(Point.class, "Point-00002"), is(point2));
  }

  @Test
  public void returnObjectByName() {
    Point point1 = new Point("Point-00001");
    Point point2 = new Point("Point-00002");

    pool.addObject(point1);
    pool.addObject(point2);

    assertThat(pool.getObjectOrNull("Point-00001"), is(point1));
    assertThat(pool.getObjectOrNull("Point-00002"), is(point2));
  }

  @Test
  public void returnObjectByClassAndRef() {
    Point point1 = new Point("Point-00001");
    Point point2 = new Point("Point-00002");

    pool.addObject(point1);
    pool.addObject(point2);

    assertThat(pool.getObjectOrNull(Point.class, point1.getReference()), is(point1));
    assertThat(pool.getObjectOrNull(Point.class, point2.getReference()), is(point2));
  }

  @Test
  public void returnObjectByRef() {
    Point point1 = new Point("Point-00001");
    Point point2 = new Point("Point-00002");

    pool.addObject(point1);
    pool.addObject(point2);

    assertThat(pool.getObjectOrNull(point1.getReference()), is(point1));
    assertThat(pool.getObjectOrNull(point2.getReference()), is(point2));
  }

  @Test
  public void returnObjectsByClass() {
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
  public void replaceObjectWithSameName() {
    Point pointV1 = new Point("some-point").withType(Point.Type.HALT_POSITION);
    Point pointV2 = pointV1.withType(Point.Type.PARK_POSITION);

    pool.addObject(pointV1);
    pool.replaceObject(pointV2);

    assertThat(pool.getObjects(Point.class).size(), is(1));
    assertThat(pool.getObjects(Point.class), contains(pointV2));
  }

  @Test
  public void throwOnReplaceObjectWithNonexistentName() {
    Point point1 = new Point("some-point").withType(Point.Type.HALT_POSITION);
    Point point2 = new Point("some-other-point").withType(Point.Type.PARK_POSITION);

    pool.addObject(point1);
    assertThrows(IllegalArgumentException.class, () -> pool.replaceObject(point2));
  }

  @Test
  public void throwOnReplaceObjectWithDifferentType() {
    Point point = new Point("my-object");
    LocationType locationType = new LocationType("my-object");

    pool.addObject(point);
    assertThrows(IllegalArgumentException.class, () -> pool.replaceObject(locationType));
  }

  @Test
  public void removeObjectByRef() {
    Point point1 = new Point("Point-00001");

    pool.addObject(point1);
    pool.removeObject(point1.getReference());

    assertThat(pool.getObjectOrNull(point1.getReference()), is(nullValue()));
  }

  @Test
  public void throwOnDuplicateName() {
    pool.addObject(new Point("some-point"));
    // Another object with the same name.
    assertThrows(ObjectExistsException.class, () -> pool.addObject(new Point("some-point")));
  }
}
