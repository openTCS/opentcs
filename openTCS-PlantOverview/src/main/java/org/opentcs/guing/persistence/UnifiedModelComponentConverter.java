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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.model.Point;
import static org.opentcs.data.model.Point.Type.HALT_POSITION;
import static org.opentcs.data.model.Point.Type.PARK_POSITION;
import static org.opentcs.data.model.Point.Type.REPORT_POSITION;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.guing.components.properties.type.AbstractProperty;
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.components.properties.type.BooleanProperty;
import org.opentcs.guing.components.properties.type.ColorProperty;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.CoursePointProperty;
import org.opentcs.guing.components.properties.type.IntegerProperty;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.LocationTypeProperty;
import org.opentcs.guing.components.properties.type.PercentProperty;
import org.opentcs.guing.components.properties.type.SpeedProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.StringSetProperty;
import org.opentcs.guing.components.properties.type.SymbolProperty;
import org.opentcs.guing.model.AbstractFigureComponent;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.AbstractConnection;
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

  private PointTO convertPoint(PointModel pointModel, List<PathModel> pathModels) {
    PointTO point = new PointTO();

    StringProperty sp = (StringProperty) pointModel.getProperty(PointModel.NAME);
    point.setName(sp.getText());

    CoordinateProperty cp = (CoordinateProperty) pointModel.getProperty(PointModel.MODEL_X_POSITION);
    point.setxPosition((long) cp.getValueByUnit(LengthProperty.Unit.MM));

    cp = (CoordinateProperty) pointModel.getProperty(PointModel.MODEL_Y_POSITION);
    point.setyPosition((long) cp.getValueByUnit(LengthProperty.Unit.MM));

    AngleProperty ap = (AngleProperty) pointModel.getProperty(PointModel.VEHICLE_ORIENTATION_ANGLE);
    point.setVehicleOrientationAngle((float) ap.getValueByUnit(AngleProperty.Unit.DEG));

    AbstractProperty selp = (AbstractProperty) pointModel.getProperty(PointModel.TYPE);
    PointModel.PointType pointType = (PointModel.PointType) selp.getValue();
    switch (pointType) {
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
        throw new IllegalArgumentException("Unknown point type.");
    }

    // Get this point's outgoing paths
    for (PathModel pathModel : pathModels) {
      StringProperty pathStartComponent
          = (StringProperty) pathModel.getProperty(AbstractConnection.START_COMPONENT);
      if (Objects.equals(pathStartComponent.getText(), point.getName())) {
        StringProperty pathName = (StringProperty) pathModel.getProperty(PathModel.NAME);
        point.getOutgoingPaths().add(new PointTO.OutgoingPath().setName(pathName.getText()));
      }
    }
    Collections.sort(point.getOutgoingPaths(), Comparators.outgoingPathsByName());

    KeyValueSetProperty kvsp
        = (KeyValueSetProperty) pointModel.getProperty(PointModel.MISCELLANEOUS);
    for (KeyValueProperty kvp : kvsp.getItems()) {
      PropertyTO property = new PropertyTO();
      property.setName(kvp.getKey());
      property.setValue(kvp.getValue());
      point.getProperties().add(property);
    }

    return point;
  }

  private PathTO convertPath(PathModel pathModel) {
    PathTO path = new PathTO();

    StringProperty sp = (StringProperty) pathModel.getProperty(PathModel.NAME);
    path.setName(sp.getText());

    sp = (StringProperty) pathModel.getProperty(PathModel.START_COMPONENT);
    path.setSourcePoint(sp.getText());

    sp = (StringProperty) pathModel.getProperty(PathModel.END_COMPONENT);
    path.setDestinationPoint(sp.getText());

    LengthProperty lp = (LengthProperty) pathModel.getProperty(PathModel.LENGTH);
    path.setLength((long) lp.getValueByUnit(LengthProperty.Unit.MM));

    IntegerProperty ip = (IntegerProperty) pathModel.getProperty(PathModel.ROUTING_COST);
    path.setRoutingCost(((Integer) ip.getValue()).longValue());

    SpeedProperty spdp = (SpeedProperty) pathModel.getProperty(PathModel.MAX_VELOCITY);
    path.setMaxVelocity((long) spdp.getValueByUnit(SpeedProperty.Unit.MM_S));

    spdp = (SpeedProperty) pathModel.getProperty(PathModel.MAX_REVERSE_VELOCITY);
    path.setMaxReverseVelocity((long) spdp.getValueByUnit(SpeedProperty.Unit.MM_S));

    BooleanProperty bp = (BooleanProperty) pathModel.getProperty(PathModel.LOCKED);
    path.setLocked((Boolean) bp.getValue());

    KeyValueSetProperty kvsp = (KeyValueSetProperty) pathModel.getProperty(PathModel.MISCELLANEOUS);
    for (KeyValueProperty kvp : kvsp.getItems()) {
      PropertyTO property = new PropertyTO();
      property.setName(kvp.getKey());
      property.setValue(kvp.getValue());
      path.getProperties().add(property);
    }

    return path;
  }

  private VehicleTO convertVehicle(VehicleModel vehicleModel) {
    VehicleTO vehicle = new VehicleTO();

    StringProperty sp = (StringProperty) vehicleModel.getProperty(VehicleModel.NAME);
    vehicle.setName(sp.getText());

    LengthProperty lp = (LengthProperty) vehicleModel.getProperty(VehicleModel.LENGTH);
    vehicle.setLength((long) lp.getValueByUnit(LengthProperty.Unit.MM));

    PercentProperty pp = (PercentProperty) vehicleModel.getProperty(VehicleModel.ENERGY_LEVEL_GOOD);
    vehicle.setEnergyLevelGood((long) pp.getValueByUnit(PercentProperty.Unit.PERCENT));

    pp = (PercentProperty) vehicleModel.getProperty(VehicleModel.ENERGY_LEVEL_CRITICAL);
    vehicle.setEnergyLevelCritical((long) pp.getValueByUnit(PercentProperty.Unit.PERCENT));

    KeyValueSetProperty kvsp
        = (KeyValueSetProperty) vehicleModel.getProperty(VehicleModel.MISCELLANEOUS);
    for (KeyValueProperty kvp : kvsp.getItems()) {
      PropertyTO property = new PropertyTO();
      property.setName(kvp.getKey());
      property.setValue(kvp.getValue());
      vehicle.getProperties().add(property);
    }

    CoursePointProperty cpp
        = (CoursePointProperty) vehicleModel.getProperty(VehicleModel.INITIAL_POSITION);
    if (!Strings.isNullOrEmpty(cpp.getPointName())) {
      vehicle.getProperties().add(new PropertyTO()
          .setName(ObjectPropConstants.VEHICLE_INITIAL_POSITION)
          .setValue(cpp.getPointName()));
    }

    return vehicle;
  }

  private LocationTypeTO convertLocationType(LocationTypeModel locationTypeModel) {
    LocationTypeTO locationType = new LocationTypeTO();

    StringProperty sp = (StringProperty) locationTypeModel.getProperty(LocationTypeModel.NAME);
    locationType.setName(sp.getText());

    StringSetProperty ssp
        = (StringSetProperty) locationTypeModel.getProperty(LocationTypeModel.ALLOWED_OPERATIONS);
    for (String operation : ssp.getItems()) {
      AllowedOperationTO allowerdOperation = new AllowedOperationTO();
      allowerdOperation.setName(operation);
      locationType.getAllowedOperations().add(allowerdOperation);
    }

    KeyValueSetProperty kvsp
        = (KeyValueSetProperty) locationTypeModel.getProperty(LocationTypeModel.MISCELLANEOUS);
    for (KeyValueProperty kvp : kvsp.getItems()) {
      locationType.getProperties().add(new PropertyTO()
          .setName(kvp.getKey())
          .setValue(kvp.getValue()));
    }

    SymbolProperty symp
        = (SymbolProperty) locationTypeModel.getProperty(ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION);
    if (symp.getLocationRepresentation() != null) {
      locationType.getProperties().add(new PropertyTO()
          .setName(ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION)
          .setValue(symp.getLocationRepresentation().name()));
    }
    else {
      locationType.getProperties().removeIf(property
          -> property.getName().equals(ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION));
    }

    return locationType;
  }

  private LocationTO convertLocation(LocationModel locationModel, List<LinkModel> linkModels) {
    LocationTO location = new LocationTO();

    StringProperty sp = (StringProperty) locationModel.getProperty(LocationModel.NAME);
    String locationName = sp.getText();
    location.setName(locationName);

    CoordinateProperty cp
        = (CoordinateProperty) locationModel.getProperty(LocationModel.MODEL_X_POSITION);
    location.setxPosition((long) cp.getValueByUnit(LengthProperty.Unit.MM));

    cp = (CoordinateProperty) locationModel.getProperty(LocationModel.MODEL_Y_POSITION);
    location.setyPosition((long) cp.getValueByUnit(LengthProperty.Unit.MM));

    LocationTypeProperty ltp = (LocationTypeProperty) locationModel.getProperty(LocationModel.TYPE);
    location.setType(ltp.getValue().toString());

    // Get this location's links
    for (LinkModel linkModel : linkModels) {
      String linkName = ((StringProperty) linkModel.getProperty(LinkModel.NAME)).getText();
      if (!linkName.contains(locationName)) {
        continue;
      }
      LocationTO.Link link = new LocationTO.Link();

      StringProperty linkPoint = (StringProperty) linkModel.getProperty(LinkModel.START_COMPONENT);
      link.setPoint(linkPoint.getText());

      StringSetProperty ssp
          = (StringSetProperty) linkModel.getProperty(LinkModel.ALLOWED_OPERATIONS);
      for (String operation : ssp.getItems()) {
        AllowedOperationTO allowerdOperation = new AllowedOperationTO();
        allowerdOperation.setName(operation);
        link.getAllowedOperations().add(allowerdOperation);
      }
      location.getLinks().add(link);
    }
    Collections.sort(location.getLinks(), Comparators.linksByPointName());

    KeyValueSetProperty kvsp = (KeyValueSetProperty) locationModel.getProperty(LocationModel.MISCELLANEOUS);
    for (KeyValueProperty kvp : kvsp.getItems()) {
      location.getProperties().add(new PropertyTO()
          .setName(kvp.getKey())
          .setValue(kvp.getValue()));
    }

    SymbolProperty symp
        = (SymbolProperty) locationModel.getProperty(ObjectPropConstants.LOC_DEFAULT_REPRESENTATION);
    if (symp.getLocationRepresentation() != null) {
      location.getProperties().add(new PropertyTO()
          .setName(ObjectPropConstants.LOC_DEFAULT_REPRESENTATION)
          .setValue(symp.getLocationRepresentation().name()));
    }
    else {
      location.getProperties().removeIf(property
          -> property.getName().equals(ObjectPropConstants.LOC_DEFAULT_REPRESENTATION));
    }

    return location;
  }

  private BlockTO convertBlock(BlockModel blockModel) {
    BlockTO block = new BlockTO();

    StringProperty sp = (StringProperty) blockModel.getProperty(BlockModel.NAME);
    block.setName(sp.getText());

    StringSetProperty ssp = (StringSetProperty) blockModel.getProperty(BlockModel.ELEMENTS);
    for (String element : ssp.getItems()) {
      MemberTO member = new MemberTO();
      member.setName(element);
      block.getMembers().add(member);
    }
    Collections.sort(block.getMembers(), Comparators.elementsByName());

    KeyValueSetProperty kvsp = (KeyValueSetProperty) blockModel.getProperty(BlockModel.MISCELLANEOUS);
    for (KeyValueProperty kvp : kvsp.getItems()) {
      block.getProperties().add(new PropertyTO()
          .setName(kvp.getKey())
          .setValue(kvp.getValue()));
    }

    return block;
  }

  private StaticRouteTO convertStaticRoute(StaticRouteModel staticRouteModel) {
    StaticRouteTO staticRoute = new StaticRouteTO();

    StringProperty sp = (StringProperty) staticRouteModel.getProperty(StaticRouteModel.NAME);
    staticRoute.setName(sp.getText());

    StringSetProperty ssp
        = (StringSetProperty) staticRouteModel.getProperty(StaticRouteModel.ELEMENTS);
    for (String element : ssp.getItems()) {
      StaticRouteTO.Hop hop = new StaticRouteTO.Hop();
      hop.setName(element);
      staticRoute.getHops().add(hop);
    }
    Collections.sort(staticRoute.getHops(), Comparators.hopsByName());

    KeyValueSetProperty kvsp
        = (KeyValueSetProperty) staticRouteModel.getProperty(StaticRouteModel.MISCELLANEOUS);
    for (KeyValueProperty kvp : kvsp.getItems()) {
      staticRoute.getProperties().add(new PropertyTO()
          .setName(kvp.getKey())
          .setValue(kvp.getValue()));
    }

    return staticRoute;
  }

  private GroupTO convertGroup(GroupModel groupModel) {
    GroupTO group = new GroupTO();

    StringProperty sp = (StringProperty) groupModel.getProperty(GroupModel.NAME);
    group.setName(sp.getText());

    StringSetProperty ssp = (StringSetProperty) groupModel.getProperty(GroupModel.ELEMENTS);
    for (String element : ssp.getItems()) {
      MemberTO member = new MemberTO();
      member.setName(element);
      group.getMembers().add(member);
    }
    Collections.sort(group.getMembers(), Comparators.elementsByName());

    KeyValueSetProperty kvsp
        = (KeyValueSetProperty) groupModel.getProperty(GroupModel.MISCELLANEOUS);
    for (KeyValueProperty kvp : kvsp.getItems()) {
      group.getProperties().add(new PropertyTO()
          .setName(kvp.getKey())
          .setValue(kvp.getValue()));
    }

    return group;
  }

  private VisualLayoutTO convertVisualLayout(LayoutModel layoutModel, SystemModel systemModel) {
    VisualLayoutTO visualLayout = new VisualLayoutTO();

    StringProperty sp = (StringProperty) layoutModel.getProperty(LayoutModel.NAME);
    visualLayout.setName(sp.getText());

    LengthProperty lp = (LengthProperty) layoutModel.getProperty(LayoutModel.SCALE_X);
    visualLayout.setScaleX((float) lp.getValueByUnit(LengthProperty.Unit.MM));

    lp = (LengthProperty) layoutModel.getProperty(LayoutModel.SCALE_Y);
    visualLayout.setScaleY((float) lp.getValueByUnit(LengthProperty.Unit.MM));

    visualLayout.getModelLayoutElements()
        .addAll(extractBlockInformation(systemModel.getBlockModels()));
    visualLayout.getModelLayoutElements()
        .addAll(extractLocationInformation(systemModel.getLocationModels()));
    visualLayout.getModelLayoutElements()
        .addAll(extractPointInformation(systemModel.getPointModels()));
    visualLayout.getModelLayoutElements()
        .addAll(extractPathInformation(systemModel.getPathModels()));
    Collections.sort(visualLayout.getModelLayoutElements(), Comparators.modelLayoutelementsByName());

    KeyValueSetProperty kvsp
        = (KeyValueSetProperty) layoutModel.getProperty(LayoutModel.MISCELLANEOUS);
    for (KeyValueProperty kvp : kvsp.getItems()) {
      visualLayout.getProperties().add(new PropertyTO()
          .setName(kvp.getKey())
          .setValue(kvp.getValue()));
    }

    return visualLayout;
  }

  private List<VisualLayoutTO.ModelLayoutElement> extractPointInformation(List<PointModel> points) {
    List<VisualLayoutTO.ModelLayoutElement> result = new ArrayList<>();
    for (PointModel point : points) {
      VisualLayoutTO.ModelLayoutElement mle = new VisualLayoutTO.ModelLayoutElement();

      StringProperty sp = (StringProperty) point.getProperty(PointModel.NAME);
      mle.setVisualizedObjectName(sp.getText());

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

      StringProperty sp = (StringProperty) path.getProperty(PathModel.NAME);
      mle.setVisualizedObjectName(sp.getText());

      AbstractProperty selp = (AbstractProperty) path.getProperty(ElementPropKeys.PATH_CONN_TYPE);
      PathModel.LinerType pathType = (PathModel.LinerType) selp.getValue();
      PropertyTO property = new PropertyTO();
      property.setName(ElementPropKeys.PATH_CONN_TYPE);
      property.setValue(pathType.name());
      mle.getProperties().add(property);

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

      StringProperty sp = (StringProperty) location.getProperty(PathModel.NAME);
      mle.setVisualizedObjectName(sp.getText());

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

      StringProperty sp = (StringProperty) block.getProperty(PathModel.NAME);
      mle.setVisualizedObjectName(sp.getText());

      ColorProperty cp = (ColorProperty) block.getProperty(ElementPropKeys.BLOCK_COLOR);
      PropertyTO property = new PropertyTO();
      property.setName(ElementPropKeys.BLOCK_COLOR);
      int rgb = cp.getColor().getRGB() & 0x00FFFFFF;  // mask alpha bits
      property.setValue(String.format("#%06X", rgb));
      mle.getProperties().add(property);

      result.add(mle);
    }
    return result;
  }

  private PropertyTO convertStringProperty(AbstractFigureComponent component, String propertyKey) {
    PropertyTO property = new PropertyTO();
    StringProperty sp = (StringProperty) component.getProperty(propertyKey);
    property.setName(propertyKey);
    property.setValue(sp.getText());
    return property;
  }

  public PointModel convertPointTO(PointTO pointTO, VisualLayoutTO visualLayoutTO) {
    PointModel model = new PointModel();

    StringProperty sp = (StringProperty) model.getProperty(PointModel.NAME);
    sp.setText(pointTO.getName());

    CoordinateProperty cp = (CoordinateProperty) model.getProperty(PointModel.MODEL_X_POSITION);
    cp.setValueAndUnit(pointTO.getxPosition(), LengthProperty.Unit.MM);

    cp = (CoordinateProperty) model.getProperty(PointModel.MODEL_Y_POSITION);
    cp.setValueAndUnit(pointTO.getyPosition(), LengthProperty.Unit.MM);

    AngleProperty ap = (AngleProperty) model.getProperty(PointModel.VEHICLE_ORIENTATION_ANGLE);
    ap.setValueAndUnit(pointTO.getVehicleOrientationAngle().doubleValue(), AngleProperty.Unit.DEG);

    AbstractProperty selp = (AbstractProperty) model.getProperty(PointModel.TYPE);
    Point.Type pointType = Point.Type.valueOf(pointTO.getType());
    switch (pointType) {
      case HALT_POSITION:
        selp.setValue(PointModel.PointType.HALT);
        break;
      case PARK_POSITION:
        selp.setValue(PointModel.PointType.PARK);
        break;
      case REPORT_POSITION:
        selp.setValue(PointModel.PointType.REPORT);
        break;
      default:
        throw new IllegalArgumentException("Unknown point type.");
    }

    KeyValueSetProperty kvsp = (KeyValueSetProperty) model.getProperty(PointModel.MISCELLANEOUS);
    for (PropertyTO property : pointTO.getProperties()) {
      kvsp.addItem(new KeyValueProperty(model, property.getName(), property.getValue()));
    }

    // Gather information contained in visual layout
    if (visualLayoutTO != null) {
      sp = (StringProperty) model.getProperty(ElementPropKeys.POINT_POS_X);
      String propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                              pointTO.getName(),
                                                              ElementPropKeys.POINT_POS_X);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        sp.setText(propertyValue);
      }

      sp = (StringProperty) model.getProperty(ElementPropKeys.POINT_POS_Y);
      propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                       pointTO.getName(),
                                                       ElementPropKeys.POINT_POS_Y);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        sp.setText(propertyValue);
      }

      sp = (StringProperty) model.getProperty(ElementPropKeys.POINT_LABEL_OFFSET_X);
      propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                       pointTO.getName(),
                                                       ElementPropKeys.POINT_LABEL_OFFSET_X);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        sp.setText(propertyValue);
      }

      sp = (StringProperty) model.getProperty(ElementPropKeys.POINT_LABEL_OFFSET_Y);
      propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                       pointTO.getName(),
                                                       ElementPropKeys.POINT_LABEL_OFFSET_Y);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        sp.setText(propertyValue);
      }

      sp = (StringProperty) model.getProperty(ElementPropKeys.POINT_LABEL_ORIENTATION_ANGLE);
      propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                       pointTO.getName(),
                                                       ElementPropKeys.POINT_LABEL_ORIENTATION_ANGLE);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        sp.setText(propertyValue);
      }
    }

    return model;
  }

  public PathModel convertPathTO(PathTO pathTO, VisualLayoutTO visualLayoutTO) {
    PathModel model = new PathModel();

    StringProperty sp = (StringProperty) model.getProperty(PathModel.NAME);
    sp.setText(pathTO.getName());

    LengthProperty lp = (LengthProperty) model.getProperty(PathModel.LENGTH);
    lp.setValueAndUnit(pathTO.getLength(), LengthProperty.Unit.MM);

    IntegerProperty ip = (IntegerProperty) model.getProperty(PathModel.ROUTING_COST);
    ip.setValue(pathTO.getRoutingCost().intValue());

    SpeedProperty spp = (SpeedProperty) model.getProperty(PathModel.MAX_VELOCITY);
    spp.setValueAndUnit(pathTO.getMaxVelocity(), SpeedProperty.Unit.MM_S);

    spp = (SpeedProperty) model.getProperty(PathModel.MAX_REVERSE_VELOCITY);
    spp.setValueAndUnit(pathTO.getMaxReverseVelocity(), SpeedProperty.Unit.MM_S);

    sp = (StringProperty) model.getProperty(PathModel.START_COMPONENT);
    sp.setText(pathTO.getSourcePoint());

    sp = (StringProperty) model.getProperty(PathModel.END_COMPONENT);
    sp.setText(pathTO.getDestinationPoint());

    BooleanProperty bp = (BooleanProperty) model.getProperty(PathModel.LOCKED);
    bp.setValue(pathTO.isLocked());

    KeyValueSetProperty kvsp = (KeyValueSetProperty) model.getProperty(PointModel.MISCELLANEOUS);
    for (PropertyTO property : pathTO.getProperties()) {
      kvsp.addItem(new KeyValueProperty(model, property.getName(), property.getValue()));
    }

    // Gather information contained in visual layout
    if (visualLayoutTO != null) {
      AbstractProperty selp = (AbstractProperty) model.getProperty(ElementPropKeys.PATH_CONN_TYPE);
      String propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                              pathTO.getName(),
                                                              ElementPropKeys.PATH_CONN_TYPE);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        selp.setValue(PathModel.LinerType.valueOf(propertyValue));
      }

      sp = (StringProperty) model.getProperty(ElementPropKeys.PATH_CONTROL_POINTS);
      propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                       pathTO.getName(),
                                                       ElementPropKeys.PATH_CONTROL_POINTS);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        sp.setText(propertyValue);
      }
    }
    return model;
  }

  public VehicleModel convertVehicleTO(VehicleTO vehicleTO) {
    VehicleModel model = new VehicleModel();

    StringProperty sp = (StringProperty) model.getProperty(VehicleModel.NAME);
    sp.setText(vehicleTO.getName());

    LengthProperty lp = (LengthProperty) model.getProperty(VehicleModel.LENGTH);
    lp.setValueAndUnit(vehicleTO.getLength(), LengthProperty.Unit.MM);

    PercentProperty pp = (PercentProperty) model.getProperty(VehicleModel.ENERGY_LEVEL_CRITICAL);
    pp.setValueAndUnit(vehicleTO.getEnergyLevelCritical(), PercentProperty.Unit.PERCENT);

    pp = (PercentProperty) model.getProperty(VehicleModel.ENERGY_LEVEL_GOOD);
    pp.setValueAndUnit(vehicleTO.getEnergyLevelGood(), PercentProperty.Unit.PERCENT);

    KeyValueSetProperty kvsp = (KeyValueSetProperty) model.getProperty(VehicleModel.MISCELLANEOUS);
    for (PropertyTO property : vehicleTO.getProperties()) {
      if (Objects.equals(property.getName(), ObjectPropConstants.VEHICLE_INITIAL_POSITION)) {
        CoursePointProperty cpp
            = (CoursePointProperty) model.getProperty(VehicleModel.INITIAL_POSITION);
        cpp.setPointName(property.getValue());
        // Don't add this to the vehicle's properties
        continue;
      }
      
      kvsp.addItem(new KeyValueProperty(model, property.getName(), property.getValue()));
    }

    return model;
  }

  public LocationTypeModel convertLocationTypeTO(LocationTypeTO locationTypeTO) {
    LocationTypeModel model = new LocationTypeModel();

    StringProperty sp = (StringProperty) model.getProperty(LocationTypeModel.NAME);
    sp.setText(locationTypeTO.getName());

    StringSetProperty ssp
        = (StringSetProperty) model.getProperty(LocationTypeModel.ALLOWED_OPERATIONS);
    for (AllowedOperationTO operation : locationTypeTO.getAllowedOperations()) {
      ssp.addItem(operation.getName());
    }

    KeyValueSetProperty kvsp = (KeyValueSetProperty) model.getProperty(LocationTypeModel.MISCELLANEOUS);
    for (PropertyTO property : locationTypeTO.getProperties()) {
      kvsp.addItem(new KeyValueProperty(model, property.getName(), property.getValue()));

      // Set the default location type symbol since its value is not synchronized with the model's
      // properties
      if (Objects.equals(property.getName(), ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION)) {
        SymbolProperty symp
            = (SymbolProperty) model.getProperty(ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION);
        symp.setLocationRepresentation(LocationRepresentation.valueOf(property.getValue()));
      }
    }

    return model;
  }

  public LocationModel convertLocationTO(LocationTO locationTO,
                                         List<LocationTO> locations,
                                         VisualLayoutTO visualLayoutTO) {
    LocationModel model = new LocationModel();

    StringProperty sp = (StringProperty) model.getProperty(LocationModel.NAME);
    sp.setText(locationTO.getName());

    CoordinateProperty cp = (CoordinateProperty) model.getProperty(LocationModel.MODEL_X_POSITION);
    cp.setValueAndUnit(locationTO.getxPosition(), LengthProperty.Unit.MM);

    cp = (CoordinateProperty) model.getProperty(LocationModel.MODEL_Y_POSITION);
    cp.setValueAndUnit(locationTO.getyPosition(), LengthProperty.Unit.MM);

    LocationTypeProperty ltp = (LocationTypeProperty) model.getProperty(LocationModel.TYPE);
    List<String> possibleLocationTypes = new ArrayList<>();
    for (LocationTO location : locations) {
      if (!possibleLocationTypes.contains(location.getType())) {
        possibleLocationTypes.add(location.getType());
      }
    }
    ltp.setPossibleValues(possibleLocationTypes);
    ltp.setValue(locationTO.getType());

    KeyValueSetProperty kvsp = (KeyValueSetProperty) model.getProperty(LocationTypeModel.MISCELLANEOUS);
    for (PropertyTO property : locationTO.getProperties()) {
      kvsp.addItem(new KeyValueProperty(model, property.getName(), property.getValue()));

      // Set the location symbol (overwriting the default location type symbol) since its value is
      // not synchronized with the model's properties
      if (Objects.equals(property.getName(), ObjectPropConstants.LOC_DEFAULT_REPRESENTATION)) {
        SymbolProperty symp
            = (SymbolProperty) model.getProperty(ObjectPropConstants.LOC_DEFAULT_REPRESENTATION);
        symp.setLocationRepresentation(LocationRepresentation.valueOf(property.getValue()));
      }
    }

    // Gather information contained in visual layout
    if (visualLayoutTO != null) {
      sp = (StringProperty) model.getProperty(ElementPropKeys.LOC_POS_X);
      String propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                              locationTO.getName(),
                                                              ElementPropKeys.LOC_POS_X);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        sp.setText(propertyValue);
      }

      sp = (StringProperty) model.getProperty(ElementPropKeys.LOC_POS_Y);
      propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                       locationTO.getName(),
                                                       ElementPropKeys.LOC_POS_Y);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        sp.setText(propertyValue);
      }

      sp = (StringProperty) model.getProperty(ElementPropKeys.LOC_LABEL_OFFSET_X);
      propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                       locationTO.getName(),
                                                       ElementPropKeys.LOC_LABEL_OFFSET_X);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        sp.setText(propertyValue);
      }

      sp = (StringProperty) model.getProperty(ElementPropKeys.LOC_LABEL_OFFSET_Y);
      propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                       locationTO.getName(),
                                                       ElementPropKeys.LOC_LABEL_OFFSET_Y);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        sp.setText(propertyValue);
      }

      sp = (StringProperty) model.getProperty(ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE);
      propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                       locationTO.getName(),
                                                       ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE);
      if (!Strings.isNullOrEmpty(propertyValue)) {
        sp.setText(propertyValue);
      }
    }

    return model;
  }

  public LinkModel convertLinkTO(LocationTO.Link linkTO, LocationTO locationTO) {
    LinkModel model = new LinkModel();

    StringProperty sp = (StringProperty) model.getProperty(LinkModel.NAME);
    sp.setText(String.format("%s --- %s",
                             linkTO.getPoint(),
                             locationTO.getName()));

    StringSetProperty ssp = (StringSetProperty) model.getProperty(LinkModel.ALLOWED_OPERATIONS);
    for (AllowedOperationTO operation : linkTO.getAllowedOperations()) {
      ssp.addItem(operation.getName());
    }

    sp = (StringProperty) model.getProperty(LinkModel.START_COMPONENT);
    sp.setText(linkTO.getPoint());

    sp = (StringProperty) model.getProperty(LinkModel.END_COMPONENT);
    sp.setText(locationTO.getName());

    return model;
  }

  public BlockModel convertBlockTO(BlockTO blockTO, VisualLayoutTO visualLayoutTO) {
    BlockModel model = new BlockModel();

    StringProperty sp = (StringProperty) model.getProperty(BlockModel.NAME);
    sp.setText(blockTO.getName());

    StringSetProperty ssp = (StringSetProperty) model.getProperty(BlockModel.ELEMENTS);
    for (MemberTO member : blockTO.getMembers()) {
      ssp.addItem(member.getName());
    }

    KeyValueSetProperty kvsp = (KeyValueSetProperty) model.getProperty(BlockModel.MISCELLANEOUS);
    for (PropertyTO property : blockTO.getProperties()) {
      kvsp.addItem(new KeyValueProperty(model, property.getName(), property.getValue()));
    }

    // Gather information contained in visual layout
    if (visualLayoutTO != null) {
      ColorProperty cp = (ColorProperty) model.getProperty(ElementPropKeys.BLOCK_COLOR);

      String propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                              blockTO.getName(),
                                                              ElementPropKeys.BLOCK_COLOR);

      if (!Strings.isNullOrEmpty(propertyValue)) {
        cp.setColor(Color.decode(propertyValue));
      }
    }

    return model;
  }

  public StaticRouteModel convertStaticRouteTO(StaticRouteTO staticRouteTO,
                                               VisualLayoutTO visualLayoutTO) {
    StaticRouteModel model = new StaticRouteModel();

    StringProperty sp = (StringProperty) model.getProperty(StaticRouteModel.NAME);
    sp.setText(staticRouteTO.getName());

    StringSetProperty ssp = (StringSetProperty) model.getProperty(StaticRouteModel.ELEMENTS);
    for (StaticRouteTO.Hop hop : staticRouteTO.getHops()) {
      ssp.addItem(hop.getName());
    }

    KeyValueSetProperty kvsp = (KeyValueSetProperty) model.getProperty(StaticRouteModel.MISCELLANEOUS);
    for (PropertyTO property : staticRouteTO.getProperties()) {
      kvsp.addItem(new KeyValueProperty(model, property.getName(), property.getValue()));
    }

    // Gather information contained in visual layout
    if (visualLayoutTO != null) {
      ColorProperty cp = (ColorProperty) model.getProperty(ElementPropKeys.BLOCK_COLOR);

      String propertyValue = getPropertyValueFromVisualLayout(visualLayoutTO,
                                                              staticRouteTO.getName(),
                                                              ElementPropKeys.BLOCK_COLOR);

      if (!Strings.isNullOrEmpty(propertyValue)) {
        cp.setColor(Color.decode(propertyValue));
      }
    }

    return model;
  }

  public GroupModel convertGroupTO(GroupTO groupTO) {
    GroupModel model = new GroupModel();

    StringProperty sp = (StringProperty) model.getProperty(GroupModel.NAME);
    sp.setText(groupTO.getName());

    StringSetProperty ssp = (StringSetProperty) model.getProperty(GroupModel.ELEMENTS);
    for (MemberTO member : groupTO.getMembers()) {
      ssp.addItem(member.getName());
    }

    KeyValueSetProperty kvsp = (KeyValueSetProperty) model.getProperty(GroupModel.MISCELLANEOUS);
    for (PropertyTO property : groupTO.getProperties()) {
      kvsp.addItem(new KeyValueProperty(model, property.getName(), property.getValue()));
    }

    return model;
  }

  public LayoutModel convertVisualLayoutTO(VisualLayoutTO visualLayoutTO) {
    LayoutModel model = new LayoutModel();

    StringProperty sp = (StringProperty) model.getProperty(LayoutModel.NAME);
    sp.setText(visualLayoutTO.getName());

    LengthProperty lp = (LengthProperty) model.getProperty(LayoutModel.SCALE_X);
    lp.setValueAndUnit(visualLayoutTO.getScaleX(), LengthProperty.Unit.MM);

    lp = (LengthProperty) model.getProperty(LayoutModel.SCALE_Y);
    lp.setValueAndUnit(visualLayoutTO.getScaleY(), LengthProperty.Unit.MM);

    KeyValueSetProperty kvsp = (KeyValueSetProperty) model.getProperty(LayoutModel.MISCELLANEOUS);
    for (PropertyTO property : visualLayoutTO.getProperties()) {
      kvsp.addItem(new KeyValueProperty(model, property.getName(), property.getValue()));
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
