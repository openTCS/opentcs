/*
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing;

import java.util.HashSet;
import java.util.Set;
import org.opentcs.components.plantoverview.PropertySuggestions;
import org.opentcs.data.ObjectPropConstants;

/**
 * The default property suggestions of the baseline plant overview.
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class DefaultPropertySuggestions
    implements PropertySuggestions {

  private final Set<String> keySuggestions = new HashSet<>();
  private final Set<String> valueSuggestions = new HashSet<>();

  /**
   * Creates a new instance.
   */
  public DefaultPropertySuggestions() {
    keySuggestions.add(ObjectPropConstants.PATH_TRAVEL_ORIENTATION);
  }

  @Override
  public Set<String> getKeySuggestions() {
    return keySuggestions;
  }

  @Override
  public Set<String> getValueSuggestions() {
    return valueSuggestions;
  }

}
