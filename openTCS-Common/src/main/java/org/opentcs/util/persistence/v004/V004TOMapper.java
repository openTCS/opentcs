/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence.v004;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.access.to.model.BlockCreationTO;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.access.to.model.PathCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.access.to.peripherals.PeripheralOperationCreationTO;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.visualization.Layer;
import org.opentcs.data.model.visualization.LayerGroup;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.util.Colors;

/**
 * Provides methods for mapping {@link PlantModelCreationTO} to {@link V004PlantModelTO} and
 * vice versa.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class V004TOMapper {

  /**
   * The file format version this mapper works with.
   */
  public static final String VERSION_STRING = "0.0.4";

  /**
   * Maps the given model to a {@link PlantModelCreationTO} instance.
   *
   * @param model The model to map.
   * @return The mapped {@link PlantModelCreationTO} instance.
   */
  public PlantModelCreationTO map(V004PlantModelTO model) {
    PlantModelCreationTO result = new PlantModelCreationTO(model.getName())
        .withPoints(toPointCreationTO(model.getPoints()))
        .withVehicles(toVehicleCreationTO(model.getVehicles()))
        .withPaths(toPathCreationTO(model.getPaths()))
        .withLocationTypes(toLocationTypeCreationTO(model.getLocationTypes()))
        .withLocations(toLocationCreationTO(model.getLocations()))
        .withBlocks(toBlockCreationTO(model.getBlocks()))
        .withVisualLayout(toVisualLayoutCreationTO(model.getVisualLayout()))
        .withProperties(convertProperties(model.getProperties()));

    return result;
  }

  /**
   * Maps the given model to a {@link V004PlantModelTO} instance.
   *
   * @param model The model to map.
   * @return The mapped {@link V004PlantModelTO} instance.
   */
  public V004PlantModelTO map(PlantModelCreationTO model) {
    V004PlantModelTO result = new V004PlantModelTO();

    result.setName(model.getName());
    result.setVersion(VERSION_STRING);
    result.getPoints().addAll(toPointTO(model.getPoints(), model.getPaths()));
    result.getVehicles().addAll(toVehicleTO(model.getVehicles()));
    result.getPaths().addAll(toPathTO(model.getPaths()));
    result.getLocationTypes().addAll(toLocationTypeTO(model.getLocationTypes()));
    result.getLocations().addAll(toLocationTO(model.getLocations()));
    result.getBlocks().addAll(toBlockTO(model.getBlocks()));
    result.setVisualLayout(toVisualLayoutTO(model.getVisualLayout()));
    result.getProperties().addAll(convertProperties(model.getProperties()));

    return result;
  }

  //Methods for mapping from PlantModelElementTO to CreationTO start here.
  private List<PointCreationTO> toPointCreationTO(List<PointTO> points) {
    List<PointCreationTO> result = new ArrayList<>();

    for (PointTO point : points) {
      result.add(
          new PointCreationTO(point.getName())
              .withPosition(new Triple(point.getxPosition(),
                                       point.getyPosition(),
                                       point.getzPosition()))
              .withVehicleOrientationAngle(point.getVehicleOrientationAngle().doubleValue())
              .withType(Point.Type.valueOf(point.getType()))
              .withProperties(convertProperties(point.getProperties()))
              .withLayout(new PointCreationTO.Layout(new Couple(point.getPointLayout().getxPosition(),
                                                                point.getPointLayout().getyPosition()),
                                                     new Couple(point.getPointLayout().getxLabelOffset(),
                                                                point.getPointLayout().getyLabelOffset()),
                                                     point.getPointLayout().getLayerId()))
      );
    }

    return result;
  }

  private List<VehicleCreationTO> toVehicleCreationTO(List<VehicleTO> vehicles) {
    List<VehicleCreationTO> result = new ArrayList<>();

    for (VehicleTO vehicle : vehicles) {
      result.add(
          new VehicleCreationTO(vehicle.getName())
              .withLength(vehicle.getLength().intValue())
              .withEnergyLevelCritical(vehicle.getEnergyLevelCritical().intValue())
              .withEnergyLevelGood(vehicle.getEnergyLevelGood().intValue())
              .withEnergyLevelFullyRecharged(vehicle.getEnergyLevelFullyRecharged().intValue())
              .withEnergyLevelSufficientlyRecharged(vehicle.getEnergyLevelSufficientlyRecharged().intValue())
              .withMaxReverseVelocity(vehicle.getMaxReverseVelocity())
              .withMaxVelocity(vehicle.getMaxVelocity())
              .withProperties(convertProperties(vehicle.getProperties()))
              .withLayout(new VehicleCreationTO.Layout(
                  Colors.decodeFromHexRGB(vehicle.getVehicleLayout().getColor())
              ))
      );
    }

    return result;
  }

  private List<PathCreationTO> toPathCreationTO(List<PathTO> paths) {
    List<PathCreationTO> result = new ArrayList<>();

    for (PathTO path : paths) {
      result.add(
          new PathCreationTO(path.getName(),
                             path.getSourcePoint(),
                             path.getDestinationPoint())
              .withLength(path.getLength())
              .withLocked(path.isLocked())
              .withMaxVelocity(path.getMaxVelocity().intValue())
              .withMaxReverseVelocity(path.getMaxReverseVelocity().intValue())
              .withPeripheralOperations(
                  toPeripheralOperationCreationTOs(path.getPeripheralOperations())
              )
              .withProperties(convertProperties(path.getProperties()))
              .withLayout(new PathCreationTO.Layout(
                  Path.Layout.ConnectionType.valueOf(path.getPathLayout().getConnectionType()),
                  path.getPathLayout().getControlPoints().stream()
                      .map(controlPoint -> new Couple(controlPoint.getX(), controlPoint.getY()))
                      .collect(Collectors.toList()),
                  path.getPathLayout().getLayerId()
              ))
      );
    }

    return result;
  }

  private List<PeripheralOperationCreationTO> toPeripheralOperationCreationTOs(
      List<PeripheralOperationTO> tos) {
    return tos.stream()
        .map(
            to -> new PeripheralOperationCreationTO(to.getName(), to.getLocationName())
                .withExecutionTrigger(
                    PeripheralOperation.ExecutionTrigger.valueOf(to.getExecutionTrigger())
                )
                .withCompletionRequired(to.isCompletionRequired())
        )
        .collect(Collectors.toList());
  }

  private List<LocationTypeCreationTO> toLocationTypeCreationTO(
      List<LocationTypeTO> locationTypes) {
    List<LocationTypeCreationTO> result = new ArrayList<>();

    for (LocationTypeTO locationType : locationTypes) {
      result.add(
          new LocationTypeCreationTO(locationType.getName())
              .withAllowedOperations(getOperationNames(locationType.getAllowedOperations()))
              .withAllowedPeripheralOperations(getPeripheralOperationNames(
                  locationType.getAllowedPeripheralOperations())
              )
              .withProperties(convertProperties(locationType.getProperties()))
              .withLayout(new LocationTypeCreationTO.Layout(
                  LocationRepresentation.valueOf(locationType.getLocationTypeLayout().getLocationRepresentation())
              ))
      );
    }

    return result;
  }

  private List<LocationCreationTO> toLocationCreationTO(List<LocationTO> locations) {
    List<LocationCreationTO> result = new ArrayList<>();

    for (LocationTO location : locations) {
      result.add(
          new LocationCreationTO(location.getName(),
                                 location.getType(),
                                 new Triple(location.getxPosition(),
                                            location.getyPosition(),
                                            location.getzPosition()))
              .withLinks(getLinks(location))
              .withLocked(location.isLocked())
              .withProperties(convertProperties(location.getProperties()))
              .withLayout(new LocationCreationTO.Layout(new Couple(location.getLocationLayout().getxPosition(),
                                                                   location.getLocationLayout().getyPosition()),
                                                        new Couple(location.getLocationLayout().getxLabelOffset(),
                                                                   location.getLocationLayout().getyLabelOffset()),
                                                        LocationRepresentation.valueOf(location.getLocationLayout().getLocationRepresentation()),
                                                        location.getLocationLayout().getLayerId()))
      );
    }

    return result;
  }

  private List<BlockCreationTO> toBlockCreationTO(List<BlockTO> blocks) {
    List<BlockCreationTO> result = new ArrayList<>();

    for (BlockTO block : blocks) {
      result.add(
          new BlockCreationTO(block.getName())
              .withType(Block.Type.valueOf(block.getType()))
              .withMemberNames(block.getMembers().stream()
                  .map(member -> member.getName())
                  .collect(Collectors.toSet()))
              .withProperties(convertProperties(block.getProperties()))
              .withLayout(new BlockCreationTO.Layout(
                  Colors.decodeFromHexRGB(block.getBlockLayout().getColor())
              ))
      );
    }

    return result;
  }

  private VisualLayoutCreationTO toVisualLayoutCreationTO(VisualLayoutTO visualLayout) {
    return new VisualLayoutCreationTO(visualLayout.getName())
        .withScaleX(visualLayout.getScaleX())
        .withScaleY(visualLayout.getScaleY())
        .withLayers(convertLayers(visualLayout.getLayers()))
        .withLayerGroups(convertLayerGroups(visualLayout.getLayerGroups()))
        .withProperties(convertProperties(visualLayout.getProperties()));
  }

  private List<Layer> convertLayers(List<VisualLayoutTO.Layer> layers) {
    List<Layer> result = new ArrayList<>();

    for (VisualLayoutTO.Layer layer : layers) {
      result.add(new Layer(layer.getId(),
                           layer.getOrdinal(),
                           layer.isVisible(),
                           layer.getName(),
                           layer.getGroupId()));
    }

    return result;
  }

  private List<LayerGroup> convertLayerGroups(List<VisualLayoutTO.LayerGroup> layerGroups) {
    List<LayerGroup> result = new ArrayList<>();

    for (VisualLayoutTO.LayerGroup layerGroup : layerGroups) {
      result.add(new LayerGroup(layerGroup.getId(),
                                layerGroup.getName(),
                                layerGroup.isVisible()));
    }

    return result;
  }

  private Map<String, String> convertProperties(List<PropertyTO> propsList) {
    Map<String, String> result = new HashMap<>();
    for (PropertyTO property : propsList) {
      String propName
          = isNullOrEmpty(property.getName()) ? "Property unknown" : property.getName();
      String propValue
          = isNullOrEmpty(property.getValue()) ? "Value unknown" : property.getValue();

      result.put(propName, propValue);
    }

    return result;
  }

  private List<String> getOperationNames(List<AllowedOperationTO> ops) {
    List<String> result = new LinkedList<>();
    for (AllowedOperationTO operation : ops) {
      result.add(operation.getName());
    }
    return result;
  }

  private List<String> getPeripheralOperationNames(List<AllowedPeripheralOperationTO> ops) {
    List<String> result = new LinkedList<>();
    for (AllowedPeripheralOperationTO operation : ops) {
      result.add(operation.getName());
    }
    return result;
  }

  private Map<String, Set<String>> getLinks(LocationTO to) {
    Map<String, Set<String>> result = new HashMap<>();
    for (LocationTO.Link linkTO : to.getLinks()) {
      result.put(linkTO.getPoint(),
                 new HashSet<>(getOperationNames(linkTO.getAllowedOperations())));
    }

    return result;
  }

  //Methods for mapping from CreationTO to PlantModelElementTO start here.
  private List<PointTO> toPointTO(List<PointCreationTO> points, List<PathCreationTO> paths) {
    List<PointTO> result = new ArrayList<>();

    for (PointCreationTO point : points) {
      PointTO pointTO = new PointTO();
      pointTO.setName(point.getName());
      pointTO.setxPosition(point.getPosition().getX())
          .setyPosition(point.getPosition().getY())
          .setVehicleOrientationAngle((float) point.getVehicleOrientationAngle())
          .setType(point.getType().name())
          .setOutgoingPaths(getOutgoingPaths(point, paths))
          .setPointLayout(new PointTO.PointLayout()
              .setxPosition(point.getLayout().getPosition().getX())
              .setyPosition(point.getLayout().getPosition().getY())
              .setxLabelOffset(point.getLayout().getLabelOffset().getX())
              .setyLabelOffset(point.getLayout().getLabelOffset().getY())
              .setLayerId(point.getLayout().getLayerId()))
          .setProperties(convertProperties(point.getProperties()));

      result.add(pointTO);
    }

    Collections.sort(result, Comparators.elementsByName());

    return result;
  }

  private List<VehicleTO> toVehicleTO(List<VehicleCreationTO> vehicles) {
    List<VehicleTO> result = new ArrayList<>();

    for (VehicleCreationTO vehicle : vehicles) {
      VehicleTO vehicleTO = new VehicleTO();
      vehicleTO.setName(vehicle.getName());
      vehicleTO.setLength((long) vehicle.getLength())
          .setMaxVelocity(vehicle.getMaxVelocity())
          .setMaxReverseVelocity(vehicle.getMaxReverseVelocity())
          .setEnergyLevelGood((long) vehicle.getEnergyLevelGood())
          .setEnergyLevelCritical((long) vehicle.getEnergyLevelCritical())
          .setEnergyLevelFullyRecharged((long) vehicle.getEnergyLevelFullyRecharged())
          .setEnergyLevelSufficientlyRecharged((long) vehicle.getEnergyLevelSufficientlyRecharged())
          .setVehicleLayout(new VehicleTO.VehicleLayout()
              .setColor(Colors.encodeToHexRGB(vehicle.getLayout().getRouteColor())))
          .setProperties(convertProperties(vehicle.getProperties()));

      result.add(vehicleTO);
    }

    Collections.sort(result, Comparators.elementsByName());

    return result;
  }

  private List<PathTO> toPathTO(List<PathCreationTO> paths) {
    List<PathTO> result = new ArrayList<>();

    for (PathCreationTO path : paths) {
      PathTO pathTO = new PathTO();
      pathTO.setName(path.getName());
      pathTO.setSourcePoint(path.getSrcPointName())
          .setDestinationPoint(path.getDestPointName())
          .setLength(path.getLength())
          .setMaxVelocity((long) path.getMaxVelocity())
          .setMaxReverseVelocity((long) path.getMaxReverseVelocity())
          .setPeripheralOperations(toPeripheralOperationTOs(path.getPeripheralOperations()))
          .setLocked(path.isLocked())
          .setPathLayout(new PathTO.PathLayout()
              .setConnectionType(path.getLayout().getConnectionType().name())
              .setControlPoints(path.getLayout().getControlPoints().stream()
                  .map(controlPoint -> new PathTO.ControlPoint().setX(controlPoint.getX()).setY(controlPoint.getY()))
                  .collect(Collectors.toList()))
              .setLayerId(path.getLayout().getLayerId()))
          .setProperties(convertProperties(path.getProperties()));

      result.add(pathTO);
    }

    Collections.sort(result, Comparators.elementsByName());

    return result;
  }

  private List<PeripheralOperationTO> toPeripheralOperationTOs(
      List<PeripheralOperationCreationTO> tos) {
    return tos.stream()
        .map(
            to -> (PeripheralOperationTO) new PeripheralOperationTO()
                .setLocationName(to.getLocationName())
                .setExecutionTrigger(to.getExecutionTrigger().name())
                .setCompletionRequired(to.isCompletionRequired())
                .setName(to.getOperation())
        )
        .collect(Collectors.toList());
  }

  private List<LocationTypeTO> toLocationTypeTO(
      List<LocationTypeCreationTO> locationTypes) {
    List<LocationTypeTO> result = new ArrayList<>();

    for (LocationTypeCreationTO locationType : locationTypes) {
      LocationTypeTO locationTypeTO = new LocationTypeTO();
      locationTypeTO.setName(locationType.getName());
      locationTypeTO.setAllowedOperations(toAllowedOperationTOs(locationType.getAllowedOperations()))
          .setAllowedPeripheralOperations(toAllowedPeripheralOperationTOs(locationType.getAllowedPeripheralOperations()))
          .setLocationTypeLayout(new LocationTypeTO.LocationTypeLayout()
              .setLocationRepresentation(locationType.getLayout().getLocationRepresentation().name()))
          .setProperties(convertProperties(locationType.getProperties()));

      result.add(locationTypeTO);
    }

    Collections.sort(result, Comparators.elementsByName());

    return result;
  }

  private List<LocationTO> toLocationTO(List<LocationCreationTO> locations) {
    List<LocationTO> result = new ArrayList<>();

    for (LocationCreationTO location : locations) {
      LocationTO locationTO = new LocationTO();
      locationTO.setName(location.getName());
      locationTO.setxPosition(location.getPosition().getX())
          .setyPosition(location.getPosition().getY())
          .setType(location.getTypeName())
          .setLinks(toLocationTOLinks(location.getLinks()))
          .setLocked(location.isLocked())
          .setLocationLayout(new LocationTO.LocationLayout()
              .setxPosition(location.getLayout().getPosition().getX())
              .setyPosition(location.getLayout().getPosition().getY())
              .setxLabelOffset(location.getLayout().getLabelOffset().getX())
              .setyLabelOffset(location.getLayout().getLabelOffset().getY())
              .setLocationRepresentation(location.getLayout().getLocationRepresentation().name())
              .setLayerId(location.getLayout().getLayerId()))
          .setProperties(convertProperties(location.getProperties()));

      result.add(locationTO);
    }

    Collections.sort(result, Comparators.elementsByName());

    return result;
  }

  private List<BlockTO> toBlockTO(List<BlockCreationTO> blocks) {
    List<BlockTO> result = new ArrayList<>();

    for (BlockCreationTO block : blocks) {
      BlockTO blockTO = new BlockTO();
      blockTO.setName(block.getName());
      blockTO.setType(block.getType().name())
          .setMembers(toMemberTOs(block.getMemberNames()))
          .setBlockLayout(new BlockTO.BlockLayout()
              .setColor(Colors.encodeToHexRGB(block.getLayout().getColor())))
          .setProperties(convertProperties(block.getProperties()));

      result.add(blockTO);
    }

    Collections.sort(result, Comparators.elementsByName());

    return result;
  }

  private VisualLayoutTO toVisualLayoutTO(VisualLayoutCreationTO layout) {
    VisualLayoutTO result = new VisualLayoutTO();

    result.setName(layout.getName())
        .setProperties(convertProperties(layout.getProperties()));
    result.setScaleX((float) layout.getScaleX())
        .setScaleY((float) layout.getScaleY())
        .setLayers(toLayerTOs(layout.getLayers()))
        .setLayerGroups(toLayerGroupTOs(layout.getLayerGroups()));

    return result;
  }

  private List<VisualLayoutTO.Layer> toLayerTOs(List<Layer> layers) {
    List<VisualLayoutTO.Layer> result = new ArrayList<>();

    for (Layer layer : layers) {
      result.add(new VisualLayoutTO.Layer()
          .setId(layer.getId())
          .setOrdinal(layer.getOrdinal())
          .setVisible(layer.isVisible())
          .setName(layer.getName())
          .setGroupId(layer.getGroupId()));
    }

    return result;
  }

  private List<VisualLayoutTO.LayerGroup> toLayerGroupTOs(List<LayerGroup> layerGroups) {
    List<VisualLayoutTO.LayerGroup> result = new ArrayList<>();

    for (LayerGroup layerGroup : layerGroups) {
      result.add(new VisualLayoutTO.LayerGroup()
          .setId(layerGroup.getId())
          .setName(layerGroup.getName())
          .setVisible(layerGroup.isVisible()));
    }

    return result;
  }

  private List<PointTO.OutgoingPath> getOutgoingPaths(PointCreationTO point,
                                                      List<PathCreationTO> paths) {
    List<PointTO.OutgoingPath> result = new ArrayList<>();

    for (PathCreationTO path : paths) {
      if (Objects.equals(path.getSrcPointName(), point.getName())) {
        result.add(new PointTO.OutgoingPath().setName(path.getName()));
      }
    }

    Collections.sort(result, Comparators.outgoingPathsByName());

    return result;
  }

  private List<AllowedOperationTO> toAllowedOperationTOs(Collection<String> allowedOperations) {
    return allowedOperations.stream()
        .sorted()
        .map(allowedOperation -> (AllowedOperationTO) new AllowedOperationTO().setName(allowedOperation))
        .collect(Collectors.toList());
  }

  private List<AllowedPeripheralOperationTO> toAllowedPeripheralOperationTOs(
      Collection<String> allowedOperations) {
    return allowedOperations.stream()
        .sorted()
        .map(allowedOperation -> (AllowedPeripheralOperationTO) new AllowedPeripheralOperationTO().setName(allowedOperation))
        .collect(Collectors.toList());
  }

  private List<LocationTO.Link> toLocationTOLinks(Map<String, Set<String>> links) {
    List<LocationTO.Link> result = new ArrayList<>();

    links.forEach((key, value) -> {
      result.add(
          new LocationTO.Link()
              .setPoint(key)
              .setAllowedOperations(toAllowedOperationTOs(value))
      );
    });

    Collections.sort(result, Comparators.linksByPointName());

    return result;
  }

  private List<MemberTO> toMemberTOs(Collection<String> members) {
    return members.stream()
        .map(member -> (MemberTO) new MemberTO().setName(member))
        .sorted(Comparators.elementsByName())
        .collect(Collectors.toList());
  }

  private List<PropertyTO> convertProperties(Map<String, String> properties) {
    List<PropertyTO> result = new ArrayList<>();

    properties.forEach((key, value) -> {
      result.add(new PropertyTO().setName(key).setValue(value));
    });

    Collections.sort(result, Comparators.propertiesByName());

    return result;
  }

  private boolean isNullOrEmpty(String s) {
    return s == null || s.isEmpty();
  }
}
