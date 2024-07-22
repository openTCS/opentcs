package org.opentcs.customadapter;


import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.opentcs.components.kernel.services.PlantModelService;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Vehicle;

@Singleton
public class CommunicationStrategy
    implements
      CustomAdapterComponentsFactory {
  private static final Logger LOG = Logger.getLogger(CommunicationStrategy.class.getName());

  private final Map<String, Provider<StrategyCreator>> strategyProviders;
  private final VehicleConfigurationProvider configProvider;
  private final ScheduledExecutorService executor;
  private final PlantModelService plantModelService;
  private final VehicleService vehicleService;

  @Inject
  CommunicationStrategy(
      Map<String, Provider<StrategyCreator>> strategyProviders,
      @KernelExecutor
      ScheduledExecutorService executor,
      VehicleConfigurationProvider configProvider,
      PlantModelService plantModelService,
      VehicleService vehicleService
  ) {
    this.strategyProviders = strategyProviders;
    this.executor = executor;
    this.configProvider = configProvider;
    this.plantModelService = plantModelService;
    this.vehicleService = vehicleService;
  }

//  @SuppressWarnings("checkstyle:TodoComment")
//  private void initializeStrategies() {
//    strategyProviders.put("ModbusTCP", (Provider<StrategyCreator>) new ModbusTCPStrategy());
//    // TODO: Add other strategies here
//  }

  @Override
  public CustomVehicleCommAdapter createCustomCommAdapter(@Assisted
  Vehicle vehicle) {
    VehicleConfiguration config = configProvider.getConfiguration(vehicle.getName());
    if (config == null) {
//      config = createConfigWithUserInput(vehicle);
//      configProvider.setConfiguration(vehicle.getName(), config);
      configProvider.setConfiguration(
          vehicle.getName(),
          new VehicleConfiguration("ModbusTCP", "192.168.0.72", 502)
      );
    }

    String strategyKey = config.currentStrategy();
    Provider<StrategyCreator> creatorProvider = strategyProviders.get(strategyKey);
    if (creatorProvider == null) {
      LOG.warning("Unknown strategy: " + strategyKey + ". Using default ModbusTCP strategy.");
      creatorProvider = strategyProviders.get("ModbusTCP");
    }

    StrategyCreator creator = creatorProvider.get();
    return creator.createAdapter(vehicle, config, executor, plantModelService);
  }

  private VehicleConfiguration createConfigWithUserInput(Vehicle vehicle) {
    String defaultHost = "localhost";
    int defaultPort = 502;
    String defaultStrategy = "ModbusTCP";

    JTextField hostField = new JTextField(defaultHost, 10);
    JTextField portField = new JTextField(String.valueOf(defaultPort), 10);
    JComboBox<String> strategyComboBox = new JComboBox<>(
        strategyProviders.keySet().toArray(new String[0])
    );
    strategyComboBox.setSelectedItem(defaultStrategy);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(new JLabel("Host:"));
    panel.add(hostField);
    panel.add(new JLabel("Port:"));
    panel.add(portField);
    panel.add(new JLabel("Communication Strategy:"));
    panel.add(strategyComboBox);

    int result = JOptionPane.showConfirmDialog(
        null, panel,
        "Enter Configuration for " + vehicle.getName(), JOptionPane.OK_CANCEL_OPTION
    );

    String host;
    int port;
    String strategy;

    if (result == JOptionPane.OK_OPTION) {
      host = hostField.getText().isEmpty() ? defaultHost : hostField.getText();
      try {
        port = Integer.parseInt(portField.getText());
        if (port < 0 || port > 65535) {
          LOG.warning("Invalid port number. Using default port " + defaultPort);
          port = defaultPort;
        }
      }
      catch (NumberFormatException e) {
        LOG.warning("Invalid port number. Using default port " + defaultPort);
        port = defaultPort;
      }
      strategy = (String) strategyComboBox.getSelectedItem();
      if (strategy == null || strategy.isEmpty()) {
        strategy = defaultStrategy;
      }
    }
    else {
      // Use default values if user cancels
      host = defaultHost;
      port = defaultPort;
      strategy = defaultStrategy;
    }

    return new VehicleConfiguration(strategy, host, port);
  }
}
