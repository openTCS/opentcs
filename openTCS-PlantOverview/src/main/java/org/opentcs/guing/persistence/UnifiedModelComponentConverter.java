/*
 * openTCS copyright information:
 * Copyright (c) 2017 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.persistence;

import com.google.common.base.Strings;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.model.Point;
import static org.opentcs.data.model.Point.Type.HALT_POSITION;
import static org.opentcs.data.model.Point.Type.PARK_POSITION;
import static org.opentcs.data.model.Point.Type.REPORT_POSITION;
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
import org.opentcs.guing.model.AbstractFigureComponent;
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
import org.opentcs.guing.model.elements.StaticRouteModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.util.Colors;
import org.opentcs.util.persistence.binding.AllowedOperationTO;
import org.opentcs.util.persistence.binding.BlockTO;
import org.opentcs.util.persistence.binding.Comparators;
import org.opentcs.util.persistence.binding.GroupTO;
import org.opentcs.util.persistence.binding.LocationTO;
import org.opentcs.util.persistence.binding.LocationTypeTO;
import org.opentcs.util.persistence.binding.MemberTO;
import org.opentcs.util.persistence.binding.PathTO;
import org.opentcs.util.persistence.binding.PlantModelTO;
import org.opentcs.util.persistence.binding.PointTO;
import org.opentcs.util.persistence.binding.PropertyTO;
import org.opentcs.util.persistence.binding.StaticRouteTO;
import org.opentcs.util.persistence.binding.VehicleTO;
import org.opentcs.util.persistence.binding.VisualLayoutTO;

/**
 * Converts <code>ModelComponents</code> to corresponding Java beans (JAXB classes).
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class UnifiedModelComponentConverter {

  /**
   * The file format version this converter works with.
   */
  private static final String VERSION_STRING = "0.0.2";

  public PlantModelTO convertSystemModel(SystemModel systemModel, String modelName) {
    PlantModelTO plantModel = new PlantModelTO();

    plantModel.setName(modelName);
    plantModel.setVersion(VERSION_STRING);
    plantModel.setProperties(convertProperties(systemModel.getPropertyMiscellaneous()));

    List<PointTO> points = new ArrayList<>();
    for (PointModel model : systemModel.getPointModels()) {
      points.add(convertPoint(model, systemModel.getPathModels()));
    }
    Collections.sort(points, Comparators.elementsByName());
    plantModel.setPoints(points);

    List<PathTO> paths = new ArrayList<>();
    for (PathModel model : systemModel.getPathModels()) {
      paths.add(convertPath(model));
    }
    Collections.sort(paths, Comparators.elementsByName());
    plantModel.setPaths(paths);

    List<VehicleTO> vehicles = new ArrayList<>();
    for (VehicleModel model : systemModel.getVehicleModels()) {
      vehicles.add(convertVehicle(model));
    }
    Collections.sort(vehicles, Comparators.elementsByName());
    plantModel.setVehicles(vehicles);

    List<LocationTypeTO> locationTypes = new ArrayList<>();
    for (LocationTypeModel model : systemModel.getLocationTypeModels()) {
      locationTypes.add(convertLocationType(model));
    }
    Collections.sort(locationTypes, Comparators.elementsByName());
    plantModel.setLocationTypes(locationTypes);

    List<LocationTO> locations = new ArrayList<>();
    for (LocationModel model : systemModel.getLocationModels()) {
      locations.add(convertLocation(model, systemModel.getLinkModels()));
    }
    Collections.sort(locations, Comparators.elementsByName());
    plantModel.setLocations(locations);

    List<BlockTO> blocks = new ArrayList<>();
    for (BlockModel model : systemModel.getBlockModels()) {
      blocks.add(convertBlock(model));
    }
    Collections.sort(blocks, Comparators.elementsByName());
    plantModel.setBlocks(blocks);

    List<StaticRouteTO> staticRoutes = new ArrayList<>();
    for (StaticRouteModel model : systemModel.getStaticRouteModels()) {
      staticRoutes.add(convertStaticRoute(model));
    }
    Collections.sort(staticRoutes, Comparators.elementsByName());
    plantModel.setStaticRoutes(staticRoutes);

    List<GroupTO> groups = new ArrayList<>();
    for (GroupModel model : systemModel.getGroupModels()) {
      groups.add(convertGroup(model));
    }
    Collections.sort(groups, Comparators.elementsByName());
    plantModel.setGroups(groups);

    List<VisualLayoutTO> visualLayouts = new ArrayList<>();
    for (LayoutModel model : systemModel.getLayoutModels()) {
      visualLayouts.add(convertVisualLayout(model, systemModel));
    }
    Collections.sort(visualLayouts, Comparators.elementsByName());
    plantModel.setVisualLayouts(visualLayouts);

    return plantModel;
  }

  private List<PropertyTO> convertProperties(KeyValueSetProperty kvsp) {
    return kvsp.getItems().stream()
        .sorted((kvp1, kvp2) -> kvp1.getKey().compareTo(kvp2.getKey()))
        .map(kvp -> new PropertyTO().setName(kvp.getKey()).setValue(kvp.getValue()))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private PointTO convertPoint(PointModel pointModel, List<PathModel> pathModels) {
    PointTO point = new PointTO();

    point.setName(pointModel.getPropertyName().getText());
    point.setxPosition((long) pointModel.getPropertyModelPositionX().getValueByUnit(LengthProperty.Unit.MM));
    point.setyPosition((long) pointModel.getPropertyModelPositionY().getValueByUnit(LengthProperty.Unit.MM));
    point.setVehicleOrientationAngle((float) pointModel.getPropertyVehicleOrientationAngle().getValueByUnit(AngleProperty.Unit.DEG));

    switch ((PointModel.PointType) pointModel.getPropertyType().getValue()) {
      case HALT:
        point.setType(HALT_POSITION.name());
        break;
      case REPORT:
        point.setType(REPORT_POSITION.name());
        break;
      case PARK:
        point.setType(PARK_POSITION.name());
        break;
      default:
        throw new IllegalArgumentException("Unhandled point type.");
    }

    // Get this point's outgoing paths
    for (PathModel pathModel : pathModels) {
      if (Objects.equals(pathModel.getPropertyStartComponent().getText(), point.getName())) {
        point.getOutgoingPaths().add(
            new PointTO.OutgoingPath().setName(pathModel.getPropertyName().getText())
        );
      }
    }
    Collections.sort(point.getOutgoingPaths(), Comparators.outgoingPathsByName());

    for (KeyValueProperty kvp : pointModel.getPropertyMiscellaneous().getItems()) {
      point.getProperties().add(new PropertyTO().setName(kvp.getKey()).setValue(kvp.getValue()));
    }

    return point;
  }

  private PathTO convertPath(PathModel pathModel) {
    PathTO path = new PathTO();

    path.setName(pathModel.getPropertyName().getText());
    path.setSourcePoint(pathModel.getPropertyStartComponent().getText());
    path.setDestinationPoint(pathModel.getPropertyEndComponent().getText());
    path.setLength((long) pathModel.getPropertyLength().getValueByUnit(LengthProperty.Unit.MM));
    path.setRoutingCost(((Integer) pathModel.getPropertyRoutingCost().getValue()).longValue());
    path.setMaxVelocity((long) pathModel.getPropertyMaxVelocity().getValueByUnit(SpeedProperty.Unit.MM_S));
    path.setMaxReverseVelocity((long) pathModel.getPropertyMaxReverseVelocity().getValueByUnit(SpeedProperty.Unit.MM_S));
    path.setLocked((Boolean) pathModel.getPropertyLocked().getValue());

    for (KeyValueProperty kvp : pathModel.getPropertyMiscellaneous().getItems()) {
      path.getProperties().add(new PropertyTO().setName(kvp.getKey()).setValue(kvp.getValue()));
    }

    return path;
  }

  private VehicleTO convertVehicle(VehicleModel vehicleModel) {
    VehicleTO vehicle = new VehicleTO();

    vehicle.setName(vehicleModel.getPropertyName().getText());
    vehicle.setLength((long) vehicleModel.getPropertyLength().getValueByUnit(LengthProperty.Unit.MM));
    vehicle.setMaxVelocity(((Double) vehicleModel.getPropertyMaxVelocity().getValueByUnit(Unit.MM_S)).intValue());
    vehicle.setMaxReverseVelocity(((Double) vehicleModel.getPropertyMaxReverseVelocity().getValueByUnit(Unit.MM_S)).intValue());
    vehicle.setEnergyLevelGood((long) vehicleModel.getPropertyEnergyLevelGood().getValueByUnit(PercentProperty.Unit.PERCENT));
    vehicle.setEnergyLevelCritical((long) vehicleModel.getPropertyEnergyLevelCritical().getValueByUnit(PercentProperty.Unit.PERCENT));

    for (KeyValueProperty kvp : vehicleModel.getPropertyMiscellaneous().getItems()) {
      vehicle.getProperties().add(new PropertyTO().setName(kvp.getKey()).setValue(kvp.getValue()));
    }

    return vehicle;
  }

  private LocationTypeTO convertLocationType(LocationTypeModel locTypeModel) {
    LocationTypeTO locType = new LocationTypeTO();

    locType.setName(locTypeModel.getPropertyName().getText());

    for (String operation : locTypeModel.getPropertyAllowedOperations().getItems()) {
      AllowedOperationTO allowedOperation = new AllowedOperationTO();
      allowedOperation.setName(operation);
      locType.getAllowedOperations().add(allowedOperation);
    }

    for (KeyValueProperty kvp : locTypeModel.getPropertyMiscellaneous().getItems()) {
      locType.getProperties().add(new PropertyTO().setName(kvp.getKey()).setValue(kvp.getValue()));
    }

    SymbolProperty symp = locTypeModel.getPropertyDefaultRepresentation();
    if (symp.getLocationRepresentation() != null) {
      locType.getProperties().add(
          new PropertyTO()
              .setName(ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION)
              .setValue(symp.getLocationRepresentation().name())
      );
    }
    else {
      locType.getProperties().removeIf(
          prop -> prop.getName().equals(ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION)
      );
    }

    return locType;
  }

  private LocationTO convertLocation(LocationModel locationModel, List<LinkModel> linkModels) {
    LocationTO location = new LocationTO();

    String locationName = locationModel.getPropertyName().getText();

    location.setName(locationName);
    location.setxPosition((long) locationModel.getPropertyModelPositionX().getValueByUnit(LengthProperty.Unit.MM));
    location.setyPosition((long) locationModel.getPropertyModelPositionY().getValueByUnit(LengthProperty.Unit.MM));
    location.setType(locationModel.getPropertyType().getValue().toString());

    // Get this location's links
    for (LinkModel linkModel : linkModels) {
      String linkLocationName = linkModel.getLocation().getName();
      if (!linkLocationName.equals(locationName)) {
        continue;
      }

      LocationTO.Link link = new LocationTO.Link();
      link.setPoint(linkModel.getPoint().getName());

      for (String operation : linkModel.getPropertyAllowedOperations().getItems()) {
        AllowedOperationTO allowedOperation = new AllowedOperationTO();
        allowedOperation.setName(operation);
        link.getAllowedOperations().add(allowedOperation);
      }
      location.getLinks().add(link);
    }
    Collections.sort(location.getLinks(), Comparators.linksByPointName());

    for (KeyValueProperty kvp : locationModel.getPropertyMiscellaneous().getItems()) {
      location.getProperties().add(new PropertyTO().setName(kvp.getKey()).setValue(kvp.getValue()));
    }

    SymbolProperty symp = locationModel.getPropertyDefaultRepresentation();
    if (symp.getLocationRepresentation() != null) {
      location.getProperties().add(
          new PropertyTO()
              .setName(ObjectPropConstants.LOC_DEFAULT_REPRESENTATION)
              .setValue(symp.getLocationRepresentation().name())
      );
    }
    else {
      location.getProperties().removeIf(
          prop -> prop.getName().equals(ObjectPropConstants.LOC_DEFAULT_REPRESENTATION)
      );
    }

    return location;
  }

  private BlockTO convertBlock(BlockModel blockModel) {
    BlockTO block = new BlockTO();

    block.setName(blockModel.getPropertyName().getText());

    for (String element : blockModel.getPropertyElements().getItems()) {
      MemberTO member = new MemberTO();
      member.setName(element);
      block.getMembers().add(member);
    }
    Collections.sort(block.getMembers(), Comparators.elementsByName());

    for (KeyValueProperty kvp : blockModel.getPropertyMiscellaneous().getItems()) {
      block.getProperties().add(new PropertyTO().setName(kvp.getKey()).setValue(kvp.getValue()));
    }

    return block;
  }

  private StaticRouteTO convertStaticRoute(StaticRouteModel staticRouteModel) {
    StaticRouteTO route = new StaticRouteTO();

    route.setName(staticRouteModel.getPropertyName().getText());

    for (String element : staticRouteModel.getPropertyElements().getItems()) {
      StaticRouteTO.Hop hop = new StaticRouteTO.Hop();
      hop.setName(element);
      route.getHops().add(hop);
    }
    Collections.sort(route.getHops(), Comparators.hopsByName());

    for (KeyValueProperty kvp : staticRouteModel.getPropertyMiscellaneous().getItems()) {
      route.getProperties().add(new PropertyTO().setName(kvp.getKey()).setValue(kvp.getValue()));
    }

    return route;
  }

  private GroupTO convertGroup(GroupModel groupModel) {
    GroupTO group = new GroupTO();

    group.setName(groupModel.getPropertyName().getText());

    for (String element : groupModel.getPropertyElements().getItems()) {
      MemberTO member = new MemberTO();
      member.setName(element);
      group.getMembers().add(member);
    }
    Collections.sort(group.getMembers(), Comparators.elementsByName());

    for (KeyValueProperty kvp : groupModel.getPropertyMiscellaneous().getItems()) {
      group.getProperties().add(new PropertyTO().setName(kvp.getKey()).setValue(kvp.getValue()));
    }

    return group;
  }

  private VisualLayoutTO convertVisualLayout(LayoutModel layoutModel, SystemModel systemModel) {
    VisualLayoutTO layout = new VisualLayoutTO();

    layout.setName(layoutModel.getPropertyName().getText());
    layout.setScaleX((float) layoutModel.getPropertyScaleX().getValueByUnit(LengthProperty.Unit.MM));
    layout.setScaleY((float) layoutModel.getPropertyScaleY().getValueByUnit(LengthProperty.Unit.MM));

    layout.getModelLayoutElements()
        .addAll(extractBlockInformation(systemModel.getBlockModels()));
    layout.getModelLayoutElements()
        .addAll(extractLocationInformation(systemModel.getLocationModels()));
    layout.getModelLayoutElements()
        .addAll(extractPointInformation(systemModel.getPointModels()));
    layout.getModelLayoutElements()
        .addAll(extractPathInformation(systemModel.getPathModels()));
    layout.getModelLayoutElements()
        .addAll(extractVehiclePathColorInformation(systemModel.getVehicleModels()));
    Collections.sort(layout.getModelLayoutElements(), Comparators.modelLayoutelementsByName());

    for (KeyValueProperty kvp : layoutModel.getPropertyMiscellaneous().getItems()) {
      layout.getProperties().add(new PropertyTO().setName(kvp.getKey()).setValue(kvp.getValue()));
    }

    return layout;
  }

  private List<VisualLayoutTO.ModelLayoutElement> extractVehiclePathColorInformation(
      List<VehicleModel> vehicleModels) {
    List<VisualLayoutTO.ModelLayoutElement> result = new LinkedList<>();

    for (VehicleModel vehicleModel : vehicleModels) {
      VisualLayoutTO.ModelLayoutElement mle = new VisualLayoutTO.ModelLayoutElement();

      mle.setVisualizedObjectName(vehicleModel.getPropertyName().getText());

      mle.getProperties().add(
          new PropertyTO()
              .setName(ElementPropKeys.VEHICLE_ROUTE_COLOR)
              .setValue(Colors.encodeToHexRGB(vehicleModel.getPropertyRouteColor().getColor()))
      );

      result.add(mle);
    }

    return result;
  }

  private List<VisualLayoutTO.ModelLayoutElement> extractPointInformation(List<PointModel> points) {
    List<VisualLayoutTO.ModelLayoutElement> result = new ArrayList<>();

    for (PointModel point : points) {
      VisualLayoutTO.ModelLayoutElement mle = new VisualLayoutTO.ModelLayoutElement();

      mle.setVisualizedObjectName(point.getPropertyName().getText());

      mle.getProperties().add(convertStringProperty(point, ElementPropKeys.POINT_POS_X));
      mle.getProperties().add(convertStringProperty(point, ElementPropKeys.POINT_POS_Y));
      mle.getProperties().add(convertStringProperty(point, ElementPropKeys.POINT_LABEL_OFFSET_X));
      mle.getProperties().add(convertStringProperty(point, ElementPropKeys.POINT_LABEL_OFFSET_Y));
      Collections.sort(mle.getProperties(), Comparators.propertiesByName());

      result.add(mle);
    }

    return result;
  }

  private List<VisualLayoutTO.ModelLayoutElement> extractPathInformation(List<PathModel> paths) {
    List<VisualLayoutTO.ModelLayoutElement> result = new ArrayList<>();

    for (PathModel path : paths) {
      VisualLayoutTO.ModelLayoutElement mle = new VisualLayoutTO.ModelLayoutElement();

      mle.setVisualizedObjectName(path.getPropertyName().getText());

      PathModel.LinerType pathType = (PathModel.LinerType) path.getPropertyPathConnType().getValue();
      mle.getProperties().add(
          new PropertyTO().setName(ElementPropKeys.PATH_CONN_TYPE).setValue(pathType.name())
      );

      if (Objects.equals(pathType, PathModel.LinerType.BEZIER)
          || Objects.equals(pathType, PathModel.LinerType.BEZIER_3)) {
        mle.getProperties().add(convertStringProperty(path, ElementPropKeys.PATH_CONTROL_POINTS));
      }
      Collections.sort(mle.getProperties(), Comparators.propertiesByName());

      result.add(mle);
    }

    return result;
  }

  private List<VisualLayoutTO.ModelLayoutElement> extractLocationInformation(
      List<LocationModel> locations) {
    List<VisualLayoutTO.ModelLayoutElement> result = new ArrayList<>();

    for (LocationModel location : locations) {
      VisualLayoutTO.ModelLayoutElement mle = new VisualLayoutTO.ModelLayoutElement();

      mle.setVisualizedObjectName(location.getPropertyName().getText());

      mle.getProperties().add(convertStringProperty(location, ElementPropKeys.LOC_LABEL_OFFSET_X));
      mle.getProperties().add(convertStringProperty(location, ElementPropKeys.LOC_LABEL_OFFSET_Y));
      mle.getProperties().add(convertStringProperty(location, ElementPropKeys.LOC_POS_X));
      mle.getProperties().add(convertStringProperty(location, ElementPropKeys.LOC_POS_Y));
      Collections.sort(mle.getProperties(), Comparators.propertiesByName());

      result.add(mle);
    }

    return result;
  }

  private List<VisualLayoutTO.ModelLayoutElement> extractBlockInformation(List<BlockModel> blocks) {
    List<VisualLayoutTO.ModelLayoutElement> result = new ArrayList<>();

    for (BlockModel block : blocks) {
      VisualLayoutTO.ModelLayoutElement mle = new VisualLayoutTO.ModelLayoutElement();

      mle.setVisualizedObjectName(block.getPropertyName().getText());

      mle.getProperties().add(
          new PropertyTO()
              .setName(ElementPropKeys.BLOCK_COLOR)
              .setValue(Colors.encodeToHexRGB(block.getPropertyColor().getColor()))
      );

      result.add(mle);
    }

    return result;
  }

  private PropertyTO convertStringProperty(AbstractFigureComponent component, String propertyKey) {
    StringProperty sp = (StringProperty) component.getProperty(propertyKey);

    return new PropertyTO()
        .setName(propertyKey)
        .setValue(sp.getText());
  }

  public PointModel convertPointTO(PointTO pointTO, VisualLayoutTO visualLayoutTO) {
    PointModel model = new PointModel();

    model.getPropertyName().setText(pointTO.getName());

    model.getPropertyModelPositionX().setValueAndUnit(pointTO.getxPosition(),
                                                      LengthProperty.Unit.MM);
    model.getPropertyModelPositionY().setValueAndUnit(pointTO.getyPosition(),
                                                      LengthProperty.Unit.MM);
    model.getPropertyVehicleOrientationAngle().setValueAndUnit(
        pointTO.getVehicleOrientationAngle().doubleValue(), AngleProperty.Unit.DEG
    );

    switch (Point.Type.valueOf(pointTO.getType())) {
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

    for (PropertyTO property : pointTO.getProperties()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    property.getName(),
                                                                    property.getValue()));
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

  public PathModel convertPathTO(PathTO pathTO, VisualLayoutTO visualLayoutTO) {
    PathModel model = new PathModel();

    model.getPropertyName().setText(pathTO.getName());
    model.getPropertyLength().setValueAndUnit(pathTO.getLength(), LengthProperty.Unit.MM);
    model.getPropertyRoutingCost().setValue(pathTO.getRoutingCost().intValue());
    model.getPropertyMaxVelocity().setValueAndUnit(pathTO.getMaxVelocity(),
                                                   SpeedProperty.Unit.MM_S);
    model.getPropertyMaxReverseVelocity().setValueAndUnit(pathTO.getMaxReverseVelocity(),
                                                          SpeedProperty.Unit.MM_S);
    model.getPropertyStartComponent().setText(pathTO.getSourcePoint());
    model.getPropertyEndComponent().setText(pathTO.getDestinationPoint());
    model.getPropertyLocked().setValue(pathTO.isLocked());

    for (PropertyTO property : pathTO.getProperties()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    property.getName(),
                                                                    property.getValue()));
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

  public VehicleModel convertVehicleTO(VehicleTO vehicleTO, VisualLayoutTO visualLayoutTO) {
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

    for (PropertyTO property : vehicleTO.getProperties()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    property.getName(),
                                                                    property.getValue()));
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

  public LocationTypeModel convertLocationTypeTO(LocationTypeTO locationTypeTO) {
    LocationTypeModel model = new LocationTypeModel();

    model.getPropertyName().setText(locationTypeTO.getName());

    for (AllowedOperationTO operation : locationTypeTO.getAllowedOperations()) {
      model.getPropertyAllowedOperations().addItem(operation.getName());
    }

    for (PropertyTO property : locationTypeTO.getProperties()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    property.getName(),
                                                                    property.getValue()));

      // Set the default location type symbol since its value is not synchronized with the model's
      // properties
      if (Objects.equals(property.getName(), ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION)) {
        model.getPropertyDefaultRepresentation().setLocationRepresentation(
            LocationRepresentation.valueOf(property.getValue())
        );
      }
    }

    return model;
  }

  public LocationModel convertLocationTO(LocationTO locationTO,
                                         List<LocationTO> locations,
                                         VisualLayoutTO visualLayoutTO) {
    LocationModel model = new LocationModel();

    model.getPropertyName().setText(locationTO.getName());
    model.getPropertyModelPositionX().setValueAndUnit(locationTO.getxPosition(),
                                                      LengthProperty.Unit.MM);
    model.getPropertyModelPositionY().setValueAndUnit(locationTO.getyPosition(),
                                                      LengthProperty.Unit.MM);

    List<String> possibleLocationTypes = new ArrayList<>();
    for (LocationTO location : locations) {
      if (!possibleLocationTypes.contains(location.getType())) {
        possibleLocationTypes.add(location.getType());
      }
    }
    model.getPropertyType().setPossibleValues(possibleLocationTypes);
    model.getPropertyType().setValue(locationTO.getType());

    for (PropertyTO property : locationTO.getProperties()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    property.getName(),
                                                                    property.getValue()));

      // Set the location symbol (overwriting the default location type symbol) since its value is
      // not synchronized with the model's properties
      if (Objects.equals(property.getName(), ObjectPropConstants.LOC_DEFAULT_REPRESENTATION)) {
        model.getPropertyDefaultRepresentation().setLocationRepresentation(
            LocationRepresentation.valueOf(property.getValue())
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

  public LinkModel convertLinkTO(LocationTO.Link linkTO, LocationTO locationTO) {
    LinkModel model = new LinkModel();

    model.getPropertyName().setText(String.format("%s --- %s",
                                                  linkTO.getPoint(),
                                                  locationTO.getName()));

    for (AllowedOperationTO operation : linkTO.getAllowedOperations()) {
      model.getPropertyAllowedOperations().addItem(operation.getName());
    }

    model.getPropertyStartComponent().setText(linkTO.getPoint());
    model.getPropertyEndComponent().setText(locationTO.getName());

    return model;
  }

  public BlockModel convertBlockTO(BlockTO blockTO, VisualLayoutTO visualLayoutTO) {
    BlockModel model = new BlockModel();

    model.getPropertyName().setText(blockTO.getName());

    for (MemberTO member : blockTO.getMembers()) {
      model.getPropertyElements().addItem(member.getName());
    }

    for (PropertyTO property : blockTO.getProperties()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    property.getName(),
                                                                    property.getValue()));
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

  public StaticRouteModel convertStaticRouteTO(StaticRouteTO staticRouteTO,
                                               VisualLayoutTO visualLayoutTO) {
    StaticRouteModel model = new StaticRouteModel();

    model.getPropertyName().setText(staticRouteTO.getName());

    for (StaticRouteTO.Hop hop : staticRouteTO.getHops()) {
      model.getPropertyElements().addItem(hop.getName());
    }

    for (PropertyTO property : staticRouteTO.getProperties()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    property.getName(),
                                                                    property.getValue()));
    }

    // Gather information contained in visual layout
    if (visualLayoutTO != null) {
      String propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                              staticRouteTO.getName(),
                                                              ElementPropKeys.BLOCK_COLOR);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        model.getPropertyColor().setColor(Color.decode(propertyValue));
      }
    }

    return model;
  }

  public GroupModel convertGroupTO(GroupTO groupTO) {
    GroupModel model = new GroupModel();

    model.getPropertyName().setText(groupTO.getName());

    for (MemberTO member : groupTO.getMembers()) {
      model.getPropertyElements().addItem(member.getName());
    }

    for (PropertyTO property : groupTO.getProperties()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    property.getName(),
                                                                    property.getValue()));
    }

    return model;
  }

  public LayoutModel convertVisualLayoutTO(VisualLayoutTO visualLayoutTO) {
    LayoutModel model = new LayoutModel();

    model.getPropertyName().setText(visualLayoutTO.getName());
    model.getPropertyScaleX().setValueAndUnit(visualLayoutTO.getScaleX(), LengthProperty.Unit.MM);
    model.getPropertyScaleY().setValueAndUnit(visualLayoutTO.getScaleY(), LengthProperty.Unit.MM);

    for (PropertyTO property : visualLayoutTO.getProperties()) {
      model.getPropertyMiscellaneous().addItem(new KeyValueProperty(model,
                                                                    property.getName(),
                                                                    property.getValue()));
    }

    return model;
  }

  @Nullable
  private String getPropertyValueFromVisualLayout(VisualLayoutTO visualLayout,
                                                  String layoutElementName,
                                                  String propertyName) {
    Optional<PropertyTO> result = visualLayout.getModelLayoutElements().stream()
        .filter(layoutElement -> layoutElement.getVisualizedObjectName().equals(layoutElementName))
        .flatMap(layoutElement -> layoutElement.getProperties().stream())
        .filter(property -> property.getName().equals(propertyName))
        .findAny();

    return result.isPresent() ? result.get().getValue() : null;
  }
}
