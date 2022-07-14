/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.drawing.figures;

import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.base.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.base.model.ModelComponent;
import static org.opentcs.guing.base.model.ModelComponent.MISCELLANEOUS;
import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.base.model.elements.LinkModel;
import org.opentcs.guing.base.model.elements.LocationModel;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.persistence.ModelManager;

/**
 * Generates tooltip texts for various model elements.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class ToolTipTextGenerator {

  /**
   * The model manager.
   */
  private final ModelManager modelManager;

  @Inject
  public ToolTipTextGenerator(ModelManager modelManager) {
    this.modelManager = requireNonNull(modelManager, "modelManager");
  }

  public String getToolTipText(PointModel model) {
    String pointDesc = model.getDescription();
    StringBuilder sb = new StringBuilder("<html>");
    sb.append(pointDesc).append(" ").append("<b>").append(model.getName()).append("</b>");

    appendBlockInfo(sb, model);
    appendMiscProps(sb, model);

    sb.append("</html>");

    return sb.toString();
  }

  public String getToolTipText(LocationModel model) {
    String locationDesc = model.getDescription();
    StringBuilder sb = new StringBuilder("<html>");
    sb.append(locationDesc).append(" ").append("<b>").append(model.getName()).append("</b>");

    appendBlockInfo(sb, model);
    appendPeripheralInformation(sb, model);
    appendMiscProps(sb, model);

    sb.append("</html>");

    return sb.toString();
  }

  public String getToolTipText(PathModel model) {
    String pathDesc = model.getDescription();
    StringBuilder sb = new StringBuilder("<html>");
    sb.append(pathDesc).append(" ").append("<b>").append(model.getName()).append("</b>");

    appendBlockInfo(sb, model);
    appendMiscProps(sb, model);

    sb.append("</html>");

    return sb.toString();
  }

  public String getToolTipText(VehicleModel model) {
    String vehicleDesc = model.getDescription();
    StringBuilder sb = new StringBuilder("<html>");

    sb.append(vehicleDesc).append(" ").append("<b>").append(model.getName()).append("</b>");

    appendVehicleState(sb, model);
    appendMiscProps(sb, model);

    sb.append("</html>");

    return sb.toString();
  }

  public String getToolTipText(LinkModel model) {
    return new StringBuilder("<html>")
        .append(model.getDescription()).append(" ")
        .append("<b>").append(model.getName()).append("</b>")
        .append("</html>").toString();
  }

  private String energyColorString(Vehicle vehicle) {
    if (vehicle.isEnergyLevelCritical()) {
      return "red";
    }
    else if (vehicle.isEnergyLevelDegraded()) {
      return "orange";
    }
    else if (vehicle.isEnergyLevelGood()) {
      return "green";
    }
    else {
      return "black";
    }
  }

  private String stateColorString(Vehicle vehicle) {
    switch (vehicle.getState()) {
      case ERROR:
        return "red";
      case UNAVAILABLE:
      case UNKNOWN:
        return "orange";
      default:
        return "black";
    }
  }

  private void appendBlockInfo(StringBuilder sb, ModelComponent component) {
    sb.append(blocksToToolTipContent(getBlocksWith(component)));
  }

  private void appendBlockInfo(StringBuilder sb, LocationModel location) {
    List<LinkModel> links = modelManager.getModel().getLinkModels();
    links = links.stream()
        .filter(link -> link.getLocation().getName().equals(location.getName()))
        .collect(Collectors.toList());

    List<BlockModel> partOfBlocks = new ArrayList<>();
    for (LinkModel link : links) {
      partOfBlocks.addAll(getBlocksWith(link.getPoint()));
    }

    sb.append(blocksToToolTipContent(partOfBlocks));
  }

  private void appendMiscProps(StringBuilder sb, ModelComponent component) {
    KeyValueSetProperty miscProps = (KeyValueSetProperty) component.getProperty(MISCELLANEOUS);

    if (miscProps.getItems().isEmpty()) {
      return;
    }

    sb.append("<hr>");
    sb.append(miscProps.getDescription()).append(": ");
    sb.append("<ul>");
    miscProps.getItems().stream()
        .sorted((o1, o2) -> o1.getKey().compareTo(o2.getKey()))
        .forEach(kvp -> {
          sb.append("<li>")
              .append(kvp.getKey()).append(": ").append(kvp.getValue())
              .append("</li>");
        });
    sb.append("</ul>");
  }

  private void appendVehicleState(StringBuilder sb, VehicleModel model) {
    sb.append("<hr>").append(model.getPropertyPoint().getDescription()).append(": ")
        .append(model.getPoint() != null ? model.getPoint().getName() : "?");
    sb.append("<br>").append(model.getPropertyNextPoint().getDescription()).append(": ")
        .append(model.getNextPoint() != null ? model.getNextPoint().getName() : "?");

    sb.append("<br>").append(model.getPropertyState().getDescription())
        .append(": <font color=").append(stateColorString(model.getVehicle())).append(">")
        .append(model.getPropertyState().getValue())
        .append("</font>");

    sb.append("<br>").append(model.getPropertyProcState().getDescription()).append(": ")
        .append(model.getPropertyProcState().getValue());
    sb.append("<br>").append(model.getPropertyIntegrationLevel().getDescription()).append(": ")
        .append(model.getPropertyIntegrationLevel().getValue());

    sb.append("<br>").append(model.getPropertyEnergyLevel().getDescription())
        .append(": <font color=").append(energyColorString(model.getVehicle())).append(">")
        .append(model.getPropertyEnergyLevel().getValue())
        .append("%</font>");
  }

  private void appendPeripheralInformation(StringBuilder sb, LocationModel model) {
    sb.append("<hr>");
    sb.append("<br>").append(model.getPropertyPeripheralReservationToken().getDescription())
        .append(": ")
        .append(model.getPropertyPeripheralReservationToken().getText());
    sb.append("<br>").append(model.getPropertyPeripheralState().getDescription())
        .append(": ")
        .append(model.getPropertyPeripheralState().getText());
    sb.append("<br>").append(model.getPropertyPeripheralProcState().getDescription())
        .append(": ")
        .append(model.getPropertyPeripheralProcState().getText());
    sb.append("<br>").append(model.getPropertyPeripheralJob().getDescription())
        .append(": ")
        .append(model.getPropertyPeripheralJob().getText());
  }

  private List<BlockModel> getBlocksWith(ModelComponent component) {
    List<BlockModel> result = new ArrayList<>();
    List<BlockModel> blocks = modelManager.getModel().getBlockModels();
    for (BlockModel block : blocks) {
      if (block.contains(component)) {
        result.add(block);
      }
    }
    return result;
  }

  private String blocksToToolTipContent(List<BlockModel> blocks) {
    if (blocks.isEmpty()) {
      return "";
    }

    blocks.sort((b1, b2) -> b1.getName().compareTo(b2.getName()));

    String desc = blocks.get(0).getDescription();
    StringBuilder sb = new StringBuilder("<hr>")
        .append(desc).append(": ");
    for (BlockModel block : blocks) {
      sb.append(block.getName()).append(", ");
    }
    sb.delete(sb.lastIndexOf(", "), sb.length());

    return sb.toString();
  }
}
