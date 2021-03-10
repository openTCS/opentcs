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

import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nullable;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.ModelLayoutElementCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.ObjectPropConstants;
import static org.opentcs.data.ObjectPropConstants.LOC_DEFAULT_REPRESENTATION;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An adapter for locations.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LocationAdapter
    extends AbstractProcessAdapter {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(LocationAdapter.class);

  @Override  // OpenTCSProcessAdapter
  public void updateModelProperties(TCSObject<?> tcsObject,
                                    ModelComponent modelComponent,
                                    SystemModel systemModel,
                                    TCSObjectService objectService,
                                    @Nullable ModelLayoutElement layoutElement) {
    Location location = requireNonNull((Location) tcsObject, "tcsObject");
    LocationModel model = (LocationModel) modelComponent;

    try {
      // Name 
      model.getPropertyName().setText(location.getName());

      // Position in model
      model.getPropertyModelPositionX().setValueAndUnit(location.getPosition().getX(),
                                                        LengthProperty.Unit.MM);
      model.getPropertyModelPositionY().setValueAndUnit(location.getPosition().getY(),
                                                        LengthProperty.Unit.MM);

      // Type
      model.getPropertyType().setValue(location.getType().getName());

      // Misc properties
      updateMiscModelProperties(model, location);
      // look for label and symbol
      updateRepresentation(model, model.getPropertyMiscellaneous());
      if (layoutElement != null) {
        updateModelLayoutElements(model, layoutElement);
      }
    }
    catch (IllegalArgumentException e) {
      LOG.warn("", e);
    }
  }

  @Override
  public PlantModelCreationTO storeToPlantModel(ModelComponent modelComponent,
                                                SystemModel systemModel,
                                                PlantModelCreationTO plantModel) {
    PlantModelCreationTO result = plantModel
        .withLocation(
            new LocationCreationTO(modelComponent.getName(),
                                   ((LocationModel) modelComponent).getLocationType().getName(),
                                   getPosition((LocationModel) modelComponent))
                .withProperties(getKernelProperties(modelComponent))
        )
        .withVisualLayouts(updatedLayouts(modelComponent,
                                          plantModel.getVisualLayouts(),
                                          systemModel));

    unmarkAllPropertiesChanged(modelComponent);

    return result;
  }

  private void updateRepresentation(LocationModel model, KeyValueSetProperty miscellaneous) {
    for (KeyValueProperty kvp : miscellaneous.getItems()) {
      switch (kvp.getKey()) {
        case ObjectPropConstants.LOC_DEFAULT_REPRESENTATION:
          model.getPropertyDefaultRepresentation().setLocationRepresentation(
              LocationRepresentation.valueOf(kvp.getValue()));
          break;
        default:
      }
    }
  }

  private void updateModelLayoutElements(LocationModel model, ModelLayoutElement layoutElement) {
    Map<String, String> properties = layoutElement.getProperties();
    // Save the properties of the kernel object in the model
    model.getPropertyLayoutPositionX().setText(properties.get(ElementPropKeys.LOC_POS_X));
    model.getPropertyLayoutPositionY().setText(properties.get(ElementPropKeys.LOC_POS_Y));
    model.getPropertyLabelOffsetX().setText(properties.get(ElementPropKeys.LOC_LABEL_OFFSET_X));
    model.getPropertyLabelOffsetY().setText(properties.get(ElementPropKeys.LOC_LABEL_OFFSET_Y));
    model.getPropertyLabelOrientationAngle().setText(properties.get(ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE));
  }

  private Triple getPosition(LocationModel model) {
    return convertToTriple(model.getPropertyModelPositionX(),
                           model.getPropertyModelPositionY());
  }

  @Override
  protected Map<String, String> getKernelProperties(ModelComponent model) {
    Map<String, String> result = super.getKernelProperties(model);

    LocationRepresentation locationRepresentation
        = ((LocationModel) model).getPropertyDefaultRepresentation().getLocationRepresentation();

    if (locationRepresentation != null) {
      result.put(LOC_DEFAULT_REPRESENTATION, locationRepresentation.name());
    }

    return result;
  }

  private Triple convertToTriple(CoordinateProperty cpx, CoordinateProperty cpy) {
    Triple result = new Triple((int) cpx.getValueByUnit(LengthProperty.Unit.MM),
                               (int) cpy.getValueByUnit(LengthProperty.Unit.MM),
                               0);

    return result;
  }

  @Override
  protected VisualLayoutCreationTO updatedLayout(ModelComponent model,
                                                 VisualLayoutCreationTO layout,
                                                 SystemModel systemModel) {
    LocationModel locationModel = (LocationModel) model;

    return layout.withModelElement(
        new ModelLayoutElementCreationTO(locationModel.getName())
            .withProperty(ElementPropKeys.LOC_POS_X,
                          locationModel.getPropertyLayoutPositionX().getText())
            .withProperty(ElementPropKeys.LOC_POS_Y,
                          locationModel.getPropertyLayoutPositionY().getText())
            .withProperty(ElementPropKeys.LOC_LABEL_OFFSET_X,
                          locationModel.getPropertyLabelOffsetX().getText())
            .withProperty(ElementPropKeys.LOC_LABEL_OFFSET_Y,
                          locationModel.getPropertyLabelOffsetY().getText())
    );
  }
}
