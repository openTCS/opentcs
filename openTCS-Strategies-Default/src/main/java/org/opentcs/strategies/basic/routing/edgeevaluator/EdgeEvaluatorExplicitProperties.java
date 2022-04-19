/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.edgeevaluator;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import static org.opentcs.components.kernel.Router.PROPKEY_ROUTING_COST_FORWARD;
import static org.opentcs.components.kernel.Router.PROPKEY_ROUTING_COST_REVERSE;
import static org.opentcs.components.kernel.Router.PROPKEY_ROUTING_GROUP;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.components.kernel.routing.EdgeEvaluator;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses an edge's explicit routing cost (given as a property value) as its weight.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class EdgeEvaluatorExplicitProperties
    implements EdgeEvaluator {

  /**
   * A key used for selecting this evaluator in a configuration setting.
   * Should be unique among all keys.
   */
  public static final String CONFIGURATION_KEY = "EXPLICIT_PROPERTIES";
  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(EdgeEvaluatorExplicitProperties.class);
  /**
   * This class's configuration.
   */
  private final ExplicitPropertiesConfiguration configuration;

  @Inject
  public EdgeEvaluatorExplicitProperties(ExplicitPropertiesConfiguration configuration) {
    this.configuration = requireNonNull(configuration, "configuration");
  }

  @Override
  public void onGraphComputationStart(Vehicle vehicle) {
  }

  @Override
  public void onGraphComputationEnd(Vehicle vehicle) {
  }

  @Override
  public double computeWeight(Edge edge, Vehicle vehicle) {
    requireNonNull(edge, "edge");
    requireNonNull(vehicle, "vehicle");

    String group = extractVehicleGroup(vehicle);

    if (edge.isTravellingReverse()) {
      return parseCosts(extractRoutingCostString(edge.getPath(),
                                                 PROPKEY_ROUTING_COST_REVERSE + group));
    }
    else {
      return parseCosts(extractRoutingCostString(edge.getPath(),
                                                 PROPKEY_ROUTING_COST_FORWARD + group));
    }
  }

  private String extractVehicleGroup(Vehicle vehicle) {
    String group = vehicle.getProperty(PROPKEY_ROUTING_GROUP);

    return group == null ? "" : group;
  }

  private String extractRoutingCostString(Path path, String propertyKey) {
    String propVal = path.getProperty(propertyKey);

    if (propVal == null) {
      LOG.warn(
          "No routing cost property value for key '{}' in path '{}'. Using configured default: {}",
          propertyKey,
          path,
          configuration.defaultValue()
      );
      return configuration.defaultValue();
    }
    return propVal;
  }

  private double parseCosts(String costs) {
    try {
      return Double.parseDouble(costs);
    }
    catch (NumberFormatException exc) {
      LOG.warn("Exception parsing routing cost value '{}'.", costs, exc);
      return Double.POSITIVE_INFINITY;
    }
  }
}
