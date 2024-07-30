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
    if (monitoringTask != null && !monitoringTask.isDone()) {
      LOG.info("Cancelling the old monitoring task");
      monitoringTask.cancel(false);
    }

    pendingCommands = new ArrayList<>(commands);
    LOG.info(String.format("SIZE OF pendingCommands: %d", pendingCommands.size()));

    currentCommandIndex = 0;
    monitoringTask = executor.scheduleAtFixedRate(
        () -> {
          try {
            checkVehicleStatus();
          }
          catch (Exception e) {
            LOG.severe("Error in checkVehicleStatus: " + e.getMessage());
          }
        },
        0, 1000, TimeUnit.MILLISECONDS
    );
  }

  private void checkVehicleStatus() {
    CompletableFuture<Integer> vehicleStatusFuture = adapter.readSingleRegister(105);
    CompletableFuture<Integer> liftStatusFuture = adapter.readSingleRegister(106);

    CompletableFuture.allOf(vehicleStatusFuture, liftStatusFuture)
        .thenCompose(v -> CompletableFuture.supplyAsync(() -> {
          int vehicleStatus = vehicleStatusFuture.join();
          int liftStatus = liftStatusFuture.join();
          return new int[]{vehicleStatus, liftStatus};
        }, executor))
        .thenAccept(statuses -> {
          LOG.info("Following messages come from MovementHandler.");
          updateVehicleStatus(statuses[0], statuses[1], adapter.getProcessModel().getPosition());
        })
        .exceptionally(ex -> {
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
    LOG.info("updateVehicleState HAS COMPLETED.");

    // Check if current movement command is completed
    if (currentCommandIndex < pendingCommands.size()) {
      MovementCommand currentCommand = pendingCommands.get(currentCommandIndex);
      LOG.info(
          String.format(
              "CURRENTLY PENDING CMD DESTINATION: %s",
              currentCommand.getStep().getDestinationPoint()
          )
      );

      if (isCommandCompleted(currentCommand, currentPosition)) {
        LOG.info(
            String.format(
                "CURRENT LOCATION MATCH THE DESTINATION: %s",
                currentPosition
            )
        );

        adapter.getProcessModel().commandExecuted(currentCommand);
        currentCommandIndex++;

        if (currentCommandIndex >= pendingCommands.size()) {
          LOG.info("All commands completed");
          monitoringTask.cancel(false);
//          adapter.getProcessModel().setPosition(currentPosition);
          adapter.getProcessModel().setState(Vehicle.State.IDLE);
        }
      }
      else {
        LOG.info(
            String.format(
                "VEHICLE HAS NOT REACH THE DESTINATION, EXPECT: %s, CURRENTLY: %s",
                currentCommand.getStep().getDestinationPoint().getName(), currentPosition
            )
        );
      }
    }
    else {
      LOG.warning(
          String.format(
              "currentCommandIndex: %d < pendingCommands.size : %d", currentCommandIndex,
              pendingCommands.size()
          )
      );
    }
  }

  private void updateVehicleState(int vehicleStatus, int liftStatus) {
    Vehicle.State vehicleState = switch (vehicleStatus) {
      case 0 -> Vehicle.State.IDLE;
      case 1 -> Vehicle.State.EXECUTING;
      case 2 -> Vehicle.State.FINISHED;
      default -> Vehicle.State.UNKNOWN;
    };

    boolean loadState = switch (vehicleStatus) {
      case 1, 2 -> true;
      default -> false;
    };

    adapter.getProcessModel().setState(vehicleState);

    // Update load handling devices based on lift status
    List<LoadHandlingDevice> devices = new ArrayList<>();
    devices.add(new LoadHandlingDevice("default", loadState));
    adapter.getProcessModel().setLoadHandlingDevices(devices);
  }

  private boolean isCommandCompleted(MovementCommand command, String currentPosition) {
    LOG.info(
        String.format(
            "CHECKING BETWEEN: %s & %s",
            command.getStep().getDestinationPoint().getName(),
            currentPosition
        )
    );
    return command.getStep().getDestinationPoint().getName().equals(currentPosition);
  }

  public void stopMonitoring() {
    if (monitoringTask != null) {
      monitoringTask.cancel(false);
    }
  }
}
