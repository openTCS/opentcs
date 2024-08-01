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

  /**
   * Handles the movement of a vehicle by executing a list of commands.
   *
   * @param executor The executor service used to schedule the execution of commands.
   * @param adapter The ModbusTCPVehicleCommAdapter used for communication with the vehicle.
   */
  public MovementHandler(ScheduledExecutorService executor, ModbusTCPVehicleCommAdapter adapter) {
    this.executor = executor;
    this.adapter = adapter;
    this.pendingCommands = new ArrayList<>();
    this.currentCommandIndex = 0;
  }

  /**
   * Starts monitoring and executing a list of movement commands.
   *
   * @param commands The list of movement commands to monitor and execute.
   */
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
    CompletableFuture<Integer> loadStatusFuture = adapter.readSingleRegister(107);

    CompletableFuture.allOf(vehicleStatusFuture, liftStatusFuture)
        .thenCompose(v -> CompletableFuture.supplyAsync(() -> {
          int vehicleStatus = vehicleStatusFuture.join();
          int liftStatus = liftStatusFuture.join();
          int loadStatus = loadStatusFuture.join();

          return new int[]{vehicleStatus, liftStatus, loadStatus};
        }, executor))
        .thenAccept(statuses -> {
          LOG.info("Following messages come from MovementHandler.");
          updateVehicleStatus(
              statuses[0], statuses[1], statuses[2], adapter.getProcessModel().getPosition()
          );
        })
        .exceptionally(ex -> {
          LOG.severe("Failed to read vehicle status: " + ex.getMessage());
          return null;
        });
  }

  private void updateVehicleStatus(
      int vehicleStatus, int liftStatus, int loadStatus, String currentPosition
  ) {
    LOG.info(
        "Updating vehicle status: vehicleStatus=" + vehicleStatus + ", liftStatus=" + liftStatus
            + ", loadStatus=" + loadStatus
            + ", currentPosition=" + currentPosition
    );

    updateVehicleState(vehicleStatus, loadStatus);
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

      if (hasReachedDestination(currentCommand, currentPosition) &&
          isOperationCompleted(currentCommand, liftStatus, loadStatus)) {
        LOG.info(
            String.format(
                "CURRENT LOCATION MATCH THE DESTINATION AND OPERATION COMPLETED: %s",
                currentPosition
            )
        );
        adapter.getProcessModel().commandExecuted(currentCommand);
        currentCommandIndex++;

        if (currentCommandIndex >= pendingCommands.size()) {
          LOG.info("All commands completed");
          monitoringTask.cancel(false);
          adapter.getProcessModel().setState(Vehicle.State.IDLE);
        }
      }
      else {
        LOG.info(
            String.format(
                "VEHICLE HAS NOT COMPLETED THE COMMAND, "
                    + "EXPECT: %s, CURRENTLY AT: %s, OPERATION: %s",
                currentCommand.getStep().getDestinationPoint().getName(),
                currentPosition,
                currentCommand.getOperation()
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

  private boolean isOperationCompleted(MovementCommand command, int liftStatus, int loadStatus) {
    String operation = command.getOperation();
    if (operation.isEmpty()) {
      return true;
    }

    if (adapter.getProcessModel().getState() != Vehicle.State.IDLE) {
      return false;
    }

    if (operation.equalsIgnoreCase("Load")) {
      return (liftStatus == 2 && loadStatus == 1);
    }
    else if (operation.equalsIgnoreCase("Unload")) {
      return (liftStatus == 0 && loadStatus == 2);
    }
    else {
      return true;
    }
  }

  private void updateVehicleState(int vehicleStatus, int loadStatus) {
    Vehicle.State vehicleState = switch (vehicleStatus) {
      case 0 -> Vehicle.State.IDLE;
      case 1 -> Vehicle.State.EXECUTING;
      case 2 -> Vehicle.State.FINISHED;
      default -> Vehicle.State.UNKNOWN;
    };

    boolean liftState = (loadStatus == 1);
    adapter.getProcessModel().setState(vehicleState);

    // Update load handling devices based on lift status
    List<LoadHandlingDevice> devices = new ArrayList<>();
    devices.add(new LoadHandlingDevice("default", liftState));
    adapter.getProcessModel().setLoadHandlingDevices(devices);
  }

  private boolean hasReachedDestination(MovementCommand command, String currentPosition) {
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
