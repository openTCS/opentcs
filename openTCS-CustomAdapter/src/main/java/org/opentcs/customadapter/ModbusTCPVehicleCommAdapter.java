package org.opentcs.customadapter;

import static java.util.Objects.requireNonNull;

import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
import com.digitalpetri.modbus.requests.ReadHoldingRegistersRequest;
import com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest;
import com.digitalpetri.modbus.responses.ModbusResponse;
import com.digitalpetri.modbus.responses.ReadHoldingRegistersResponse;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgrapht.alg.util.Pair;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
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
  private final Map<Integer, String> map1 = new HashMap<>();
  /**
   * MAP2: station name -> <CMD1, CMD2>.
   */
  private Map<String, Pair<CMD1, CMD2>> map2 = new ConcurrentHashMap<>();
  /**
   * MovementCommand pool.
   */
  private Set<MovementCommand> mcPool = new HashSet<>();
  /**
   * Represents a vehicle associated with a ModbusTCPVehicleCommAdapter.
   */
  private final Vehicle vehicle;
  /**
   * The vehicle's load state.
   */
  private LoadState loadState = LoadState.EMPTY;
  /**
   * Map that represents the mapping between positions and stations.
   * The key of the map is the position (integer) and the value is the corresponding station
   * (string).
   */
  private final Map<Integer, String> positionToStation = new HashMap<>();
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
   * The VelocityController class represents a controller for managing the velocity of a vehicle.
   * It allows setting and retrieving the current velocity, as well as configuring the
   * maximum acceleration, maximum deceleration, maximum forward velocity, and maximum reverse
   * velocity.
   */
  private final VelocityController velocityController;
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
    this.velocityController = new VelocityController(
        maxAcceleration, maxDeceleration, maxFwdVelocity, maxRevVelocity
    );
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }
    super.initialize();

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
  // TODO: rename
  private void initializeMap1() {
    for (int i = 1000; i <= 100000; i += 1000) {
      map1.put(i, "MK" + (i / 1000));
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


    if (mcPool.isEmpty()) {
      initializeMCPool(cmd);
    }

    if (mcPool.contains(cmd)) {
      LOG.info("Skipping already processed command: ");
      return;
    }

    mcPool.add(cmd);
    processMovementCommand(cmd);

    if (mcPool.size() == getSentCommands().size() + getUnsentCommands().size()) {
      writeAllModbusCommands();
    }
    // Set destination
    int destination = Integer.parseInt(cmd.getStep().getDestinationPoint().getName());
    sendModbusCommand("SET_DESTINATION", destination);

    // Set direction (assume 0 for forward, 1 for backward)
    int direction = cmd.getStep().getVehicleOrientation() == Vehicle.Orientation.FORWARD ? 0 : 1;
    sendModbusCommand("SET_DIRECTION", direction);

    // Set speed
    double speed = velocityController.getCurrentVelocity();
    // TODO: overload a double type parameter version of sendModbusCommand for speed.
    sendModbusCommand("SET_SPEED", (int) speed);

    // Start command
    sendModbusCommand("SET_COMMAND", 1);  // Assume 1 means start

    // Verify commands were written correctly
    verifyModbusCommands();
  }

  private void initializeMCPool(MovementCommand cmd) {
    TransportOrder order = cmd.getTransportOrder();
    List<DriveOrder> driveOrders = order.getAllDriveOrders();

//    List<MovementCommand> allCommands = order.getAllDriveOrders().stream()
//        .map(DriveOrder::getRoute)
//        .flatMap(route -> route.getSteps().stream())
//        .map(step -> createMovementCommand(order, step))
//        .collect(Collectors.toList());


    List<DriveOrder> relevantOrders = new ArrayList<>();

    // Get the current DriveOrder
    DriveOrder currentDriveOrder = order.getCurrentDriveOrder();
    if (currentDriveOrder != null) {
      relevantOrders.add(currentDriveOrder);
    }

    // Get future DriveOrders
    relevantOrders.addAll(order.getFutureDriveOrders());

    for (DriveOrder driveOrder : relevantOrders) {
      // TODO: add movementCommands into mcpool.
      LOG.info("DDDD");
//      Route route = driveOrder.getRoute();
//      if (route != null) {
//        for (Route.Step step : route.getSteps()) {
//          //Create a MovementCommand for each Step
//          MovementCommand movementCmd = createMovementCommand(step, driveOrder);
//          mcPool.add(movementCmd);
//        }
//      }
    }

    LOG.info(
        String.format(
            "Initialized MC pool for TransportOrder: %s. Pool size: %d", order.getName(), mcPool
                .size()
        )
    );
  }

  private void processMovementCommand(MovementCommand cmd) {
    Point destination = cmd.getStep().getDestinationPoint();
    String stationName = map1.get(Integer.parseInt(destination.getName()));
    if (stationName == null) {
      throw new IllegalArgumentException("Cannot find station for the given destination point.");
    }

    CMD1 cmd1 = createCMD1(cmd);
    CMD2 cmd2 = createCMD2(cmd);

    map2.put(stationName, new Pair<>(cmd1, cmd2));
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
    // TODO: MAKE IT REAL
    return new CMD1(
        0, 3, 4, cmd.getStep().getVehicleOrientation() == Vehicle.Orientation.FORWARD ? 0 : 1
    );
  }

  private CMD2 createCMD2(MovementCommand cmd) {
    // TODO: MAKE IT REA
    return new CMD2(0, 0, 2);
  }

//  private MovementCommand createMovementCommand(Route.Step step, DriveOrder driveOrder) {
//    TCSObjectReference<TransportOrder> transportOrder = driveOrder.getTransportOrder();
//    String operation = driveOrder.getDestination().getOperation();
//    Location opLocation = driveOrder.getDestination().getLocation();
//
//    // 判斷是否為最後一個移動命令
//    boolean isFinalMovement = driveOrder.equals(transportOrder.getAllDriveOrders());
//
//    // 獲取最終目的地信息
//    DriveOrder finalDriveOrder = transportOrder.getFinalDriveOrder();
//    Location finalDestinationLocation = finalDriveOrder.getDestination().getLocation();
//    Point finalDestination = finalDriveOrder.getDestination().getDestination();
//    String finalOperation = finalDriveOrder.getDestination().getOperation();
//
//    return new MovementCommand(
//        transportOrder,
//        driveOrder,
//        step,
//        operation,
//        opLocation,
//        isFinalMovement,
//        finalDestinationLocation,
//        finalDestination,
//        finalOperation,
//        transportOrder.getProperties()
//    );
//  }

  /**
   * Converts a TransportOrder to a list of ModbusCommands.
   *
   * @param order The TransportOrder to convert.
   * @return A list of ModbusCommands representing the TransportOrder.
   */
  private List<ModbusCommand> convertTransportOrderToModbusCommands(TransportOrder order) {
    List<ModbusCommand> commands = new ArrayList<>();

    for (DriveOrder driveOrder : order.getAllDriveOrders()) {
      String destinationName = driveOrder.getDestination().getDestination().getName();
      int position = Integer.parseInt(destinationName.substring(2)) * 1000;

      Pair<CMD1, CMD2> cmds = stationToCommands.get(destinationName);
      if (cmds == null) {
        LOG.warning("No commands found for station: " + destinationName);
        continue;
      }

      commands.add(new ModbusCommand("CMD1", cmds.getFirst().toInt(), position));
      commands.add(new ModbusCommand("CMD2", cmds.getSecond().toInt(), position + 1));
    }

    return commands;
  }

  private void writeModbusCommands(List<ModbusCommand> commands) {
    int startAddress = 1000;  // 假設起始地址為 1000
    int quantity = commands.size();

    ByteBuf values = Unpooled.buffer(quantity * 2);  // 每個寄存器 2 個字節

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

  private void initializeStationMaps() {
    // Initialize positionToStation
    for (int i = 1000; i <= 100000; i += 1000) {
      positionToStation.put(i, "MK" + (i / 1000));
    }

    // Initialize stationToCommands
    for (int i = 1; i <= 100; i++) {
      String station = "MK" + i;
      CMD1 cmd1 = new CMD1(0, 0, 0, 0);  // 示例值
      CMD2 cmd2 = new CMD2(0, 0, 0);     // 示例值
      stationToCommands.put(station, new Pair<>(cmd1, cmd2));
    }
  }

  private double getMaxAllowedSpeed(Path path) {
    // TODO:
    return 2.4;
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

  private void verifyModbusCommands() {
    ModbusRegister destinationRegister = REGISTER_MAP.get("SET_DESTINATION");
    sendModbusRequestAndHandleResponse(destinationRegister);
  }

  private void sendModbusRequestAndHandleResponse(ModbusRegister register) {
    sendModbusRequest(new ReadHoldingRegistersRequest(register.address(), 1))
        .thenAccept(response -> handleSuccessResponse(response, register))
        .exceptionally(throwable -> handleErrorResponse(throwable, register));
  }

  private void handleSuccessResponse(ModbusResponse response, ModbusRegister register) {
    if (response instanceof ReadHoldingRegistersResponse holdingRegistersResponse) {
      ByteBuf buffer = holdingRegistersResponse.getRegisters();
      try {
        if (buffer.readableBytes() >= 2) {
          int readValue = buffer.readShort();
          LOG.info("Verified " + register.function().name() + ": " + readValue);
        }
        else {
          LOG.warning(
              "Insufficient data in response for register: " + register.function().name()
          );
        }
      }
      finally {
        buffer.release();
      }
    }
    else {
      LOG.warning("Unexpected response type: " + response.getClass().getSimpleName());
    }
  }

  private Void handleErrorResponse(Throwable throwable, ModbusRegister register) {
    LOG.log(Level.SEVERE, "Failed to verify " + register.function().name(), throwable);
    return null;
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

  @Override
  protected void updateVehiclePosition() {
    if (!isVehicleConnected()) {
      LOG.warning("Not connected to Modbus TCP server. Cannot update position.");
      return;
    }

    sendModbusRequest(new ReadHoldingRegistersRequest(REGISTER_MAP.get("POSITION").address(), 1))
        .thenAccept(response -> {
          if (response instanceof ReadHoldingRegistersResponse holdingRegistersResponse) {
            ByteBuf buffer = holdingRegistersResponse.getRegisters();
            try {
              if (buffer.readableBytes() >= 2) {
                int position = buffer.readShort();
                getProcessModel().setPosition(String.valueOf(position));
                LOG.info("Updated vehicle position: " + position);
              }
              else {
                LOG.warning("Insufficient data in response for POSITION register");
              }
            }
            finally {
              buffer.release();
            }
          }
          else {
            LOG.warning(
                "Unexpected response type for POSITION: " + response.getClass().getSimpleName()
            );
          }
        })
        .exceptionally(throwable -> {
          LOG.log(Level.SEVERE, "Failed to read vehicle position", throwable);
          return null;
        });
  }

  @Override
  protected void updateVehicleState() {
    if (!isVehicleConnected()) {
      LOG.warning("Not connected to Modbus TCP server. Cannot update state.");
      return;
    }

    sendModbusRequest(new ReadHoldingRegistersRequest(REGISTER_MAP.get("STATUS").address(), 1))
        .thenAccept(response -> {
          if (response instanceof ReadHoldingRegistersResponse holdingRegistersResponse) {
            ByteBuf buffer = holdingRegistersResponse.getRegisters();
            try {
              if (buffer.readableBytes() >= 2) {
                int state = buffer.readShort();
                Vehicle.State newState = mapModbusStateToVehicleState(state);
                getProcessModel().setState(newState);
              }
              else {
                LOG.warning("Insufficient data in response for STATUS register");
              }
            }
            finally {
              buffer.release();
            }
          }
          else {
            LOG.warning(
                "Unexpected response type for STATUS: " + response.getClass().getSimpleName()
            );
          }
        })
        .exceptionally(throwable -> {
          LOG.log(Level.SEVERE, "Failed to read vehicle state", throwable);
          return null;
        });

    // Read current speed from Modbus (register 303)
    sendModbusRequest(new ReadHoldingRegistersRequest(303, 1))
        .thenAccept(response -> {
          if (response instanceof ReadHoldingRegistersResponse holdingRegistersResponse) {
            ByteBuf buffer = holdingRegistersResponse.getRegisters();
            try {
              if (buffer.readableBytes() >= 2) {
                int currentSpeed = buffer.readShort();
                velocityController.setCurrentVelocity(currentSpeed);
                LOG.info("Current vehicle speed: " + currentSpeed);
              }
              else {
                LOG.warning("Insufficient data in response for SPEED register");
              }
            }
            finally {
              buffer.release();
            }
          }
          else {
            LOG.warning(
                "Unexpected response type for SPEED: " + response.getClass().getSimpleName()
            );
          }
        })
        .exceptionally(throwable -> {
          LOG.log(Level.SEVERE, "Failed to read current speed", throwable);
          return null;
        });
  }

  private Vehicle.State mapModbusStateToVehicleState(int modbusState) {
    return switch (modbusState) {
      case 0 -> Vehicle.State.IDLE;
      case 1 -> Vehicle.State.EXECUTING;
      case 2 -> Vehicle.State.CHARGING;
      default -> Vehicle.State.UNKNOWN;
    };
  }

  private CompletableFuture<ModbusResponse> sendModbusRequest(
      com.digitalpetri.modbus.requests.ModbusRequest request
  ) {
    return master.sendRequest(request, 0);
  }

  @Nonnull
  @Override
  public ExplainedBoolean canProcess(@Nonnull
  TransportOrder order) {
    return new ExplainedBoolean(true, "ModbusTCP adapter can process all orders.");
  }

  @Override
  public void processMessage(@Nullable
  Object message) {
    LOG.info("Received message: " + message);
    // Implement specific message processing logic
  }

  @Override
  protected VehicleProcessModelTO createCustomTransferableProcessModel() {
    return new CustomProcessModelTO()
        .setCustomProperty(getProcessModel().getCustomProperty())
        .setLoadOperation(getProcessModel().getLoadOperation())
        .setUnloadOperation(getProcessModel().getUnloadOperation());
  }

  /**
   * Updates the vehicle's speed.
   *
   * @param newSpeed The new speed value to set.
   */
  // Add a method to update the vehicle's speed
  public void updateVehicleSpeed(int newSpeed) {
    velocityController.setCurrentVelocity(newSpeed);
    currentVelocity = newSpeed;
    // TODO: overload a double type parameter version of sendModbusCommand for speed.
    sendModbusCommand("SET_SPEED", (int) currentVelocity);
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
