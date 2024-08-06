package org.opentcs.peripheralcustomadapter;

import static java.util.Objects.requireNonNull;

import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
import com.digitalpetri.modbus.requests.ReadInputRegistersRequest;
import com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest;
import com.digitalpetri.modbus.responses.ModbusResponse;
import com.digitalpetri.modbus.responses.ReadHoldingRegistersResponse;
import com.digitalpetri.modbus.responses.ReadInputRegistersResponse;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import org.opentcs.components.kernel.services.PeripheralService;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.PeripheralInformation;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.drivers.peripherals.PeripheralProcessModel;
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
  private final AtomicInteger heartBeatCount = new AtomicInteger(0);
  private final AtomicBoolean heartBeatFail = new AtomicBoolean(false);
  private final AtomicInteger loadingEFEMStatus = new AtomicInteger(0);
  private final AtomicInteger eFEMQuantity = new AtomicInteger(0);
  private final AtomicInteger eFEMStatus = new AtomicInteger(0);
  private final AtomicInteger loadingZIP1Status = new AtomicInteger(0);
  private final AtomicInteger loadingZIP2Status = new AtomicInteger(0);
  private final AtomicInteger loadingOHBStatus = new AtomicInteger(0);
  private final AtomicInteger loadingSideFork1Status = new AtomicInteger(0);
  private final AtomicInteger loadingSideFork2Status = new AtomicInteger(0);
  private ScheduledFuture<?> heartBeatFuture;
  private ScheduledFuture<?> pollingStatusFuture;
  private final PeripheralDeviceConfigurationProvider configProvider;
  private TCSResourceReference<Location> location;
  private final PeripheralService peripheralService;

  /**
   * Creates a new instance.
   *
   * @param location The reference to the location this adapter is attached to.
   * @param eventHandler The handler used to send events to.
   * @param kernelExecutor The kernel's executor.
   * @param peripheralService Peripheral Service.
   */
  @Inject
  public ModbusTCPPeripheralCommunicationAdapter(
      @Assisted
      TCSResourceReference<Location> location,
      @ApplicationEventBus
      EventHandler eventHandler,
      @KernelExecutor
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
    this.peripheralService = requireNonNull(peripheralService, "peripheralService");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      LOG.warning("Peripheral Device has been initialized");
      return;
    }
    super.initialize();
    setProcessModel(getProcessModel().withState(PeripheralInformation.State.IDLE));
    sendProcessModelChangedEvent(PeripheralProcessModel.Attribute.STATE);
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
            stopHeartBeat();
            stopPollingSensor();
          })
          .exceptionally(ex -> {
            LOG.log(Level.SEVERE, "Failed to disconnect from Modbus TCP server", ex);
            return null;
          })
          .isDone();
    }
    return true;
  }

  private void getEFEMInfo(Map<Integer, Integer> value, int index) {
    switch (index) {
      case 0 -> {
        int newResult = value.get(301);
        int oldResult = loadingEFEMStatus.getAndSet(newResult);
        if (newResult != oldResult) {
          if (newResult == 2) {
            peripheralService.updateObjectProperty(location, "LoadingStatus", "Load");
            LOG.info("Peripheral :" + location.getName() + ", Current Status :Load");
          }
          else if (newResult == 1) {
            peripheralService.updateObjectProperty(location, "LoadingStatus", "Unload");
            LOG.info("Peripheral :" + location.getName() + ", Current Status :Unload");
          }
          else {
            peripheralService.updateObjectProperty(location, "LoadingStatus", "Unknown");
            LOG.info("Peripheral :" + location.getName() + ", Current Status :Unknown");
          }
        }
      }
      case 1 -> {
        eFEMQuantity.set(value.get(301 + index));
        peripheralService.updateObjectProperty(
            location, "Magazine_Quantity ", String.valueOf(eFEMQuantity.get())
        );
        LOG.info("Peripheral :" + location.getName() + ", Count :" + eFEMQuantity.get());
      }
      case 2 -> {

        eFEMStatus.set(value.get(301 + index));
        if (eFEMStatus.get() == 1) {
          setProcessModel(getProcessModel().withState(PeripheralInformation.State.EXECUTING));
        }
        else if (eFEMStatus.get() == 2) {
          setProcessModel(getProcessModel().withState(PeripheralInformation.State.UNAVAILABLE));
        }
        else if (eFEMStatus.get() == 4) {
          setProcessModel(getProcessModel().withState(PeripheralInformation.State.IDLE));
        }
        else if (eFEMStatus.get() == 8) {
          setProcessModel(getProcessModel().withState(PeripheralInformation.State.ERROR));
        }
        else if (eFEMStatus.get() == 16) {
          setProcessModel(getProcessModel().withState(PeripheralInformation.State.ERROR));
        }
        else {
          setProcessModel(getProcessModel().withState(PeripheralInformation.State.UNKNOWN));
        }
        sendProcessModelChangedEvent(PeripheralProcessModel.Attribute.STATE);
      }
      default -> throw new IllegalStateException("Unexpected value: " + index);
    }
  }

  private void getOHBInfo(Map<Integer, Integer> value) {
    int newResult = value.get(303);
    int oldResult = loadingOHBStatus.getAndSet(newResult);

    if (newResult != oldResult) {
      if (newResult == 2) {
        peripheralService.updateObjectProperty(location, "LoadingStatus", "Load");
        LOG.info("Peripheral : " + location.getName() + ", Current Status :Load");
      }
      else if (newResult == 1) {
        peripheralService.updateObjectProperty(location, "LoadingStatus", "Unload");
        LOG.info("Peripheral : " + location.getName() + ", Current Status :Unload");
      }
      else {
        peripheralService.updateObjectProperty(location, "LoadingStatus", "Unknown");
        LOG.info("Peripheral : " + location.getName() + ", Current Status :Unknown");
      }
    }
  }

  private void getSideForkInfo(Map<Integer, Integer> value, int index) {
    switch (index) {
      case 0 -> {
        int newResult = value.get(305);
        int oldResult = loadingSideFork1Status.getAndSet(newResult);
        if (newResult != oldResult) {
          if (newResult == 2) {
            peripheralService.updateObjectProperty(location, "LoadingStatus", "Load");
            LOG.info("Peripheral :" + location.getName() + "#1, Current Status :Load");
          }
          else if (newResult == 1) {
            peripheralService.updateObjectProperty(location, "LoadingStatus", "Unload");
            LOG.info("Peripheral :" + location.getName() + "#1, Current Status :Unload");
          }
          else {
            peripheralService.updateObjectProperty(location, "LoadingStatus", "Unknown");
            LOG.info("Peripheral :" + location.getName() + "#1, Current Status :Unknown");
          }
        }
      }
      case 1 -> {
        int newResult = value.get(305 + index);
        int oldResult = loadingSideFork2Status.getAndSet(newResult);
        if (newResult != oldResult) {
          if (newResult == 2) {
            peripheralService.updateObjectProperty(location, "LoadingStatus", "Load");
            LOG.info("Peripheral :" + location.getName() + "#2, Current Status :Load");
          }
          else if (newResult == 1) {
            peripheralService.updateObjectProperty(location, "LoadingStatus", "Unload");
            LOG.info("Peripheral :" + location.getName() + "#2, Current Status :Unload");
          }
          else {
            peripheralService.updateObjectProperty(location, "LoadingStatus", "Unknown");
            LOG.info("Peripheral :" + location.getName() + "#2, Current Status :Unknown");
          }
        }
      }
      default -> throw new IllegalStateException("Unexpected value: " + index);
    }
  }

  private void getZIPInfo(Map<Integer, Integer> value, int index) {
    switch (index) {
      case 0 -> {
        int newResult = value.get(301);
        int oldResult = loadingZIP1Status.getAndSet(newResult);
        if (newResult != oldResult) {
          if (newResult == 2) {
            peripheralService.updateObjectProperty(location, "LoadingStatus", "Load");
            LOG.info("Peripheral :" + location.getName() + "#1, Current Status :Load");
          }
          else if (newResult == 1) {
            peripheralService.updateObjectProperty(location, "LoadingStatus", "Unload");
            LOG.info("Peripheral :" + location.getName() + "#1, Current Status :Unload");
          }
          else {
            peripheralService.updateObjectProperty(location, "LoadingStatus", "Unknown");
            LOG.info("Peripheral :" + location.getName() + "#1, Current Status :Unknown");
          }
        }
      }
      case 1 -> {
        int newResult = value.get(301 + index);
        int oldResult = loadingZIP2Status.getAndSet(newResult);
        if (newResult != oldResult) {
          if (newResult == 2) {
            peripheralService.updateObjectProperty(location, "LoadingStatus", "Load");
            LOG.info("Peripheral :" + location.getName() + "#2, Current Status :Load");
          }
          else if (newResult == 1) {
            peripheralService.updateObjectProperty(location, "LoadingStatus", "Unload");
            LOG.info("Peripheral :" + location.getName() + "#2, Current Status :Unload");
          }
          else {
            peripheralService.updateObjectProperty(location, "LoadingStatus", "Unknown");
            LOG.info("Peripheral :" + location.getName() + "#2, Current Status :Unknown");
          }
        }
      }
      default -> throw new IllegalStateException("Unexpected value: " + index);
    }
  }

  private void pollingSensorStatus() {
    pollingStatusFuture = executor.scheduleAtFixedRate(() -> {
      if (!heartBeatFail.get()) {
        if (String.CASE_INSENSITIVE_ORDER.compare(location.getName(), "Magazine_loadport")
            == 0) {
          readSingleRegister(301, 3).thenAccept(
              value -> {
                IntStream.range(0, 3).forEachOrdered(i -> {
                  getEFEMInfo(value, i);
                });
              }
          );
        }
        else if (String.CASE_INSENSITIVE_ORDER.compare(
            location.getName(), "STK_IN"
        )
            == 0) {
              readSingleRegister(301, 2).thenAccept(
                  value -> {
                    IntStream.range(0, 2).forEachOrdered(i -> {
                      getZIPInfo(value, i);
                    });
                  }
              );
            }
        else if (String.CASE_INSENSITIVE_ORDER.compare(
            location.getName(), "OHB"
        )
            == 0) {
              readSingleRegister(303, 1).thenAccept(
                  this::getOHBInfo
              );
            }
        else if (String.CASE_INSENSITIVE_ORDER.compare(
            location.getName(), "Sidefork"
        ) == 0) {
          readSingleRegister(305, 2).thenAccept(
              value -> {
                IntStream.range(0, 2).forEachOrdered(i -> {
                  getSideForkInfo(value, i);
                });
              }
          );
        }
      }
    }, 0, 500, TimeUnit.MILLISECONDS);
  }

  private void stopPollingSensor() {
    if (pollingStatusFuture != null && !pollingStatusFuture.isCancelled()) {
      LOG.info("Stop Polling Sensor.");
      pollingStatusFuture.cancel(true);
    }
  }

  private void startHeartbeat() {
    LOG.info("Starting sending heart bit, Peripheral Name : " + location.getName() + ".");

    heartBeatFuture = executor.scheduleAtFixedRate(() -> {
      readSingleRegister(300, 1).thenAccept(
          value -> {
            boolean newHeartBit = value.get(300) == 1;
            boolean oldHeartBit = heartBeatToggle.getAndSet(newHeartBit);
            if (oldHeartBit == newHeartBit) {
              if (heartBeatCount.get() >= 3) {
                LOG.info(
                    "The new heart bit and old heart bit is Same, Peripheral Name : " + location
                        .getName() + "."
                );
                heartBeatFail.set(true);
                setProcessModel(getProcessModel().withState(PeripheralInformation.State.ERROR));
              }
              heartBeatCount.addAndGet(1);
            }
            heartBeatCount.set(0);
            heartBeatFail.set(false);
            setProcessModel(getProcessModel().withState(PeripheralInformation.State.EXECUTING));
            sendProcessModelChangedEvent(PeripheralProcessModel.Attribute.STATE);
          }
      );
    }, 0, 200, TimeUnit.MILLISECONDS);
  }

  private void stopHeartBeat() {
    if (heartBeatFuture != null && !heartBeatFuture.isCancelled()) {
      LOG.info("Stop sending heart bit.");
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
              //LOG.info(String.format("READ ADDRESS %d GOT %d", address + i, value));
            }

            return result;
          }
          throw new RuntimeException("Invalid response type");
        });
  }

  private CompletableFuture<ModbusResponse> sendModbusRequest(
      com.digitalpetri.modbus.requests.ModbusRequest request
  ) {
    return sendModbusRequestWithRetry(request, 3).exceptionally(ex -> {
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
