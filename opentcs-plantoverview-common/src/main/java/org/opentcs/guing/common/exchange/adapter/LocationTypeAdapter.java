// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.exchange.adapter;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.opentcs.access.to.model.LocationRepresentationTO;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.LocationTypeModel;
import org.opentcs.guing.common.model.SystemModel;

/**
 * An adapter for location types.
 */
public class LocationTypeAdapter
    extends
      AbstractProcessAdapter {

  /**
   * Creates a new instance.
   */
  public LocationTypeAdapter() {
  }

  @Override // OpenTCSProcessAdapter
  public void updateModelProperties(
      TCSObject<?> tcsObject,
      ModelComponent modelComponent,
      SystemModel systemModel,
      TCSObjectService objectService
  ) {
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
  }

  @Override
  public PlantModelCreationTO storeToPlantModel(
      ModelComponent modelComponent,
      SystemModel systemModel,
      PlantModelCreationTO plantModel
  ) {
    PlantModelCreationTO result = plantModel
        .withLocationType(
            new LocationTypeCreationTO(modelComponent.getName())
                .withAllowedOperations(getAllowedOperations((LocationTypeModel) modelComponent))
                .withAllowedPeripheralOperations(
                    getAllowedPeripheralOperations((LocationTypeModel) modelComponent)
                )
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
        convertToLocationRepresentationTO(
            model.getPropertyDefaultRepresentation().getLocationRepresentation()
        )
    );
  }

  private LocationRepresentationTO convertToLocationRepresentationTO(
      LocationRepresentation locRepresentation
  ) {
    return switch (locRepresentation) {
      case DEFAULT -> LocationRepresentationTO.DEFAULT;
      case LOAD_TRANSFER_ALT_1 -> LocationRepresentationTO.LOAD_TRANSFER_ALT_1;
      case LOAD_TRANSFER_ALT_2 -> LocationRepresentationTO.LOAD_TRANSFER_ALT_2;
      case LOAD_TRANSFER_ALT_3 -> LocationRepresentationTO.LOAD_TRANSFER_ALT_3;
      case LOAD_TRANSFER_ALT_4 -> LocationRepresentationTO.LOAD_TRANSFER_ALT_4;
      case LOAD_TRANSFER_ALT_5 -> LocationRepresentationTO.LOAD_TRANSFER_ALT_5;
      case LOAD_TRANSFER_GENERIC -> LocationRepresentationTO.LOAD_TRANSFER_GENERIC;
      case NONE -> LocationRepresentationTO.NONE;
      case RECHARGE_ALT_1 -> LocationRepresentationTO.RECHARGE_ALT_1;
      case RECHARGE_ALT_2 -> LocationRepresentationTO.RECHARGE_ALT_2;
      case RECHARGE_GENERIC -> LocationRepresentationTO.RECHARGE_GENERIC;
      case WORKING_ALT_1 -> LocationRepresentationTO.WORKING_ALT_1;
      case WORKING_ALT_2 -> LocationRepresentationTO.WORKING_ALT_2;
      case WORKING_GENERIC -> LocationRepresentationTO.WORKING_GENERIC;
    };
  }
}
