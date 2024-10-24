// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.modeleditor.application.menus;

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import org.opentcs.guing.base.components.properties.type.CoordinateProperty;
import org.opentcs.guing.common.components.properties.CoordinateUndoActivity;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.modeleditor.util.I18nPlantOverviewModeling;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 */
public class LayoutToModelCoordinateUndoActivity
    extends
      CoordinateUndoActivity {

  @Inject
  public LayoutToModelCoordinateUndoActivity(
      @Assisted
      CoordinateProperty property,
      ModelManager modelManager
  ) {
    super(property, modelManager);
  }

  @Override
  public String getPresentationName() {
    return ResourceBundleUtil.getBundle(I18nPlantOverviewModeling.MISC_PATH)
        .getString("layoutToModelCoordinateUndoActivity.presentationName");
  }

  @Override
  protected void saveTransformBeforeModification() {
  }

  @Override
  protected void saveTransformForUndo() {
  }

  @Override
  protected void saveTransformForRedo() {
  }

}
