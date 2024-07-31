package org.opentcs.peripheralcustomadapter;

import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
import com.digitalpetri.modbus.requests.ModbusRequest;
import com.digitalpetri.modbus.requests.ReadInputRegistersRequest;
import com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest;
import com.digitalpetri.modbus.responses.ModbusResponse;
import com.digitalpetri.modbus.responses.ReadHoldingRegistersResponse;
import com.digitalpetri.modbus.responses.ReadInputRegistersResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.timeout.TimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import org.opentcs.components.kernel.services.PeripheralService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.PeripheralInformation;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.util.event.EventHandler;

public class ModbusTCPPeripheralCommunicationAdapter
    extends
      PeripheralCommunicationAdapter {

  private static final Logger LOG = Logger.getLogger(
      ModbusTCPPeripheralCommunicationAdapter.class.getName()
  );


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
  private boolean initialized;
  private final ScheduledExecutorService executor;
  private ModbusTcpMaster master;
  private final AtomicBoolean heartBeatToggle = new AtomicBoolean(false);
  private ScheduledFuture<?> heartBeatFuture;
  private ScheduledFuture<?> pollingStatusFuture;
  private final PeripheralDeviceConfigurationProvider configProvider;
  private final TCSResourceReference<Location> location;
  private final LocationSensor1Status locationSensor1Status;
  private final LocationSensor2Status locationSensor2Status;
  private final PeripheralService peripheralService;

  /**
   * Creates a new instance.
   *
   * @param location The reference to the location this adapter is attached to.
   * @param eventHandler The handler used to send events to.
   * @param kernelExecutor The kernel's executor.
   * @param peripheralService Peripheral Service.
   */
  public ModbusTCPPeripheralCommunicationAdapter(
      TCSResourceReference<Location> location,
      EventHandler eventHandler,
      ScheduledExecutorService kernelExecutor,
      PeripheralService peripheralService
  ) {
    super(location, eventHandler, kernelExecutor, peripheralService);
    this.configProvider = new PeripheralDeviceConfigurationProvider();
    this.host = configProvider.getConfiguration(location.getName()).host();
    this.port = configProvider.getConfiguration(location.getName()).port();
    this.executor = kernelExecutor;
    this.location = location;
    this.isConnected = false;
    this.locationSensor1Status = new LocationSensor1Status();
    this.locationSensor2Status = new LocationSensor2Status();
    this.peripheralService = peripheralService;
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      LOG.warning("Peripheral Device has been initialized");
      return;
    }
    super.initialize();
    getProcessModel().withState(PeripheralInformation.State.IDLE);
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
    stopHeartBeat();
    stopPollingSensor();
    initialized = false;// Stop the heartbeat mechanism
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
            getProcessModel().withCommAdapterConnected(true);
            startHeartbeat();
            pollingSensorStatus();
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
            getProcessModel().withCommAdapterConnected(false);
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

  private void pollingSensorStatus() {
    pollingStatusFuture = executor.scheduleAtFixedRate(() -> {
      int address = 301;
      if (String.CASE_INSENSITIVE_ORDER.compare(location.getName(), "SAA-mini-OHT-Sensor0001")
          == 0) {
        readSingleRegister(address, 3).thenAccept(
            value -> IntStream.range(0, 3).forEachOrdered(i -> {
              switch (i) {
                case 0 -> {

                  locationSensor1Status.setEFEMMagazineStatus(
                      value.get(address) == 1
                  );
                }
                case 1 -> {
                  locationSensor1Status.setEFEMMagazineNumber(
                      value.get(address + i)
                  );
                }
                case 2 -> {
                  locationSensor1Status.setEFEMStatus(
                      value.get(address + i)
                  );
                }
                default -> throw new IllegalStateException("Unexpected value: " + i);
              }

            })
        );
      }
      else if (String.CASE_INSENSITIVE_ORDER.compare(location.getName(), "SAA-mini-OHT-Sensor0002")
          == 0) {
            readSingleRegister(address, 6).thenAccept(
                value -> IntStream.range(0, 6).forEachOrdered(i -> {
                  switch (i) {
                    case 0 -> {
                      locationSensor2Status.setSTKPort1MagazineStatus(
                          value.get(address) == 1
                      );
                    }
                    case 1 -> {
                      locationSensor2Status.setSTKPort1MagazineStatus(
                          value.get(address + i) == 1
                      );
                    }
                    case 2 -> {
                      locationSensor2Status.setOHB1MagazineStatus(
                          value.get(address + i) == 1
                      );
                    }
                    case 3 -> {
                      locationSensor2Status.setOHB2MagazineStatus(
                          value.get(address + i) == 1
                      );
                    }
                    case 4 -> {
                      locationSensor2Status.setSideFork1MagazineStatus(
                          value.get(address + i) == 1
                      );
                    }
                    case 5 -> {
                      locationSensor2Status.setSideFork2MagazineStatus(
                          value.get(address + i) == 1
                      );
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + i);
                  }
                })
            );
          }
    }, 0, 500, TimeUnit.MILLISECONDS);
  }

  private void stopPollingSensor() {
    if (pollingStatusFuture != null && !pollingStatusFuture.isCancelled()) {
      pollingStatusFuture.cancel(true);
    }
  }

  private void startHeartbeat() {
    heartBeatFuture = executor.scheduleAtFixedRate(() -> {
      boolean currentValue = heartBeatToggle.getAndSet(!heartBeatToggle.get());
      writeSingleRegister(300, currentValue ? 1 : 0)
          .thenCompose(v -> readSingleRegister(300, 1))
          .thenAccept(value -> {
            if (value.get(300) != (currentValue ? 1 : 0)) {
              LOG.warning("Heartbeat value mismatch! Retrying...");
              writeSingleRegister(300, currentValue ? 1 : 0)
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

  private CompletableFuture<Map<Integer, Integer>> readSingleRegister(int address, int quantity) {
    ReadInputRegistersRequest request = new ReadInputRegistersRequest(address, quantity);
    return sendModbusRequest(request)
        .thenApply(response -> {
          Map<Integer, Integer> result = new HashMap<>();
          if (response instanceof ReadInputRegistersResponse readResponse) {
            ByteBuf responseBuffer = readResponse.getRegisters();
            for (int i = 0; i < quantity; i++) {
              int value = responseBuffer.readUnsignedShort();
              result.put(address + i, value);
              LOG.info(String.format("READ ADDRESS %d GOT %d", address + i, value));
            }
            return result;
          }
          throw new RuntimeException("Invalid response type");
        });
  }

  private CompletableFuture<ModbusResponse> sendModbusRequest(
      ModbusRequest request
  ) {
    return sendModbusRequestWithRetry(request, 3);
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

    return CompletableFuture.supplyAsync(() -> sendRequest(request), executor)
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
            }, executor)
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
    return response;
  }
}
