/*
 */
package org.opentcs.kernel.queries;

import org.junit.*;
import static org.junit.Assert.fail;
import org.opentcs.access.queries.Availability;
import org.opentcs.access.queries.Queries;
import org.opentcs.access.queries.Query;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class QueriesTest {
  
  @Before
  public void setUp() {
    // Do nada.
  }
  
  @After
  public void tearDown() {
    // Do nada.
  }
  
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
