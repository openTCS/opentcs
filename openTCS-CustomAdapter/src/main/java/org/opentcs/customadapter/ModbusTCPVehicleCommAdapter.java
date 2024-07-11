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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.jgrapht.alg.util.Pair;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;
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
   * Map of Modbus register address and function.
   */
  private static final Map<String, ModbusTCPVehicleCommAdapter.ModbusRegister> REGISTER_MAP
      = new HashMap<>();
  /**
   * MAP1: absolute distance (mm) -> station name.
   */
  private final Map<Integer, String> distanceToStation = new HashMap<>();
  /**
   * MAP2: station name -> <CMD1, CMD2>.
   */
  private Map<String, Pair<CMD1, CMD2>> map2 = new ConcurrentHashMap<>();
  /**
   * MovementCommand pool.
   */
  private Set<MovementCommand> mcPool = new HashSet<>();

  private TransportOrder currentTransportOrder = null;
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
    for (int i = 1000; i <= 100000; i += 1000) {
      distanceToStation.put(i, "MK" + (i / 1000));
    }
    LOG.info(String.format("Size of map: {}", distanceToStation.size()));
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
    }
    return true;
  }

  @Override
  protected void sendSpecificCommand(MovementCommand cmd) {
    if (!isVehicleConnected()) {
      LOG.warning("Not connected to Modbus TCP server. Cannot send command.");
      return;
    }

//    if (mcPool.isEmpty()) {
//      initializeMCPool(cmd);
//    }
//
//    if (mcPool.contains(cmd)) {
//      LOG.info("Skipping already processed command: ");
//      return;
//    }
//
//    mcPool.add(cmd);
//    processMovementCommand(cmd);
//
//    if (mcPool.size() == getSentCommands().size() + getUnsentCommands().size()) {
//      writeAllModbusCommands();
//    }
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
    writeModbusCommands(modbusCommands);
    allMovementCommands.clear();
    currentTransportOrder = null;
//    Point destination = cmd.getStep().getDestinationPoint();
//    String stationName = map1.get(Integer.parseInt(destination.getName()));
//    if (stationName == null) {
//      throw new IllegalArgumentException("Cannot find station for the given destination point.");
//    }
//
//    CMD1 cmd1 = createCMD1(cmd);
//    CMD2 cmd2 = createCMD2(cmd);
//
//    map2.put(stationName, new Pair<>(cmd1, cmd2));
  }

  // TODO: Important part of command converting.
  private List<ModbusCommand> convertMovementCommandsToModbusCommands(
      List<MovementCommand> commands
  ) {
    List<ModbusCommand> modbusCommands = new ArrayList<>();

    for (MovementCommand cmd : commands) {
      Point destination = cmd.getStep().getDestinationPoint();
      String stationName = distanceToStation.get((int) destination.getPose().getPosition().getX());
      if (stationName == null) {
        throw new IllegalArgumentException("Cannot find station for the given destination point.");
      }

      CMD1 cmd1 = createCMD1(cmd);
      CMD2 cmd2 = createCMD2(cmd);

      int position = Integer.parseInt(stationName.substring(2)) * 1000;
      modbusCommands.add(new ModbusCommand("CMD1", cmd1.toInt(), position));
      modbusCommands.add(new ModbusCommand("CMD2", cmd2.toInt(), position + 1));
    }

    return modbusCommands;
  }

  private void writeAllModbusCommands() {
    List<ModbusCommand> commands = new ArrayList<>();
    for (Map.Entry<String, Pair<CMD1, CMD2>> entry : map2.entrySet()) {
      String stationName = entry.getKey();
      Pair<CMD1, CMD2> cmds = entry.getValue();
      int position = Integer.parseInt(stationName.substring(2)) * 1000;
      commands.add(new ModbusCommand("CMD1", cmds.getFirst().toInt(), position));
      commands.add(new ModbusCommand("CMD2", cmds.getSecond().toInt(), position + 1));
    }
    writeModbusCommands(commands);
    mcPool.clear();
    map2.clear();
  }

  @SuppressWarnings("checkstyle:TodoComment")
  private CMD1 createCMD1(MovementCommand cmd) {
    int liftCmd = 0;
    int speedLevel = 0;
    int obstacleSensor = 0;
    String command = cmd.getFinalOperation();

    // TODO: load operation not only have up and down properties.
    if(command.equals("Load")) {
      liftCmd = 2;
    }
    else liftCmd = 1;

    // TODO: MAKE IT REAL
    return new CMD1(

        liftCmd, speedLevel, obstacleSensor, cmd.getStep().getVehicleOrientation()
            == Vehicle.Orientation.FORWARD ? 0 : 1
    );
  }

  private CMD2 createCMD2(MovementCommand cmd) {
    cmd.getFinalOperation()
    // TODO: MAKE IT REAL
    return new CMD2(0, 0, 2);
  }

  private void writeModbusCommands(List<ModbusCommand> commands) {
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

  private double getMaxAllowedSpeed(Path path) {
    // TODO:
    return path.getMaxReverseVelocity();
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

//  @Override
//  protected void updateVehiclePosition() {
//    if (!isVehicleConnected()) {
//      LOG.warning("Not connected to Modbus TCP server. Cannot update position.");
//      return;
//    }
//
//    sendModbusRequest(new ReadHoldingRegistersRequest(REGISTER_MAP.get("POSITION").address(), 1))
//        .thenAccept(response -> {
//          if (response instanceof ReadHoldingRegistersResponse holdingRegistersResponse) {
//            ByteBuf buffer = holdingRegistersResponse.getRegisters();
//            try {
//              if (buffer.readableBytes() >= 2) {
//                int position = buffer.readShort();
//                getProcessModel().setPosition(String.valueOf(position));
//                LOG.info("Updated vehicle position: " + position);
//              }
//              else {
//                LOG.warning("Insufficient data in response for POSITION register");
//              }
//            }
//            finally {
//              buffer.release();
//            }
//          }
//          else {
//            LOG.warning(
//                "Unexpected response type for POSITION: " + response.getClass().getSimpleName()
//            );
//          }
//        })
//        .exceptionally(throwable -> {
//          LOG.log(Level.SEVERE, "Failed to read vehicle position", throwable);
//          return null;
//        });
//  }
//
//  @Override
//  protected void updateVehicleState() {
//    if (!isVehicleConnected()) {
//      LOG.warning("Not connected to Modbus TCP server. Cannot update state.");
//      return;
//    }
//
//    sendModbusRequest(new ReadHoldingRegistersRequest(REGISTER_MAP.get("STATUS").address(), 1))
//        .thenAccept(response -> {
//          if (response instanceof ReadHoldingRegistersResponse holdingRegistersResponse) {
//            ByteBuf buffer = holdingRegistersResponse.getRegisters();
//            try {
//              if (buffer.readableBytes() >= 2) {
//                int state = buffer.readShort();
//                Vehicle.State newState = mapModbusStateToVehicleState(state);
//                getProcessModel().setState(newState);
//              }
//              else {
//                LOG.warning("Insufficient data in response for STATUS register");
//              }
//            }
//            finally {
//              buffer.release();
//            }
//          }
//          else {
//            LOG.warning(
//                "Unexpected response type for STATUS: " + response.getClass().getSimpleName()
//            );
//          }
//        })
//        .exceptionally(throwable -> {
//          LOG.log(Level.SEVERE, "Failed to read vehicle state", throwable);
//          return null;
//        });
//
//    // Read current speed from Modbus (register 303)
//    sendModbusRequest(new ReadHoldingRegistersRequest(303, 1))
//        .thenAccept(response -> {
//          if (response instanceof ReadHoldingRegistersResponse holdingRegistersResponse) {
//            ByteBuf buffer = holdingRegistersResponse.getRegisters();
//            try {
//              if (buffer.readableBytes() >= 2) {
//                int currentSpeed = buffer.readShort();
//                velocityController.setCurrentVelocity(currentSpeed);
//                LOG.info("Current vehicle speed: " + currentSpeed);
//              }
//              else {
//                LOG.warning("Insufficient data in response for SPEED register");
//              }
//            }
//            finally {
//              buffer.release();
//            }
//          }
//          else {
//            LOG.warning(
//                "Unexpected response type for SPEED: " + response.getClass().getSimpleName()
//            );
//          }
//        })
//        .exceptionally(throwable -> {
//          LOG.log(Level.SEVERE, "Failed to read current speed", throwable);
//          return null;
//        });
//  }
//
//  private Vehicle.State mapModbusStateToVehicleState(int modbusState) {
//    return switch (modbusState) {
//      case 0 -> Vehicle.State.IDLE;
//      case 1 -> Vehicle.State.EXECUTING;
//      case 2 -> Vehicle.State.CHARGING;
//      default -> Vehicle.State.UNKNOWN;
//    };
//  }

  private CompletableFuture<ModbusResponse> sendModbusRequest(
      com.digitalpetri.modbus.requests.ModbusRequest request
  ) {
    return master.sendRequest(request, 0);
  }

  @Override
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

  /**
   * Updates the vehicle's speed.
   *
   * @param newSpeed The new speed value to set.
   */
  // Add a method to update the vehicle's speed
  public void updateVehicleSpeed(int newSpeed) {
    currentVelocity = newSpeed;
    // TODO: overload a double type parameter version of sendModbusCommand for speed.
    sendModbusCommand("SET_SPEED", currentVelocity);
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
