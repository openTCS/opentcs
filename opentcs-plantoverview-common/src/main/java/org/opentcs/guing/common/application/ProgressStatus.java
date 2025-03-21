// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.application;

/**
 * A state of progress, to be used with {@link ProgressIndicator}.
 */
public interface ProgressStatus {

  /**
   * Returns a percentage value.
   *
   * @return A percentage value, greater than or equal to 0 and less than or equal to 100.
   */
  int getPercentage();

  /**
   * Returns a (possibly localized) description of the current status to be displayed.
   *
   * @return A description of the current status to be displayed.
   */
  String getStatusDescription();
}
