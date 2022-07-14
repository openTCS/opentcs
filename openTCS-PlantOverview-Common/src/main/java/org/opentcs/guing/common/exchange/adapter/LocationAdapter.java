/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.exchange.adapter;

import java.util.Map;
import static java.util.Objects.requireNonNull;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.ObjectPropConstants;
import static org.opentcs.data.ObjectPropConstants.LOC_DEFAULT_REPRESENTATION;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.guing.base.components.layer.LayerWrapper;
import org.opentcs.guing.base.components.properties.type.CoordinateProperty;
import org.opentcs.guing.base.components.properties.type.KeyValueProperty;
import org.opentcs.guing.base.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.base.components.properties.type.LengthProperty;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.base.model.elements.LocationModel;
import org.opentcs.guing.common.model.SystemModel;
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
                                    TCSObjectService objectService) {
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
      model.getPropertyLocked().setValue(location.isLocked());

      // Peripheral information
      model.getPropertyPeripheralReservationToken().setText(location.getPeripheralInformation().getReservationToken());
      model.getPropertyPeripheralState().setText(location.getPeripheralInformation().getState().name());
      model.getPropertyPeripheralProcState().setText(location.getPeripheralInformation().getProcState().name());
      model.getPropertyPeripheralJob().setText(extractPeripheralJobName(location));

      // Misc properties
      updateMiscModelProperties(model, location);
      updateModelLayoutProperties(model, location, systemModel.getLayoutModel());
      // look for label and symbol
      updateRepresentation(model, model.getPropertyMiscellaneous());

      model.setLocation(location);
      model.propertiesChanged(model);
    }
    catch (IllegalArgumentException e) {
      LOG.warn("", e);
    }
  }

  @Override
  public PlantModelCreationTO storeToPlantModel(ModelComponent modelComponent,
                                                SystemModel systemModel,
                                                PlantModelCreationTO plantModel) {
    LocationModel locationModel = (LocationModel) modelComponent;

    PlantModelCreationTO result = plantModel
        .withLocation(
            new LocationCreationTO(modelComponent.getName(),
                                   locationModel.getLocationType().getName(),
                                   getPosition(locationModel))
                .withLocked(getLocked(locationModel))
                .withProperties(getKernelProperties(modelComponent))
                .withLayout(getLayout(locationModel))
        );

    unmarkAllPropertiesChanged(modelComponent);

    return result;
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

  private void updateModelLayoutProperties(LocationModel model,
                                           Location location,
                                           LayoutModel layoutModel) {
    model.getPropertyLayoutPositionX()
        .setText(String.valueOf(location.getLayout().getPosition().getX()));
    model.getPropertyLayoutPositionY()
        .setText(String.valueOf(location.getLayout().getPosition().getY()));
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

  private Triple getPosition(LocationModel model) {
    return convertToTriple(model.getPropertyModelPositionX(),
                           model.getPropertyModelPositionY());
  }

  private boolean getLocked(LocationModel model) {
    if (model.getPropertyLocked().getValue() instanceof Boolean) {
      return (boolean) model.getPropertyLocked().getValue();
    }
    return false;
  }

  private LocationCreationTO.Layout getLayout(LocationModel model) {
    return new LocationCreationTO.Layout(
        new Couple(Long.parseLong(model.getPropertyLayoutPositionX().getText()),
                   Long.parseLong(model.getPropertyLayoutPositionY().getText())),
        new Couple(Long.parseLong(model.getPropertyLabelOffsetX().getText()),
                   Long.parseLong(model.getPropertyLabelOffsetY().getText())),
        model.getPropertyDefaultRepresentation().getLocationRepresentation(),
        model.getPropertyLayerWrapper().getValue().getLayer().getId()
    );
  }

  private Triple convertToTriple(CoordinateProperty cpx, CoordinateProperty cpy) {
    Triple result = new Triple((int) cpx.getValueByUnit(LengthProperty.Unit.MM),
                               (int) cpy.getValueByUnit(LengthProperty.Unit.MM),
                               0);

    return result;
  }
}
