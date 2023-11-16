/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.components.drawing.figures;

import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.opentcs.data.model.Vehicle;
import static org.opentcs.data.model.Vehicle.State.ERROR;
import static org.opentcs.data.model.Vehicle.State.UNAVAILABLE;
import static org.opentcs.data.model.Vehicle.State.UNKNOWN;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.guing.base.AllocationState;
import org.opentcs.guing.base.model.FigureDecorationDetails;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.components.drawing.figures.ToolTipTextGenerator;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.operationsdesk.peripherals.jobs.PeripheralJobsContainer;
import org.opentcs.operationsdesk.util.I18nPlantOverviewOperating;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * A tool tip generator integrating information about a vehicle's state.
 */
public class ToolTipTextGeneratorOperationsDesk
    extends ToolTipTextGenerator {

  /**
   * A unicode play symbol.
   */
  private static final String PLAY_SYMBOL = "\u23f5";
  /**
   * A unicode hourglass symbol.
   */
  private static final String HOURGLASS_SYMBOL = "\u29d6";

  /**
   * Collection of peripheral jobs.
   */
  private final PeripheralJobsContainer peripheralJobContainer;

  /**
   * Creates a new instance.
   *
   * @param modelManager The model manager to use.
   * @param peripheralJobContainer The peripheral job container to use.
   */
  @Inject
  public ToolTipTextGeneratorOperationsDesk(ModelManager modelManager,
                                            PeripheralJobsContainer peripheralJobContainer) {
    super(modelManager);
    this.peripheralJobContainer = requireNonNull(peripheralJobContainer, "peripheralJobContainer");
  }

  @Override
  public String getToolTipText(VehicleModel model) {
    requireNonNull(model, "model");

    StringBuilder sb = new StringBuilder("<html>\n");

    sb.append(model.getDescription())
        .append(" ")
        .append("<b>")
        .append(model.getName())
        .append("</b>\n");

    appendVehicleState(sb, model);
    appendMiscProps(sb, model);
    appendPeripheralInformationVehicle(sb, model);

    sb.append("</html>\n");
    return sb.toString();
  }

  @Override
  protected void appendAllocatingVehicle(StringBuilder sb, FigureDecorationDetails figure) {
    List<Entry<VehicleModel, AllocationState>> allocationStates = figure.getAllocationStates()
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue() == AllocationState.ALLOCATED)
        .collect(Collectors.toList());

    if (allocationStates.isEmpty()) {
      return;
    }

    sb.append("<hr>\n");
    allocationStates
        .forEach(entry -> {
          sb.append(getResourceAllocatedByText())
              .append(" ")
              .append(entry.getKey().getName())
              .append("<br>");
        });
  }

  private void appendPeripheralInformationVehicle(StringBuilder sb, VehicleModel vehicle) {
    sb.append("<hr>\n");
    sb.append(getRelatedPeripheralJobHeadingText()).append('\n');
    sb.append("<ul>\n");
    peripheralJobContainer.getPeripheralJobs().stream()
        .filter(job -> !job.getState().isFinalState())
        .filter(job -> job.getPeripheralOperation().isCompletionRequired())
        .filter(job -> Objects.equals(job.getRelatedVehicle(), vehicle.getVehicle().getReference()))
        .sorted(Comparator.comparing(PeripheralJob::getCreationTime))
        .forEach(job -> appendPeripheralJobListItem(sb, job));
    sb.append("</ul>\n");
  }

  @SuppressWarnings("checkstyle:LineLength")
  private String getRelatedPeripheralJobHeadingText() {
    return ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.MISC_PATH)
        .getString("toolTipTextGeneratorOperationsDesk.vehicleModel.awaitPeripheralJobCompletion.text");
  }

  private void appendPeripheralJobListItem(StringBuilder sb, PeripheralJob job) {
    sb.append("<li>")
        .append(job.getState() == PeripheralJob.State.BEING_PROCESSED
            ? PLAY_SYMBOL
            : HOURGLASS_SYMBOL)
        .append(job.getPeripheralOperation().getLocation().getName())
        .append(": ")
        .append(job.getPeripheralOperation().getOperation())
        .append(" (")
        .append(job.getName())
        .append(")</li>\n");
  }

  private void appendVehicleState(StringBuilder sb, VehicleModel model) {
    sb.append("<hr>\n");
    sb.append(model.getPropertyPoint().getDescription()).append(": ")
        .append(model.getPoint() != null ? model.getPoint().getName() : "?")
        .append('\n');
    sb.append("<br>\n");
    sb.append(model.getPropertyNextPoint().getDescription()).append(": ")
        .append(model.getNextPoint() != null ? model.getNextPoint().getName() : "?")
        .append('\n');

    sb.append("<br>\n");
    sb.append(model.getPropertyState().getDescription())
        .append(": <font color=").append(stateColorString(model.getVehicle())).append(">")
        .append(model.getPropertyState().getValue())
        .append("</font>\n");

    sb.append("<br>\n");
    sb.append(model.getPropertyProcState().getDescription()).append(": ")
        .append(model.getPropertyProcState().getValue())
        .append('\n');
    sb.append("<br>\n");
    sb.append(model.getPropertyIntegrationLevel().getDescription()).append(": ")
        .append(model.getPropertyIntegrationLevel().getValue())
        .append('\n');

    sb.append("<br>\n");
    sb.append(model.getPropertyEnergyLevel().getDescription())
        .append(": <font color=").append(energyColorString(model.getVehicle())).append(">")
        .append(model.getPropertyEnergyLevel().getValue())
        .append("%</font>\n");
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

  @SuppressWarnings("checkstyle:LineLength")
  private String getResourceAllocatedByText() {
    return ResourceBundleUtil.getBundle(I18nPlantOverviewOperating.MISC_PATH)
        .getString("toolTipTextGeneratorOperationsDesk.figureDecorationDetails.resourceAllocatedBy.text");
  }
}
