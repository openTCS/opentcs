/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data;

import java.io.Serializable;
import java.util.regex.Pattern;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A filter for <code>TCSObjectEvent</code>s.
 * <p>
 * A <code>TCSObjectEventFilter</code> accepts <code>TCSObjectEvent</code>s
 * only; other events are never accepted.
 * </p>
 * <p>
 * A <code>TCSObjectEventFilter</code> uses two criteria to check if a
 * <code>TCSObjectEvent</code> is acceptable or not - the name of the object
 * associated with the event and the name of that object's implementing class.
 * For each of the two a <code>Pattern</code> is defined, and every event will
 * be checked against these.
 * </p>
 * <p>
 * Any of the two <code>Pattern</code>s may be <code>null</code>, meaning that
 * matching based on the respective attribute is not wanted. That is, if the
 * pattern for class name matching is <code>null</code>, only the object names
 * have to match; if the pattern for object name matching is <code>null</code>,
 * only the class names have to match. By default, events are <em>not</em>
 * accepted, so if both the pattern for class name matching and the one for
 * object name matching are <code>null</code>, all events will be refused.
 * </p>
 * <p>
 * Instances of this class are immutable and can safely be used by concurrent
 * threads.
 * </p>
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated {@link org.opentcs.util.eventsystem.EventFilter} is deprecated.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public class TCSObjectEventFilter
    implements org.opentcs.util.eventsystem.EventFilter<org.opentcs.util.eventsystem.TCSEvent>,
               Serializable {

  /**
   * A filter instance that does not accept any events.
   */
  public static final TCSObjectEventFilter ACCEPT_NONE
      = new TCSObjectEventFilter(null, null);
  /**
   * A filter instance that accepts all TCSObjectEvents.
   */
  public static final TCSObjectEventFilter ACCEPT_ALL
      = new TCSObjectEventFilter(Pattern.compile(".*"), null);
  /**
   * The pattern for matching class names of objects associated with events.
   */
  private final Pattern classNamePattern;
  /**
   * The pattern for matching names of objects associated with events.
   */
  private final Pattern objectNamePattern;

  /**
   * Creates a new TCSObjectEventFilter with the given parameters.
   * Note that, if both parameters are <code>null</code>, the filter will not
   * accept any event.
   *
   * @param acceptedClassNames A <code>Pattern</code> for matching names of
   * classes of the objects associated with events. May be <code>null</code> to
   * express that matching based on the class name is not wanted.
   * @param acceptedObjectNames A <code>Pattern</code> for matching names of
   * the objects associated with events. May be <code>null</code> to express
   * that matching based on the object name is not wanted.
   */
  public TCSObjectEventFilter(Pattern acceptedClassNames,
                              Pattern acceptedObjectNames) {
    classNamePattern = acceptedClassNames;
    objectNamePattern = acceptedObjectNames;
  }

  @Override
  public boolean accept(org.opentcs.util.eventsystem.TCSEvent event) {
    boolean result = false;
    if (!(event instanceof TCSObjectEvent)) {
      return false;
    }
    TCSObjectEvent objEvent = (TCSObjectEvent) event;
    TCSObject<?> obj = objEvent.getCurrentOrPreviousObjectState();
    if (classNamePattern == null) {
      if (objectNamePattern != null) {
        result = objectNamePattern.matcher(obj.getName()).matches();
      }
    }
    else {
      result = classNamePattern.matcher(obj.getClass().getName()).matches();
      if (objectNamePattern != null) {
        result = result && objectNamePattern.matcher(obj.getName()).matches();
      }
    }
    return result;
  }

  /**
   * Returns this filter's pattern for class name matching.
   *
   * @return this filter's pattern for class name matching.
   */
  public Pattern getClassNamePattern() {
    return classNamePattern;
  }

  /**
   * Returns this filter's pattern for object name matching.
   *
   * @return this filter's pattern for object name matching.
   */
  public Pattern getObjectNamePattern() {
    return objectNamePattern;
  }
}
