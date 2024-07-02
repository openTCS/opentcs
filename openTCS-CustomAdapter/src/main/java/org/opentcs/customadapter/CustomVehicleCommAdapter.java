package org.opentcs.customadapter;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.BasicVehicleCommAdapter;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.util.ExplainedBoolean;

public abstract class CustomVehicleCommAdapter
    extends BasicVehicleCommAdapter {

  private static final Logger LOG = Logger.getLogger(CustomVehicleCommAdapter.class.getName());

  public CustomVehicleCommAdapter(
      VehicleProcessModel processModel,
      String rechargeOperation,
      int commandsCapacity,
      @KernelExecutor ScheduledExecutorService executor
  ) {
    super(
        processModel,
        commandsCapacity,
        rechargeOperation,
        executor
    );
  }

  @Override
  public void sendCommand(MovementCommand cmd) {
    LOG.info("Sending command to vehicle: " + cmd.toString());
    sendSpecificCommand(cmd);
  }

  protected abstract void sendSpecificCommand(MovementCommand cmd);

  @Override
  protected void connectVehicle() {
    LOG.info("Connecting to vehicle...");
    if (performConnection()) {
      getProcessModel().setCommAdapterConnected(true);
    }
  }

  protected abstract boolean performConnection();

  @Override
  protected void disconnectVehicle() {
    LOG.info("Disconnecting from vehicle...");
    if (performDisconnection()) {
      getProcessModel().setCommAdapterConnected(false);
    }
  }

  protected abstract boolean performDisconnection();

  @Override
  protected boolean isVehicleConnected() {
    return getProcessModel().isCommAdapterConnected();
  }

  @Nonnull
  @Override
  public ExplainedBoolean canProcess(
      @Nonnull
      TransportOrder order
  ) {
    return new ExplainedBoolean(true, "Custom adapter can process all orders.");
  }

  @Override
  public void onVehiclePaused(boolean paused) {
    LOG.info("Vehicle paused: " + paused);
    // Implement specific behavior for vehicle paused state
  }

  @Override
  public void processMessage(
      @Nullable
      Object message
  ) {
    LOG.info("Processing message: " + (message != null ? message.toString() : "null"));
    // Implement message processing logic
  }

  protected abstract void updateVehiclePosition();

  protected abstract void updateVehicleState();

  // You can add more abstract methods here if needed for specific implementations
}
