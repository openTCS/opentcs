/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface ProgressIndicator {

  /**
   * Initializes the progress indicator, possibly resetting the percentage and
   * message.
   */
  void initialize();

  /**
   * Sets/publishes the current progress.
   *
   * @param percent The progress.
   * @param message A message to be added to the progress.
   */
  void setProgress(int percent, String message);

  /**
   * Terminates the progress indicator, indicating that no further progress is
   * going to be published.
   * The progress indicator may be reused after a call to {@code initialize()}.
   */
  void terminate();
}
