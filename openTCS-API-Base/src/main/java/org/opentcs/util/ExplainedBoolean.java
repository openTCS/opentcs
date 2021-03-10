/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A boolean with an explanation/reason for its value.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ExplainedBoolean {

  /**
   * The actual value.
   */
  private final boolean value;
  /**
   * A reason/explanation for the value.
   */
  private final String reason;

  /**
   * Creates a new instance.
   *
   * @param value The actual value.
   * @param reason A reason/explanation for the value.
   */
  public ExplainedBoolean(boolean value, @Nonnull String reason) {
    this.value = value;
    this.reason = requireNonNull(reason, "reason");
  }

  /**
   * Returns the actual value.
   *
   * @return The actual value.
   */
  public boolean getValue() {
    return value;
  }

  /**
   * Returns the actual value.
   *
   * @return The actual value.
   * @deprecated Use {@link #getValue()} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public boolean isTrue() {
    return value;
  }

  /**
   * A reason/explanation for the value.
   *
   * @return The reason
   */
  @Nonnull
  public String getReason() {
    return reason;
  }
}
