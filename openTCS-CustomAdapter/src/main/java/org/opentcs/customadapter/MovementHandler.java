package org.opentcs.customadapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.drivers.vehicle.MovementCommand;

public class MovementHandler {
  private static final Logger LOG = Logger.getLogger(MovementHandler.class.getName());

  private final ScheduledExecutorService executor;
  private final ModbusTCPVehicleCommAdapter adapter;
  private ScheduledFuture<?> monitoringTask;
  private List<MovementCommand> pendingCommands;
  private int currentCommandIndex;

  public MovementHandler(ScheduledExecutorService executor, ModbusTCPVehicleCommAdapter adapter) {
    this.executor = executor;
    this.adapter = adapter;
    this.pendingCommands = new ArrayList<>();
    this.currentCommandIndex = 0;
  }

  public void startMonitoring(List<MovementCommand> commands) {
    pendingCommands = new ArrayList<>(commands);
    currentCommandIndex = 0;
    monitoringTask = executor.scheduleAtFixedRate(
        this::checkVehicleStatus, 0, 500, TimeUnit.MILLISECONDS
    );
  }

  private void checkVehicleStatus() {
    CompletableFuture.allOf(
        adapter.readSingleRegister(105),
        adapter.readSingleRegister(106)
    ).thenAccept(v -> {
      LOG.info("Following messages come from MovementHandler.");
      int vehicleStatus = adapter.readSingleRegister(105).join();
      int liftStatus = adapter.readSingleRegister(106).join();
      String currentPosition = adapter.getProcessModel().getPosition();

      updateVehicleStatus(vehicleStatus, liftStatus, currentPosition);
    }).exceptionally(ex -> {
      LOG.severe("Failed to read vehicle status: " + ex.getMessage());
      return null;
    });
  }

  private void updateVehicleStatus(int vehicleStatus, int liftStatus, String currentPosition) {
    LOG.info(
        "Updating vehicle status: vehicleStatus=" + vehicleStatus + ", liftStatus=" + liftStatus
            + ", currentPosition=" + currentPosition
    );

    updateVehicleState(vehicleStatus, liftStatus);

    // Check if current movement command is completed
    if (currentCommandIndex < pendingCommands.size()) {
      MovementCommand currentCommand = pendingCommands.get(currentCommandIndex);
      if (isCommandCompleted(currentCommand, currentPosition)) {
        adapter.getProcessModel().commandExecuted(currentCommand);
        currentCommandIndex++;

        if (currentCommandIndex >= pendingCommands.size()) {
          LOG.info("All commands completed");
          monitoringTask.cancel(false);
          adapter.getProcessModel().setPosition(currentPosition);
          adapter.getProcessModel().setState(Vehicle.State.IDLE);
        }
      }
    }
  }

  private void updateVehicleState(int vehicleStatus, int liftStatus) {
    Vehicle.State newState = switch (vehicleStatus) {
      case 0 -> Vehicle.State.IDLE;
      case 1 -> Vehicle.State.EXECUTING;
      case 2 -> Vehicle.State.ERROR;
      default -> Vehicle.State.UNKNOWN;
    };

    adapter.getProcessModel().setState(newState);

    // Update load handling devices based on lift status
    List<LoadHandlingDevice> devices = new ArrayList<>();
    devices.add(new LoadHandlingDevice("default", true));
    adapter.getProcessModel().setLoadHandlingDevices(devices);
  }

  private boolean isCommandCompleted(MovementCommand command, String currentPosition) {
    return command.getStep().getDestinationPoint().getName().equals(currentPosition);
  }

  public void stopMonitoring() {
    if (monitoringTask != null) {
      monitoringTask.cancel(false);
    }
  }
}
