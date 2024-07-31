package org.opentcs.peripheralcustomadapter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import org.opentcs.components.kernel.services.PeripheralService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.util.event.EventHandler;

public class PeripheralCommunicationStrategy
    implements
      PeripheralCustomAdapterComponentsFactory {
  private static final Logger LOG = Logger.getLogger(
      PeripheralCommunicationStrategy.class.getName()
  );

  private final Map<String, Provider<StrategyCreator>> strategyProviders;
  private final PeripheralDeviceConfigurationProvider configProvider;
  private final ScheduledExecutorService executor;
  private final EventHandler eventHandler;
  private final PeripheralService peripheralService;

  @Inject
  PeripheralCommunicationStrategy(
      Map<String, Provider<StrategyCreator>> strategyProviders,
      @KernelExecutor
      ScheduledExecutorService executor,
      EventHandler eventHandler,
      PeripheralService peripheralService
  ) {
    this.strategyProviders = strategyProviders;
    this.executor = executor;
    this.configProvider = new PeripheralDeviceConfigurationProvider();
    this.eventHandler = eventHandler;
    this.peripheralService = peripheralService;
  }

  @Override
  public PeripheralCommunicationAdapter createPeripheralCustomCommAdapter(
      TCSResourceReference<Location> location,
      PeripheralService peripheralService
  ) {
    PeripheralDeviceConfiguration config = configProvider.getConfiguration(location.getName());
    if (config == null) {
      if (String.CASE_INSENSITIVE_ORDER.compare(location.getName(), "SAA-mini-OHT-Sensor0001-EFEM")
          == 0) {
        config = new PeripheralDeviceConfiguration("ModbusTCP", "192.168.1.20", 502);
        configProvider.setConfiguration(location.getName(), config);
      }
      else {
        config = new PeripheralDeviceConfiguration("ModbusTCP", "192.168.1.21", 502);
        configProvider.setConfiguration(location.getName(), config);
      }
    }

    String strategyKey = config.currentStrategy();
    Provider<StrategyCreator> creatorProvider = strategyProviders.get(strategyKey);
    if (creatorProvider == null) {
      LOG.warning("Unknown strategy: " + strategyKey + ". Using default ModbusTCP strategy.");
      creatorProvider = strategyProviders.get("ModbusTCP");
    }

    StrategyCreator creator = creatorProvider.get();
    return creator.createAdapter(location, eventHandler, executor, peripheralService);
  }
}
