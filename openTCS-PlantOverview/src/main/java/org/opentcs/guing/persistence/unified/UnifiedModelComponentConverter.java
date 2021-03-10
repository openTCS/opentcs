/*
 * openTCS copyright information:
 * Copyright (c) 2017 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.persistence.unified;

import com.google.common.base.Strings;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.opentcs.access.to.model.BlockCreationTO;
import org.opentcs.access.to.model.GroupCreationTO;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.access.to.model.ModelLayoutElementCreationTO;
import org.opentcs.access.to.model.PathCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Point;
import static org.opentcs.data.model.Point.Type.HALT_POSITION;
import static org.opentcs.data.model.Point.Type.PARK_POSITION;
import static org.opentcs.data.model.Point.Type.REPORT_POSITION;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.PercentProperty;
import org.opentcs.guing.components.properties.type.SpeedProperty;
import org.opentcs.guing.components.properties.type.SpeedProperty.Unit;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.SymbolProperty;
import org.opentcs.guing.model.AbstractModelComponent;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.model.elements.LayoutModel;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import static org.opentcs.guing.model.elements.PointModel.PointType.HALT;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.util.Colors;
import org.opentcs.util.persistence.v002.PropertyTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts <code>ModelComponents</code> to corresponding Java beans (JAXB classes).
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class UnifiedModelComponentConverter {

  private static final Logger LOG = LoggerFactory.getLogger(UnifiedModelComponentConverter.class);

  public PlantModelCreationTO convertSystemModel(SystemModel systemModel, String modelName) {
    return new PlantModelCreationTO(modelName)
        .withProperties(convertProperties(systemModel.getPropertyMiscellaneous()))
        .withPoints(convertPoints(systemModel.getPointModels()))
        .withPaths(convertPaths(systemModel.getPathModels()))
        .withVehicles(convertVehicles(systemModel.getVehicleModels()))
        .withLocationTypes(convertLocationTypes(systemModel.getLocationTypeModels()))
        .withLocations(convertLocations(systemModel.getLocationModels(), systemModel.getLinkModels()))
        .withBlocks(convertBlocks(systemModel.getBlockModels()))
        .withGroups(convertGroups(systemModel.getGroupModels()))
        .withVisualLayouts(convertVisualLayouts(systemModel));
  }

  private Map<String, String> convertProperties(KeyValueSetProperty kvsp) {
    return kvsp.getItems().stream()
        .sorted((kvp1, kvp2) -> kvp1.getKey().compareTo(kvp2.getKey()))
        .collect(Collectors.toMap(KeyValueProperty::getKey, KeyValueProperty::getValue));
  }

  private List<PointCreationTO> convertPoints(List<PointModel> points) {
    List<PointCreationTO> result = new ArrayList<>();

    for (PointModel point : points) {
      result.add(convertPoint(point));
    }

    return result;
  }

  private PointCreationTO convertPoint(PointModel pointModel) {
    Triple position
        = new Triple((long) pointModel.getPropertyModelPositionX().getValueByUnit(LengthProperty.Unit.MM),
                     (long) pointModel.getPropertyModelPositionY().getValueByUnit(LengthProperty.Unit.MM),
                     0);

    Point.Type type;
    switch ((PointModel.PointType) pointModel.getPropertyType().getValue()) {
      case HALT:
        type = HALT_POSITION;
        break;
      case REPORT:
        type = REPORT_POSITION;
        break;
      case PARK:
        type = PARK_POSITION;
        break;
      default:
        throw new IllegalArgumentException("Unhandled point type.");
    }

    PointCreationTO point = new PointCreationTO(pointModel.getPropertyName().getText())
        .withPosition(position)
        .withVehicleOrientationAngle(pointModel.getPropertyVehicleOrientationAngle().getValueByUnit(AngleProperty.Unit.DEG))
        .withType(type)
        .withProperties(convertProperties(pointModel.getPropertyMiscellaneous()));
    
    return point;
  }

  private List<PathCreationTO> convertPaths(List<PathModel> paths) {
    List<PathCreationTO> result = new ArrayList<>();

    for (PathModel path : paths) {
      result.add(convertPath(path));
    }

    return result;
  }

  private PathCreationTO convertPath(PathModel pathModel) {
    return new PathCreationTO(pathModel.getPropertyName().getText(),
                              pathModel.getPropertyStartComponent().getText(),
                              pathModel.getPropertyEndComponent().getText())
        .withLength((long) pathModel.getPropertyLength().getValueByUnit(LengthProperty.Unit.MM))
        .withRoutingCost(((Integer) pathModel.getPropertyRoutingCost().getValue()).longValue())
        .withMaxVelocity((int) pathModel.getPropertyMaxVelocity().getValueByUnit(SpeedProperty.Unit.MM_S))
        .withMaxReverseVelocity((int) pathModel.getPropertyMaxReverseVelocity().getValueByUnit(SpeedProperty.Unit.MM_S))
        .withLocked((Boolean) pathModel.getPropertyLocked().getValue())
        .withProperties(convertProperties(pathModel.getPropertyMiscellaneous()));
  }

  private List<VehicleCreationTO> convertVehicles(List<VehicleModel> vehicles) {
    List<VehicleCreationTO> result = new ArrayList<>();

    for (VehicleModel vehicle : vehicles) {
      result.add(convertVehicle(vehicle));
    }

    return result;
  }

  private VehicleCreationTO convertVehicle(VehicleModel vehicleModel) {
    return new VehicleCreationTO(vehicleModel.getPropertyName().getText())
        .withLength((int) vehicleModel.getPropertyLength().getValueByUnit(LengthProperty.Unit.MM))
        .withMaxVelocity(((Double) vehicleModel.getPropertyMaxVelocity().getValueByUnit(Unit.MM_S)).intValue())
        .withMaxReverseVelocity(((Double) vehicleModel.getPropertyMaxReverseVelocity().getValueByUnit(Unit.MM_S)).intValue())
        .withEnergyLevelGood((int) vehicleModel.getPropertyEnergyLevelGood().getValueByUnit(PercentProperty.Unit.PERCENT))
        .withEnergyLevelCritical((int) vehicleModel.getPropertyEnergyLevelCritical().getValueByUnit(PercentProperty.Unit.PERCENT))
        .withEnergyLevelFullyRecharged((int) vehicleModel.getPropertyEnergyLevelFullyRecharged().getValueByUnit(PercentProperty.Unit.PERCENT))
        .withEnergyLevelSufficientlyRecharged((int) vehicleModel.getPropertyEnergyLevelSufficientlyRecharged().getValueByUnit(PercentProperty.Unit.PERCENT))
        .withProperties(convertProperties(vehicleModel.getPropertyMiscellaneous()));
  }

  private List<LocationTypeCreationTO> convertLocationTypes(List<LocationTypeModel> locationTypes) {
    List<LocationTypeCreationTO> result = new ArrayList<>();

    for (LocationTypeModel locationType : locationTypes) {
      result.add(convertLocationType(locationType));
    }

    return result;
  }

  private LocationTypeCreationTO convertLocationType(LocationTypeModel locTypeModel) {
    Map<String, String> properties = convertProperties(locTypeModel.getPropertyMiscellaneous());

    SymbolProperty symp = locTypeModel.getPropertyDefaultRepresentation();
    if (symp.getLocationRepresentation() != null) {
      properties.put(ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION,
                     symp.getLocationRepresentation().name());
    }
    else {
      properties.remove(ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION);
    }

    return new LocationTypeCreationTO(locTypeModel.getPropertyName().getText())
        .withAllowedOperations(locTypeModel.getPropertyAllowedOperations().getItems())
        .withProperties(properties);
  }

  private List<LocationCreationTO> convertLocations(List<LocationModel> locations,
                                                    List<LinkModel> links) {
    List<LocationCreationTO> result = new ArrayList<>();

    for (LocationModel model : locations) {
      result.add(convertLocation(model, links));
    }

    return result;
  }

  private LocationCreationTO convertLocation(LocationModel locationModel, List<LinkModel> linkModels) {
    String locationName = locationModel.getPropertyName().getText();

    Triple position
        = new Triple((long) locationModel.getPropertyModelPositionX().getValueByUnit(LengthProperty.Unit.MM),
                     (long) locationModel.getPropertyModelPositionY().getValueByUnit(LengthProperty.Unit.MM),
                     0);

    Map<String, Set<String>> links = new HashMap<>();
    for (LinkModel linkModel : linkModels) {
      String linkLocationName = linkModel.getLocation().getName();
      if (!linkLocationName.equals(locationName)) {
        continue;
      }

      links.put(linkModel.getPoint().getName(),
                new HashSet<>(linkModel.getPropertyAllowedOperations().getItems()));
    }

    Map<String, String> properties = convertProperties(locationModel.getPropertyMiscellaneous());

    SymbolProperty symp = locationModel.getPropertyDefaultRepresentation();
    if (symp.getLocationRepresentation() != null) {
      properties.put(ObjectPropConstants.LOC_DEFAULT_REPRESENTATION,
                     symp.getLocationRepresentation().name());
    }
    else {
      properties.remove(ObjectPropConstants.LOC_DEFAULT_REPRESENTATION);
    }

    return new LocationCreationTO(locationName,
                                  locationModel.getPropertyType().getValue().toString(),
                                  position)
        .withLinks(links)
        .withProperties(properties);
  }

  private List<BlockCreationTO> convertBlocks(List<BlockModel> blocks) {
    List<BlockCreationTO> result = new ArrayList<>();

    for (BlockModel block : blocks) {
      result.add(convertBlock(block));
    }

    return result;
  }

  private BlockCreationTO convertBlock(BlockModel blockModel) {
    Block.Type type;
    switch ((BlockModel.BlockType) blockModel.getPropertyType().getValue()) {
      case SAME_DIRECTION_ONLY:
        type = Block.Type.SAME_DIRECTION_ONLY;
        break;
      case SINGLE_VEHICLE_ONLY:
        type = Block.Type.SINGLE_VEHICLE_ONLY;
        break;
      default:
        throw new IllegalArgumentException("Unhandled point type.");
    }

    Set<String> members = new HashSet<>(blockModel.getPropertyElements().getItems());

    return new BlockCreationTO(blockModel.getPropertyName().getText())
        .withType(type)
        .withMemberNames(members)
        .withProperties(convertProperties(blockModel.getPropertyMiscellaneous()));
  }

  private List<GroupCreationTO> convertGroups(List<GroupModel> groups) {
    List<GroupCreationTO> result = new ArrayList<>();

    for (GroupModel group : groups) {
      result.add(convertGroup(group));
    }

    return result;
  }

  private GroupCreationTO convertGroup(GroupModel groupModel) {
    Set<String> members = new HashSet<>(groupModel.getPropertyElements().getItems());

    return new GroupCreationTO(groupModel.getPropertyName().getText())
        .withMemberNames(members)
        .withProperties(convertProperties(groupModel.getPropertyMiscellaneous()));
  }

  private List<VisualLayoutCreationTO> convertVisualLayouts(SystemModel systemModel) {
    List<VisualLayoutCreationTO> result = new ArrayList<>();

    for (LayoutModel layout : systemModel.getLayoutModels()) {
      result.add(convertVisualLayout(layout, systemModel));
    }

    return result;
  }

  private VisualLayoutCreationTO convertVisualLayout(LayoutModel layoutModel,
                                                     SystemModel systemModel) {
    List<ModelLayoutElementCreationTO> modelElements = new ArrayList<>();
    modelElements.addAll(extractBlockInformation(systemModel.getBlockModels()));
    modelElements.addAll(extractLocationInformation(systemModel.getLocationModels()));
    modelElements.addAll(extractPointInformation(systemModel.getPointModels()));
    modelElements.addAll(extractPathInformation(systemModel.getPathModels()));
    modelElements.addAll(extractVehiclePathColorInformation(systemModel.getVehicleModels()));

    // XXX Get information about shape layout elements?
    
    VisualLayoutCreationTO layout = new VisualLayoutCreationTO(layoutModel.getPropertyName().getText())
        .withScaleX(layoutModel.getPropertyScaleX().getValueByUnit(LengthProperty.Unit.MM))
        .withScaleY(layoutModel.getPropertyScaleY().getValueByUnit(LengthProperty.Unit.MM))
        .withModelElements(modelElements)
        .withProperties(convertProperties(layoutModel.getPropertyMiscellaneous()));

    return layout;
  }

  private List<ModelLayoutElementCreationTO> extractVehiclePathColorInformation(
      List<VehicleModel> vehicleModels) {
    List<ModelLayoutElementCreationTO> result = new LinkedList<>();

    for (VehicleModel vehicleModel : vehicleModels) {
      result.add(
          new ModelLayoutElementCreationTO(vehicleModel.getPropertyName().getText())
              .withProperty(ElementPropKeys.VEHICLE_ROUTE_COLOR,
                            Colors.encodeToHexRGB(vehicleModel.getPropertyRouteColor().getColor()))
      );
    }

    return result;
  }

  private List<ModelLayoutElementCreationTO> extractPointInformation(List<PointModel> points) {
    List<ModelLayoutElementCreationTO> result = new ArrayList<>();

    for (PointModel point : points) {
      Map<String, String> properties = new HashMap<>();
      properties.put(ElementPropKeys.POINT_POS_X, point.getPropertyLayoutPosX().getText());
      properties.put(ElementPropKeys.POINT_POS_Y, point.getPropertyLayoutPosY().getText());
      properties.put(ElementPropKeys.POINT_LABEL_OFFSET_X, point.getPropertyPointLabelOffsetX().getText());
      properties.put(ElementPropKeys.POINT_LABEL_OFFSET_Y, point.getPropertyPointLabelOffsetY().getText());

      result.add(
          new ModelLayoutElementCreationTO(point.getPropertyName().getText())
              .withProperties(properties)
      );
    }

    return result;
  }

  private List<ModelLayoutElementCreationTO> extractPathInformation(List<PathModel> paths) {
    List<ModelLayoutElementCreationTO> result = new ArrayList<>();

    for (PathModel path : paths) {
      PathModel.LinerType pathType = (PathModel.LinerType) path.getPropertyPathConnType().getValue();

      Map<String, String> properties = new HashMap<>();
      properties.put(ElementPropKeys.PATH_CONN_TYPE, pathType.name());

      if (Objects.equals(pathType, PathModel.LinerType.BEZIER)
          || Objects.equals(pathType, PathModel.LinerType.BEZIER_3)) {
        properties.put(ElementPropKeys.PATH_CONTROL_POINTS, path.getPropertyPathControlPoints().getText());
      }

      result.add(
          new ModelLayoutElementCreationTO(path.getPropertyName().getText())
              .withProperties(properties)
      );
    }

    return result;
  }

  private List<ModelLayoutElementCreationTO> extractLocationInformation(
      List<LocationModel> locations) {
    List<ModelLayoutElementCreationTO> result = new ArrayList<>();

    for (LocationModel location : locations) {
      Map<String, String> properties = new HashMap<>();
      properties.put(ElementPropKeys.LOC_LABEL_OFFSET_X, location.getPropertyLabelOffsetX().getText());
      properties.put(ElementPropKeys.LOC_LABEL_OFFSET_Y, location.getPropertyLabelOffsetY().getText());
      properties.put(ElementPropKeys.LOC_POS_X, location.getPropertyLayoutPositionX().getText());
      properties.put(ElementPropKeys.LOC_POS_Y, location.getPropertyLayoutPositionY().getText());

      result.add(
          new ModelLayoutElementCreationTO(location.getPropertyName().getText())
              .withProperties(properties)
      );
    }

    return result;
  }

  private List<ModelLayoutElementCreationTO> extractBlockInformation(List<BlockModel> blocks) {
    List<ModelLayoutElementCreationTO> result = new ArrayList<>();

    for (BlockModel block : blocks) {
      result.add(
          new ModelLayoutElementCreationTO(block.getPropertyName().getText())
              .withProperty(ElementPropKeys.BLOCK_COLOR,
                            Colors.encodeToHexRGB(block.getPropertyColor().getColor()))
      );
    }

    return result;
  }

  private PropertyTO convertStringProperty(AbstractModelComponent component, String propertyKey) {
    StringProperty sp = (StringProperty) component.getProperty(propertyKey);

    return new PropertyTO()
        .setName(propertyKey)
        .setValue(sp.getText());
  }

  public PointModel convertPointTO(PointCreationTO pointTO, VisualLayoutCreationTO visualLayoutTO) {
    PointModel model = new PointModel();

    model.getPropertyName().setText(pointTO.getName());

    model.getPropertyModelPositionX().setValueAndUnit(pointTO.getPosition().getX(),
                                                      LengthProperty.Unit.MM);
    model.getPropertyModelPositionY().setValueAndUnit(pointTO.getPosition().getY(),
                                                      LengthProperty.Unit.MM);
    model.getPropertyVehicleOrientationAngle().setValueAndUnit(
        pointTO.getVehicleOrientationAngle(), AngleProperty.Unit.DEG
    );

    switch (pointTO.getType()) {
      case HALT_POSITION:
        model.getPropertyType().setValue(PointModel.PointType.HALT);
        break;
      case PARK_POSITION:
        model.getPropertyType().setValue(PointModel.PointType.PARK);
        break;
      case REPORT_POSITION:
        model.getPropertyType().setValue(PointModel.PointType.REPORT);
        break;
      default:
        throw new IllegalArgumentException("Unknown point type.");
    }

    for (Map.Entry<String, String> entry : pointTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    entry.getKey(),
                                                                    entry.getValue()));
    }

    // Gather information contained in visual layout
    if (visualLayoutTO != null) {
      String propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                              pointTO.getName(),
                                                              ElementPropKeys.POINT_POS_X);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        model.getPropertyLayoutPosX().setText(propertyValue);
      }

      propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                       pointTO.getName(),
                                                       ElementPropKeys.POINT_POS_Y);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        model.getPropertyLayoutPosY().setText(propertyValue);
      }

      propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                       pointTO.getName(),
                                                       ElementPropKeys.POINT_LABEL_OFFSET_X);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        model.getPropertyPointLabelOffsetX().setText(propertyValue);
      }

      propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                       pointTO.getName(),
                                                       ElementPropKeys.POINT_LABEL_OFFSET_Y);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        model.getPropertyPointLabelOffsetY().setText(propertyValue);
      }

      propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                       pointTO.getName(),
                                                       ElementPropKeys.POINT_LABEL_ORIENTATION_ANGLE);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        model.getPropertyPointLabelOrientationAngle().setText(propertyValue);
      }
    }

    return model;
  }

  public PathModel convertPathTO(PathCreationTO pathTO, VisualLayoutCreationTO visualLayoutTO) {
    PathModel model = new PathModel();

    model.getPropertyName().setText(pathTO.getName());
    model.getPropertyLength().setValueAndUnit(pathTO.getLength(), LengthProperty.Unit.MM);
    model.getPropertyRoutingCost().setValue((int) pathTO.getRoutingCost());
    model.getPropertyMaxVelocity().setValueAndUnit(pathTO.getMaxVelocity(),
                                                   SpeedProperty.Unit.MM_S);
    model.getPropertyMaxReverseVelocity().setValueAndUnit(pathTO.getMaxReverseVelocity(),
                                                          SpeedProperty.Unit.MM_S);
    model.getPropertyStartComponent().setText(pathTO.getSrcPointName());
    model.getPropertyEndComponent().setText(pathTO.getDestPointName());
    model.getPropertyLocked().setValue(pathTO.isLocked());

    for (Map.Entry<String, String> entry : pathTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    entry.getKey(),
                                                                    entry.getValue()));
    }

    // Gather information contained in visual layout
    if (visualLayoutTO != null) {
      String propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                              pathTO.getName(),
                                                              ElementPropKeys.PATH_CONN_TYPE);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        model.getPropertyPathConnType().setValue(PathModel.LinerType.valueOf(propertyValue));
      }

      propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                       pathTO.getName(),
                                                       ElementPropKeys.PATH_CONTROL_POINTS);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        model.getPropertyPathControlPoints().setText(propertyValue);
      }
    }

    return model;
  }

  public VehicleModel convertVehicleTO(VehicleCreationTO vehicleTO,
                                       VisualLayoutCreationTO visualLayoutTO) {
    VehicleModel model = new VehicleModel();

    model.getPropertyName().setText(vehicleTO.getName());
    model.getPropertyLength().setValueAndUnit(vehicleTO.getLength(), LengthProperty.Unit.MM);
    model.getPropertyMaxVelocity().setValueAndUnit(((double) vehicleTO.getMaxVelocity()),
                                                   Unit.MM_S);
    model.getPropertyMaxReverseVelocity().setValueAndUnit(
        ((double) vehicleTO.getMaxReverseVelocity()), Unit.MM_S);
    model.getPropertyEnergyLevelCritical().setValueAndUnit(vehicleTO.getEnergyLevelCritical(),
                                                           PercentProperty.Unit.PERCENT);
    model.getPropertyEnergyLevelGood().setValueAndUnit(vehicleTO.getEnergyLevelGood(),
                                                       PercentProperty.Unit.PERCENT);
    model.getPropertyEnergyLevelFullyRecharged()
        .setValueAndUnit(vehicleTO.getEnergyLevelFullyRecharged(),
                         PercentProperty.Unit.PERCENT);
    model.getPropertyEnergyLevelSufficientlyRecharged()
        .setValueAndUnit(vehicleTO.getEnergyLevelSufficientlyRecharged(),
                         PercentProperty.Unit.PERCENT);

    for (Map.Entry<String, String> entry : vehicleTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    entry.getKey(),
                                                                    entry.getValue()));
    }

    // Gather information contained in visual layout
    String propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                            vehicleTO.getName(),
                                                            ElementPropKeys.VEHICLE_ROUTE_COLOR);
    if (!Strings.isNullOrEmpty(propertyValue)) {
      model.getPropertyRouteColor().setColor(Colors.decodeFromHexRGB(propertyValue));
    }

    return model;
  }

  public LocationTypeModel convertLocationTypeTO(LocationTypeCreationTO locationTypeTO) {
    LocationTypeModel model = new LocationTypeModel();

    model.getPropertyName().setText(locationTypeTO.getName());

    for (String operation : locationTypeTO.getAllowedOperations()) {
      model.getPropertyAllowedOperations().addItem(operation);
    }

    for (Map.Entry<String, String> entry : locationTypeTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    entry.getKey(),
                                                                    entry.getValue()));

      // Set the default location type symbol since its value is not synchronized with the model's
      // properties
      if (Objects.equals(entry.getKey(), ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION)) {
        model.getPropertyDefaultRepresentation().setLocationRepresentation(
            LocationRepresentation.valueOf(entry.getValue())
        );
      }
    }

    return model;
  }

  public LocationModel convertLocationTO(LocationCreationTO locationTO,
                                         List<LocationCreationTO> locations,
                                         VisualLayoutCreationTO visualLayoutTO) {
    LocationModel model = new LocationModel();

    model.getPropertyName().setText(locationTO.getName());
    model.getPropertyModelPositionX().setValueAndUnit(locationTO.getPosition().getX(),
                                                      LengthProperty.Unit.MM);
    model.getPropertyModelPositionY().setValueAndUnit(locationTO.getPosition().getY(),
                                                      LengthProperty.Unit.MM);

    List<String> possibleLocationTypes = new ArrayList<>();
    for (LocationCreationTO location : locations) {
      if (!possibleLocationTypes.contains(location.getTypeName())) {
        possibleLocationTypes.add(location.getTypeName());
      }
    }
    model.getPropertyType().setPossibleValues(possibleLocationTypes);
    model.getPropertyType().setValue(locationTO.getTypeName());

    for (Map.Entry<String, String> entry : locationTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    entry.getKey(),
                                                                    entry.getValue()));

      // Set the location symbol (overwriting the default location type symbol) since its value is
      // not synchronized with the model's properties
      if (Objects.equals(entry.getKey(), ObjectPropConstants.LOC_DEFAULT_REPRESENTATION)) {
        model.getPropertyDefaultRepresentation().setLocationRepresentation(
            LocationRepresentation.valueOf(entry.getValue())
        );
      }
    }

    // Gather information contained in visual layout
    if (visualLayoutTO != null) {
      String propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                              locationTO.getName(),
                                                              ElementPropKeys.LOC_POS_X);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        model.getPropertyLayoutPositionX().setText(propertyValue);
      }

      propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                       locationTO.getName(),
                                                       ElementPropKeys.LOC_POS_Y);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        model.getPropertyLayoutPositionY().setText(propertyValue);
      }

      propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                       locationTO.getName(),
                                                       ElementPropKeys.LOC_LABEL_OFFSET_X);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        model.getPropertyLabelOffsetX().setText(propertyValue);
      }

      propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                       locationTO.getName(),
                                                       ElementPropKeys.LOC_LABEL_OFFSET_Y);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        model.getPropertyLabelOffsetY().setText(propertyValue);
      }

      propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                       locationTO.getName(),
                                                       ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        model.getPropertyLabelOrientationAngle().setText(propertyValue);
      }
    }

    return model;
  }

  public LinkModel convertLinkTO(Map.Entry<String, Set<String>> linkTO,
                                 LocationCreationTO locationTO) {
    LinkModel model = new LinkModel();

    model.getPropertyName().setText(String.format("%s --- %s",
                                                  linkTO.getKey(),
                                                  locationTO.getName()));

    for (String operation : linkTO.getValue()) {
      model.getPropertyAllowedOperations().addItem(operation);
    }

    model.getPropertyStartComponent().setText(linkTO.getKey());
    model.getPropertyEndComponent().setText(locationTO.getName());

    return model;
  }

  public BlockModel convertBlockTO(BlockCreationTO blockTO, VisualLayoutCreationTO visualLayoutTO) {
    BlockModel model = new BlockModel();

    model.getPropertyName().setText(blockTO.getName());

    model.getPropertyType().setValue(convertBlockType(blockTO));

    for (String member : blockTO.getMemberNames()) {
      model.getPropertyElements().addItem(member);
    }

    for (Map.Entry<String, String> entry : blockTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    entry.getKey(),
                                                                    entry.getValue()));
    }

    // Gather information contained in visual layout
    if (visualLayoutTO != null) {
      String propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                              blockTO.getName(),
                                                              ElementPropKeys.BLOCK_COLOR);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        model.getPropertyColor().setColor(Color.decode(propertyValue));
      }
    }

    return model;
  }

  private BlockModel.BlockType convertBlockType(BlockCreationTO blockTO) {
    switch (blockTO.getType()) {
      case SAME_DIRECTION_ONLY:
        return BlockModel.BlockType.SAME_DIRECTION_ONLY;
      case SINGLE_VEHICLE_ONLY:
        return BlockModel.BlockType.SINGLE_VEHICLE_ONLY;
      default:
        LOG.warn("Unhandled block type '{}'. Falling back to '{}'",
                 blockTO.getType(),
                 BlockModel.BlockType.SINGLE_VEHICLE_ONLY);
        return BlockModel.BlockType.SINGLE_VEHICLE_ONLY;
    }
  }

  public GroupModel convertGroupTO(GroupCreationTO groupTO) {
    GroupModel model = new GroupModel();

    model.getPropertyName().setText(groupTO.getName());

    for (String member : groupTO.getMemberNames()) {
      model.getPropertyElements().addItem(member);
    }

    for (Map.Entry<String, String> entry : groupTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    entry.getKey(),
                                                                    entry.getValue()));
    }

    return model;
  }

  public LayoutModel convertVisualLayoutTO(VisualLayoutCreationTO visualLayoutTO) {
    LayoutModel model = new LayoutModel();

    model.getPropertyName().setText(visualLayoutTO.getName());
    model.getPropertyScaleX().setValueAndUnit(visualLayoutTO.getScaleX(), LengthProperty.Unit.MM);
    model.getPropertyScaleY().setValueAndUnit(visualLayoutTO.getScaleY(), LengthProperty.Unit.MM);

    for (Map.Entry<String, String> entry : visualLayoutTO.getProperties().entrySet()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    entry.getKey(),
                                                                    entry.getValue()));
    }

    return model;
  }

  @Nullable
  private String getPropertyValueFromVisualLayout(VisualLayoutCreationTO visualLayout,
                                                  String layoutElementName,
                                                  String propertyName) {
    Optional<Map.Entry<String, String>> result = visualLayout.getModelElements().stream()
        .filter(layoutElement -> layoutElement.getName().equals(layoutElementName))
        .flatMap(layoutElement -> layoutElement.getProperties().entrySet().stream())
        .filter(entry -> entry.getKey().equals(propertyName))
        .findAny();

    return result.isPresent() ? result.get().getValue() : null;
  }
}
