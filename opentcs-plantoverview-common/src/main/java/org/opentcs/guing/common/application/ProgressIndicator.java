// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.application;

/**
 * Makes progress information available in some way.
 */
public interface ProgressIndicator {

  /**
   * Initializes the progress indicator, possibly resetting the percentage and
   * message.
   */
  void initialize();

  /**
   * Sets/publishes the current progress status.
   *
   * @param progressStatus The progress status.
   */
  void setProgress(ProgressStatus progressStatus);

  /**
   * Terminates the progress indicator, indicating that no further progress is
   * going to be published.
   * The progress indicator may be reused after a call to {@code initialize()}.
   */
  void terminate();
}
