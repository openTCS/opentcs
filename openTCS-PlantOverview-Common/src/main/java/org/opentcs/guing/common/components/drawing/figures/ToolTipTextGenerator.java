/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.drawing.figures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.opentcs.guing.base.components.properties.type.KeyValueProperty;
import org.opentcs.guing.base.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.base.model.FigureDecorationDetails;
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
 */
public class ToolTipTextGenerator {

  /**
   * The model manager.
   */
  private final ModelManager modelManager;

  /**
   * Create a new instance.
   *
   * @param modelManager The model manager to use.
   */
  @Inject
  public ToolTipTextGenerator(ModelManager modelManager) {
    this.modelManager = requireNonNull(modelManager, "modelManager");
  }

  /**
   * Generate a tooltip text for a vehicle model.
   *
   * @param model The vehicle model.
   * @return A tooltip text for the model element.
   */
  public String getToolTipText(VehicleModel model) {
    return "";
  }

  /**
   * Generate a tooltip text for a point model.
   *
   * @param model The point model.
   * @return A tooltip text for the model element.
   */
  public String getToolTipText(PointModel model) {
    String pointDesc = model.getDescription();
    StringBuilder sb = new StringBuilder("<html>");
    sb.append(pointDesc).append(" ").append("<b>").append(model.getName()).append("</b>");

    appendBlockInfo(sb, model);
    appendMiscProps(sb, model);
    appendAllocatingVehicle(sb, model);

    sb.append("</html>");

    return sb.toString();
  }

  /**
   * Generate a tooltip text for a location model.
   *
   * @param model The location model.
   * @return A tooltip text for the model element.
   */
  public String getToolTipText(LocationModel model) {
    String locationDesc = model.getDescription();
    StringBuilder sb = new StringBuilder("<html>");
    sb.append(locationDesc).append(" ").append("<b>").append(model.getName()).append("</b>");

    appendBlockInfo(sb, model);
    appendPeripheralInformation(sb, model);
    appendMiscProps(sb, model);
    appendAllocatingVehicle(sb, model);

    sb.append("</html>");

    return sb.toString();
  }

  /**
   * Generate a tooltip text for a path model.
   *
   * @param model The path model.
   * @return A tooltip text for the model element.
   */
  public String getToolTipText(PathModel model) {
    String pathDesc = model.getDescription();
    StringBuilder sb = new StringBuilder("<html>");
    sb.append(pathDesc).append(" ").append("<b>").append(model.getName()).append("</b>");

    appendBlockInfo(sb, model);
    appendMiscProps(sb, model);
    appendAllocatingVehicle(sb, model);

    sb.append("</html>");

    return sb.toString();
  }

  /**
   * Generate a tooltip text for a link model.
   *
   * @param model The link model.
   * @return A tooltip text for the model element.
   */
  public String getToolTipText(LinkModel model) {
    return new StringBuilder("<html>")
        .append(model.getDescription()).append(" ")
        .append("<b>").append(model.getName()).append("</b>")
        .append("</html>").toString();
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

  protected void appendMiscProps(StringBuilder sb, ModelComponent component) {
    KeyValueSetProperty miscProps = (KeyValueSetProperty) component.getProperty(MISCELLANEOUS);

    if (miscProps.getItems().isEmpty()) {
      return;
    }

    sb.append("<hr>\n");
    sb.append(miscProps.getDescription()).append(": \n");
    sb.append("<ul>\n");
    miscProps.getItems().stream()
        .sorted(Comparator.comparing(KeyValueProperty::getKey))
        .forEach(kvp -> {
          sb.append("<li>")
              .append(kvp.getKey()).append(": ").append(kvp.getValue())
              .append("</li>\n");
        });
    sb.append("</ul>\n");
  }

  protected void appendAllocatingVehicle(StringBuilder sb, FigureDecorationDetails figure) {
    // Displaying information about allocating vehicles is only relevant in the Operations Desk
    // application, which is why this method is empty here.
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
