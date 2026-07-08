// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.Property;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.PropertyTO;

/**
 * Includes the conversion methods for all Property classes.
 */
public class PropertyConverter {

  public PropertyConverter() {
  }

  public List<Property> toProperties(Map<String, String> properties) {
    return properties.entrySet().stream()
        .map(property -> new Property(property.getKey(), property.getValue()))
        .sorted(Comparator.comparing(Property::getKey))
        .collect(Collectors.toList());
  }

  public Map<String, String> toPropertyMap(List<PropertyTO> properties) {
    return properties.stream()
        .collect(Collectors.toMap(PropertyTO::getName, PropertyTO::getValue));
  }
}
