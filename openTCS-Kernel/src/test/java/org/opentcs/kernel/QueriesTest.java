/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import org.junit.*;
import static org.junit.Assert.fail;
import org.opentcs.access.queries.Availability;
import org.opentcs.access.queries.Query;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class QueriesTest {
  
  /**
   * Verify that all queries returned by Queries.getAllQueries() are annotated
   * with Availability.
   */
  @Test
  public void testIfAllQueriesHaveAvailabilityAnnotations() {
    for (Class<? extends Query<?>> clazz : Queries.getAllQueries()) {
      Availability availability = clazz.getAnnotation(Availability.class);
      if (availability == null) {
        fail("Class " + clazz.getName() + " lacks annotation Availability!");
      }
    }
  }
}
