/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence.v004;

import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.opentcs.access.to.model.PlantModelCreationTO;
import static org.opentcs.data.ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION;
import static org.opentcs.data.ObjectPropConstants.LOC_DEFAULT_REPRESENTATION;
import org.opentcs.data.model.ModelConstants;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.util.Colors;
import org.opentcs.util.persistence.v003.V003ModelParser;
import org.opentcs.util.persistence.v003.V003PlantModelTO;
import org.opentcs.util.persistence.v003.VisualLayoutTO.ModelLayoutElement;

/**
 * The parser for V004 models.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class V004ModelParser {

  /**
   * Reads a model with the given reader and parses it to a {@link PlantModelCreationTO} instance.
   *
   * @param reader The reader to use.
   * @param modelVersion The model version.
   * @return The parsed {@link PlantModelCreationTO}.
   * @throws IOException If there was an error reading the model.
   */
  public PlantModelCreationTO read(Reader reader, String modelVersion)
      throws IOException {
    return new V004TOMapper().map(readRaw(reader, modelVersion));
  }

  /**
   * Reads a model with the given reader and parses it to a {@link V004PlantModelTO} instance.
   *
   * @param reader The reader to use.
   * @param modelVersion The model version.
   * @return The parsed {@link V004PlantModelTO}.
   * @throws IOException If there was an error reading the model.
   */
  public V004PlantModelTO readRaw(Reader reader, String modelVersion)
      throws IOException {
    if (Objects.equals(modelVersion, V004TOMapper.VERSION_STRING)) {
      return V004PlantModelTO.fromXml(reader);
    }
    else {
      return convert(new V003ModelParser().readRaw(reader, modelVersion));
    }
  }

  private V004PlantModelTO convert(V003PlantModelTO to) {
    Map<String, ModelLayoutElement> modelLayoutElementMap = getModelLayoutElementMap(to);

    V004PlantModelTO result = new V004PlantModelTO()
        .setName(to.getName())
        .setPoints(convertPoints(to, modelLayoutElementMap))
        .setPaths(convertPaths(to, modelLayoutElementMap))
        .setVehicles(convertVehicles(to, modelLayoutElementMap))
        .setLocationTypes(convertLocationTypes(to))
        .setLocations(convertLocations(to, modelLayoutElementMap))
        .setBlocks(convertBlocks(to, modelLayoutElementMap))
        .setVisualLayout(convertVisualLayout(to))
        .setProperties(convertProperties(to.getProperties()));

    return assignElementsToDefaultLayer(result);
  }

  private Map<String, ModelLayoutElement> getModelLayoutElementMap(
      V003PlantModelTO to) {
    Map<String, ModelLayoutElement> result = new HashMap<>();
    for (ModelLayoutElement mle : to.getVisualLayouts().get(0).getModelLayoutElements()) {
      result.put(mle.getVisualizedObjectName(), mle);
    }
    return result;
  }

  private List<PropertyTO> convertProperties(
      List<org.opentcs.util.persistence.v003.PropertyTO> tos) {
    return tos.stream()
        .map(property -> new PropertyTO().setName(property.getName()).setValue(property.getValue()))
        .collect(Collectors.toList());
  }

  private V004PlantModelTO assignElementsToDefaultLayer(V004PlantModelTO model) {
    List<PointTO> pointsAssignedToDefaultLayer = new ArrayList<>();
    for (PointTO point : model.getPoints()) {
      pointsAssignedToDefaultLayer.add(
          point.setPointLayout(point.getPointLayout().setLayerId(ModelConstants.DEFAULT_LAYER_ID))
      );
    }

    List<PathTO> pathsAssignedToDefaultLayer = new ArrayList<>();
    for (PathTO path : model.getPaths()) {
      pathsAssignedToDefaultLayer.add(
          path.setPathLayout(path.getPathLayout().setLayerId(ModelConstants.DEFAULT_LAYER_ID))
      );
    }

    List<LocationTO> locationsAssignedToDefaultLayer = new ArrayList<>();
    for (LocationTO path : model.getLocations()) {
      locationsAssignedToDefaultLayer.add(
          path.setLocationLayout(path.getLocationLayout().setLayerId(ModelConstants.DEFAULT_LAYER_ID))
      );
    }

    return model.setPoints(pointsAssignedToDefaultLayer)
        .setPaths(pathsAssignedToDefaultLayer)
        .setLocations(locationsAssignedToDefaultLayer);
  }

  private List<PointTO> convertPoints(V003PlantModelTO to,
                                      Map<String, ModelLayoutElement> modelLayoutElementMap) {
    return to.getPoints().stream()
        .map(point -> {
          Map<String, String> layoutProperties
              = toPropertiesMap(modelLayoutElementMap.get(point.getName()).getProperties());
          long positionX = layoutProperties.get(ElementPropKeys.POINT_POS_X) != null
              ? Integer.parseInt(layoutProperties.get(ElementPropKeys.POINT_POS_X))
              : 0;
          long positionY = layoutProperties.get(ElementPropKeys.POINT_POS_Y) != null
              ? Integer.parseInt(layoutProperties.get(ElementPropKeys.POINT_POS_Y))
              : 0;
          long labelOffsetX = layoutProperties.get(ElementPropKeys.POINT_LABEL_OFFSET_X) != null
              ? Integer.parseInt(layoutProperties.get(ElementPropKeys.POINT_LABEL_OFFSET_X))
              : 0;
          long labelOffsetY = layoutProperties.get(ElementPropKeys.POINT_LABEL_OFFSET_Y) != null
              ? Integer.parseInt(layoutProperties.get(ElementPropKeys.POINT_LABEL_OFFSET_Y))
              : 0;

          PointTO result = new PointTO();
          result.setName(point.getName())
              .setProperties(convertProperties(point.getProperties()));
          result.setxPosition(point.getxPosition())
              .setyPosition(point.getyPosition())
              .setzPosition(point.getzPosition())
              .setVehicleOrientationAngle(point.getVehicleOrientationAngle())
              .setType(point.getType())
              .setOutgoingPaths(convertOutgoingPaths(point))
              .setPointLayout(new PointTO.PointLayout()
                  .setxPosition(positionX)
                  .setyPosition(positionY)
                  .setxLabelOffset(labelOffsetX)
                  .setyLabelOffset(labelOffsetY));
          return result;
        })
        .collect(Collectors.toList());
  }

  private Map<String, String> toPropertiesMap(
      List<org.opentcs.util.persistence.v003.PropertyTO> properties) {
    Map<String, String> result = new HashMap<>();
    for (org.opentcs.util.persistence.v003.PropertyTO property : properties) {
      result.put(property.getName(), property.getValue());
    }
    return result;
  }

  private List<PointTO.OutgoingPath> convertOutgoingPaths(
      org.opentcs.util.persistence.v003.PointTO to) {
    return to.getOutgoingPaths().stream()
        .map(path -> new PointTO.OutgoingPath().setName(path.getName()))
        .collect(Collectors.toList());
  }

  private List<PathTO> convertPaths(V003PlantModelTO to,
                                    Map<String, ModelLayoutElement> modelLayoutElementMap) {
    return to.getPaths().stream()
        .map(path -> {
          Map<String, String> layoutProperties
              = toPropertiesMap(modelLayoutElementMap.get(path.getName()).getProperties());
          String connectionType = layoutProperties.getOrDefault(ElementPropKeys.PATH_CONN_TYPE,
                                                                "DIRECT");

          List<PathTO.ControlPoint> controlPoints = new ArrayList<>();
          String controlPointsString = layoutProperties.get(ElementPropKeys.PATH_CONTROL_POINTS);
          if (controlPointsString != null) {
            controlPoints = Arrays.asList(controlPointsString.split(";")).stream()
                .map(controlPointString -> {
                  String[] coordinateStrings = controlPointString.split(",");
                  return new PathTO.ControlPoint()
                      .setX(Long.parseLong(coordinateStrings[0]))
                      .setY(Long.parseLong(coordinateStrings[1]));
                })
                .collect(Collectors.toList());
          }

          PathTO result = new PathTO();
          result.setName(path.getName())
              .setProperties(convertProperties(path.getProperties()));
          result.setSourcePoint(path.getSourcePoint())
              .setDestinationPoint(path.getDestinationPoint())
              .setLength(path.getLength())
              .setMaxVelocity(path.getMaxVelocity())
              .setMaxReverseVelocity(path.getMaxReverseVelocity())
              .setLocked(path.isLocked())
              .setPathLayout(new PathTO.PathLayout()
                  .setConnectionType(connectionType)
                  .setControlPoints(controlPoints));
          return result;
        })
        .collect(Collectors.toList());
  }

  private List<VehicleTO> convertVehicles(V003PlantModelTO to,
                                          Map<String, ModelLayoutElement> modelLayoutElementMap) {
    return to.getVehicles().stream()
        .map(vehicle -> {
          Map<String, String> layoutProperties
              = toPropertiesMap(modelLayoutElementMap.get(vehicle.getName()).getProperties());
          String color = layoutProperties.get(ElementPropKeys.VEHICLE_ROUTE_COLOR) != null
              ? layoutProperties.get(ElementPropKeys.VEHICLE_ROUTE_COLOR)
              : Colors.encodeToHexRGB(Color.RED);

          VehicleTO result = new VehicleTO();
          result.setName(vehicle.getName())
              .setProperties(convertProperties(vehicle.getProperties()));
          result.setLength(vehicle.getLength())
              .setEnergyLevelCritical(vehicle.getEnergyLevelCritical())
              .setEnergyLevelGood(vehicle.getEnergyLevelGood())
              .setEnergyLevelFullyRecharged(vehicle.getEnergyLevelFullyRecharged())
              .setEnergyLevelSufficientlyRecharged(vehicle.getEnergyLevelSufficientlyRecharged())
              .setMaxVelocity(vehicle.getMaxVelocity())
              .setMaxReverseVelocity(vehicle.getMaxReverseVelocity())
              .setVehicleLayout(new VehicleTO.VehicleLayout()
                  .setColor(color));
          return result;
        })
        .collect(Collectors.toList());
  }

  private List<LocationTypeTO> convertLocationTypes(V003PlantModelTO to) {
    return to.getLocationTypes().stream()
        .map(locationType -> {
          String locationRepresentation = toPropertiesMap(locationType.getProperties())
              .getOrDefault(LOCTYPE_DEFAULT_REPRESENTATION, LocationRepresentation.NONE.name());

          LocationTypeTO result = new LocationTypeTO();
          result.setName(locationType.getName())
              .setProperties(convertProperties(locationType.getProperties()));
          result.setLocationNamePrefix(locationType.getLocationNamePrefix())
              .setAllowedOperations(convertAllowedOperations(locationType.getAllowedOperations()))
              .setLocationTypeLayout(new LocationTypeTO.LocationTypeLayout()
                  .setLocationRepresentation(locationRepresentation));
          return result;
        })
        .collect(Collectors.toList());
  }

  private List<AllowedOperationTO> convertAllowedOperations(
      List<org.opentcs.util.persistence.v003.AllowedOperationTO> tos) {
    return tos.stream()
        .map(allowedOperation -> {
          AllowedOperationTO result = new AllowedOperationTO();
          result.setName(allowedOperation.getName());
          result.setProperties(convertProperties(allowedOperation.getProperties()));
          return result;
        })
        .collect(Collectors.toList());
  }

  private List<LocationTO> convertLocations(V003PlantModelTO to,
                                            Map<String, ModelLayoutElement> modelLayoutElementMap) {
    return to.getLocations().stream()
        .map(location -> {
          Map<String, String> layoutProperties
              = toPropertiesMap(modelLayoutElementMap.get(location.getName()).getProperties());
          long positionX = layoutProperties.get(ElementPropKeys.LOC_POS_X) != null
              ? Integer.parseInt(layoutProperties.get(ElementPropKeys.LOC_POS_X))
              : 0;
          long positionY = layoutProperties.get(ElementPropKeys.LOC_POS_Y) != null
              ? Integer.parseInt(layoutProperties.get(ElementPropKeys.LOC_POS_Y))
              : 0;
          long labelOffsetX = layoutProperties.get(ElementPropKeys.LOC_LABEL_OFFSET_X) != null
              ? Integer.parseInt(layoutProperties.get(ElementPropKeys.LOC_LABEL_OFFSET_X))
              : 0;
          long labelOffsetY = layoutProperties.get(ElementPropKeys.LOC_LABEL_OFFSET_Y) != null
              ? Integer.parseInt(layoutProperties.get(ElementPropKeys.LOC_LABEL_OFFSET_Y))
              : 0;
          String locationRepresentation = toPropertiesMap(location.getProperties())
              .getOrDefault(LOC_DEFAULT_REPRESENTATION, LocationRepresentation.DEFAULT.name());

          LocationTO result = new LocationTO();
          result.setName(location.getName())
              .setProperties(convertProperties(location.getProperties()));
          result.setxPosition(location.getxPosition())
              .setyPosition(location.getyPosition())
              .setzPosition(location.getzPosition())
              .setType(location.getType())
              .setLinks(convertLinks(location))
              .setLocked(location.isLocked())
              .setLocationLayout(new LocationTO.LocationLayout()
                  .setxPosition(positionX)
                  .setyPosition(positionY)
                  .setxLabelOffset(labelOffsetX)
                  .setyLabelOffset(labelOffsetY)
                  .setLocationRepresentation(locationRepresentation));
          return result;
        })
        .collect(Collectors.toList());
  }

  private List<LocationTO.Link> convertLinks(org.opentcs.util.persistence.v003.LocationTO to) {
    return to.getLinks().stream()
        .map(link -> {
          return new LocationTO.Link()
              .setPoint(link.getPoint())
              .setAllowedOperations(convertAllowedOperations(link.getAllowedOperations()));
        })
        .collect(Collectors.toList());
  }

  private List<BlockTO> convertBlocks(V003PlantModelTO to,
                                      Map<String, ModelLayoutElement> modelLayoutElementMap) {
    return to.getBlocks().stream()
        .map(block -> {
          Map<String, String> layoutProperties
              = toPropertiesMap(modelLayoutElementMap.get(block.getName()).getProperties());
          String color = layoutProperties.get(ElementPropKeys.BLOCK_COLOR) != null
              ? layoutProperties.get(ElementPropKeys.BLOCK_COLOR)
              : Colors.encodeToHexRGB(Color.RED);

          BlockTO result = new BlockTO();
          result.setName(block.getName())
              .setProperties(convertProperties(block.getProperties()));
          result.setType(block.getType())
              .setMembers(convertMembers(block.getMembers()))
              .setBlockLayout(new BlockTO.BlockLayout()
                  .setColor(color));
          return result;
        })
        .collect(Collectors.toList());
  }

  private List<MemberTO> convertMembers(List<org.opentcs.util.persistence.v003.MemberTO> tos) {
    return tos.stream()
        .map(member -> {
          MemberTO result = new MemberTO();
          result.setName(member.getName())
              .setProperties(convertProperties(member.getProperties()));
          return result;
        })
        .collect(Collectors.toList());
  }

  private VisualLayoutTO convertVisualLayout(V003PlantModelTO to) {
    VisualLayoutTO result = new VisualLayoutTO();
    result.setName(to.getVisualLayouts().get(0).getName())
        .setProperties(convertProperties(to.getVisualLayouts().get(0).getProperties()));
    result.setScaleX(to.getVisualLayouts().get(0).getScaleX())
        .setScaleY(to.getVisualLayouts().get(0).getScaleY())
        .setLayers(createDefaultLayer())
        .setLayerGroups(createDefaultLayerGroup());
    return result;
  }

  private List<VisualLayoutTO.Layer> createDefaultLayer() {
    return Arrays.asList(
        new VisualLayoutTO.Layer()
            .setId(ModelConstants.DEFAULT_LAYER_ID)
            .setOrdinal(ModelConstants.DEFAULT_LAYER_ORDINAL)
            .setVisible(Boolean.TRUE)
            .setName(ModelConstants.DEFAULT_LAYER_NAME)
            .setGroupId(ModelConstants.DEFAULT_LAYER_GROUP_ID)
    );
  }

  private List<VisualLayoutTO.LayerGroup> createDefaultLayerGroup() {
    return Arrays.asList(
        new VisualLayoutTO.LayerGroup()
            .setId(ModelConstants.DEFAULT_LAYER_GROUP_ID)
            .setVisible(Boolean.TRUE)
            .setName(ModelConstants.DEFAULT_LAYER_GROUP_NAME)
    );
  }
}
