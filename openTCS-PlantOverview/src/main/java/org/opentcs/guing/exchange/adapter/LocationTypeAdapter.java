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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nullable;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.ObjectPropConstants;
import static org.opentcs.data.ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.LocationTypeModel;

/**
 * An adapter for location types.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LocationTypeAdapter
    extends AbstractProcessAdapter {

  @Override // OpenTCSProcessAdapter
  public void updateModelProperties(TCSObject<?> tcsObject,
                                    ModelComponent modelComponent,
                                    SystemModel systemModel,
                                    TCSObjectService objectService,
                                    @Nullable ModelLayoutElement layoutElement) {
    LocationType locationType = requireNonNull((LocationType) tcsObject, "tcsObject");
    LocationTypeModel model = (LocationTypeModel) modelComponent;

    // Name
    model.getPropertyName().setText(locationType.getName());
    // Allowed operations
    model.getPropertyAllowedOperations()
        .setItems(new ArrayList<>(locationType.getAllowedOperations()));
    updateMiscModelProperties(model, locationType);

    for (KeyValueProperty next : model.getPropertyMiscellaneous().getItems()) {
      if (next.getKey().equals(ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION)) {
        model.getPropertyDefaultRepresentation()
            .setLocationRepresentation(LocationRepresentation.valueOf(next.getValue()));
        break;
      }
    }
  }

  @Override
  public PlantModelCreationTO storeToPlantModel(ModelComponent modelComponent,
                                                SystemModel systemModel,
                                                PlantModelCreationTO plantModel) {
    PlantModelCreationTO result = plantModel
        .withLocationType(
            new LocationTypeCreationTO(modelComponent.getName())
                .withAllowedOperations(getAllowedOperations((LocationTypeModel) modelComponent))
                .withProperties(getKernelProperties(modelComponent))
        );

    unmarkAllPropertiesChanged(modelComponent);

    return result;
  }

  private List<String> getAllowedOperations(LocationTypeModel model) {
    return new ArrayList<>(model.getPropertyAllowedOperations().getItems());
  }

  @Override
  protected Map<String, String> getKernelProperties(ModelComponent model) {
    Map<String, String> result = super.getKernelProperties(model);
    LocationTypeModel locationTypeModel = (LocationTypeModel) model;

    // Add the location representation (symbol) from the model.
    LocationRepresentation locationRepresentation
        = locationTypeModel.getPropertyDefaultRepresentation().getLocationRepresentation();

    if (locationRepresentation != null) {
      result.put(LOCTYPE_DEFAULT_REPRESENTATION, locationRepresentation.name());
    }

    return result;
  }

}
