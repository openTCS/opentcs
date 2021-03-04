/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Checks whether the input argument is assignable to any of a given set of classes.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class ClassMatcher
    implements Predicate<Object>,
               Serializable {

  /**
   * The set of classes to evaluate incoming events to.
   */
  private final Set<Class<?>> clazzes = new HashSet<>();

  /**
   * Creates a new instance.
   *
   * @param events The set of classes to evaluate incoming events to.
   */
  public ClassMatcher(Class<?>... events) {
    clazzes.addAll(Arrays.asList(events));
  }

  @Override
  public boolean test(Object object) {
    return clazzes.stream().anyMatch(clazz -> clazz.isAssignableFrom(object.getClass()));
  }

  @Override
  public Predicate<Object> negate() {
    return new ClassMatcher() {
      @Override
      public boolean test(Object t) {
        return !ClassMatcher.this.test(t);
      }
    };
  }
}
