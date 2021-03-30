/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application;

import java.util.ResourceBundle;
import org.opentcs.guing.util.I18nPlantOverview;

/**
 * Progress status for the process of loading a model.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public enum ModelRestorationProgressStatus
    implements ProgressStatus {

  CLEANUP(0, "modelRestorationProgressStatus.description.cleanup"),
  START_LOADING_MODEL(50, "modelRestorationProgressStatus.description.startLoadingModel"),
  SET_UP_MODEL_VIEW(70, "modelRestorationProgressStatus.description.setUpModelView"),
  SET_UP_DIRECTORY_TREE(80, "modelRestorationProgressStatus.description.setUpDirectoryTree"),
  SET_UP_WORKING_AREA(90, "modelRestorationProgressStatus.description.setUpWorkingArea");

  private final int percentage;

  private final String description;

  private ModelRestorationProgressStatus(int percentage, String description) {
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
