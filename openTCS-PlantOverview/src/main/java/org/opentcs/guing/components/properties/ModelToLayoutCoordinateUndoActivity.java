/*
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.properties;

import com.google.inject.assistedinject.Assisted;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import javax.inject.Inject;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.guing.components.drawing.course.Origin;
import org.opentcs.guing.components.drawing.figures.FigureConstants;
import org.opentcs.guing.components.drawing.figures.TCSFigure;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.model.AbstractConnectableModelComponent;
import org.opentcs.guing.persistence.ModelManager;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ModelToLayoutCoordinateUndoActivity
    extends CoordinateUndoActivity {

  /**
   * This class's logger.
   */
  private static final Logger LOG
      = LoggerFactory.getLogger(ModelToLayoutCoordinateUndoActivity.class);

  @Inject
  public ModelToLayoutCoordinateUndoActivity(@Assisted CoordinateProperty property,
                                             ModelManager modelManager) {
    super(property, modelManager);
  }

  @Override
  public String getPresentationName() {
    return ResourceBundleUtil.getBundle(I18nPlantOverview.MISC_PATH)
        .getString("modelToLayoutCoordinateUndoActivity.presentationName");
  }

  @Override
  protected void saveTransformBeforeModification() {
    AbstractConnectableModelComponent model = (AbstractConnectableModelComponent) property.getModel();
    StringProperty pxLayout = (StringProperty) model.getProperty(ElementPropKeys.POINT_POS_X);
    StringProperty pyLayout = (StringProperty) model.getProperty(ElementPropKeys.POINT_POS_Y);

    Origin origin = bufferedFigure.get(FigureConstants.ORIGIN);
    TCSFigure pf = bufferedFigure.getPresentationFigure();
    double zoomScale = pf.getZoomPoint().scale();
    double xModel = pxModel.getValueByUnit(CoordinateProperty.Unit.MM) / (zoomScale * origin.getScaleX());
    double yModel = pyModel.getValueByUnit(CoordinateProperty.Unit.MM) / (-zoomScale * origin.getScaleY());
    String sx = (String) pxLayout.getComparableValue();
    double xLayout = Double.parseDouble(sx) / (zoomScale * origin.getScaleX());
    String sy = (String) pyLayout.getComparableValue();
    double yLayout = Double.parseDouble(sy) / (-zoomScale * origin.getScaleY());

    bufferedTransform.translate(xModel - xLayout, yModel - yLayout);
  }

  @Override
  protected void saveTransformForUndo() {
    try {
      AffineTransform inverse = bufferedTransform.createInverse();
      bufferedFigure.willChange();
      bufferedFigure.transform(inverse);
      bufferedFigure.changed();
    }
    catch (NoninvertibleTransformException e) {
      LOG.warn("Exception inverting transform.", e);
    }
  }

  @Override
  protected void saveTransformForRedo() {
    bufferedFigure.willChange();
    bufferedFigure.transform(bufferedTransform);
    bufferedFigure.changed();
  }

}
