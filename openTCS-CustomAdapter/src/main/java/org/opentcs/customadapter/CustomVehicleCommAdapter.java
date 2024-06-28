package org.opentcs.customadapter;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.concurrent.ScheduledExecutorService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.BasicVehicleCommAdapter;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.util.ExplainedBoolean;

public class CustomVehicleCommAdapter
    extends BasicVehicleCommAdapter {
  public CustomVehicleCommAdapter(
      VehicleProcessModel processModel,
      String rechargeOperation,
      int commandsCapacity,
      @KernelExecutor ScheduledExecutorService executor
  ) {
    super(
//        new CustomAdapterComponentsFactory().createVehicleCommAdapterDescription(),
        processModel,
        commandsCapacity,
        rechargeOperation,
        executor
    );
  }
  @Override
  public void sendCommand(MovementCommand cmd) {
    // Implement the logic of sending commands to the vehicle here
  }

  @Override
  protected void connectVehicle() {

  }

  @Override
  protected void disconnectVehicle() {

  }

  @Override
  protected boolean isVehicleConnected() {
    return false;
  }

  @Nonnull
  @Override
  public ExplainedBoolean canProcess(
      @Nonnull
      TransportOrder order
  ) {
    return new ExplainedBoolean(true, "");
  }

  @Override
  public void onVehiclePaused(boolean paused) {

  }

  @Override
  public void processMessage(
      @Nullable
      Object message
  ) {

  }

//  @Override
//  protected VehicleProcessModelTO createCustomTransferableProcessModel() {
//    CustomVehicleProcessModelTO transferableProcessModel = new CustomVehicleProcessModelTO()
//        .setVehicleName(getProcessModel().getVehicleName())
//        .setCommAdapterConnected(isConnected())
//        .setCommAdapterEnabled(isEnabled())
//        .setPosition(getProcessModel().getVehiclePosition())
//        .setPrecisePosition(getProcessModel().getPrecisePosition())
//        .setOrientationAngle(getProcessModel().getOrientationAngle())
//        .setEnergyLevel(getProcessModel().getVehicleEnergyLevel())
//        .setLoadHandlingDevices(getProcessModel().getLoadHandlingDevices());
//
//    return transferableProcessModel;
//  }

  // TODO
  // Don't forget to implement all other necessary methods
}
