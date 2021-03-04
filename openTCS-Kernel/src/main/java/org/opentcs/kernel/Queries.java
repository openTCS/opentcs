/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.opentcs.access.Kernel;

/**
 * Convenience methods for working with queries.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@SuppressWarnings("deprecation")
public final class Queries {

  /**
   * An unmodifiable set of all queries.
   */
  private static final Set<Class<? extends org.opentcs.access.queries.Query<?>>> allQueries;

  static {
    Set<Class<? extends org.opentcs.access.queries.Query<?>>> queries = new HashSet<>();
    queries.add(org.opentcs.access.queries.QueryAvailableScriptFiles.class);
    queries.add(org.opentcs.access.queries.QueryRecoveryStatus.class);
    queries.add(org.opentcs.access.queries.QueryRoutingInfo.class);
    queries.add(org.opentcs.access.queries.QuerySchedulerAllocations.class);
    queries.add(org.opentcs.access.queries.QueryTopologyInfo.class);
    allQueries = Collections.unmodifiableSet(queries);
  }

  /**
   * Prevents instantiation.
   */
  private Queries() {
    // Do nada.
  }

  /**
   * Returns an unmodifiable set of all queries.
   *
   * @return An unmodifiable set of all queries.
   */
  public static Set<Class<? extends org.opentcs.access.queries.Query<?>>> getAllQueries() {
    return allQueries;
  }

  /**
   * Returns a set of queries that are in the given set of queries and are
   * available in the given kernel state.
   *
   * @param queries The queries to be filtered.
   * @param state The state in which the returned queries must be available.
   * @return A set of queries that are in the given set of queries and that are
   * available in the given kernel state. If there are no such queries, the
   * returned set is empty.
   */
  public static Set<Class<? extends org.opentcs.access.queries.Query<?>>> availableInState(
      Set<Class<? extends org.opentcs.access.queries.Query<?>>> queries,
      Kernel.State state) {
    Objects.requireNonNull(queries, "queries is null");
    Objects.requireNonNull(state, "state is null");

    Set<Class<? extends org.opentcs.access.queries.Query<?>>> result = new HashSet<>();
    for (Class<? extends org.opentcs.access.queries.Query<?>> query : queries) {
      if (availableInState(query, state)) {
        result.add(query);
      }
    }
    return result;
  }

  /**
   * Returns a set of queries that are available in the given kernel state.
   *
   * @param state The state in which the returned queries must be available.
   * @return A set of queries that are available in the given kernel state. If
   * there are no such queries, the returned set is empty.
   */
  public static Set<Class<? extends org.opentcs.access.queries.Query<?>>> availableInState(
      Kernel.State state) {
    return availableInState(allQueries, state);
  }

  /**
   * Checks whether the given query is available in the given kernel state.
   *
   * @param query The query to be checked.
   * @param state The kernel state.
   * @return <code>true</code> if, and only if, the given query is available
   * in the given kernel state.
   */
  public static boolean availableInState(Class<? extends org.opentcs.access.queries.Query<?>> query,
                                         Kernel.State state) {
    Objects.requireNonNull(query, "query is null");
    Objects.requireNonNull(state, "state is null");

    org.opentcs.access.queries.Availability availability
        = query.getAnnotation(org.opentcs.access.queries.Availability.class);
    for (Kernel.State availState : availability.value()) {
      if (state.equals(availState)) {
        return true;
      }
    }
    return false;
  }
}
