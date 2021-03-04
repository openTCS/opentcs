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

/**
 * A boolean with an explanation/reason for its value.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ExplainedBoolean {

  /**
   * The actual value.
   */
  private final boolean isTrue;
  /**
   * A reason/explanation for the value.
   */
  private final String reason;

  /**
   * Creates a new instance.
   *
   * @param isTrue The actual value.
   * @param reason A reason/explanation for the value.
   */
  public ExplainedBoolean(boolean isTrue, @Nonnull String reason) {
    this.isTrue = isTrue;
    this.reason = requireNonNull(reason, "reason");
  }

  /**
   * Returns the actual value.
   *
   * @return The actual value.
   */
  public boolean isTrue() {
    return isTrue;
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
