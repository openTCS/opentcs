/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange.adapter;

import com.google.inject.assistedinject.Assisted;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.Kernel;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.exchange.EventDispatcher;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.LayoutModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An adapter for VisualLayout instances.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LayoutAdapter
    extends AbstractProcessAdapter {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(LayoutAdapter.class);

  /**
   * Creates a new instance.
   *
   * @param model The corresponding model comoponent.
   * @param eventDispatcher The event dispatcher.
   */
  @Inject
  public LayoutAdapter(@Assisted LayoutModel model,
                       @Assisted EventDispatcher eventDispatcher) {
    super(model, eventDispatcher);
  }

  @Override
  public LayoutModel getModel() {
    return (LayoutModel) super.getModel();
  }

  @Override // OpenTCSProcessAdapter
  public void updateModelProperties(Kernel kernel,
                                    TCSObject<?> tcsObject,
                                    @Nullable ModelLayoutElement layoutElement) {
    VisualLayout layout = requireNonNull((VisualLayout) tcsObject, "tcsObject");

    try {
      StringProperty name = (StringProperty) getModel().getProperty(ModelComponent.NAME);
      name.setText(layout.getName());
      name.markChanged();
      updateModelLengthProperty(layout);

      updateMiscModelProperties(layout);
    }
    catch (IllegalArgumentException e) {
      LOG.warn("", e);
    }
  }

  @Override // OpenTCSProcessAdapter
  public void storeToPlantModel(PlantModelCreationTO plantModel) {
    plantModel.getVisualLayouts().add(
        new VisualLayoutCreationTO(getModel().getName())
            .setScaleX(getScaleX())
            .setScaleY(getScaleY())
            .setProperties(getKernelProperties())
    );
  }

  private double getScaleX() {
    LengthProperty pScale = (LengthProperty) getModel().getProperty(LayoutModel.SCALE_X);
    return pScale.getValueByUnit(LengthProperty.Unit.MM);
  }

  private double getScaleY() {
    LengthProperty pScale = (LengthProperty) getModel().getProperty(LayoutModel.SCALE_Y);
    return pScale.getValueByUnit(LengthProperty.Unit.MM);
  }

  private void updateModelLengthProperty(VisualLayout layout)
      throws IllegalArgumentException {
    LengthProperty lp = (LengthProperty) getModel().getProperty(LayoutModel.SCALE_X);
    double scale = layout.getScaleX();
    lp.setValueAndUnit(scale, LengthProperty.Unit.MM);
    lp.markChanged();

    lp = (LengthProperty) getModel().getProperty(LayoutModel.SCALE_Y);
    scale = layout.getScaleY();
    lp.setValueAndUnit(scale, LengthProperty.Unit.MM);
    lp.markChanged();
  }
}
