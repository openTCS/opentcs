package org.opentcs.customadapter;

import static java.util.Objects.requireNonNull;

import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
import com.digitalpetri.modbus.requests.ReadHoldingRegistersRequest;
import com.digitalpetri.modbus.requests.ReadInputRegistersRequest;
import com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest;
import com.digitalpetri.modbus.responses.ModbusResponse;
import com.digitalpetri.modbus.responses.ReadHoldingRegistersResponse;
import com.digitalpetri.modbus.responses.ReadInputRegistersResponse;
import com.digitalpetri.modbus.responses.WriteMultipleRegistersResponse;
import com.digitalpetri.modbus.responses.WriteSingleRegisterResponse;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.timeout.TimeoutException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.jgrapht.alg.util.Pair;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.components.kernel.services.PlantModelService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.PlantModel;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.util.ExplainedBoolean;

public class ModbusTCPVehicleCommAdapter
    extends
      CustomVehicleCommAdapter {
  /**
   * The name of the load handling device set by this adapter.
   */
  public static final String LHD_NAME = "default";
  /**
   * This class's Logger.
   */
  private static final Logger LOG = Logger.getLogger(ModbusTCPVehicleCommAdapter.class.getName());
  /**
   * An error code indicating that there's a conflict between a load operation and the vehicle's
   * current load state.
   */
  private static final String LOAD_OPERATION_CONFLICT = "cannotLoadWhenLoaded";
  /**
   * An error code indicating that there's a conflict between an unload operation and the vehicle's
   * current load state.
   */
  private static final String UNLOAD_OPERATION_CONFLICT = "cannotUnloadWhenNotLoaded";
  /**
   * MAP2: station name -> <CMD1, CMD2>.
   */
  private final Map<Long, Pair<CMD1, CMD2>> stationCommandsMap = new LinkedHashMap<>();
  private final Map<Long, String> positionMap;
  private final List<MovementCommand> allMovementCommands = new ArrayList<>();
  private final List<ModbusCommand> positionModbusCommand = new ArrayList<>();
  private final List<ModbusCommand> cmdModbusCommand = new ArrayList<>();
  private TransportOrder currentTransportOrder;
  /**
   * Represents a vehicle associated with a ModbusTCPVehicleCommAdapter.
   */
  private final Vehicle vehicle;
  /**
   * The vehicle's load state.
   */
  private LoadState loadState = LoadState.EMPTY;
  /**
   * The host address for the TCP connection.
   */
  private final String host;
  /**
   * The port number for the TCP connection.
   */
  private final int port;
  /**
   * Indicates whether the vehicle is currently connected.
   */
  private boolean isConnected;
  /**
   * Represents a Modbus TCP master used for communication with Modbus TCP devices.
   */
  private ModbusTcpMaster master;

  /**
   * Represents the state of a variable indicating whether it has been initialized.
   */
  private boolean initialized;
  private final AtomicBoolean heartBeatToggle = new AtomicBoolean(false);
  private ScheduledFuture<?> heartBeatFuture;
  private PositionUpdater positionUpdater;
  private final PlantModelService plantModelService;
  private final VehicleConfigurationProvider configProvider;

  /**
   * A communication adapter for ModbusTCP-based vehicle communication.
   * <p>
   * Allows communication between the vehicle and the control system using the ModbusTCP protocol.
   *
   * @param executor The executor for handling background tasks.
   * @param vehicle The vehicle associated with this communication adapter.
   * @param host The host IP address for the ModbusTCP connection.
   * @param port The port number for the ModbusTCP connection.
   * @param plantModelService The plant model service for accessing plant model information.
   */
  @SuppressWarnings("checkstyle:TodoComment")

  @Inject
  public ModbusTCPVehicleCommAdapter(
      @KernelExecutor
      ScheduledExecutorService executor,
      @Assisted
      Vehicle vehicle,
      String host,
      int port,
      PlantModelService plantModelService
  ) {
    super(new CustomProcessModel(vehicle), "RECHARGE", 1000, executor);
    this.configProvider = new VehicleConfigurationProvider();
    this.host = configProvider.getConfiguration(vehicle.getName()).host();
    this.port = configProvider.getConfiguration(vehicle.getName()).port();

    LOG.warning(String.format("DEVICE HOST:%s, PORT: %d", this.host, this.port));
    this.vehicle = requireNonNull(vehicle, "vehicle");
    this.plantModelService = requireNonNull(plantModelService, "plantModelService");

    this.isConnected = false;
    this.currentTransportOrder = null;
    this.positionMap = new HashMap<>();
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      LOG.warning("Device has been initialized");
      return;
    }
    super.initialize();
    getProcessModel().setState(Vehicle.State.IDLE);
    LOG.warning("Device has been set to IDLE state");
    ((ExecutorService) getExecutor()).submit(() -> getProcessModel().setPosition("Point-0026"));
    LOG.warning("Device has been set to Point-0026");
    getProcessModel().setLoadHandlingDevices(
        List.of(new LoadHandlingDevice(LHD_NAME, false))
    );
    getProcessModel().setMaxFwdVelocity(vehicle.getMaxVelocity());
    LOG.warning("Device has set load handling device");
    initializePositionMap();
    this.positionUpdater = new PositionUpdater(getProcessModel(), getExecutor());
    LOG.warning("Starting sending heart bit.");
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }
    super.terminate();
    positionUpdater.stopPositionUpdates();
    stopHeartBeat();
    initialized = false;// Stop the heartbeat mechanism
  }

  private void startHeartbeat() {
    heartBeatFuture = getExecutor().scheduleAtFixedRate(() -> {

      boolean currentValue = heartBeatToggle.getAndSet(!heartBeatToggle.get());
      writeSingleRegister(100, currentValue ? 1 : 0)
          .thenCompose(v -> readSingleRegister(100))
          .thenAccept(value -> {
            if (value != (currentValue ? 1 : 0)) {
              LOG.warning("Heartbeat value mismatch! Retrying...");
              writeSingleRegister(100, currentValue ? 1 : 0)
                  .exceptionally(ex -> {
                    LOG.severe("Failed to retry heartbeat write: " + ex.getMessage());
                    return null;
                  });
            }
          })
          .exceptionally(ex -> {
            LOG.severe("Failed to write or read heartbeat: " + ex.getMessage());
            return null;
          });
    }, 0, 500, TimeUnit.MILLISECONDS);
  }

  private void stopHeartBeat() {
    if (heartBeatFuture != null && !heartBeatFuture.isCancelled()) {
      heartBeatFuture.cancel(true);
    }
  }

  private void initializePositionMap() {
    try {
      PlantModel plantModel = plantModelService.getPlantModel();
      for (Point point : plantModel.getPoints()) {
        String positionName = point.getName();
        LOG.info(String.format("positionName: %s", positionName));
        Long precisePosition = point.getPose().getPosition().getX();
        LOG.info(String.format("precisePosition: %d", precisePosition));
        positionMap.put(precisePosition, positionName);
      }

      LOG.info("Position map initialized with " + positionMap.size() + " entries.");
    }
    catch (KernelRuntimeException e) {
      LOG.severe("Failed to initialize position map: " + e.getMessage());
    }
  }


  /**
   * Processes updates of the {@link CustomProcessModel}.
   *
   * <p><em>Overriding methods should also call this.</em></p>
   *
   * @param evt The property change event published by the model.
   */
  @SuppressWarnings("checkstyle:TodoComment")
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    super.propertyChange(evt);

    if (!((evt.getSource()) instanceof CustomProcessModel)) {
      return;
    }
    if (Objects.equals(
        evt.getPropertyName(),
        VehicleProcessModel.Attribute.LOAD_HANDLING_DEVICES.name()
    )) {
      if (!getProcessModel().getLoadHandlingDevices().isEmpty()
          && getProcessModel().getLoadHandlingDevices().getFirst().isFull()) {
        loadState = LoadState.FULL;
        // TODO: need change vehicle model size in future.
//        getProcessModel().setLength(configuration.vehicleLengthLoaded());
      }
      else {
        loadState = LoadState.EMPTY;
//        getProcessModel().setLength(configuration.vehicleLengthUnloaded());
      }
    }
  }

  /**
   * Enables the communication adapter.
   */
  @Override
  public synchronized void enable() {
    if (isEnabled()) {
      return;
    }
    super.enable();
  }

  /**
   * Disables the communication adapter.
   *
   * <p>If the adapter is already disabled, the method returns without doing anything.
   * When disabling the adapter, the vehicle's connection to the adapter is terminated,
   * the adapter is marked as disabled, and the vehicle's state is set to UNKNOWN.</p>
   *
   * <p>This method is synchronized to ensure that only one thread can disable the adapter
   * at a time.</p>
   *
   * @see CustomVehicleCommAdapter#disable()
   */
  @Override
  public synchronized void disable() {
    if (!isEnabled()) {
      return;
    }
    positionUpdater.stopPositionUpdates();
    stopHeartBeat();
    super.disable();
  }

  /**
   * Retrieves the custom process model associated with the vehicle.
   *
   * @return The custom process model.
   */
  @Override
  @Nonnull
  public CustomProcessModel getProcessModel() {
    return (CustomProcessModel) super.getProcessModel();
  }

  @Override
  protected void sendSpecificCommand(MovementCommand cmd) {
    if (!isVehicleConnected()) {
      LOG.warning("Not connected to Modbus TCP server. Cannot send command.");
      return;
    }

    if (!allMovementCommands.contains(cmd)) {
      LOG.warning(
          String.format(
              "%s: Command is NOT in MovementCommands pool : %s.", getName(),
              cmd.getStep().getDestinationPoint().getName()
          )
      );
      // open this comment back after testing.
      // getProcessModel().commandFailed(cmd);
    }
  }

  @Override
  public synchronized boolean enqueueCommand(MovementCommand newCommand) {
    requireNonNull(newCommand, "newCommand cannot be empty");

    if (!canAcceptNextCommand()) {
      return false;
    }

    checkTransportOrderAndLog(newCommand);
    queueNewCommand(newCommand);

    if (newCommand.isFinalMovement()) {
      LOG.info(
          String.format(
              "FINAL MOVEMENTCOMMAND DESTINATION: %s", newCommand.getStep().getDestinationPoint()
                  .getName()
          )
      );
      LOG.info("RECEIVED FINAL COMMAND, PROCESSING COMMANDS. ");
      processAllMovementCommands();
      allMovementCommands.clear();
    }
    return true;
  }

  private void checkTransportOrderAndLog(MovementCommand newCommand) {
    if (currentTransportOrder == null || !currentTransportOrder.equals(
        newCommand.getTransportOrder()
    )) {
      LOG.info(
          String.format(
              "New Transport order (%s) has received.", newCommand.getTransportOrder().getName()
          )
      );
      currentTransportOrder = newCommand.getTransportOrder();
      allMovementCommands.clear();
    }
  }

  private void queueNewCommand(MovementCommand newCommand) {
    allMovementCommands.add(newCommand);

    LOG.info(
        String.format(
            "%s: Custom Adding command: , Current Movement Pool Size: %d", getName(),
            allMovementCommands.size()
        )
    );

    getUnsentCommands().add(newCommand);
    getProcessModel().commandEnqueued(newCommand);
  }

  private void processAllMovementCommands() {
    LOG.info(
        String.format(
            "Processing all movement commands for TransportOrder: %s, POOL size: %d",
            currentTransportOrder.getName(), allMovementCommands.size()
        )
    );
    convertMovementCommandsToModbusCommands(allMovementCommands);
    writeAllModbusCommands();
    allMovementCommands.clear();
    currentTransportOrder = null;
  }

  /**
   * Converts a list of MovementCommand objects to a list of ModbusCommand objects.
   *
   * @param commands The list of MovementCommand objects to be converted.
   */
  private void convertMovementCommandsToModbusCommands(
      List<MovementCommand> commands
  ) {
    stationCommandsMap.clear();
    positionModbusCommand.clear();
    cmdModbusCommand.clear();
    LOG.info("ALL COMMAND BUFFER HAS BEEN CLEARED.");

    processMovementCommands(commands);
    int positionBaseAddress = 1000;
    int cmdBaseAddress = 1200;

    // Convert stationCommandsMap to ModbusCommand list
    for (Map.Entry<Long, Pair<CMD1, CMD2>> entry : stationCommandsMap.entrySet()) {
      long stationPosition = entry.getKey();
      Pair<CMD1, CMD2> cmds = entry.getValue();
      positionModbusCommand.add(
          new ModbusCommand(
              "POSITION", (int) stationPosition, positionBaseAddress,
              ModbusCommand.DataFormat.DECIMAL
          )
      );
      cmdModbusCommand.add(
          new ModbusCommand(
              "CMD1", cmds.getFirst().toShort(), cmdBaseAddress,
              ModbusCommand.DataFormat.HEXADECIMAL
          )
      );
      cmdModbusCommand.add(
          new ModbusCommand(
              "CMD2", cmds.getSecond().toShort(), cmdBaseAddress + 1,
              ModbusCommand.DataFormat.HEXADECIMAL
          )
      );

      positionBaseAddress += 2;
      cmdBaseAddress += 2;
    }
  }

  /**
   * Processes the given list of movement commands.
   *
   * @param commands The list of movement commands to be processed.
   */
  private void processMovementCommands(List<MovementCommand> commands) {
    long startPosition;
    for (MovementCommand cmd : commands) {
      // If step doesn't have source point,
      // that means vehicle is start from the destination, no need to move.
      if (cmd.getStep().getSourcePoint() == null) {
        LOG.warning(String.format("Vehicle %s don't need to move actually.", vehicle.getName()));
        continue;
      }
      LOG.info(
          String.format(
              "Vehicle %s got movements at Source point %s.",
              vehicle.getName(),
              cmd.getStep().getSourcePoint().getName()
          )
      );
      startPosition = cmd.getStep().getSourcePoint().getPose().getPosition().getX();
      LOG.info(String.format("CREATING COMMAND FOR POSITION: %d", startPosition));

      Pair<CMD1, CMD2> pairCommands = new Pair<>(createCMD1(cmd), createCMD2(cmd));
      stationCommandsMap.put(startPosition, pairCommands);
    }
  }

  @SuppressWarnings("checkstyle:TodoComment")
  private CMD1 createCMD1(MovementCommand cmd) {
    int liftCmd = 0;
    int speedLevel = 0;
    int obstacleSensor = 5;
    double maxSpeed = 0;
    String command = cmd.getOperation();
    liftCmd = getLiftCommand(command);
    if (cmd.getStep().getPath() != null) {
      maxSpeed = getMaxAllowedSpeed(cmd.getStep().getPath());
      LOG.info(String.format("GOT MAX SPEED: %f", maxSpeed));
    }
    int speedCase = (int) (maxSpeed * 0.005);
    speedLevel = switch (speedCase) {
      case 0 -> 1;
      case 3 -> 2;
      case 6 -> 3;
      case 9 -> 4;
      case 12 -> 5;
      default -> 1;
    };
    return new CMD1(
        liftCmd, speedLevel, obstacleSensor, 0
    );
    // cmd.getStep().getVehicleOrientation()
    //            == Vehicle.Orientation.FORWARD ? 0 : 1
  }

  private CMD2 createCMD2(MovementCommand cmd) {
    String switchOperation = "";
    int liftHeight = 0;
    int motionCommand;
    Map<String, String> pathOperation = null;

    if (cmd.getStep().getDestinationPoint().getName().equals("Sidefork")) {
      liftHeight = 4095;
    }
    if (cmd.getStep().getPath() != null) {
      pathOperation = cmd.getStep().getPath().getProperties();
    }
    if (pathOperation != null && !pathOperation.isEmpty()) {
      switchOperation = pathOperation.get("switch");
    }
    motionCommand = switch (switchOperation) {
      case "left" -> 1;
      case "right" -> 2;
      default -> 1;
    };
    if (cmd.isFinalMovement()) {
      motionCommand = 3;
    }
    return new CMD2(liftHeight, motionCommand);
  }

  private double getMaxAllowedSpeed(Path path) {
    double processModelMaxFwdVelocity = getProcessModel().getMaxFwdVelocity();
    LOG.info(String.format("processModelMaxFwdVelocity: %f", processModelMaxFwdVelocity));
    double maxPathVelocity = path.getMaxVelocity();
    LOG.info(String.format("maxPathVelocity: %f", maxPathVelocity));

    return Math.min(processModelMaxFwdVelocity, maxPathVelocity);
  }

  private static int getLiftCommand(String command) {
    return switch (command) {
      case "Load" -> 2;
      case "Unload" -> 1;
      default -> 1;
    };
  }

  private CompletableFuture<Void> writeAllModbusCommands() {
    return writeSingleRegister(108, 0)
        .thenCompose(v -> {
          if (!positionModbusCommand.isEmpty()) {
            try {
              return writeModbusCommands(positionModbusCommand, "Position");
            }
            catch (ExecutionException | InterruptedException e) {
              throw new RuntimeException(e);
            }
          }
          else {
            return CompletableFuture.completedFuture(null);
          }
        })
        .thenCompose(v -> {
          if (!cmdModbusCommand.isEmpty()) {
            try {
              return writeModbusCommands(cmdModbusCommand, "CMD");
            }
            catch (ExecutionException | InterruptedException e) {
              throw new RuntimeException(e);
            }
          }
          else {
            return CompletableFuture.completedFuture(null);
          }
        })
        .thenCompose(v -> writeSingleRegister(108, 1))
        .thenCompose(v -> readAndVerifyCommands())
        .exceptionally(ex -> {
          LOG.severe("Error in writeAllModbusCommands: " + ex.getMessage());
          throw new CompletionException(ex);
        });
  }

  private CompletableFuture<Void> writeSingleRegister(int address, int value) {
    ByteBuf buffer = Unpooled.buffer(2);
    buffer.writeShort(value);
    WriteMultipleRegistersRequest request = new WriteMultipleRegistersRequest(address, 1, buffer);

    return sendModbusRequest(request)
        .thenAccept(response -> {
          LOG.info("Successfully wrote register at address " + address + " with value " + value);
        })
        .exceptionally(ex -> {
          LOG.severe("Failed to write register at address " + address + ": " + ex.getMessage());
          return null;
        })
        .whenComplete((v, ex) -> {
          if (buffer.refCnt() > 0) {
            buffer.release();
          }
        });
  }

  private CompletableFuture<Integer> readSingleRegister(int address) {
    ReadInputRegistersRequest request = new ReadInputRegistersRequest(address, 1);
    return sendModbusRequest(request)
        .thenApply(response -> {
          if (response instanceof ReadInputRegistersResponse readResponse) {
            ByteBuf responseBuffer = readResponse.getRegisters();
            int value = responseBuffer.readUnsignedShort();
            LOG.info(String.format("READ ADDRESS %d GOT %d", address, value));
            return value;
          }
          throw new RuntimeException("Invalid response type");
        });
  }

  private CompletableFuture<Long> readDWordRegister(int startAddress) {
    return sendModbusRequest(new ReadHoldingRegistersRequest(startAddress, 2))
        .thenApply(response -> {
          if (response instanceof ReadHoldingRegistersResponse readResponse) {
            ByteBuf registers = readResponse.getRegisters();
            try {
              // Read two 16-bit registers and combine into a 32-bit integer
              int lowWord = registers.readUnsignedShort();
              int highWord = registers.readUnsignedShort();
              // Use little endian combination
              return (long) (highWord << 16 | lowWord);
            }
            finally {
              registers.release();
            }
          }
          else {
            throw new IllegalArgumentException("Unexpected response type");
          }
        });
  }

  private CompletableFuture<Void> readAndVerifyCommands() {
    List<CompletableFuture<Boolean>> readFutures = cmdModbusCommand.stream()
        .map(this::readAndCompareCommand)
        .toList();
    CompletableFuture<?>[] futuresArray = readFutures.toArray(new CompletableFuture<?>[0]);

    return CompletableFuture.allOf(futuresArray)
        .thenRun(() -> {
//          boolean allVerified = readFutures.stream().allMatch(future -> {
//            try {
//              return future.get();
//            }
//            catch (InterruptedException | ExecutionException e) {
//              LOG.severe("Error while verifying command: " + e.getMessage());
//              return false;
//            }
//          });
          boolean allVerified = readFutures.stream()
              .allMatch(CompletableFuture::join);
          if (!allVerified) {
            throw new CompletionException(new RuntimeException("Verification failed, retrying..."));
          }
        })
        .exceptionally(ex -> {
          LOG.warning("Verification failed, retrying: " + ex.getMessage());
          return writeAllModbusCommands().thenRun(() -> {}).join();
        });
  }

  private CompletableFuture<Boolean> readAndCompareCommand(ModbusCommand command) {
    ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(command.address(), 1);
    return sendModbusRequest(request)
        .thenApply(response -> {
          if (response instanceof ReadHoldingRegistersResponse readResponse) {
            ByteBuf registers = readResponse.getRegisters();
            if (registers.readableBytes() >= 2) {
              int value = registers.readUnsignedShort();
              boolean matches = (value == command.value());
              LOG.info(
                  String.format(
                      "Read and verified command at address %d: expected %d, got %d",
                      command.address(), command.value(), value
                  )
              );
              return matches;
            }
            else {
              LOG.warning("Insufficient data returned for address " + command.address());
              return false;
            }
          }
          else {
            LOG.warning("Unexpected response type for address " + command.address());
            return false;
          }
        })
        .exceptionally(ex -> {
          LOG.severe(
              "Failed to read and compare command at address " + command.address() + ": " + ex
                  .getMessage()
          );
          return false;
        });
  }

  /**
   * Writes Modbus commands in batches to the specified command type.
   *
   * @param commands The list of ModbusCommand objects to be written.
   * @param commandType The type of the command being written.
   * @return A CompletableFuture representing the completion of the write operation.
   * @throws ExecutionException If an execution error occurs during the write operation.
   * @throws InterruptedException If the write operation is interrupted.
   */
  private CompletableFuture<Void> writeModbusCommands(
      List<ModbusCommand> commands,
      String commandType
  )
      throws ExecutionException,
        InterruptedException {
    commands.sort(Comparator.comparingInt(ModbusCommand::address));
    CompletableFuture<Void> futureChain = CompletableFuture.completedFuture(null);
    List<ModbusCommand> batch = new ArrayList<>();
    int batchWordSize = 0;
    int startAddress = commands.getFirst().address();
    int maxBatchSize = 120;

    for (ModbusCommand command : commands) {
      // Get the word size of the command
      int commandWordSize = getCommandWordSize(command);
      LOG.info(String.format("commandWordSize: %d", commandWordSize));
      // If the word size of the current batch plus the new command exceeds the limit,
      // write the current batch and start a new batch
      if (batchWordSize + commandWordSize >= maxBatchSize) {
        // Write the current batch into the chain call
        int finalStartAddress = startAddress;
        LOG.info(String.format("finalStartAddress: %d", finalStartAddress));

        List<ModbusCommand> finalBatch = new ArrayList<>(batch);
        futureChain = futureChain.thenCompose(
            v -> writeBatch(
                finalBatch, finalStartAddress, commandType
            )
        );
        batch.clear();
        startAddress = command.address() + commandWordSize;
        batchWordSize = 0;
      }
      batch.add(command);
      LOG.info(String.format("batch size: %d", batch.size()));
      batchWordSize += commandWordSize;
    }

    // Write the last batch if it's not empty
    if (!batch.isEmpty()) {
      int finalStartAddress = startAddress;
      LOG.info(
          String.format(
              "WRITING LAST BATCH, finalStartAddress: %d",
              startAddress
          )
      );
      futureChain = futureChain.thenCompose(
          v -> writeBatch(batch, finalStartAddress, commandType)
      );
    }

    return futureChain;
  }

  private int getCommandWordSize(ModbusCommand command) {
    return command.format() == ModbusCommand.DataFormat.DECIMAL ? 2 : 1;
  }

  private CompletableFuture<Void> writeBatch(
      List<ModbusCommand> batch, int startAddress, String commandType
  ) {
    ByteBuf values = Unpooled.buffer();
    int registerCount = 0;

    for (ModbusCommand command : batch) {
      if ("Position".equalsIgnoreCase(commandType)) {
        values.writeIntLE(command.value());
        registerCount += 2;
      }
      else {
        values.writeShort(command.value());
        registerCount += 1;
      }
      LOG.info("Writing " + commandType + " command: " + command.toLogString());
    }

    WriteMultipleRegistersRequest request = new WriteMultipleRegistersRequest(
        startAddress,
        registerCount,
        values
    );

    int finalRegisterCount = registerCount;
    return sendModbusRequest(request)
        .thenAccept(response -> {
          if (response instanceof WriteMultipleRegistersResponse) {
            LOG.info(
                "Successfully wrote " + batch.size() + " " + commandType
                    + " commands (total " + finalRegisterCount + " registers) starting at address "
                    + startAddress
            );
          }
          else {
            throw new CompletionException(
                "Unexpected response type: " + response.getClass().getSimpleName(), null
            );
          }
        })
        .exceptionally(ex -> {
          LOG.severe("Failed to write " + commandType + " registers: " + ex.getMessage());
          throw new CompletionException(ex);
        });
  }

  @Override
  protected boolean performConnection() {
    LOG.info("Connecting to Modbus TCP server at " + host + ":" + port);
    ModbusTcpMasterConfig config = new ModbusTcpMasterConfig.Builder(host)
        .setPort(port)
        .build();

    try {
      return CompletableFuture.supplyAsync(() -> {
        LOG.info("Creating new ModbusTcpMaster instance");
        return new ModbusTcpMaster(config);
      })
          .thenCompose(newMaster -> {
            this.master = newMaster;
            LOG.info("Initiating connection to Modbus TCP server");
            return newMaster.connect();
          })
          .thenRun(() -> {
            this.isConnected = true;
            LOG.info("Successfully connected to Modbus TCP server");
            getProcessModel().setCommAdapterConnected(true);
//            startHeartbeat();
//            positionUpdater.startPositionUpdates();
          })
          .exceptionally(ex -> {
            LOG.log(Level.SEVERE, "Failed to connect to Modbus TCP server", ex);
            this.isConnected = false;
            return null;
          })
          .isDone();
    }
    catch (Exception e) {
      LOG.log(Level.SEVERE, "Unexpected error during connection attempt", e);
      return false;
    }
  }

  @Override
  protected boolean performDisconnection() {
    LOG.info("Disconnecting from Modbus TCP server");
    if (master != null) {
      return master.disconnect()
          .thenRun(() -> {
            LOG.info("Successfully disconnected from Modbus TCP server");
            this.isConnected = false;
            getProcessModel().setCommAdapterConnected(false);
            this.master = null;
          })
          .exceptionally(ex -> {
            LOG.log(Level.SEVERE, "Failed to disconnect from Modbus TCP server", ex);
            return null;
          })
          .isDone();
    }
    return true;
  }

  @Override
  protected boolean isVehicleConnected() {
    return isConnected && master != null;
  }

  private CompletableFuture<ModbusResponse> sendModbusRequest(
      com.digitalpetri.modbus.requests.ModbusRequest request
  ) {
    return sendModbusRequestWithRetry(request, 3)
        .exceptionally(ex -> {
          LOG.severe("All retries failed for Modbus request: " + ex.getMessage());
          throw new CompletionException("Failed to send Modbus request after retries", ex);
        });
  }

  private CompletableFuture<ModbusResponse> sendModbusRequestWithRetry(
      com.digitalpetri.modbus.requests.ModbusRequest request,
      int retriesLeft
  ) {
    if (master == null) {
      return CompletableFuture.failedFuture(
          new IllegalStateException("Modbus master is not initialized")
      );
    }

    return CompletableFuture.supplyAsync(() -> sendRequest(request), getExecutor())
        .thenApply(this::processResponse)
        .exceptionally(ex -> {
          LOG.severe("Failed to send Modbus request: " + ex.getMessage());
          return null;
        }).thenCompose(response -> {
          boolean shouldRetry = response == null && retriesLeft > 0;
          if (shouldRetry) {
            return CompletableFuture.runAsync(() -> {
              try {
                Thread.sleep(1000);
              }
              catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            }, getExecutor())
                .thenCompose(v -> sendModbusRequestWithRetry(request, retriesLeft - 1));
          }
          return CompletableFuture.completedFuture(response);
        });
  }

  private ModbusResponse sendRequest(com.digitalpetri.modbus.requests.ModbusRequest request) {
    try {
      return master.sendRequest(request, 0).get(500, TimeUnit.MILLISECONDS);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new CompletionException("Request interrupted", e);
    }
    catch (ExecutionException e) {
      throw new CompletionException("Request failed", e.getCause());
    }
    catch (TimeoutException | java.util.concurrent.TimeoutException e) {
      throw new CompletionException("Request timed out", e);
    }
  }

  private ModbusResponse processResponse(ModbusResponse response) {
    if (response instanceof ReadHoldingRegistersResponse readResponse) {
      return handleReadHoldingRegistersResponse(readResponse);
    }
    else if (response instanceof WriteMultipleRegistersResponse writeResponse) {
      LOG.info("instanceof WriteMultipleRegistersResponse writeResponse");
      return handleWriteMultipleRegistersResponse(writeResponse);
    }
    else if (response instanceof ReadInputRegistersResponse readInputResponse) {
      return handleReadInputRegistersResponse(readInputResponse);
    }
    else if (response instanceof WriteSingleRegisterResponse writeSingleResponse) {
      return handleWriteSingleRegisterResponse(writeSingleResponse);
    }
    return response;
  }

  private ReadHoldingRegistersResponse handleReadHoldingRegistersResponse(
      ReadHoldingRegistersResponse readResponse
  ) {
    ByteBuf registers = readResponse.getRegisters();
    registers.retain();
    return new ReadHoldingRegistersResponse(registers) {
      @Override
      public boolean release() {
        boolean released = super.release();
        if (released && registers.refCnt() > 0) {
          return registers.release();
        }
        return released;
      }

      @Override
      public boolean release(int decrement) {
        boolean released = super.release(decrement);
        if (released && registers.refCnt() > 0) {
          return registers.release(decrement);
        }
        return released;
      }
    };
  }

  private WriteMultipleRegistersResponse handleWriteMultipleRegistersResponse(
      WriteMultipleRegistersResponse writeResponse
  ) {
    return writeResponse;
  }

  private ReadInputRegistersResponse handleReadInputRegistersResponse(
      ReadInputRegistersResponse readInputResponse
  ) {
    ByteBuf registers = readInputResponse.getRegisters();
    registers.retain();
    return new ReadInputRegistersResponse(registers) {
      @Override
      public boolean release() {
        boolean released = super.release();
        if (released && registers.refCnt() > 0) {
          return registers.release();
        }
        return released;
      }

      @Override
      public boolean release(int decrement) {
        boolean released = super.release(decrement);
        if (released && registers.refCnt() > 0) {
          return registers.release(decrement);
        }
        return released;
      }
    };
  }

  private WriteSingleRegisterResponse handleWriteSingleRegisterResponse(
      WriteSingleRegisterResponse writeSingleResponse
  ) {
    return writeSingleResponse;
  }

