/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.customadapter;

import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
import com.digitalpetri.modbus.requests.ReadHoldingRegistersRequest;
import com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest;
import com.digitalpetri.modbus.responses.ReadHoldingRegistersResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import jakarta.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.util.ExplainedBoolean;

/**
 * A communication adapter implementation for Modbus TCP protocol.
 */
public class ModbusTCPVehicleCommAdapter
    extends
      CustomVehicleCommAdapter {

  /**
   * This class's logger.
   */
  private static final Logger LOG = Logger.getLogger(ModbusTCPVehicleCommAdapter.class.getName());

  /**
   * The default recharge operation.
   */
  private static final String DEFAULT_RECHARGE_OPERATION = "";
  /**
   * The default commands capacity.
   */
  private static final int DEFAULT_COMMANDS_CAPACITY = Integer.MAX_VALUE;
  /**
   * The Modbus TCP master.
   */
  private ModbusTcpMaster master;
  /**
   * The host to connect to.
   */
  private final String host;
  /**
   * The port to connect to.
   */
  private final int port;
  /**
   * Indicates whether the adapter is connected.
   */
  private boolean isConnected;

  /**
   * Creates a new instance.
   *
   * @param processModel The vehicle process model.
   * @param executor The kernel executor.
   * @param host The host to connect to.
   * @param port The port to connect to.
   */
  public ModbusTCPVehicleCommAdapter(
      VehicleProcessModel processModel,
      @KernelExecutor
      ScheduledExecutorService executor,
      String host,
      int port
  ) {
    this(processModel, DEFAULT_RECHARGE_OPERATION, DEFAULT_COMMANDS_CAPACITY, executor, host, port);
  }

  /**
   * Creates a new instance.
   *
   * @param processModel The vehicle process model.
   * @param rechargeOperation The recharge operation.
   * @param commandsCapacity The commands' capacity.
   * @param executor The kernel executor.
   * @param host The host to connect to.
   * @param port The port to connect to.
   */
  public ModbusTCPVehicleCommAdapter(
      VehicleProcessModel processModel,
      String rechargeOperation,
      int commandsCapacity,
      @KernelExecutor
      ScheduledExecutorService executor,
      String host,
      int port
  ) {
    super(processModel, rechargeOperation, commandsCapacity, executor);
    this.host = host;
    this.port = port;
  }

  @Override
  protected void sendSpecificCommand(MovementCommand cmd) {
    if (master == null || !isVehicleConnected()) {
      LOG.warning("Not connected to Modbus TCP server. Cannot send command.");
      return;
    }

    // Example: Write the X and Y coordinates to holding registers 0 and 1
    int x = (int) cmd.getStep().getDestinationPoint().getPose().getPosition().getX();
    int y = (int) cmd.getStep().getDestinationPoint().getPose().getPosition().getY();

    ByteBuf buffer = Unpooled.buffer(4);
    buffer.writeShort(x);
    buffer.writeShort(y);

    int registerQuantity = 2;
    CompletableFuture<Void> future = master.sendRequest(
        new WriteMultipleRegistersRequest(0, registerQuantity, buffer), 0
    )
        .thenAccept(response -> LOG.info("Command sent successfully"));

    future.exceptionally(throwable -> {
      LOG.log(Level.SEVERE, "Failed to send command", throwable);
      return null;
    });
  }

  /**
   * Performs the connection to the Modbus TCP server.
   *
   * @return true if the connection is successful, false otherwise.
   */
  @Override
  protected boolean performConnection() {
    ModbusTcpMasterConfig config = new ModbusTcpMasterConfig.Builder(host)
        .setPort(port)
        .build();
    master = new ModbusTcpMaster(config);
    master.connect();
    isConnected = true;
    return true;
  }

  /**
   * Performs the disconnection from the Modbus TCP server.
   *
   * @return true if the disconnection is successful, false otherwise
   */
  @Override
  protected boolean performDisconnection() {
    if (master != null) {
      try {
        master.disconnect();
        isConnected = false;
        return true;
      }
      finally {
        master = null;
      }
    }
    return true;
  }

  /**
   * Checks if the vehicle is connected.
   *
   * @return true if connected, false otherwise.
   */
  protected boolean isVehicleConnected() {
    return isConnected && master != null;
  }

  @Override
  protected void updateVehiclePosition() {
    if (master == null || !isVehicleConnected()) {
      LOG.warning("Not connected to Modbus TCP server. Cannot update position.");
      return;
    }

    CompletableFuture<ReadHoldingRegistersResponse> future = master.sendRequest(
        new ReadHoldingRegistersRequest(0, 2), 0
    );

    future.thenAccept(response -> {
      ByteBuf buffer = response.getRegisters();
      int x = buffer.readShort();
      int y = buffer.readShort();

      // TODO: check setPosition parameter format
      getProcessModel().setPosition(String.format("%d,%d", x, y));
    }).exceptionally(throwable -> {
      LOG.log(Level.SEVERE, "Failed to read vehicle position", throwable);
      return null;
    });
  }

  @Override
  protected void updateVehicleState() {
    if (master == null || !isVehicleConnected()) {
      LOG.warning("Not connected to Modbus TCP server. Cannot update state.");
      return;
    }

    CompletableFuture<ReadHoldingRegistersResponse> future = master.sendRequest(
        new ReadHoldingRegistersRequest(2, 1), 0
    );

    future.thenAccept(response -> {
      ByteBuf buffer = response.getRegisters();
      int state = buffer.readShort();
      Vehicle.State newState;
      switch (state) {
        case 0:
          newState = Vehicle.State.IDLE;
          break;
        case 1:
          newState = Vehicle.State.EXECUTING;
          break;
        case 2:
          newState = Vehicle.State.CHARGING;
          break;
        default:
          newState = Vehicle.State.UNKNOWN;
      }
      // TODO: check setState parameter format
      getProcessModel().setState(newState);
    }).exceptionally(throwable -> {
      LOG.log(Level.SEVERE, "Failed to read vehicle state", throwable);
      return null;
    });
  }

  @Nonnull
  @Override
  public ExplainedBoolean canProcess(
      @Nonnull
      org.opentcs.data.order.TransportOrder order
  ) {
    return new ExplainedBoolean(true, "ModbusTCP adapter can process all orders.");
  }

  @Override
  public void processMessage(
      @Nonnull
      Object message
  ) {
    LOG.info("Received message: " + message);
    // Implement specific message processing logic
  }
}
