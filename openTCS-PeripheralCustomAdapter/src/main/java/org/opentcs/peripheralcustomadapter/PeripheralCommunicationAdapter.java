package org.opentcs.peripheralcustomadapter;

import com.google.inject.assistedinject.Assisted;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import org.opentcs.components.kernel.services.PeripheralService;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.drivers.peripherals.BasicPeripheralCommAdapter;
import org.opentcs.drivers.peripherals.PeripheralAdapterCommand;
import org.opentcs.drivers.peripherals.PeripheralCommAdapter;
import org.opentcs.drivers.peripherals.PeripheralJobCallback;
import org.opentcs.util.ExplainedBoolean;
import org.opentcs.util.event.EventHandler;

/**
 * A {@link PeripheralCommAdapter} implementation that is doing nothing.
 */
public abstract class PeripheralCommunicationAdapter
    extends
      BasicPeripheralCommAdapter {

  private static final Logger LOG = Logger.getLogger(
      PeripheralCommunicationAdapter.class.getName()
  );

  /**
   * Creates a new instance.
   *
   * @param location The reference to the location this adapter is attached to.
   * @param eventHandler The handler used to send events to.
   * @param kernelExecutor The kernel's executor.
   * @param peripheralService Peripheral Service.
   */
  @Inject
  public PeripheralCommunicationAdapter(
      @Assisted
      TCSResourceReference<Location> location,
      @ApplicationEventBus
      EventHandler eventHandler,
      @KernelExecutor
      ScheduledExecutorService kernelExecutor,
      PeripheralService peripheralService
  ) {
    super(new PeripheralCustomProcessModel(location), eventHandler);
  }

  @Override
  protected void connectPeripheral() {
    LOG.info("Connecting to peripheral Device...");
    if (performConnection()) {
      getProcessModel().withCommAdapterConnected(true);
    }
  }

  protected abstract boolean performConnection();

  @Override
  protected void disconnectPeripheral() {
    LOG.info("Disconnecting from vehicle...");
    if (performDisconnection()) {
      getProcessModel().withCommAdapterConnected(false);
    }
  }

  protected abstract boolean performDisconnection();


  @Nonnull
  @Override
  public ExplainedBoolean canProcess(
      @Nonnull
      PeripheralJob job
  ) {
    return null;
  }

  @Override
  public void process(
      @Nonnull
      PeripheralJob job,
      @Nonnull
      PeripheralJobCallback callback
  ) {

  }

  @Override
  public void abortJob() {

  }

  @Override
  public void execute(
      @Nonnull
      PeripheralAdapterCommand command
  ) {

  }
}
