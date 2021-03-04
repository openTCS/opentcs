/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.storage;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opentcs.access.to.model.BlockCreationTO;
import org.opentcs.access.to.model.GroupCreationTO;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.access.to.model.PathCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.guing.components.drawing.figures.TCSLabelFigure;
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.PercentProperty;
import org.opentcs.guing.components.properties.type.SpeedProperty;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.model.elements.LayoutModel;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.StaticRouteModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.util.Colors;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PlantModelElementConverter {

  public PlantModelElementConverter() {
  }

  public PointModel importPoint(PointCreationTO pointTO, VisualLayoutCreationTO layoutTO) {
    requireNonNull(pointTO, "pointTO");
    requireNonNull(layoutTO, "layoutTO");

    PointModel model = new PointModel();

    model.setName(pointTO.getName());

    model.getPropertyModelPositionX().setValueAndUnit(pointTO.getPosition().getX(),
                                                      LengthProperty.Unit.MM);
    model.getPropertyModelPositionY().setValueAndUnit(pointTO.getPosition().getY(),
                                                      LengthProperty.Unit.MM);
    model.getPropertyVehicleOrientationAngle().setValueAndUnit(pointTO.getVehicleOrientationAngle(),
                                                               AngleProperty.Unit.DEG);
    model.getPropertyType().setValue(mapPointType(pointTO.getType()));
    for (Map.Entry<String, String> property : pointTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    property.getKey(),
                                                                    property.getValue()));
    }

    // Gather information contained in visual layout
    model.getPropertyLayoutPosX().setText(
        extractLayoutPropertyValue(layoutTO,
                                   pointTO.getName(),
                                   ElementPropKeys.POINT_POS_X)
            .orElse(""));
    model.getPropertyLayoutPosY().setText(
        extractLayoutPropertyValue(layoutTO,
                                   pointTO.getName(),
                                   ElementPropKeys.POINT_POS_Y)
            .orElse(""));
    model.getPropertyPointLabelOffsetX().setText(
        extractLayoutPropertyValue(layoutTO,
                                   pointTO.getName(),
                                   ElementPropKeys.POINT_LABEL_OFFSET_X)
            .orElse(String.valueOf(TCSLabelFigure.DEFAULT_LABEL_OFFSET_X)));
    model.getPropertyPointLabelOffsetY().setText(
        extractLayoutPropertyValue(layoutTO,
                                   pointTO.getName(),
                                   ElementPropKeys.POINT_LABEL_OFFSET_Y)
            .orElse(String.valueOf(TCSLabelFigure.DEFAULT_LABEL_OFFSET_Y)));
    model.getPropertyPointLabelOrientationAngle().setText(
        extractLayoutPropertyValue(layoutTO,
                                   pointTO.getName(),
                                   ElementPropKeys.POINT_LABEL_ORIENTATION_ANGLE)
            .orElse(""));

    return model;
  }

  public PathModel importPath(PathCreationTO pathTO, VisualLayoutCreationTO layoutTO) {
    PathModel model = new PathModel();

    model.setName(pathTO.getName());
    model.getPropertyLength().setValueAndUnit(pathTO.getLength(), LengthProperty.Unit.MM);
    model.getPropertyRoutingCost().setValue((int) pathTO.getRoutingCost());
    model.getPropertyMaxVelocity().setValueAndUnit(pathTO.getMaxVelocity(),
                                                   SpeedProperty.Unit.MM_S);
    model.getPropertyMaxReverseVelocity().setValueAndUnit(pathTO.getMaxReverseVelocity(),
                                                          SpeedProperty.Unit.MM_S);
    model.getPropertyStartComponent().setText(pathTO.getSrcPointName());
    model.getPropertyEndComponent().setText(pathTO.getDestPointName());
    model.getPropertyLocked().setValue(pathTO.isLocked());

    for (Map.Entry<String, String> property : pathTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    property.getKey(),
                                                                    property.getValue()));
    }

    // Gather information contained in visual layout
    model.getPropertyPathConnType().setValue(
        extractLayoutPropertyValue(layoutTO, pathTO.getName(), ElementPropKeys.PATH_CONN_TYPE)
            .map(typeName -> PathModel.LinerType.valueOf(typeName))
            .orElse(PathModel.LinerType.DIRECT)
    );
    model.getPropertyPathControlPoints().setText(
        extractLayoutPropertyValue(layoutTO,
                                   pathTO.getName(),
                                   ElementPropKeys.PATH_CONTROL_POINTS)
            .orElse("")
    );

    return model;
  }

  public VehicleModel importVehicle(VehicleCreationTO vehicleTO, VisualLayoutCreationTO layoutTO) {
    VehicleModel model = new VehicleModel();

    model.setName(vehicleTO.getName());
    model.getPropertyLength().setValueAndUnit(vehicleTO.getLength(), LengthProperty.Unit.MM);
    model.getPropertyMaxVelocity().setValueAndUnit(((double) vehicleTO.getMaxVelocity()),
                                                   SpeedProperty.Unit.MM_S);
    model.getPropertyMaxReverseVelocity().setValueAndUnit(
        ((double) vehicleTO.getMaxReverseVelocity()), SpeedProperty.Unit.MM_S);
    model.getPropertyEnergyLevelCritical().setValueAndUnit(vehicleTO.getEnergyLevelCritical(),
                                                           PercentProperty.Unit.PERCENT);
    model.getPropertyEnergyLevelGood().setValueAndUnit(vehicleTO.getEnergyLevelGood(),
                                                       PercentProperty.Unit.PERCENT);
    for (Map.Entry<String, String> property : vehicleTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    property.getKey(),
                                                                    property.getValue()));
    }

    // Gather information contained in visual layout
    model.getPropertyRouteColor().setColor(
        extractLayoutPropertyValue(layoutTO,
                                   vehicleTO.getName(),
                                   ElementPropKeys.VEHICLE_ROUTE_COLOR)
            .map(colorRepresentation -> Colors.decodeFromHexRGB(colorRepresentation))
            .orElse(Color.red)
    );

    return model;
  }

  public LocationTypeModel importLocationType(LocationTypeCreationTO locTypeTO,
                                              VisualLayoutCreationTO layoutTO) {
    LocationTypeModel model = new LocationTypeModel();

    model.setName(locTypeTO.getName());
    for (String allowedOperation : locTypeTO.getAllowedOperations()) {
      model.getPropertyAllowedOperations().addItem(allowedOperation);
    }

    for (Map.Entry<String, String> property : locTypeTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    property.getKey(),
                                                                    property.getValue()));

      // Set the default location type symbol since its value is not synchronized with the model's
      // properties
      if (Objects.equals(property.getKey(), ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION)) {
        model.getPropertyDefaultRepresentation().setLocationRepresentation(
            LocationRepresentation.valueOf(property.getValue())
        );
      }
    }

    return model;
  }

  public LocationModel importLocation(LocationCreationTO locationTO,
                                      Collection<LocationTypeCreationTO> locTypes,
                                      VisualLayoutCreationTO layoutTO) {
    LocationModel model = new LocationModel();

    model.setName(locationTO.getName());
    model.getPropertyModelPositionX().setValueAndUnit(locationTO.getPosition().getX(),
                                                      LengthProperty.Unit.MM);
    model.getPropertyModelPositionY().setValueAndUnit(locationTO.getPosition().getY(),
                                                      LengthProperty.Unit.MM);

    List<String> possibleLocationTypes = new ArrayList<>();
    for (LocationTypeCreationTO locType : locTypes) {
      if (!possibleLocationTypes.contains(locType.getName())) {
        possibleLocationTypes.add(locType.getName());
      }
    }
    model.getPropertyType().setPossibleValues(possibleLocationTypes);
    model.getPropertyType().setValue(locationTO.getTypeName());

    for (Map.Entry<String, String> property : locationTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    property.getKey(),
                                                                    property.getValue()));

      // Set the location symbol (overwriting the default location type symbol) since its value is
      // not synchronized with the model's properties
      if (Objects.equals(property.getKey(), ObjectPropConstants.LOC_DEFAULT_REPRESENTATION)) {
        model.getPropertyDefaultRepresentation().setLocationRepresentation(
            LocationRepresentation.valueOf(property.getValue()));
      }
    }

    // Gather information contained in visual layout
    model.getPropertyLayoutPositionX().setText(
        extractLayoutPropertyValue(layoutTO, locationTO.getName(), ElementPropKeys.LOC_POS_X)
            .orElse("")
    );
    model.getPropertyLayoutPositionY().setText(
        extractLayoutPropertyValue(layoutTO, locationTO.getName(), ElementPropKeys.LOC_POS_Y)
            .orElse("")
    );
    model.getPropertyLabelOffsetX().setText(
        extractLayoutPropertyValue(layoutTO,
                                   locationTO.getName(),
                                   ElementPropKeys.LOC_LABEL_OFFSET_X)
            .orElse(String.valueOf(TCSLabelFigure.DEFAULT_LABEL_OFFSET_X))
    );
    model.getPropertyLabelOffsetY().setText(
        extractLayoutPropertyValue(layoutTO,
                                   locationTO.getName(),
                                   ElementPropKeys.LOC_LABEL_OFFSET_Y)
            .orElse(String.valueOf(TCSLabelFigure.DEFAULT_LABEL_OFFSET_Y))
    );
    model.getPropertyLabelOrientationAngle().setText(
        extractLayoutPropertyValue(layoutTO,
                                   locationTO.getName(),
                                   ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE)
            .orElse("")
    );

    return model;
  }

  public LinkModel importLocationLink(LocationCreationTO locationTO,
                                      String pointName,
                                      Set<String> operations) {
    LinkModel model = new LinkModel();

    model.setName(String.format("%s --- %s", pointName, locationTO.getName()));

    for (String operation : operations) {
      model.getPropertyAllowedOperations().addItem(operation);
    }

    model.getPropertyStartComponent().setText(pointName);
    model.getPropertyEndComponent().setText(locationTO.getName());

    return model;
  }

  public BlockModel importBlock(BlockCreationTO blockTO, VisualLayoutCreationTO layoutTO) {
    BlockModel model = new BlockModel();

    model.setName(blockTO.getName());
    for (String member : blockTO.getMemberNames()) {
      model.getPropertyElements().addItem(member);
    }

    for (Map.Entry<String, String> property : blockTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    property.getKey(),
                                                                    property.getValue()));
    }

    // Gather information contained in visual layout
    model.getPropertyColor().setColor(
        extractLayoutPropertyValue(layoutTO, blockTO.getName(), ElementPropKeys.BLOCK_COLOR)
            .map(colorName -> Color.decode(colorName))
            .orElse(Color.red)
    );

    return model;
  }

  @SuppressWarnings("deprecation")
  public StaticRouteModel importStaticRoute(
      org.opentcs.access.to.model.StaticRouteCreationTO staticRouteTO,
      VisualLayoutCreationTO layoutTO) {
    StaticRouteModel model = new StaticRouteModel();

    model.setName(staticRouteTO.getName());

    for (String hop : staticRouteTO.getHopNames()) {
      model.getPropertyElements().addItem(hop);
    }

    for (Map.Entry<String, String> property : staticRouteTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    property.getKey(),
                                                                    property.getValue()));
    }

    // Gather information contained in visual layout
    model.getPropertyColor().setColor(
        extractLayoutPropertyValue(layoutTO, staticRouteTO.getName(), ElementPropKeys.BLOCK_COLOR)
            .map(colorName -> Color.decode(colorName))
            .orElse(Color.red)
    );

    return model;
  }

  public GroupModel importGroup(GroupCreationTO groupTO, VisualLayoutCreationTO layoutTO) {
    GroupModel model = new GroupModel();

    model.setName(groupTO.getName());

    for (String member : groupTO.getMemberNames()) {
      model.getPropertyElements().addItem(member);
    }

    for (Map.Entry<String, String> property : groupTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    property.getKey(),
                                                                    property.getValue()));
    }

    return model;
  }

  public LayoutModel importLayout(VisualLayoutCreationTO layoutTO) {
    LayoutModel model = new LayoutModel();

    model.setName(layoutTO.getName());
    model.getPropertyScaleX().setValueAndUnit(layoutTO.getScaleX(), LengthProperty.Unit.MM);
    model.getPropertyScaleY().setValueAndUnit(layoutTO.getScaleY(), LengthProperty.Unit.MM);

    for (Map.Entry<String, String> property : layoutTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    property.getKey(),
                                                                    property.getValue()));
    }

    return model;
  }

  private PointModel.PointType mapPointType(Point.Type type) {
    switch (type) {
      case HALT_POSITION:
        return PointModel.PointType.HALT;
      case PARK_POSITION:
        return PointModel.PointType.PARK;
      case REPORT_POSITION:
        return PointModel.PointType.REPORT;
      default:
        throw new IllegalArgumentException("Unhandled point type: " + type);
    }
  }

  @Nonnull
  private Optional<String> extractLayoutPropertyValue(VisualLayoutCreationTO visualLayout,
                                                      String layoutElementName,
                                                      String propertyName) {
    return visualLayout.getModelElements().stream()
        .filter(mleTO -> Objects.equals(mleTO.getName(), layoutElementName))
        .findAny()
        .map(mleTO -> mleTO.getProperties().get(propertyName));
  }
}
