/*
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.properties;

import com.google.inject.assistedinject.Assisted;
import javax.inject.Inject;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.persistence.ModelManager;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LayoutToModelCoordinateUndoActivity
    extends CoordinateUndoActivity {

  @Inject
  public LayoutToModelCoordinateUndoActivity(@Assisted CoordinateProperty property,
                                             ModelManager modelManager) {
    super(property, modelManager);
  }

  @Override
  public String getPresentationName() {
    return ResourceBundleUtil.getBundle(I18nPlantOverview.MISC_PATH)
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
