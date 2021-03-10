/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.drawing.figures;

import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javax.inject.Inject;
import static org.opentcs.guing.I18nPlantOverviewBase.BUNDLE_PATH;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.model.ModelComponent;
import static org.opentcs.guing.model.ModelComponent.MISCELLANEOUS;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.persistence.ModelManager;

/**
 * Generates tooltip texts for various model elements.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class ToolTipTextGenerator {

  /**
   * This class's resource bundle.
   */
  private final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH);
  /**
   * The model manager.
   */
  private final ModelManager modelManager;

  @Inject
  public ToolTipTextGenerator(ModelManager modelManager) {
    this.modelManager = requireNonNull(modelManager, "modelManager");
  }

  public String getToolTipText(PointModel model) {
    String pointDesc = bundle.getString("point.description");
    StringBuilder sb = new StringBuilder("<html>");
    sb.append(pointDesc).append(" ").append("<b>").append(model.getName()).append("</b>");

    appendBlockInfo(sb, model);
    appendMiscProps(sb, model);

    sb.append("</html>");

    return sb.toString();
  }

  public String getToolTipText(LocationModel model) {
    String locationDesc = bundle.getString("location.description");
    StringBuilder sb = new StringBuilder("<html>");
    sb.append(locationDesc).append(" ").append("<b>").append(model.getName()).append("</b>");

    appendBlockInfo(sb, model);
    appendMiscProps(sb, model);

    sb.append("</html>");

    return sb.toString();
  }

  public String getToolTipText(PathModel model) {
    String pathDesc = bundle.getString("path.description");
    StringBuilder sb = new StringBuilder("<html>");
    sb.append(pathDesc).append(" ").append("<b>").append(model.getName()).append("</b>");

    appendBlockInfo(sb, model);

    sb.append("</html>");

    return sb.toString();
  }

  public String getToolTipText(VehicleModel model) {
    String vehicleDesc = bundle.getString("vehicle.description");
    String positionDesc = bundle.getString("vehicle.point.text");
    String nextPositionDesc = bundle.getString("vehicle.nextPoint.text");
    String stateDesc = bundle.getString("vehicle.state.text");
    String procStateDesc = bundle.getString("vehicle.procState.text");
    String integrationLevelDesc = bundle.getString("vehicle.integrationLevel.text");
    String energyDesc = bundle.getString("vehicle.energyLevel.text");
    StringBuilder sb = new StringBuilder("<html>");
    sb.append(vehicleDesc).append(" ").append("<b>").append(model.getName()).append("</b>");
    sb.append("<br>").append(positionDesc).append(": ")
        .append(model.getPoint() != null ? model.getPoint().getName() : "?");
    sb.append("<br>").append(nextPositionDesc).append(": ")
        .append(model.getNextPoint() != null ? model.getNextPoint().getName() : "?");
    sb.append("<br>").append(stateDesc).append(": ").append(model.getPropertyState().getValue());
    sb.append("<br>").append(procStateDesc).append(": ").append(model.getPropertyProcState().getValue());
    sb.append("<br>").append(integrationLevelDesc).append(": ").append(model.getPropertyIntegrationLevel().getValue());

    VehicleModel.EnergyState state = (VehicleModel.EnergyState) model.getPropertyEnergyState().getValue();
    String sColor;
    switch (state) {
      case CRITICAL:
        sColor = "red";
        break;

      case DEGRADED:
        sColor = "orange";
        break;

      case GOOD:
        sColor = "green";
        break;

      default:
        sColor = "black";
    }

    sb.append("<br>").append(energyDesc).append(": <font color=").append(sColor).append(">")
        .append(model.getPropertyEnergyLevel().getValue())
        .append("%</font>");
    sb.append("</html>");

    return sb.toString();
  }

  public String getToolTipText(LinkModel model) {
    String linkDesc = bundle.getString("link.description");
    StringBuilder sb = new StringBuilder("<html>");
    sb.append(linkDesc).append(" ").append("<b>").append(model.getName()).append("</b>");
    sb.append("</html>");

    return sb.toString();
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

    for (KeyValueProperty kvp : miscProps.getItems()) {
      sb.append("<br>").append(kvp.getKey()).append(": ").append(kvp.getValue());
    }
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

    String desc = bundle.getString("modelComponent.blocks.description");
    StringBuilder sb = new StringBuilder("<br>").append(desc);
    for (BlockModel block : blocks) {
      sb.append(block.getName()).append(", ");
    }
    sb.delete(sb.lastIndexOf(", "), sb.length());

    return sb.toString();
  }
}
