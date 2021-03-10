/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application;

/**
 * A state of progress, to be used with {@link ProgressIndicator}.
 *
 * @author Stefan Walter (Fraunhofer IML)
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
