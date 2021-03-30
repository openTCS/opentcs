/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence.v003;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.opentcs.util.persistence.v002.V002ModelParser;
import org.opentcs.util.persistence.v002.V002PlantModelTO;

/**
 * The parser for V003 models.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class V003ModelParser {

  /**
   * The file format version this parser works with.
   */
  public static final String VERSION_STRING = "0.0.3";

  /**
   * Reads a model with the given reader and parses it to a {@link V003PlantModelTO} instance.
   *
   * @param reader The reader to use.
   * @param modelVersion The model version.
   * @return The parsed {@link V003PlantModelTO}.
   * @throws IOException If there was an error reading the model.
   */
  public V003PlantModelTO readRaw(Reader reader, String modelVersion)
      throws IOException {
    if (Objects.equals(modelVersion, VERSION_STRING)) {
      return V003PlantModelTO.fromXml(reader);
    }
    else {
      return convert(new V002ModelParser().readRaw(reader, modelVersion));
    }
  }

  private V003PlantModelTO convert(V002PlantModelTO to) {
    return new V003PlantModelTO()
        .setName(to.getName())
        .setPoints(convertPoints(to))
        .setPaths(convertPaths(to))
        .setVehicles(convertVehicles(to))
        .setLocationTypes(convertLocationTypes(to))
        .setLocations(convertLocations(to))
        .setBlocks(convertBlocks(to))
        .setGroups(convertGroups(to))
        .setVisualLayouts(convertVisualLayouts(to))
        .setProperties(convertProperties(to.getProperties()));
  }

  private List<PointTO> convertPoints(V002PlantModelTO to) {
    return to.getPoints().stream()
        .map(point -> {
          PointTO result = new PointTO();
          result.setName(point.getName())
              .setProperties(convertProperties(point.getProperties()));
          result.setxPosition(point.getxPosition())
              .setyPosition(point.getyPosition())
              .setzPosition(point.getzPosition())
              .setVehicleOrientationAngle(point.getVehicleOrientationAngle())
              .setType(point.getType())
              .setOutgoingPaths(convertOutgoingPaths(point));
          return result;
        })
        .collect(Collectors.toList());
  }

  private List<PropertyTO> convertProperties(
      List<org.opentcs.util.persistence.v002.PropertyTO> tos) {
    return tos.stream()
        .map(property -> new PropertyTO().setName(property.getName()).setValue(property.getValue()))
        .collect(Collectors.toList());
  }

  private List<PointTO.OutgoingPath> convertOutgoingPaths(
      org.opentcs.util.persistence.v002.PointTO to) {
    return to.getOutgoingPaths().stream()
        .map(path -> new PointTO.OutgoingPath().setName(path.getName()))
        .collect(Collectors.toList());
  }

  private List<PathTO> convertPaths(V002PlantModelTO to) {
    return to.getPaths().stream()
        .map(path -> {
          PathTO result = new PathTO();
          result.setName(path.getName())
              .setProperties(convertProperties(path.getProperties()));
          result.setSourcePoint(path.getSourcePoint())
              .setDestinationPoint(path.getDestinationPoint())
              .setLength(path.getLength())
              .setMaxVelocity(path.getMaxVelocity())
              .setMaxReverseVelocity(path.getMaxReverseVelocity())
              .setLocked(path.isLocked());
          return result;
        })
        .collect(Collectors.toList());
  }

  private List<VehicleTO> convertVehicles(V002PlantModelTO to) {
    return to.getVehicles().stream()
        .map(vehicle -> {
          VehicleTO result = new VehicleTO();
          result.setName(vehicle.getName())
              .setProperties(convertProperties(vehicle.getProperties()));
          result.setLength(vehicle.getLength())
              .setEnergyLevelCritical(vehicle.getEnergyLevelCritical())
              .setEnergyLevelGood(vehicle.getEnergyLevelGood())
              .setEnergyLevelFullyRecharged(vehicle.getEnergyLevelFullyRecharged())
              .setEnergyLevelSufficientlyRecharged(vehicle.getEnergyLevelSufficientlyRecharged())
              .setMaxVelocity(vehicle.getMaxVelocity())
              .setMaxReverseVelocity(vehicle.getMaxReverseVelocity());
          return result;
        })
        .collect(Collectors.toList());
  }

  private List<LocationTypeTO> convertLocationTypes(V002PlantModelTO to) {
    return to.getLocationTypes().stream()
        .map(locationType -> {
          LocationTypeTO result = new LocationTypeTO();
          result.setName(locationType.getName())
              .setProperties(convertProperties(locationType.getProperties()));
          result.setLocationNamePrefix(locationType.getLocationNamePrefix())
              .setAllowedOperations(convertAllowedOperations(locationType.getAllowedOperations()));
          return result;
        })
        .collect(Collectors.toList());
  }

  private List<AllowedOperationTO> convertAllowedOperations(
      List<org.opentcs.util.persistence.v002.AllowedOperationTO> tos) {
    return tos.stream()
        .map(allowedOperation -> {
          AllowedOperationTO result = new AllowedOperationTO();
          result.setName(allowedOperation.getName());
          result.setProperties(convertProperties(allowedOperation.getProperties()));
          return result;
        })
        .collect(Collectors.toList());
  }

  private List<LocationTO> convertLocations(V002PlantModelTO to) {
    return to.getLocations().stream()
        .map(location -> {
          LocationTO result = new LocationTO();
          result.setName(location.getName())
              .setProperties(convertProperties(location.getProperties()));
          result.setxPosition(location.getxPosition())
              .setyPosition(location.getyPosition())
              .setzPosition(location.getzPosition())
              .setType(location.getType())
              .setLinks(convertLinks(location))
              .setLocked(location.isLocked());
          return result;
        })
        .collect(Collectors.toList());
  }

  private List<LocationTO.Link> convertLinks(org.opentcs.util.persistence.v002.LocationTO to) {
    return to.getLinks().stream()
        .map(link -> {
          return new LocationTO.Link()
              .setPoint(link.getPoint())
              .setAllowedOperations(convertAllowedOperations(link.getAllowedOperations()));
        })
        .collect(Collectors.toList());
  }

  private List<BlockTO> convertBlocks(V002PlantModelTO to) {
    return to.getBlocks().stream()
        .map(block -> {
          BlockTO result = new BlockTO();
          result.setName(block.getName())
              .setProperties(convertProperties(block.getProperties()));
          result.setType(block.getType())
              .setMembers(convertMembers(block.getMembers()));
          return result;
        })
        .collect(Collectors.toList());
  }

  private List<MemberTO> convertMembers(List<org.opentcs.util.persistence.v002.MemberTO> tos) {
    return tos.stream()
        .map(member -> {
          MemberTO result = new MemberTO();
          result.setName(member.getName())
              .setProperties(convertProperties(member.getProperties()));
          return result;
        })
        .collect(Collectors.toList());
  }

  private List<GroupTO> convertGroups(V002PlantModelTO to) {
    return to.getGroups().stream()
        .map(group -> {
          GroupTO result = new GroupTO();
          result.setName(group.getName())
              .setProperties(convertProperties(group.getProperties()));
          result.setMembers(convertMembers(group.getMembers()));
          return result;
        })
        .collect(Collectors.toList());
  }

  private List<VisualLayoutTO> convertVisualLayouts(V002PlantModelTO to) {
    return to.getVisualLayouts().stream()
        .map(visualLayout -> {
          VisualLayoutTO result = new VisualLayoutTO();
          result.setName(visualLayout.getName())
              .setProperties(convertProperties(visualLayout.getProperties()));
          result.setScaleX(visualLayout.getScaleX())
              .setScaleY(visualLayout.getScaleY())
              .setShapeLayoutElements(convertShapeLayoutElements(visualLayout))
              .setModelLayoutElements(convertModelLayoutElements(visualLayout));
          return result;
        })
        .collect(Collectors.toList());
  }

  private List<VisualLayoutTO.ShapeLayoutElement> convertShapeLayoutElements(
      org.opentcs.util.persistence.v002.VisualLayoutTO to) {
    return to.getShapeLayoutElements().stream()
        .map(shapeLayoutElement -> {
          return new VisualLayoutTO.ShapeLayoutElement()
              .setLayer(shapeLayoutElement.getLayer())
              .setProperties(convertProperties(shapeLayoutElement.getProperties()));
        })
        .collect(Collectors.toList());
  }

  private List<VisualLayoutTO.ModelLayoutElement> convertModelLayoutElements(
      org.opentcs.util.persistence.v002.VisualLayoutTO to) {
    return to.getModelLayoutElements().stream()
        .map(modelLayoutElement -> {
          return new VisualLayoutTO.ModelLayoutElement()
              .setVisualizedObjectName(modelLayoutElement.getVisualizedObjectName())
              .setLayer(modelLayoutElement.getLayer())
              .setProperties(convertProperties(modelLayoutElement.getProperties()));
        })
        .collect(Collectors.toList());
  }
}
