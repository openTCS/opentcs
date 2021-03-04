/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data;

import java.io.Serializable;
import static java.util.Objects.requireNonNull;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Instances of this class represent events emitted by/for business objects.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@ScheduledApiChange(when = "5.0", details = "Will not extend TCSEvent any more.")
@SuppressWarnings("deprecation")
public class TCSObjectEvent
    extends org.opentcs.util.eventsystem.TCSEvent
    implements Serializable {

  /**
   * The current state of the object for which this event was created.
   */
  private final TCSObject<?> currentObjectState;
  /**
   * The previous state of the object for which this event was created.
   */
  private final TCSObject<?> previousObjectState;
  /**
   * This event's type.
   */
  private final Type type;

  /**
   * Creates a new TCSObjectEvent.
   *
   * @param currentObjectState The current state of the object for which this
   * event was created. Value is irrelevant/may be <code>null</code> if
   * <code>eventType</code> is <code>OBJECT_REMOVED</code>.
   * @param previousObjectState The previous state of the object for which this
   * event was created.Value is irrelevant/may be <code>null</code> if
   * <code>eventType</code> is <code>OBJECT_CREATED</code>.
   * @param eventType The event's type.
   * @throws NullPointerException If <code>eventType</code> is
   * <code>null</code>.
   * @throws IllegalArgumentException If either <code>currentObjectState</code>
   * or <code>previousObjectState</code> is <code>null</code> while
   * <code>eventType</code> does not have an appropriate value.
   */
  public TCSObjectEvent(TCSObject<?> currentObjectState,
                        TCSObject<?> previousObjectState,
                        Type eventType) {
    this.type = requireNonNull(eventType, "eventType");
    if (currentObjectState == null && !Type.OBJECT_REMOVED.equals(eventType)) {
      throw new IllegalArgumentException("currentObjectState == null but "
          + "eventType != OBJECT_REMOVED");
    }
    if (previousObjectState == null && !Type.OBJECT_CREATED.equals(eventType)) {
      throw new IllegalArgumentException("previousObjectState == null but "
          + "eventType != OBJECT_CREATED");
    }
    this.currentObjectState = currentObjectState;
    this.previousObjectState = previousObjectState;
  }

  /**
   * Returns the current state of the object for which this event was created.
   *
   * @return The current state of the object for which this event was created.
   */
  public TCSObject<?> getCurrentObjectState() {
    return currentObjectState;
  }

  /**
   * Returns the previous state of the object for which this event was created.
   *
   * @return The previous state of the object for which this event was created.
   */
  public TCSObject<?> getPreviousObjectState() {
    return previousObjectState;
  }

  /**
   * Returns the current state of the object for which this event was created,
   * or, if the current state is <code>null</code>, the previous state.
   *
   * @return The current or the previous state of the object for which this
   * event was created.
   */
  public TCSObject<?> getCurrentOrPreviousObjectState() {
    if (currentObjectState != null) {
      return currentObjectState;
    }
    else {
      return previousObjectState;
    }
  }

  /**
   * Returns this event's type.
   *
   * @return This event's type.
   */
  public Type getType() {
    return type;
  }

  @Override
  public String toString() {
    return "TCSObjectEvent{"
        + "type=" + type
        + ", currentObjectState=" + currentObjectState
        + ", previousObjectState=" + previousObjectState
        + '}';
  }

  /**
   * Indicates the type of an event, which can be helpful with filtering events.
   */
  public static enum Type {

    /**
     * Indicates that the referenced object has been newly created.
     */
    OBJECT_CREATED,
    /**
     * Indicates that the referenced object has been modified.
     */
    OBJECT_MODIFIED,
    /**
     * Indicates that the referenced object is no longer a valid kernel object.
     */
    OBJECT_REMOVED;
  }
}
