// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.common;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.opentcs.data.model.Vehicle;

/**
 * Filters optional order message fields based on whether the vehicle supports them.
 */
public class UnsupportedPropertiesFilter
    implements
      Function<JsonNode, JsonNode> {

  /**
   * The vehicle's support status for optional parameters.
   */
  private final Map<String, OptionalParameterSupport> unsupportedOptionalParameters;


  @Inject
  public UnsupportedPropertiesFilter(
      @Nonnull
      @Assisted
      Vehicle vehicle,
      @Nonnull
      @Assisted
      Function<Vehicle, Map<String, OptionalParameterSupport>> propertiesExtractor
  ) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(propertiesExtractor, "propertiesExtractor");
    this.unsupportedOptionalParameters = propertiesExtractor.apply(vehicle);
  }

  @Override
  public JsonNode apply(
      @Nonnull
      JsonNode treeModel
  ) {
    requireNonNull(treeModel, "treeModel");
    for (String path : unsupportedOptionalParameters.keySet()) {
      deletePath(treeModel, path.split("\\."));
    }
    return treeModel;
  }

  private static void deletePath(JsonNode currentNode, String[] path) {
    deletePath(currentNode, path, 0);
  }

  private static void deletePath(JsonNode currentNode, String[] path, int index) {
    if (index >= path.length) {
      return;
    }

    String currentPath = path[index];
    if (currentNode.has(currentPath)) {
      JsonNode childNode = currentNode.get(currentPath);

      if (index == path.length - 1) {
        // Last element in the path, remove it
        ((ObjectNode) currentNode).remove(currentPath);
      }
      else {
        if (childNode.isArray()) {
          // If it's an array, iterate over all elements
          for (JsonNode arrayElement : childNode) {
            deletePath(arrayElement, path, index + 1);
          }
        }
        else {
          // Recursively process the next element in the path
          deletePath(childNode, path, index + 1);
        }
      }
    }
  }
}
