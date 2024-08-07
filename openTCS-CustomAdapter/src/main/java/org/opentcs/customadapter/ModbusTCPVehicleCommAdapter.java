package org.opentcs.customadapter;

import static java.util.Objects.requireNonNull;

import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
import com.digitalpetri.modbus.requests.ModbusRequest;
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
import java.util.Set;
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
import org.opentcs.components.kernel.services.PeripheralService;
import org.opentcs.components.kernel.services.PlantModelService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.PeripheralInformation;
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
  private MovementHandler movementHandler;
  private boolean shouldAbort = false;
  private final PeripheralService peripheralService;

  /**
   * A communication adapter for ModbusTCP-based vehicle communication.
   * <p>
   * Allows communication between the vehicle and the control system using the ModbusTCP protocol.
   *
   * @param executor The executor for handling background tasks.
   * @param vehicle The vehicle associated with this communication adapter.
   * @param plantModelService The plant model service for accessing plant model information.
   * @param peripheralService The Peripheral Service.
   */
  @SuppressWarnings("checkstyle:TodoComment")

  @Inject
  public ModbusTCPVehicleCommAdapter(
      @KernelExecutor
      ScheduledExecutorService executor,
      @Assisted
      Vehicle vehicle,

      PlantModelService plantModelService,
      PeripheralService peripheralService
  ) {
    super(new CustomProcessModel(vehicle), "RECHARGE", 1000, executor);
    VehicleConfigurationProvider configProvider = new VehicleConfigurationProvider();

    this.host = configProvider.getConfiguration(vehicle.getName()).host();
    this.port = configProvider.getConfiguration(vehicle.getName()).port();

    LOG.warning(String.format("DEVICE HOST:%s, PORT: %d", this.host, this.port));
    this.vehicle = requireNonNull(vehicle, "vehicle");
    this.plantModelService = requireNonNull(plantModelService, "plantModelService");

    this.isConnected = false;
    this.currentTransportOrder = null;
    this.positionMap = new HashMap<>();
    this.peripheralService = peripheralService;
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

    ((ExecutorService) getExecutor()).submit(() -> getProcessModel().setPosition("Point-0003"));
    LOG.warning("Device has been set to Point-0003");
    getProcessModel().setLoadHandlingDevices(
        List.of(new LoadHandlingDevice(LHD_NAME, false))
    );
    getProcessModel().setMaxFwdVelocity(vehicle.getMaxVelocity());
    initializePositionMap();
    this.positionUpdater = new PositionUpdater(getProcessModel(), getExecutor());
    this.movementHandler = new MovementHandler(getExecutor(), this);

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
    movementHandler.stopMonitoring();
    initialized = false;// Stop the heartbeat mechanism
  }

  private void startHeartbeat() {
    heartBeatFuture = getExecutor().scheduleAtFixedRate(() -> {
      boolean currentValue = toggleHeartbeatAndRegisterWriting();
      addDelayAndReadRegister(currentValue)
          .thenAccept(value -> handleHeartbeatValueMismatch(currentValue, value))
          .exceptionally(ex -> {
            logError("Failed to write or read heartbeat: ", ex);
            return null;
          });
    }, 0, 300, TimeUnit.MILLISECONDS);
  }

  private boolean toggleHeartbeatAndRegisterWriting() {
    boolean currentValue = heartBeatToggle.getAndSet(!heartBeatToggle.get());
    writeSingleRegister(100, currentValue ? 1 : 0);
    return currentValue;
  }

  private CompletableFuture<Integer> addDelayAndReadRegister(boolean currentValue) {
    try {
      Thread.sleep(200);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logError("Failed to sleep thread: ", e);
      writeSingleRegister(100, currentValue ? 1 : 0);
      return CompletableFuture.completedFuture(-1);
    }
    return readSingleRegister(100);
  }

  private void handleHeartbeatValueMismatch(boolean currentValue, int value) {
    if (value != (currentValue ? 1 : 0)) {
      LOG.warning("Heartbeat value mismatch! Retrying...");
      writeSingleRegister(100, currentValue ? 1 : 0)
          .exceptionally(ex -> {
            logError("Failed to retry heartbeat write: ", ex);
            return null;
          });
    }
  }

  private void logError(String message, Throwable ex) {
    LOG.severe(message + ex.getMessage());
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

    if (evt.getPropertyName().equals(VehicleProcessModel.Attribute.STATE.name())) {
      Vehicle.State newState = (Vehicle.State) evt.getNewValue();
      handleVehicleStateChange(newState);
    }
    if (Objects.equals(
        evt.getPropertyName(),
        VehicleProcessModel.Attribute.LOAD_HANDLING_DEVICES.name()
    )) {
      handleLoadHandlingDeviceChange();
    }
  }

  private void handleLoadHandlingDeviceChange() {
    if (!getProcessModel().getLoadHandlingDevices().isEmpty()
        && getProcessModel().getLoadHandlingDevices().getFirst().isFull()) {
      loadState = LoadState.FULL;
      // TODO: need change vehicle model size in future.
    }
    else {
      loadState = LoadState.EMPTY;
    }
  }

  private void handleVehicleStateChange(Vehicle.State newState) {
    getProcessModel().setState(newState);
    if (newState == Vehicle.State.IDLE) {
      checkAndHandleTrafficControl();
    }
  }

  private void checkAndHandleTrafficControl() {
    if (vehicle == null) {
      LOG.warning("Unable to check ProcState: Vehicle reference is null");
      return;
    }

    Vehicle.ProcState procState = vehicle.getProcState();
    if (procState == Vehicle.ProcState.PROCESSING_ORDER) {
      try {
        writeSingleRegister(105, 0);
        LOG.info("Traffic control: Vehicle stopped due to IDLE state while processing order.");
      }
      catch (Exception e) {
        LOG.severe(String.format("Failed to write to register for traffic control %s", e));
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
      processFinalMovement(newCommand);
    }
    return true;
  }

  private void processFinalMovement(MovementCommand newCommand) {
    logFinalMovementInfo(newCommand);

    CompletableFuture<Boolean> checkStatusFuture = checkVehicleStatus();
    CompletableFuture<Boolean> checkLocationFuture = checkLocationStatus(newCommand);

    CompletableFuture.allOf(checkStatusFuture, checkLocationFuture)
        .thenRun(
            () -> handleFinalMovementResult(checkStatusFuture, checkLocationFuture, newCommand)
        )
        .exceptionally(ex -> handleException(ex, newCommand));
  }

  private void logFinalMovementInfo(MovementCommand newCommand) {
    LOG.info(
        String.format(
            "FINAL MOVEMENT COMMAND DESTINATION: %s",
            newCommand.getStep().getDestinationPoint().getName()
        )
    );
    LOG.info("RECEIVED FINAL COMMAND, PROCESSING COMMANDS.");
  }

  private CompletableFuture<Boolean> checkVehicleStatus() {
    return readSingleRegister(114)
        .thenCombine(readSingleRegister(115), (value114, value115) -> {
          boolean isValid = isAutoModeEnabled(114, value114) && isAutoModeEnabled(115, value115);
          shouldAbort = !isValid;
          return isValid;
        });
  }

  private boolean isAutoModeEnabled(int register, int value) {
    if (value != 2) {
      LOG.warning(
          String.format("Register %d is not set to auto mode, current value: %d", register, value)
      );
      return false;
    }
    return true;
  }

  private CompletableFuture<Boolean> checkLocationStatus(MovementCommand newCommand) {
    return CompletableFuture.supplyAsync(() -> {
      Location location = getLocationFromCommand(newCommand);
      logLocationInfo(location);
      return isLocationStatusValid(newCommand, location);
    });
  }

  private Location getLocationFromCommand(MovementCommand newCommand) {
    String locationName = getLocationNameFromDestinationPoint(newCommand);
    return peripheralService.fetchObject(Location.class, locationName);
  }

  private void logLocationInfo(Location location) {
    LOG.info(String.format("Location Name: %s", location.getName()));
    LOG.info(String.format("Property: %s", location.getProperty("LoadingStatus")));
    LOG.info(String.format("State: %s", getStateString(location)));
  }

  private boolean isLocationStatusValid(MovementCommand newCommand, Location location) {
    if (isMagazineLoadport(newCommand) && hasLoadingStatusProperty(location)) {
      String operation = newCommand.getFinalOperation();
      String loadingStatus = location.getProperty("LoadingStatus");
      return !(("Load".equals(operation) && "Unload".equals(loadingStatus)) ||
          ("Unload".equals(operation) && "Load".equals(loadingStatus)));
    }
    return true;
  }

  private boolean isMagazineLoadport(MovementCommand newCommand) {
    return newCommand.getFinalDestinationLocation() != null &&
        "Magazine_loadport".equals(newCommand.getFinalDestinationLocation().getName());
  }

  private boolean hasLoadingStatusProperty(Location location) {
    return location.getProperty("LoadingStatus") != null;
  }

  private void handleFinalMovementResult(
      CompletableFuture<Boolean> vehicleStatusFuture,
      CompletableFuture<Boolean> locationStatusFuture,
      MovementCommand newCommand
  ) {

    boolean vehicleStatusValid = vehicleStatusFuture.join();
    boolean locationStatusValid = locationStatusFuture.join();

    if (!vehicleStatusValid || !locationStatusValid) {
      LOG.warning("Aborting current transport order due to invalid status.");
      abortCurrentTransportOrder(newCommand);
    }
    else {
      LOG.info("Status is valid, processing commands.");
      processAllMovementCommands();
    }
  }

  private Void handleException(Throwable ex, MovementCommand newCommand) {
    LOG.severe("Error processing final movement: " + ex.getMessage());
    abortCurrentTransportOrder(newCommand);
    return null;
  }

  public void abortCurrentTransportOrder(MovementCommand failedCommand) {
    if (currentTransportOrder != null) {
      LOG.info("Aborting current transport order: " + currentTransportOrder.getName());

      allMovementCommands.clear();
      getUnsentCommands().clear();
      getSentCommands().clear();

      getProcessModel().setState(Vehicle.State.ERROR);
      getProcessModel().commandFailed(failedCommand);

      currentTransportOrder = null;
      stopVehicle();
      shouldAbort = false;
    }
    else {
      LOG.info("No current transport order to abort.");
    }
  }

  private void stopVehicle() {
    writeSingleRegister(105, 0)
        .exceptionally(ex -> {
          logError("Failed to set vehicle stop: ", ex);
          return null;
        });
    getProcessModel().setState(Vehicle.State.IDLE);
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
    writeAllModbusCommands()
        .thenRun(() -> {
          movementHandler.startMonitoring(allMovementCommands);
        })
        .exceptionally(ex -> {
          LOG.severe("Failed to write commands and start monitoring: " + ex.getMessage());
          return null;
        });
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
    processMovementCommands(commands);
    int positionBaseAddress = 1000;
    int cmdBaseAddress = 1200;

    int currentCommandSize = commands.size();

    // Convert stationCommandsMap to ModbusCommand list
    for (Map.Entry<Long, Pair<CMD1, CMD2>> entry : stationCommandsMap.entrySet()) {
      long stationPosition = entry.getKey();
      Pair<CMD1, CMD2> cmds = entry.getValue();
      LOG.info(String.format("stationPosition: %d", stationPosition));

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
      LOG.info(String.format("cmds.getFirst().toShort(): %d", cmds.getFirst().toShort()));

      cmdModbusCommand.add(
          new ModbusCommand(
              "CMD2", cmds.getSecond().toInt(), cmdBaseAddress + 1,
              ModbusCommand.DataFormat.HEXADECIMAL
          )
      );
      LOG.info(String.format("cmds.getSecond().toShort(): %d", cmds.getFirst().toShort()));


      positionBaseAddress += 2;
      cmdBaseAddress += 2;
    }

    for (int i = currentCommandSize; i < 30; i++) {
      positionModbusCommand.add(
          new ModbusCommand(
              "POSITION", 0, positionBaseAddress,
              ModbusCommand.DataFormat.DECIMAL
          )
      );
      cmdModbusCommand.add(
          new ModbusCommand(
              "CMD1", createEmptyCMD1().toShort(), cmdBaseAddress,
              ModbusCommand.DataFormat.HEXADECIMAL
          )
      );
      cmdModbusCommand.add(
          new ModbusCommand(
              "CMD2", createEmptyCMD2().toShort(), cmdBaseAddress + 1,
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
    for (MovementCommand cmd : commands) {
      Point sourcePoint = cmd.getStep().getSourcePoint();
      Point destPoint = cmd.getStep().getDestinationPoint();
      long destPosition = destPoint.getPose().getPosition().getX();

      if (sourcePoint == null) {
        if (cmd.getOperation().isEmpty()) {
          LOG.info(
              String.format(
                  "No operation for in-place command at position %d",
                  destPoint.getPose().getPosition().getX()
              )
          );
        }
        Pair<CMD1, CMD2> operationCommands = createOperationCommands(cmd);
        stationCommandsMap.put(destPosition, operationCommands);
        continue;
      }

      long sourcePosition = sourcePoint.getPose().getPosition().getX();
      LOG.info(String.format("CREATING COMMAND FOR POSITION: %d", sourcePosition));
      LOG.info(String.format("CREATING COMMAND FOR END POSITION: %d", destPosition));

      if (!cmd.isFinalMovement()) {
        Pair<CMD1, CMD2> pairCommands = new Pair<>(createCMD1(cmd), createCMD2(cmd));
        stationCommandsMap.put(sourcePosition, pairCommands);
        continue;
      }

      Pair<CMD1, CMD2> moveCommands = createDefaultCommands(cmd);
      stationCommandsMap.put(sourcePosition, moveCommands);
      Pair<CMD1, CMD2> operationCommands = createOperationCommands(cmd);
      stationCommandsMap.put(destPosition, operationCommands);
    }
  }

  public String getLocationNameFromDestinationPoint(MovementCommand command) {
    Point destinationPoint = command.getStep().getDestinationPoint();
    Set<Location.Link> attachedLinks = destinationPoint.getAttachedLinks();

    for (Location.Link link : attachedLinks) {
      return link.getLocation().getName();
    }

    return null;
  }

  private int getSpeedLevel(MovementCommand cmd) {
    double maxSpeed = 0;
    int speedLevel = 0;
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
    return speedLevel;
  }

  private int getStation(MovementCommand cmd) {
    String locationNameFromDestinationPoint = getLocationNameFromDestinationPoint(cmd);
    if (locationNameFromDestinationPoint != null && locationNameFromDestinationPoint.equals(
        "Magazine_loadport"
    )) {
      return 1;
    }
    else if (locationNameFromDestinationPoint != null && locationNameFromDestinationPoint.equals(
        "STK_IN"
    )) {
      return 2;
    }
    else if (locationNameFromDestinationPoint != null && locationNameFromDestinationPoint.equals(
        "OHB"
    )) {
      return 3;
    }
    else if (locationNameFromDestinationPoint != null && locationNameFromDestinationPoint.equals(
        "Sidefork"
    )) {
      return 4;
    }

    // TODO: Make sure 0 work here
    return 0;
  }

  @SuppressWarnings("checkstyle:TodoComment")
  private CMD1 createCMD1(MovementCommand cmd) {
    int liftCmd = 0;
    int speedLevel = getSpeedLevel(cmd);
    int obstacleSensor = 5;
    String command = cmd.getOperation();
    liftCmd = getLiftCommand(command);
    return new CMD1(
        liftCmd, speedLevel, obstacleSensor, 0
    );
  }

  private CMD2 createCMD2(MovementCommand cmd) {
    String switchOperation = "";
    int liftHeight = 0;
    int station;
    int motionCommand;
    Map<String, String> pathOperation = null;

    String locationNameFromDestinationPoint = getLocationNameFromDestinationPoint(cmd);
    if (locationNameFromDestinationPoint != null && locationNameFromDestinationPoint.equals(
        "Sidefork"
    )) {
      liftHeight = 255;
    }
    station = getStation(cmd);
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
    return new CMD2(liftHeight, motionCommand, station);
  }

  private Pair<CMD1, CMD2> createDefaultCommands(MovementCommand cmd) {
    return new Pair<>(createDefaultCMD1(cmd), createDefaultCMD2(cmd));
  }

  private CMD1 createDefaultCMD1(MovementCommand cmd) {
    int speedLevel = getSpeedLevel(cmd);
    return new CMD1(0, speedLevel, 5, 0);
  }

  private CMD2 createDefaultCMD2(MovementCommand cmd) {
    return new CMD2(0, 1, 0);
  }

  private Pair<CMD1, CMD2> createOperationCommands(MovementCommand cmd) {
    return new Pair<>(createOperationCMD1(cmd), createOperationCMD2(cmd));
  }

  private CMD1 createOperationCMD1(MovementCommand cmd) {
    return new CMD1(getLiftCommand(cmd.getOperation()), getSpeedLevel(cmd), 5, 0);
  }

  private CMD2 createOperationCMD2(MovementCommand cmd) {
    int liftHeight = 0;
    int station = 0;
    String locationNameFromDestinationPoint = getLocationNameFromDestinationPoint(cmd);
    if (locationNameFromDestinationPoint != null && locationNameFromDestinationPoint.equals(
        "Sidefork"
    )) {
      liftHeight = 255;
    }
    station = getStation(cmd);
    return new CMD2(liftHeight, 3, station);
  }

  private CMD1 createEmptyCMD1() {
    return new CMD1(0, 1, 5, 0);
  }

  private CMD2 createEmptyCMD2() {
    return new CMD2(0, 1, 0);
  }

  private static String getStateString(Location location) {
    PeripheralInformation.State state = location.getPeripheralInformation().getState();
    String stateString = "";
    switch (state) {
      case NO_PERIPHERAL -> {
        stateString = "NO_PERIPHERAL";
      }
      case UNKNOWN -> {
        stateString = "UNKNOWN";
      }
      case UNAVAILABLE -> {
        stateString = "UNAVAILABLE";
      }
      case ERROR -> {
        stateString = "ERROR";
      }
      case IDLE -> {
        stateString = "IDLE";
      }
      case EXECUTING -> {
        stateString = "EXECUTING";
      }

      default -> throw new IllegalStateException("Unexpected value: " + state);
    }
    return stateString;
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
      default -> 0;
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
//          LOG.info("Successfully wrote register at address " + address + " with value " + value);
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

  CompletableFuture<Integer> readSingleRegister(int address) {
    ReadInputRegistersRequest request = new ReadInputRegistersRequest(address, 1);
    return sendModbusRequest(request)
        .thenApply(response -> {
          if (response instanceof ReadInputRegistersResponse readResponse) {
            ByteBuf responseBuffer = readResponse.getRegisters();
//            int value = responseBuffer.readUnsignedShort();
//            LOG.info(String.format("READ ADDRESS %d GOT %d", address, value));
//            return value;
            return responseBuffer.readUnsignedShort();
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
        int lowWord = (command.value() >> 16) & 0xFFFF;
        int highWord = command.value() & 0xFFFF;
        values.writeShort(highWord);
        values.writeShort(lowWord);

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
            startHeartbeat();
            LOG.warning("Starting sending heart bit.");
            positionUpdater.startPositionUpdates();
            LOG.warning("Starting positioning.");
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
      ModbusRequest request
  ) {
    return sendModbusRequestWithRetry(request, 3)
        .exceptionally(ex -> {
          LOG.severe("All retries failed for Modbus request: " + ex.getMessage());
          throw new CompletionException("Failed to send Modbus request after retries", ex);
        });
  }

  private CompletableFuture<ModbusResponse> sendModbusRequestWithRetry(
      ModbusRequest request,
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

  private ModbusResponse sendRequest(ModbusRequest request) {
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
        value = value & 0xFFFF;
//        throw new IllegalArgumentException("Value cannot be negative");
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
    private static final int POSITION_REGISTER_ADDRESS = 109;
    private final ScheduledExecutorService executor;
    private ScheduledFuture<?> positionFuture;
    private String lastKnownPosition;

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
            return null;
          });
    }

    private void processPositionUpdate(long stationMark) {
      long currentPosition = getPositionFromStationModbusCommand(stationMark);
      String openTcsPosition = convertToOpenTcsPosition(stationMark);
      Triple precisePosition = convertToPrecisePosition(currentPosition);
      getProcessModel().setPosition(openTcsPosition);
      getProcessModel().setPrecisePosition(precisePosition);
    }

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
      if (index <= 0 || index > positionModbusCommand.size()) {
        throw new IllegalArgumentException("Index out of positionModbusCommand bounds");
      }
      return positionModbusCommand.get((int) index - 1).value();
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
