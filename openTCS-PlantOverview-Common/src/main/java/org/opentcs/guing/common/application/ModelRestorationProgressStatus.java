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
 * Progress status for the process of loading a model.
 */
public enum ModelRestorationProgressStatus
    implements ProgressStatus {

  /**
   * Cleanup phase.
   */
  CLEANUP(0, "modelRestorationProgressStatus.description.cleanup"),
  /**
   * Starting to load model.
   */
  START_LOADING_MODEL(10, "modelRestorationProgressStatus.description.startLoadingModel"),
  /**
   * Loading points.
   */
  LOADING_POINTS(20, "modelRestorationProgressStatus.description.startLoadingPoints"),
  /**
   * Loading paths.
   */
  LOADING_PATHS(30, "modelRestorationProgressStatus.description.startLoadingPaths"),
  /**
   * Loading locations.
   */
  LOADING_LOCATIONS(40, "modelRestorationProgressStatus.description.startLoadingLocations"),
  /**
   * Loading vehicles.
   */
  LOADING_VEHICLES(50, "modelRestorationProgressStatus.description.startLoadingVehicles"),
  /**
   * Loading blocks.
   */
  LOADING_BLOCKS(60, "modelRestorationProgressStatus.description.startLoadingBlocks"),
  /**
   * Setting up model view.
   */
  SET_UP_MODEL_VIEW(70, "modelRestorationProgressStatus.description.setUpModelView"),
  /**
   * Setting up directory tree.
   */
  SET_UP_DIRECTORY_TREE(80, "modelRestorationProgressStatus.description.setUpDirectoryTree"),
  /**
   * Setting up working area.
   */
  SET_UP_WORKING_AREA(90, "modelRestorationProgressStatus.description.setUpWorkingArea");

  private final int percentage;

  private final String description;

  ModelRestorationProgressStatus(int percentage, String description) {
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
