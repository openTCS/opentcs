/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.exchange.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.ObjectPropConstants;
import static org.opentcs.data.ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.guing.base.components.properties.type.KeyValueProperty;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.LocationTypeModel;
import org.opentcs.guing.common.model.SystemModel;

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
                                    TCSObjectService objectService) {
    LocationType locationType = requireNonNull((LocationType) tcsObject, "tcsObject");
    LocationTypeModel model = (LocationTypeModel) modelComponent;

    // Name
    model.getPropertyName().setText(locationType.getName());
    // Allowed operations
    model.getPropertyAllowedOperations()
        .setItems(new ArrayList<>(locationType.getAllowedOperations()));
    model.getPropertyAllowedPeripheralOperations()
        .setItems(new ArrayList<>(locationType.getAllowedPeripheralOperations()));
    updateMiscModelProperties(model, locationType);
    updateModelLayoutProperties(model, locationType);
    model.setLocationType(locationType);

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
                .withAllowedPeripheralOperations(getAllowedPeripheralOperations((LocationTypeModel) modelComponent))
                .withProperties(getKernelProperties(modelComponent))
                .withLayout(getLayout((LocationTypeModel) modelComponent))
        );

    unmarkAllPropertiesChanged(modelComponent);

    return result;
  }

  private void updateModelLayoutProperties(LocationTypeModel model, LocationType location) {
    model.getPropertyDefaultRepresentation()
        .setLocationRepresentation(location.getLayout().getLocationRepresentation());
  }

  private List<String> getAllowedOperations(LocationTypeModel model) {
    return new ArrayList<>(model.getPropertyAllowedOperations().getItems());
  }

  private List<String> getAllowedPeripheralOperations(LocationTypeModel model) {
    return new ArrayList<>(model.getPropertyAllowedPeripheralOperations().getItems());
  }

  private LocationTypeCreationTO.Layout getLayout(LocationTypeModel model) {
    return new LocationTypeCreationTO.Layout(
        model.getPropertyDefaultRepresentation().getLocationRepresentation()
    );
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
