/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.application;

import java.util.ResourceBundle;
import org.opentcs.guing.common.util.I18nPlantOverview;

/**
 * Progress status for the process of starting the application.
 */
public enum StartupProgressStatus
    implements ProgressStatus {

  /**
   * Starting the plant overview.
   */
  START_PLANT_OVERVIEW(0, "startupProgressStatus.description.startPlantOverview"),
  /**
   * Showing the plant overview.
   */
  SHOW_PLANT_OVERVIEW(5, "startupProgressStatus.description.showPlantOverview"),
  /**
   * Application initialized.
   */
  INITIALIZED(10, "startupProgressStatus.description.initialized"),
  /**
   * Initializing model.
   */
  INITIALIZE_MODEL(15, "startupProgressStatus.description.initializeModel");

  private final int percentage;

  private final String description;

  StartupProgressStatus(int percentage, String description) {
    this.percentage = percentage;
    this.description = ResourceBundle.getBundle(I18nPlantOverview.MISC_PATH).getString(description);
  }

  @Override
  public int getPercentage() {
    return percentage;
  }

  @Override
  public String getStatusDescription() {
    return description;
  }

}
