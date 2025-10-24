// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.exchange.adapter;

import static java.util.Objects.requireNonNull;

import org.opentcs.access.to.model.CoupleCreationTO;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.LocationRepresentationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.TripleCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.guing.base.components.layer.LayerWrapper;
import org.opentcs.guing.base.components.properties.type.CoordinateProperty;
import org.opentcs.guing.base.components.properties.type.LengthProperty;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.base.model.elements.LocationModel;
import org.opentcs.guing.common.model.SystemModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An adapter for locations.
 */
public class LocationAdapter
    extends
      AbstractProcessAdapter {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(LocationAdapter.class);

  /**
   * Creates a new instance.
   */
  public LocationAdapter() {
  }

  @Override  // OpenTCSProcessAdapter
  public void updateModelProperties(
      TCSObject<?> tcsObject,
      ModelComponent modelComponent,
      SystemModel systemModel,
      TCSObjectService objectService
  ) {
    Location location = requireNonNull((Location) tcsObject, "tcsObject");
    LocationModel model = (LocationModel) modelComponent;

    try {
      // Name
      model.getPropertyName().setText(location.getName());

      // Position in model
      model.getPropertyModelPositionX().setValueAndUnit(
          location.getPosition().getX(),
          LengthProperty.Unit.MM
      );
      model.getPropertyModelPositionY().setValueAndUnit(
          location.getPosition().getY(),
          LengthProperty.Unit.MM
      );

      // Type
      model.getPropertyType().setValue(location.getType().getName());
      model.getPropertyLocked().setValue(location.isLocked());

      // Peripheral information
      model.getPropertyPeripheralReservationToken().setText(
          location.getPeripheralInformation().getReservationToken()
      );
      model.getPropertyPeripheralState().setText(
          location.getPeripheralInformation().getState().name()
      );
      model.getPropertyPeripheralProcState().setText(
          location.getPeripheralInformation().getProcState().name()
      );
      model.getPropertyPeripheralJob().setText(extractPeripheralJobName(location));

      // Misc properties
      updateMiscModelProperties(model, location);
      updateModelLayoutProperties(model, location, systemModel.getLayoutModel());

      model.setLocation(location);
      model.propertiesChanged(model);
    }
    catch (IllegalArgumentException e) {
      LOG.warn("", e);
    }
  }

  @Override
  public PlantModelCreationTO storeToPlantModel(
      ModelComponent modelComponent,
      SystemModel systemModel,
      PlantModelCreationTO plantModel
  ) {
    LocationModel locationModel = (LocationModel) modelComponent;

    PlantModelCreationTO result = plantModel
        .withLocation(
            new LocationCreationTO(
                modelComponent.getName(),
                locationModel.getLocationType().getName(),
                getPosition(locationModel)
            )
                .withLocked(getLocked(locationModel))
                .withProperties(getKernelProperties(modelComponent))
                .withLayout(getLayout(locationModel))
        );

    unmarkAllPropertiesChanged(modelComponent);

    return result;
  }

  private void updateModelLayoutProperties(
      LocationModel model,
      Location location,
      LayoutModel layoutModel
  ) {
    model.getPropertyLabelOffsetX()
        .setText(String.valueOf(location.getLayout().getLabelOffset().getX()));
    model.getPropertyLabelOffsetY()
        .setText(String.valueOf(location.getLayout().getLabelOffset().getY()));
    model.getPropertyDefaultRepresentation()
        .setLocationRepresentation(location.getLayout().getLocationRepresentation());
    LayerWrapper layerWrapper = layoutModel.getPropertyLayerWrappers()
        .getValue().get(location.getLayout().getLayerId());
    model.getPropertyLayerWrapper().setValue(layerWrapper);
  }

  private String extractPeripheralJobName(Location location) {
    return location.getPeripheralInformation().getPeripheralJob() == null
        ? null
        : location.getPeripheralInformation().getPeripheralJob().getName();
  }

  private TripleCreationTO getPosition(LocationModel model) {
    return convertToTripleCreationTO(
        model.getPropertyModelPositionX(),
        model.getPropertyModelPositionY()
    );
  }

  private boolean getLocked(LocationModel model) {
    if (model.getPropertyLocked().getValue() instanceof Boolean) {
      return (boolean) model.getPropertyLocked().getValue();
    }
    return false;
  }

  private LocationCreationTO.Layout getLayout(LocationModel model) {
    return new LocationCreationTO.Layout(
        new CoupleCreationTO(
            Long.parseLong(model.getPropertyLabelOffsetX().getText()),
            Long.parseLong(model.getPropertyLabelOffsetY().getText())
        ),
        convertToLocationRepresentationTO(
            model.getPropertyDefaultRepresentation().getLocationRepresentation()
        ),
        model.getPropertyLayerWrapper().getValue().getLayer().getId()
    );
  }

  private TripleCreationTO convertToTripleCreationTO(
      CoordinateProperty cpx, CoordinateProperty cpy
  ) {
    TripleCreationTO result = new TripleCreationTO(
        (int) cpx.getValueByUnit(LengthProperty.Unit.MM),
        (int) cpy.getValueByUnit(LengthProperty.Unit.MM),
        0
    );

    return result;
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
