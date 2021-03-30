/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence.v002;

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
import org.opentcs.access.to.model.GroupCreationTO;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.access.to.model.ModelLayoutElementCreationTO;
import org.opentcs.access.to.model.PathCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.access.to.model.ShapeLayoutElementCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;

/**
 * Provides methods for mapping {@link PlantModelCreationTO} to {@link V002PlantModelTO} and vice versa.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class V002TOMapper {

  /**
   * The file format version this mapper works with.
   */
  public static final String VERSION_STRING = "0.0.2";

  /**
   * Maps the given model to a {@link PlantModelCreationTO} instance.
   *
   * @param model The model to map.
   * @return The mapped {@link PlantModelCreationTO} instance.
   */
  public PlantModelCreationTO map(V002PlantModelTO model) {
    PlantModelCreationTO result = new PlantModelCreationTO(model.getName())
        .withPoints(toPointCreationTO(model.getPoints()))
        .withVehicles(toVehicleCreationTO(model.getVehicles()))
        .withPaths(toPathCreationTO(model.getPaths()))
        .withLocationTypes(toLocationTypeCreationTO(model.getLocationTypes()))
        .withLocations(toLocationCreationTO(model.getLocations()))
        .withBlocks(toBlockCreationTO(model.getBlocks()))
        .withGroups(toGroupCreationTO(model.getGroups()))
        .withVisualLayouts(toVisualLayoutCreationTO(model.getVisualLayouts()))
        .withProperties(convertProperties(model.getProperties()));

    return result;
  }

  /**
   * Maps the given model to a {@link V002PlantModelTO} instance.
   *
   * @param model The model to map.
   * @return The mapped {@link V002PlantModelTO} instance.
   */
  public V002PlantModelTO map(PlantModelCreationTO model) {
    V002PlantModelTO result = new V002PlantModelTO();

    result.setName(model.getName());
    result.setVersion(VERSION_STRING);
    result.getPoints().addAll(toPointTO(model.getPoints(), model.getPaths()));
    result.getVehicles().addAll(toVehicleTO(model.getVehicles()));
    result.getPaths().addAll(toPathTO(model.getPaths()));
    result.getLocationTypes().addAll(toLocationTypeTO(model.getLocationTypes()));
    result.getLocations().addAll(toLocationTO(model.getLocations()));
    result.getBlocks().addAll(toBlockTO(model.getBlocks()));
    result.getGroups().addAll(toGroupTO(model.getGroups()));
    result.getVisualLayouts().addAll(toVisualLayoutTO(model.getVisualLayouts()));
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
      );
    }

    return result;
  }

  @SuppressWarnings("deprecation")
  private List<PathCreationTO> toPathCreationTO(List<PathTO> paths) {
    List<PathCreationTO> result = new ArrayList<>();

    for (PathTO path : paths) {
      result.add(
          new PathCreationTO(path.getName(),
                             path.getSourcePoint(),
                             path.getDestinationPoint())
              .withLength(path.getLength())
              .withRoutingCost(path.getRoutingCost())
              .withLocked(path.isLocked())
              .withMaxVelocity(path.getMaxVelocity().intValue())
              .withMaxReverseVelocity(path.getMaxReverseVelocity().intValue())
              .withProperties(convertProperties(path.getProperties()))
      );
    }

    return result;
  }

  private List<LocationTypeCreationTO> toLocationTypeCreationTO(
      List<LocationTypeTO> locationTypes) {
    List<LocationTypeCreationTO> result = new ArrayList<>();

    for (LocationTypeTO locationType : locationTypes) {
      result.add(
          new LocationTypeCreationTO(locationType.getName())
              .withAllowedOperations(getOperationNames(locationType.getAllowedOperations()))
              .withProperties(convertProperties(locationType.getProperties()))
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
      );
    }

    return result;
  }

  private List<GroupCreationTO> toGroupCreationTO(List<GroupTO> groups) {
    List<GroupCreationTO> result = new ArrayList<>();

    for (GroupTO group : groups) {
      result.add(
          new GroupCreationTO(group.getName())
              .withMemberNames(group.getMembers().stream()
                  .map(member -> member.getName())
                  .collect(Collectors.toSet()))
              .withProperties(convertProperties(group.getProperties()))
      );
    }

    return result;
  }

  private List<VisualLayoutCreationTO> toVisualLayoutCreationTO(List<VisualLayoutTO> visualLayouts) {
    List<VisualLayoutCreationTO> result = new ArrayList<>();

    for (VisualLayoutTO visualLayout : visualLayouts) {
      result.add(
          new VisualLayoutCreationTO(visualLayout.getName())
              .withScaleX(visualLayout.getScaleX())
              .withScaleY(visualLayout.getScaleY())
              .withShapeElements(getShapeElements(visualLayout.getShapeLayoutElements()))
              .withModelElements(getModelElements(visualLayout.getModelLayoutElements()))
              .withProperties(convertProperties(visualLayout.getProperties()))
      );
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

  private Map<String, Set<String>> getLinks(LocationTO to) {
    Map<String, Set<String>> result = new HashMap<>();
    for (LocationTO.Link linkTO : to.getLinks()) {
      result.put(linkTO.getPoint(),
                 new HashSet<>(getOperationNames(linkTO.getAllowedOperations())));
    }

    return result;
  }

  private List<ModelLayoutElementCreationTO> getModelElements(
      List<VisualLayoutTO.ModelLayoutElement> elements) {
    List<ModelLayoutElementCreationTO> result = new LinkedList<>();
    for (VisualLayoutTO.ModelLayoutElement element : elements) {
      result.add(new ModelLayoutElementCreationTO(element.getVisualizedObjectName())
          .withLayer(element.getLayer().intValue())
          .withProperties(convertProperties(element.getProperties()))
      );
    }

    return result;
  }

  private List<ShapeLayoutElementCreationTO> getShapeElements(
      List<VisualLayoutTO.ShapeLayoutElement> elements) {
    List<ShapeLayoutElementCreationTO> result = new LinkedList<>();
    for (VisualLayoutTO.ShapeLayoutElement element : elements) {
      result.add(new ShapeLayoutElementCreationTO("")
          .withLayer(element.getLayer().intValue())
          .withProperties(convertProperties(element.getProperties())));
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
          .setProperties(convertProperties(vehicle.getProperties()));

      result.add(vehicleTO);
    }

    Collections.sort(result, Comparators.elementsByName());

    return result;
  }

  @SuppressWarnings("deprecation")
  private List<PathTO> toPathTO(List<PathCreationTO> paths) {
    List<PathTO> result = new ArrayList<>();

    for (PathCreationTO path : paths) {
      PathTO pathTO = new PathTO();
      pathTO.setName(path.getName());
      pathTO.setSourcePoint(path.getSrcPointName())
          .setDestinationPoint(path.getDestPointName())
          .setLength(path.getLength())
          .setRoutingCost(path.getRoutingCost())
          .setMaxVelocity((long) path.getMaxVelocity())
          .setMaxReverseVelocity((long) path.getMaxReverseVelocity())
          .setLocked(path.isLocked())
          .setProperties(convertProperties(path.getProperties()));

      result.add(pathTO);
    }

    Collections.sort(result, Comparators.elementsByName());

    return result;
  }

  private List<LocationTypeTO> toLocationTypeTO(
      List<LocationTypeCreationTO> locationTypes) {
    List<LocationTypeTO> result = new ArrayList<>();

    for (LocationTypeCreationTO locationType : locationTypes) {
      LocationTypeTO locationTypeTO = new LocationTypeTO();
      locationTypeTO.setName(locationType.getName());
      locationTypeTO.setAllowedOperations(toAllowedOperationTOs(locationType.getAllowedOperations()))
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
      blockTO.setType(block.getType().name()).
          setMembers(toMemberTOs(block.getMemberNames()))
          .setProperties(convertProperties(block.getProperties()));

      result.add(blockTO);
    }

    Collections.sort(result, Comparators.elementsByName());

    return result;
  }

  private List<GroupTO> toGroupTO(List<GroupCreationTO> groups) {
    List<GroupTO> result = new ArrayList<>();

    for (GroupCreationTO group : groups) {
      GroupTO groupTO = new GroupTO();
      groupTO.setName(group.getName());
      groupTO.setMembers(toMemberTOs(group.getMemberNames()))
          .setProperties(convertProperties(group.getProperties()));

      result.add(groupTO);
    }

    Collections.sort(result, Comparators.elementsByName());

    return result;
  }

  private List<VisualLayoutTO> toVisualLayoutTO(
      List<VisualLayoutCreationTO> layouts) {
    List<VisualLayoutTO> result = new ArrayList<>();

    for (VisualLayoutCreationTO layout : layouts) {
      VisualLayoutTO layoutTO = new VisualLayoutTO();
      layoutTO.setName(layout.getName());
      layoutTO.setScaleX((float) layout.getScaleX())
          .setScaleY((float) layout.getScaleY())
          .setModelLayoutElements(toModelLayoutElements(layout.getModelElements()))
          .setShapeLayoutElements(toShapeLayoutElements(layout.getShapeElements()))
          .setProperties(convertProperties(layout.getProperties()));

      result.add(layoutTO);
    }

    Collections.sort(result, Comparators.elementsByName());

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

  private List<VisualLayoutTO.ModelLayoutElement> toModelLayoutElements(
      List<ModelLayoutElementCreationTO> elements) {
    List<VisualLayoutTO.ModelLayoutElement> result = new ArrayList<>();

    for (ModelLayoutElementCreationTO element : elements) {
      result.add(
          new VisualLayoutTO.ModelLayoutElement()
              .setVisualizedObjectName(element.getName())
              .setLayer((long) element.getLayer())
              .setProperties(convertProperties(element.getProperties()))
      );
    }

    Collections.sort(result, Comparators.modelLayoutelementsByName());

    return result;
  }

  private List<VisualLayoutTO.ShapeLayoutElement> toShapeLayoutElements(
      List<ShapeLayoutElementCreationTO> elements) {
    List<VisualLayoutTO.ShapeLayoutElement> result = new ArrayList<>();

    for (ShapeLayoutElementCreationTO element : elements) {
      result.add(
          new VisualLayoutTO.ShapeLayoutElement()
              .setLayer((long) element.getLayer())
              .setProperties(convertProperties(element.getProperties()))
      );
    }

    return result;
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
