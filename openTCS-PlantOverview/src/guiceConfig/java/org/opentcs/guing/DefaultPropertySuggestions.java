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
import org.opentcs.common.LoopbackAdapterConstants;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.plantoverview.PropertySuggestions;

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
    keySuggestions.add(Router.PROPKEY_ROUTING_GROUP);
    keySuggestions.add(Dispatcher.PROPKEY_ASSIGNED_PARKING_POSITION);
    keySuggestions.add(Dispatcher.PROPKEY_PREFERRED_PARKING_POSITION);
    keySuggestions.add(Dispatcher.PROPKEY_ASSIGNED_RECHARGE_LOCATION);
    keySuggestions.add(Dispatcher.PROPKEY_PREFERRED_RECHARGE_LOCATION);
    keySuggestions.add(LoopbackAdapterConstants.PROPKEY_INITIAL_POSITION);
    keySuggestions.add(LoopbackAdapterConstants.PROPKEY_OPERATING_TIME);
    keySuggestions.add(LoopbackAdapterConstants.PROPKEY_LOAD_OPERATION);
    keySuggestions.add(LoopbackAdapterConstants.PROPKEY_UNLOAD_OPERATION);
    keySuggestions.add(LoopbackAdapterConstants.PROPKEY_ACCELERATION);
    keySuggestions.add(LoopbackAdapterConstants.PROPKEY_DECELERATION);

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