//  private ModbusResponse processResponse(ModbusResponse response) {
//    if (response instanceof ReadHoldingRegistersResponse readResponse) {
//      ByteBuf registers = readResponse.getRegisters();
//      registers.retain();
//      return new ReadHoldingRegistersResponse(registers) {
//        @Override
//        public boolean release() {
//          boolean released = super.release();
//          if (released && registers.refCnt() > 0) {
//            return registers.release();
//          }
//          return released;
//        }
//
//        @Override
//        public boolean release(int decrement) {
//          boolean released = super.release(decrement);
//          if (released && registers.refCnt() > 0) {
//            return registers.release(decrement);
//          }
//          return released;
//        }
//      };
//    }
//    return response;
//  }


  //  private CompletableFuture<ModbusResponse> sendModbusRequest(
//      com.digitalpetri.modbus.requests.ModbusRequest request
//  ) {
//    if (master == null) {
//      return CompletableFuture.failedFuture(
//          new IllegalStateException(
//              "Modbus master is not initialized"
//          )
//      );
//    }
//
//    return CompletableFuture.supplyAsync(() -> {
//      try {
//        return master.sendRequest(request, 0).get(500, TimeUnit.MILLISECONDS);
//      }
//      catch (InterruptedException e) {
//        Thread.currentThread().interrupt();
//        throw new CompletionException("Request interrupted", e);
//      }
//      catch (ExecutionException e) {
//        throw new CompletionException("Request failed", e.getCause());
//      }
//      catch (TimeoutException e) {
//        throw new CompletionException("Request timed out", e);
//      }
//      catch (java.util.concurrent.TimeoutException e) {
//        throw new RuntimeException("Concurrent request timed out", e);
//      }
//    }, getExecutor()).thenApply(response -> {
//      if (response instanceof ReadHoldingRegistersResponse readResponse) {
//        ByteBuf registers = readResponse.getRegisters();
//        try {
//          return readResponse;
//        } finally {
//          registers.release();
//        }
//      }
//      return response;
//    }).exceptionally(ex -> {
//      LOG.severe("Failed to send Modbus request: " + ex.getMessage());
//      return null;
//    });
//  }
//
//  private CompletableFuture<ModbusResponse> retrySendModbusRequest(
//      com.digitalpetri.modbus.requests.ModbusRequest request,
//      int retries,
//      long delay
//  ) {
//    if (retries <= 0) {
//      return CompletableFuture.failedFuture(new RuntimeException("Max retries reached"));
//    }
//
//    return CompletableFuture.supplyAsync(() -> {
//      try {
//        Thread.sleep(delay);
//      }
//      catch (InterruptedException e) {
//        Thread.currentThread().interrupt();
//        throw new CompletionException(e);
//      }
//      return sendModbusRequest(request);
//    }, getExecutor()).thenCompose(future -> future.handle((response, ex) -> {
//      if (ex != null) {
//        LOG.warning("Retry failed: " + ex.toString() + ". Retries left: " + (retries - 1));
//        return retrySendModbusRequest(request, retries - 1, delay * 2);
//      }
//      return CompletableFuture.completedFuture(response);
//    })).thenCompose(innerFuture -> innerFuture);
//  }

  @Override
  @Nonnull
  public synchronized ExplainedBoolean canProcess(
      @Nonnull
      TransportOrder order
  ) {
    requireNonNull(order, "order");

    return canProcess(
        order.getFutureDriveOrders().stream()
            .map(driveOrder -> driveOrder.getDestination().getOperation())
            .collect(Collectors.toList())
    );
  }

  private ExplainedBoolean canProcess(List<String> operations) {
    requireNonNull(operations, "operations");

    LOG.info(String.format("%s: Checking process ability of %s...", getName(), operations));
    boolean canProcess = true;
    String reason = "";

    // Do NOT require the vehicle to be IDLE or CHARGING here!
    // That would mean a vehicle moving to a parking position or recharging location would always
    // have to finish that order first, which would render a transport order's dispensable flag
    // useless.
    boolean loaded = loadState == LoadState.FULL;
    Iterator<String> opIter = operations.iterator();
    while (canProcess && opIter.hasNext()) {
      final String nextOp = opIter.next();
      // If we're loaded, we cannot load another piece, but could unload.
      if (loaded) {
        if (nextOp.startsWith(getProcessModel().getLoadOperation())) {
          canProcess = false;
          reason = LOAD_OPERATION_CONFLICT;
        }
        else if (nextOp.startsWith(getProcessModel().getUnloadOperation())) {
          loaded = false;
        }
      } // If we're not loaded, we could load, but not unload.
      else if (nextOp.startsWith(getProcessModel().getLoadOperation())) {
        loaded = true;
      }
      else if (nextOp.startsWith(getProcessModel().getUnloadOperation())) {
        canProcess = false;
        reason = UNLOAD_OPERATION_CONFLICT;
      }
    }
    if (!canProcess) {
      LOG.info(String.format("%s: Cannot process %s, reason: '%s'", getName(), operations, reason));
    }
    return new ExplainedBoolean(canProcess, reason);
  }

  @Override
  public void processMessage(@Nullable
  Object message) {
    LOG.info("Received message: " + message);
    // Implement specific message processing logic
  }

  /**
   * null.
   */
  @Override
  protected void updateVehiclePosition(String position) {
    getProcessModel().setPosition(position);
  }

  /**
   * null.
   */
  @Override
  protected void updateVehicleState() {

  }

  @Override
  protected VehicleProcessModelTO createCustomTransferableProcessModel() {
    return new CustomProcessModelTO()
        .setLoadOperation(getProcessModel().getLoadOperation())
        .setUnloadOperation(getProcessModel().getUnloadOperation())
        .setMaxAcceleration(getProcessModel().getMaxAcceleration())
        .setMaxDeceleration(getProcessModel().getMaxDecceleration())
        .setMaxFwdVelocity(getProcessModel().getMaxFwdVelocity())
        .setMaxRevVelocity(getProcessModel().getMaxRevVelocity());
  }

  private record ModbusCommand(String name, int value, int address, DataFormat format) {
    public enum DataFormat {
      DECIMAL,
      HEXADECIMAL
    }

    public ModbusCommand {
      if (value < 0) {
        throw new IllegalArgumentException("Value cannot be negative");
      }
    }

    /**
     * Returns the string representation of the value based on its format.
     *
     * @return A string representation of the value.
     */
    public String getFormattedValue() {
      return switch (format) {
        case DECIMAL -> String.valueOf(value);
        case HEXADECIMAL -> String.format("%04X", value);
      };
    }

    public String toLogString() {
      return String.format(
          "ModbusCommand{name='%s', value=%s, address=%d, format=%s}",
          name, getFormattedValue(), address, format
      );
    }
  }

  /**
   * The vehicle's possible load states.
   */
  private enum LoadState {
    EMPTY,
    FULL;
  }

  public class PositionUpdater {
    private static final int UPDATE_INTERVAL = 500;
    private static final int MAX_INTERPOLATION_TIME = 500;
    private static final int POSITION_REGISTER_ADDRESS = 109;

    private final VehicleProcessModel processModel;
    private final ScheduledExecutorService executor;
    private ScheduledFuture<?> positionFuture;

    private String lastKnownPosition;
    //    private Triple lastPrecisePosition;
//    private double lastOrientation;
//    private double estimatedSpeed; // mm/s

    /**
     * The PositionUpdater class is responsible for updating the position of a vehicle.
     * It schedules regular updates of the vehicle position using a fixed rate.
     * The position updates are executed by invoking the updatePosition() method.
     * <p>
     * public PositionUpdater(VehicleProcessModel processModel, ScheduledExecutorService executor)
     * <p>
     * Constructor for the PositionUpdater class. Initializes the PositionUpdater object
     * with the provided processModel and executor objects.
     *
     * @param processModel The VehicleProcessModel object associated with the vehicle.
     * @param executor The ScheduledExecutorService used to schedule position updates.
     */
    public PositionUpdater(VehicleProcessModel processModel, ScheduledExecutorService executor) {
      this.processModel = processModel;
      this.executor = executor;
      this.lastKnownPosition = null;
    }

    /**
     * Starts position updates for the vehicle.
     * This method schedules regular updates of the vehicle position using a fixed rate.
     * The position updates are executed by invoking the updatePosition() method.
     * The initial delay is 0, and the update interval is configured by the UPDATE_INTERVAL field.
     */
    public void startPositionUpdates() {
      positionFuture = executor.scheduleAtFixedRate(
          this::updatePosition,
          0,
          UPDATE_INTERVAL,
          TimeUnit.MILLISECONDS
      );
    }

    private void stopPositionUpdates() {
      if (positionFuture != null && !positionFuture.isCancelled()) {
        positionFuture.cancel(true);
      }
    }

    private void updatePosition() {
      // Reads the station MK instead of precise position.
      readSingleRegister(POSITION_REGISTER_ADDRESS)
          .thenAccept(this::processPositionUpdate)
          .exceptionally(ex -> {
            LOG.warning("Failed to update position: " + ex.getMessage());
//            interpolatePosition();
            return null;
          });
    }

    private void processPositionUpdate(long stationMark) {
      long currentTime = System.currentTimeMillis();
      long currentPosition = getPositionFromStationModbusCommand(stationMark);

      String openTcsPosition = convertToOpenTcsPosition(stationMark);
      Triple precisePosition = convertToPrecisePosition(currentPosition);

//      if (lastUpdateTime > 0) {
//        long timeDiff = currentTime - lastUpdateTime;
//        long posDiff = currentPosition - lastPosition;
//        estimatedSpeed = (double) posDiff / timeDiff * 1000;
//      }

      processModel.setPosition(openTcsPosition);
      processModel.setPrecisePosition(precisePosition);

//      lastPrecisePosition = precisePosition;
    }

//    private void interpolatePosition() {
//      long currentTime = System.currentTimeMillis();
//      long timeSinceLastUpdate = currentTime - lastUpdateTime;
//
//      if (timeSinceLastUpdate > MAX_INTERPOLATION_TIME) {
//        // If the maximum interpolation time is exceeded, no interpolation will be performed
//        return;
//      }
//
//      // Calculate the interpolated position
//      double interpolationFactor = (double) timeSinceLastUpdate / UPDATE_INTERVAL;
//      long interpolatedPosition = lastPosition + (long) (estimatedSpeed * interpolationFactor);
//
//      String openTcsPosition = convertToOpenTcsPosition(interpolatedPosition);
//      Triple interpolatedPrecisePosition = interpolatePrecisePosition(interpolationFactor);
//
//      processModel.setPosition(openTcsPosition);
//      processModel.setPrecisePosition(interpolatedPrecisePosition);
//    }

//    private Triple interpolatePrecisePosition(double factor) {
//      if (lastPrecisePosition == null) {
//        return null;
//      }
//      long dx = (long) (estimatedSpeed * factor * Math.cos(Math.toRadians(lastOrientation)));
//      long dy = (long) (estimatedSpeed * factor * Math.sin(Math.toRadians(lastOrientation)));
//      return new Triple(
//          lastPrecisePosition.getX() + dx,
//          lastPrecisePosition.getY() + dy,
//          lastPrecisePosition.getZ()
//      );
//    }

    private String convertToOpenTcsPosition(long position) {
      LOG.info(
          String.format(
              "GOT POSITION FROM MAP: %s",
              getPositionFromMap(getPositionFromStationModbusCommand(position))
          )
      );

      return getPositionFromMap(getPositionFromStationModbusCommand(position));
    }

    private long getPositionFromStationModbusCommand(long index) {
      if (index < 0 || index >= positionModbusCommand.size()) {
        throw new IllegalArgumentException("Index out of positionModbusCommand bounds");
      }
      return positionModbusCommand.get((int) index).value();
    }

    /**
     * Retrieves the position associated with the given precise position.
     *
     * @param precisePosition The precise position for which to retrieve the position.
     * @return The position associated with the given precise position. Returns the last known
     * position
     * if no position is found in the map.
     */
    private String getPositionFromMap(Long precisePosition) {
      String position = positionMap.get(precisePosition);
      if (position != null) {
        positionUpdater.lastKnownPosition = position;
        getProcessModel().setPosition(position);
        return position;
      }
      else {
        return positionUpdater.lastKnownPosition;
      }
    }

    private Triple convertToPrecisePosition(long position) {
      // Implement conversion from original position value to Triple
      return new Triple(position, 0, 0);
    }
  }
}
