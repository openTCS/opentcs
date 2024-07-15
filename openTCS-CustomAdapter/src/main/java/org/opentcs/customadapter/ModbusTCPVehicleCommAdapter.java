package org.opentcs.customadapter;

import static java.util.Objects.requireNonNull;

import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
import com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest;
import com.digitalpetri.modbus.responses.ModbusResponse;
import com.google.inject.assistedinject.Assisted;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.jgrapht.alg.util.Pair;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralOperation;
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
   * Map of Modbus register address and function.
   */
  private static final Map<String, ModbusTCPVehicleCommAdapter.ModbusRegister> REGISTER_MAP
      = new HashMap<>();
  /**
   * MAP1: absolute distance (mm) -> station name.
   */
  private final Map<Integer, String> distanceToStation = new HashMap<>();
  private final Map<String, Integer> stationToDistance = new HashMap<>();
  /**
   * MAP2: station name -> <CMD1, CMD2>.
   */
  private Map<String, Pair<CMD1, CMD2>> stationCommandsMap = new ConcurrentHashMap<>();
  private TransportOrder currentTransportOrder;
  private List<MovementCommand> allMovementCommands = new ArrayList<>();
  /**
   * Represents a vehicle associated with a ModbusTCPVehicleCommAdapter.
   */
  private final Vehicle vehicle;
  /**
   * The vehicle's load state.
   */
  private LoadState loadState = LoadState.EMPTY;
  /**
   * Map that stores pairs of CMD1 and CMD2 objects associated with station names.
   */
  private final Map<String, Pair<CMD1, CMD2>> stationToCommands = new HashMap<>();
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
   * The maximum velocity for the vehicle.
   */
  private int maxVelocity;
  /**
   * The current velocity of the vehicle.
   */
  private int currentVelocity;
  /**
   * Represents a Modbus TCP master used for communication with Modbus TCP devices.
   */
  private ModbusTcpMaster master;
  /**
   * Represents the state of a variable indicating whether it has been initialized.
   */
  private boolean initialized;

  /**
   * Initializes a new instance of ModbusTCPVehicleCommAdapter.
   *
   * @param processModel The process model associated with the vehicle.
   * @param rechargeOperation The name of the recharge operation.
   * @param commandsCapacity The maximum capacity for storing commands.
   * @param executor The executor for scheduled tasks.
   * @param vehicle The vehicle this adapter is associated with.
   * @param host The host address for the TCP connection.
   * @param port The port number for the TCP connection.
   */
  @SuppressWarnings("checkstyle:TodoComment")
  public ModbusTCPVehicleCommAdapter(
      VehicleProcessModel processModel,
      String rechargeOperation,
      int commandsCapacity,
      @KernelExecutor
      ScheduledExecutorService executor,
      @Assisted
      Vehicle vehicle,
      String host,
      int port
  ) {
    super(processModel, rechargeOperation, commandsCapacity, executor);
    this.host = host;
    this.port = port;
    this.vehicle = requireNonNull(vehicle, "vehicle");
    this.isConnected = false;
    this.currentVelocity = 0;
    this.currentTransportOrder = null;

    // Initialize VelocityController with default values unit: meter
    // TODO: avoid magic number here.
    double maxAcceleration = 1.6;
    double maxDeceleration = -1.6;
    double maxFwdVelocity = 2.4;
    double maxRevVelocity = -2.4;

  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }
    super.initialize();
    initializeDistanceToStationMap();
    getProcessModel().setState(Vehicle.State.IDLE);
    getProcessModel().setLoadHandlingDevices(
        Arrays.asList(new LoadHandlingDevice(LHD_NAME, false))
    );
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
    initialized = false;
  }

  /**
   * Processes updates of the {@link CustomProcessModel}.
   *
   * <p><em>Overriding methods should also call this.</em></p>
   *
   * @param evt The property change event published by the model.
   */
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
          && getProcessModel().getLoadHandlingDevices().get(0).isFull()) {
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
   * Initializes the map named map1 with integer keys and string values.
   * The keys start from 1000 and increment by 1000 up to 100000, and the corresponding values are
   * generated by concatenating "MK" with the key divided by 1000.
   */
  // TODO: rename, can change the map definination using DI.
  private void initializeDistanceToStationMap() {
//    for (int i = 1000; i <= 100000; i += 1000) {
//      distanceToStation.put(i, "MK" + (i / 1000));
//    }
    for (int i = 1000; i <= 100000; i += 1000) {
      stationToDistance.put("MK" + (i / 1000), i);
    }
    LOG.info(String.format("Size of map: {}", stationToDistance.size()));
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
  public synchronized boolean enqueueCommand(MovementCommand newCommand) {
    requireNonNull(newCommand, "newCommand");

    if (!canAcceptNextCommand()) {
      return false;
    }

    if (currentTransportOrder == null || !currentTransportOrder.equals(
        newCommand.getTransportOrder()
    )) {
      currentTransportOrder = newCommand.getTransportOrder();
      allMovementCommands.clear();
    }

    allMovementCommands.add(newCommand);
    LOG.info(
        String.format(
            "{}: Custom Adding command: {}, Current Movement Pool Size: {}", getName(), newCommand,
            allMovementCommands.size()
        )
    );

    getUnsentCommands().add(newCommand);
    getProcessModel().commandEnqueued(newCommand);

    if (newCommand.isFinalMovement()) {
      processAllMovementCommands();
      allMovementCommands.clear();
    }
    return true;
  }

  @Override
  protected void sendSpecificCommand(MovementCommand cmd) {
    if (!isVehicleConnected()) {
      LOG.warning("Not connected to Modbus TCP server. Cannot send command.");
      return;
    }

    if (!allMovementCommands.contains(cmd)) {
      LOG.warning(String.format("{}: Command {} is NOT in MovementCommands pool.", getName(), cmd));
      // TODO: open this comment back after testing.
      // getProcessModel().commandFailed(cmd);
    }
  }

  private void processAllMovementCommands() {
    LOG.info(
        String.format(
            "Processing all movement commands for TransportOrder: %s", currentTransportOrder
                .getName()
        )
    );
    List<ModbusCommand> modbusCommands = convertMovementCommandsToModbusCommands(
        allMovementCommands
    );
    writeAllModbusCommands(modbusCommands);
    allMovementCommands.clear();
    currentTransportOrder = null;
  }

  // TODO: Important part of command converting.
  private List<ModbusCommand> convertMovementCommandsToModbusCommands(
      List<MovementCommand> commands
  ) {
    stationCommandsMap.clear();
    String station = "";
    String startPoint = "";
    List<ModbusCommand> modbusCommands = new ArrayList<>();
    processMovementCommands(commands);

    // Convert stationCommandsMap to ModbusCommand list
    for (Map.Entry<String, Pair<CMD1, CMD2>> entry : stationCommandsMap.entrySet()) {
      String stationName = entry.getKey();
      Pair<CMD1, CMD2> cmds = entry.getValue();

      // Assuming station names are in the format "MKxx" where xx is the station number
      int stationNumber = Integer.parseInt(stationName.substring(2));
      int baseAddress = 1000 + (2 * stationNumber) - 2;

      // Create ModbusCommand for CMD1
      modbusCommands.add(new ModbusCommand("CMD1", cmds.getFirst().toInt(), baseAddress));

      // Create ModbusCommand for CMD2
      modbusCommands.add(new ModbusCommand("CMD2", cmds.getSecond().toInt(), baseAddress + 1));
    }
    return modbusCommands;
  }

  private void processMovementCommands(List<MovementCommand> commands) {
    String startPoint;
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
      // TODO: make sure all points follow the TOYO naming style.
      startPoint = cmd.getStep().getSourcePoint().getName();
      Pair<CMD1, CMD2> pairCommands = new Pair<>(createCMD1(cmd), createCMD2(cmd));
      stationCommandsMap.put(startPoint, pairCommands);
    }
  }

  @SuppressWarnings("checkstyle:TodoComment")
  private CMD1 createCMD1(MovementCommand cmd) {
    int liftCmd = 0;
    int speedLevel = 0;
    int obstacleSensor = 5;
    double maxSpeed = 0;
    String command = cmd.getFinalOperation();
    liftCmd = getLiftCommand(command);
    if (cmd.getStep().getPath() != null) {
      maxSpeed = getMaxAllowedSpeed(cmd.getStep().getPath());
    }
    int speedCase = (int) (maxSpeed * 5);
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
    // TODO: horizontalCommand & guideTimeoutCommand
    String switchOperation = "";
    int horizontalCommand = 0;
    int guideTimeoutCommand = 0;
    int motionCommand = 0;
    List<PeripheralOperation> peripheralOperations = null;

    if (cmd.getStep().getPath() != null) {
      peripheralOperations = cmd.getStep().getPath().getPeripheralOperations();
    }
    if (peripheralOperations != null && !peripheralOperations.isEmpty()) {
      switchOperation = peripheralOperations.getFirst().getOperation();
    }
    motionCommand = switch (switchOperation) {
      case "switchMiddle" -> 0;
      case "switchLeft" -> 1;
      case "switchRight" -> 2;
      default -> 0;
    };
    return new CMD2(horizontalCommand, guideTimeoutCommand, motionCommand);
  }

  private double getMaxAllowedSpeed(Path path) {
    double maxFwdVelocity = getProcessModel().getMaxFwdVelocity();
    double maxRevVelocity = path.getMaxReverseVelocity();

    return Math.min(maxFwdVelocity, maxRevVelocity);
  }

  private static int getLiftCommand(String command) {
    return switch (command) {
      case "Load" -> 2;
      case "Unload" -> 1;
      default -> 1;
    };
  }

  private void writeAllModbusCommands(List<ModbusCommand> commands) {
    int startAddress = 1000;
    int quantity = commands.size();

    ByteBuf values = Unpooled.buffer(quantity * 2);

    for (ModbusCommand command : commands) {
      values.writeShort(command.value());
    }

    WriteMultipleRegistersRequest request = new WriteMultipleRegistersRequest(
        startAddress,
        quantity,
        values
    );

    sendModbusRequest(request)
        .thenAccept(response -> {
          LOG.info("All commands written successfully");
          values.release();
        })
        .exceptionally(ex -> {
          LOG.severe("Failed to write commands: " + ex.getMessage());
          values.release();
          return null;
        });
  }

  private void sendModbusCommand(String command, int value) {
    ModbusRegister register = REGISTER_MAP.get(command);
    if (register == null || register.function() != ModbusFunction.WRITE_MULTIPLE_REGISTERS) {
      LOG.warning("Invalid command or function: " + command);
      return;
    }

    ByteBuf buffer = Unpooled.buffer(2);
    buffer.writeShort(value);

    sendModbusRequest(new WriteMultipleRegistersRequest(register.address(), 1, buffer))
        .thenAccept(response -> LOG.info(command + " set successfully"))
        .exceptionally(throwable -> {
          LOG.log(Level.SEVERE, "Failed to set " + command, throwable);
          return null;
        });
  }

  @Override
  protected boolean performConnection() {
    LOG.info("Connecting to Modbus TCP server at " + host + ":" + port);
    ModbusTcpMasterConfig config = new ModbusTcpMasterConfig.Builder(host)
        .setPort(port)
        .build();

    return CompletableFuture.supplyAsync(() -> new ModbusTcpMaster(config))
        .thenCompose(newMaster -> {
          this.master = newMaster;
          return newMaster.connect();
        })
        .thenRun(() -> {
          this.isConnected = true;
          LOG.info("Successfully connected to Modbus TCP server");
          getProcessModel().setCommAdapterConnected(true);
        })
        .exceptionally(ex -> {
          LOG.log(Level.SEVERE, "Failed to connect to Modbus TCP server", ex);
          this.isConnected = false;
          return null;
        })
        .isDone();
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
    return master.sendRequest(request, 0);
  }

  @Override
  @Nonnull
  public synchronized ExplainedBoolean canProcess(TransportOrder order) {
    requireNonNull(order, "order");

    return canProcess(
        order.getFutureDriveOrders().stream()
            .map(driveOrder -> driveOrder.getDestination().getOperation())
            .collect(Collectors.toList())
    );
  }

  private ExplainedBoolean canProcess(List<String> operations) {
    requireNonNull(operations, "operations");

    LOG.info(String.format("{}: Checking processability of {}...", getName(), operations));
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
      LOG.info(String.format("{}: Cannot process {}, reason: '{}'", getName(), operations, reason));
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

  private double maxVelocity(Route.Step step) {
    return (step.getVehicleOrientation() == Vehicle.Orientation.BACKWARD)
        ? (double) step.getPath().getMaxReverseVelocity()
        : (double) step.getPath().getMaxVelocity();
  }

  static {
    // OHT movement handshake position - status (0x04)
    REGISTER_MAP.put("HEART_BIT", new ModbusRegister(300, ModbusFunction.READ_INPUT_REGISTERS));
    REGISTER_MAP.put("DIRECTION", new ModbusRegister(301, ModbusFunction.READ_INPUT_REGISTERS));
    REGISTER_MAP.put("FORK", new ModbusRegister(302, ModbusFunction.READ_INPUT_REGISTERS));
    REGISTER_MAP.put("SPEED", new ModbusRegister(303, ModbusFunction.READ_INPUT_REGISTERS));
    REGISTER_MAP.put("OBSTACLE", new ModbusRegister(304, ModbusFunction.READ_INPUT_REGISTERS));
    REGISTER_MAP.put("STATUS", new ModbusRegister(305, ModbusFunction.READ_INPUT_REGISTERS));
    REGISTER_MAP.put("ERROR_CODE", new ModbusRegister(306, ModbusFunction.READ_INPUT_REGISTERS));
    REGISTER_MAP.put("DESTINATION", new ModbusRegister(308, ModbusFunction.READ_INPUT_REGISTERS));
    REGISTER_MAP.put("MARK_NO", new ModbusRegister(309, ModbusFunction.READ_INPUT_REGISTERS));
    REGISTER_MAP.put("POSITION", new ModbusRegister(310, ModbusFunction.READ_INPUT_REGISTERS));
    REGISTER_MAP.put("IO_IN", new ModbusRegister(318, ModbusFunction.READ_INPUT_REGISTERS));
    REGISTER_MAP.put("IO_OUT", new ModbusRegister(319, ModbusFunction.READ_INPUT_REGISTERS));

    // OHT movement handshake position - command (0x03, 0x10)
    REGISTER_MAP.put(
        "SET_HEART_BIT", new ModbusRegister(300, ModbusFunction.WRITE_MULTIPLE_REGISTERS)
    );
    REGISTER_MAP.put(
        "SET_DIRECTION", new ModbusRegister(301, ModbusFunction.WRITE_MULTIPLE_REGISTERS)
    );
    REGISTER_MAP.put(
        "SET_FORK", new ModbusRegister(
            302,
            ModbusFunction.WRITE_MULTIPLE_REGISTERS
        )
    );
    REGISTER_MAP.put(
        "SET_SPEED", new ModbusRegister(
            303,
            ModbusFunction.WRITE_MULTIPLE_REGISTERS
        )
    );
    REGISTER_MAP.put(
        "SET_OBSTACLE", new ModbusRegister(304, ModbusFunction.WRITE_MULTIPLE_REGISTERS)
    );
    REGISTER_MAP.put(
        "SET_COMMAND", new ModbusRegister(305, ModbusFunction.WRITE_MULTIPLE_REGISTERS)
    );
    REGISTER_MAP.put(
        "SET_MODE", new ModbusRegister(
            306,
            ModbusFunction.WRITE_MULTIPLE_REGISTERS
        )
    );
    REGISTER_MAP.put(
        "SET_DESTINATION", new ModbusRegister(308, ModbusFunction.WRITE_MULTIPLE_REGISTERS)
    );
    REGISTER_MAP.put(
        "SET_JOG_MOVE", new ModbusRegister(312, ModbusFunction.WRITE_MULTIPLE_REGISTERS)
    );
  }

  private enum ModbusFunction {
//    READ_COILS(0x01),
//    READ_DISCRETE_INPUTS(0x02),
//    READ_HOLDING_REGISTERS(0x03),
    READ_INPUT_REGISTERS(0x04),
//    WRITE_SINGLE_COIL(0x05),
//    WRITE_SINGLE_REGISTER(0x06),
//    WRITE_MULTIPLE_COILS(0x0F),
    WRITE_MULTIPLE_REGISTERS(0x10);

    private final int functionCode;

    ModbusFunction(int functionCode) {
      this.functionCode = functionCode;
    }

    public int getFunctionCode() {
      return functionCode;
    }
  }

  private record ModbusRegister(int address, ModbusFunction function) {
  }

  private record ModbusCommand(String name, int value, int address) {
    public ModbusCommand {
      if (value < 0) {
        throw new IllegalArgumentException("Value cannot be negative");
      }
    }

    public String toLogString() {
      return String.format("ModbusCommand{name='%s', value=%d, address=%d}", name, value, address);
    }
  }

  /**
   * The vehicle's possible load states.
   */
  private enum LoadState {
    EMPTY,
    FULL;
  }
}
