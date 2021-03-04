/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles;

import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleController;
import org.opentcs.util.ExplainedBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Null-object implementation of {@link VehicleController}.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class NullVehicleController
    implements VehicleController {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(NullVehicleController.class);
  /**
   * The associated vehicle's name.
   */
  private final String vehicleName;

  /**
   * Creates a new instance.
   *
   * @param vehicleName The associated vehicle's name.
   */
  public NullVehicleController(@Nonnull String vehicleName) {
    this.vehicleName = requireNonNull(vehicleName, "vehicleName");
  }

  @Override
  public void initialize() {
  }

  @Override
  public boolean isInitialized() {
    return true;
  }

  @Override
  public void terminate() {
  }

  @Override
  public void setDriveOrder(DriveOrder newOrder, Map<String, String> orderProperties)
      throws IllegalStateException {
    LOG.warn("No comm adapter attached to vehicle {}", vehicleName);
  }

  @Override
  public void clearDriveOrder() {
    LOG.warn("No comm adapter attached to vehicle {}", vehicleName);
  }

  @Override
  public void abortDriveOrder() {
    LOG.warn("No comm adapter attached to vehicle {}", vehicleName);
  }

  @Override
  public void clearCommandQueue() {
    LOG.warn("No comm adapter attached to vehicle {}", vehicleName);
  }

  @Override
  @Deprecated
  public void resetVehiclePosition() {
    LOG.warn("No comm adapter attached to vehicle {}", vehicleName);
  }

  @Override
  public ExplainedBoolean canProcess(List<String> operations) {
    return new ExplainedBoolean(false, "NullVehicleController");
  }

  @Override
  public void sendCommAdapterMessage(Object message) {
    LOG.warn("No comm adapter attached to vehicle {}", vehicleName);
  }

  @Override
  public void sendCommAdapterCommand(AdapterCommand command) {
    LOG.warn("No comm adapter attached to vehicle {}", vehicleName);
  }

  @Override
  public String getId() {
    return vehicleName;
  }

  @Override
  public boolean allocationSuccessful(Set<TCSResource<?>> resources) {
    LOG.warn("No comm adapter attached to vehicle {}", vehicleName);
    return false;
  }

  @Override
  public void allocationFailed(Set<TCSResource<?>> resources) {
    LOG.warn("No comm adapter attached to vehicle {}", vehicleName);
  }
}
