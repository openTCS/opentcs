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

import static java.util.Objects.requireNonNull;
import javax.annotation.Nullable;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
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

  @Override // OpenTCSProcessAdapter
  public void updateModelProperties(TCSObject<?> tcsObject,
                                    ModelComponent modelComponent,
                                    SystemModel systemModel,
                                    TCSObjectService objectService,
                                    @Nullable ModelLayoutElement layoutElement) {
    VisualLayout layout = requireNonNull((VisualLayout) tcsObject, "tcsObject");
    LayoutModel model = (LayoutModel) modelComponent;

    try {
      model.getPropertyName().setText(layout.getName());
      model.getPropertyName().markChanged();

      model.getPropertyScaleX().setValueAndUnit(layout.getScaleX(), LengthProperty.Unit.MM);
      model.getPropertyScaleX().markChanged();
      model.getPropertyScaleY().setValueAndUnit(layout.getScaleY(), LengthProperty.Unit.MM);
      model.getPropertyScaleY().markChanged();

      updateMiscModelProperties(model, layout);
    }
    catch (IllegalArgumentException e) {
      LOG.warn("", e);
    }
  }

  @Override
  public PlantModelCreationTO storeToPlantModel(ModelComponent modelComponent,
                                                SystemModel systemModel,
                                                PlantModelCreationTO plantModel) {
    return plantModel.withVisualLayout(
        new VisualLayoutCreationTO(modelComponent.getName())
            .withScaleX(getScaleX((LayoutModel) modelComponent))
            .withScaleY(getScaleY((LayoutModel) modelComponent))
            .withProperties(getKernelProperties(modelComponent))
    );
  }

  private double getScaleX(LayoutModel model) {
    return model.getPropertyScaleX().getValueByUnit(LengthProperty.Unit.MM);
  }

  private double getScaleY(LayoutModel model) {
    return model.getPropertyScaleY().getValueByUnit(LengthProperty.Unit.MM);
  }
}
