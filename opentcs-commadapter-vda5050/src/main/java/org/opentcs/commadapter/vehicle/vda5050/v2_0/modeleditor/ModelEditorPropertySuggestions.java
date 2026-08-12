// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.modeleditor;

import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_CUSTOM_ACTION_PREFIX;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_CUSTOM_DEST_ACTION_PREFIX;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_EXECUTABLE_ACTIONS_TAGS;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_PATH_ORIENTATION_FORWARD;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_PATH_ORIENTATION_REVERSE;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_PATH_ROTATION_ALLOWED_FORWARD;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_PATH_ROTATION_ALLOWED_REVERSE;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_POINT_DEVIATION_THETA;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_POINT_DEVIATION_XY;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_POINT_MAP_ID;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_DEVIATION_THETA;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_DEVIATION_XY;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_INTERFACE_NAME;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_MANUFACTURER;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_MAP_ID;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_MAX_STEPS_BASE;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_MAX_STEPS_HORIZON;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_MIN_VISU_INTERVAL;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_RECHARGE_OPERATION;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_SERIAL_NUMBER;

import java.util.HashSet;
import java.util.Set;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.action.Drop;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.action.Pick;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.BlockingType;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.ordermapping.ActionTrigger;
import org.opentcs.components.plantoverview.PropertySuggestions;

/**
 * Property suggestions for the Model Editor application specific to the VDA5050 adapter.
 */
public class ModelEditorPropertySuggestions
    implements
      PropertySuggestions {

  private final Set<String> keySuggestions = new HashSet<>();
  private final Set<String> valueSuggestions = new HashSet<>();
  private final Set<String> actionTriggers = new HashSet<>();
  private final Set<String> blockingTypes = new HashSet<>();
  private final Set<String> parameterSuggestions = new HashSet<>();

  public ModelEditorPropertySuggestions() {

    keySuggestions.add(PROPKEY_CUSTOM_ACTION_PREFIX + ".<INDEX>");
    keySuggestions.add(PROPKEY_CUSTOM_ACTION_PREFIX + ".<INDEX>.blockingType");
    keySuggestions.add(PROPKEY_CUSTOM_ACTION_PREFIX + ".<INDEX>.parameter.<PARAMETER>");
    keySuggestions.add(PROPKEY_CUSTOM_ACTION_PREFIX + ".<INDEX>.tags");
    keySuggestions.add(PROPKEY_CUSTOM_ACTION_PREFIX + ".<INDEX>.when");
    keySuggestions.add(PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".<ACTIONTYPE>.blockingType");
    keySuggestions.add(PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".<ACTIONTYPE>.parameter.<PARAMETER>");
    keySuggestions.add(PROPKEY_EXECUTABLE_ACTIONS_TAGS);
    keySuggestions.add(PROPKEY_VEHICLE_DEVIATION_XY);
    keySuggestions.add(PROPKEY_VEHICLE_DEVIATION_THETA);
    keySuggestions.add(PROPKEY_VEHICLE_INTERFACE_NAME);
    keySuggestions.add(PROPKEY_VEHICLE_MANUFACTURER);
    keySuggestions.add(PROPKEY_VEHICLE_MAP_ID);
    keySuggestions.add(PROPKEY_VEHICLE_MAX_STEPS_BASE);
    keySuggestions.add(PROPKEY_VEHICLE_MAX_STEPS_HORIZON);
    keySuggestions.add(PROPKEY_VEHICLE_MIN_VISU_INTERVAL);
    keySuggestions.add(PROPKEY_VEHICLE_RECHARGE_OPERATION);
    keySuggestions.add(PROPKEY_VEHICLE_SERIAL_NUMBER);
    keySuggestions.add(PROPKEY_PATH_ORIENTATION_FORWARD);
    keySuggestions.add(PROPKEY_PATH_ORIENTATION_REVERSE);
    keySuggestions.add(PROPKEY_PATH_ROTATION_ALLOWED_FORWARD);
    keySuggestions.add(PROPKEY_PATH_ROTATION_ALLOWED_REVERSE);
    keySuggestions.add(PROPKEY_POINT_MAP_ID);
    keySuggestions.add(PROPKEY_POINT_DEVIATION_XY);
    keySuggestions.add(PROPKEY_POINT_DEVIATION_THETA);
    // pick parameters
    keySuggestions.add(
        PROPKEY_CUSTOM_ACTION_PREFIX + ".<INDEX>.parameter." + Pick.PARAMKEY_LOAD_HANDLING_DEVICE
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_ACTION_PREFIX + ".<INDEX>.parameter." + Pick.PARAMKEY_STATION_TYPE
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_ACTION_PREFIX + ".<INDEX>.parameter." + Pick.PARAMKEY_STATION_NAME
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_ACTION_PREFIX + ".<INDEX>.parameter." + Pick.PARAMKEY_LOAD_TYPE
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_ACTION_PREFIX + ".<INDEX>.parameter." + Pick.PARAMKEY_LOAD_ID
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_ACTION_PREFIX + ".<INDEX>.parameter." + Pick.PARAMKEY_HEIGHT
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_ACTION_PREFIX + ".<INDEX>.parameter." + Pick.PARAMKEY_DEPTH
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_ACTION_PREFIX + ".<INDEX>.parameter." + Pick.PARAMKEY_SIDE
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".pick.parameter." + Pick.PARAMKEY_LOAD_HANDLING_DEVICE
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".pick.parameter." + Pick.PARAMKEY_STATION_TYPE
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".pick.parameter." + Pick.PARAMKEY_STATION_NAME
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".pick.parameter." + Pick.PARAMKEY_LOAD_TYPE
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".pick.parameter." + Pick.PARAMKEY_LOAD_ID
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".pick.parameter." + Pick.PARAMKEY_HEIGHT
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".pick.parameter." + Pick.PARAMKEY_DEPTH
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".pick.parameter." + Pick.PARAMKEY_SIDE
    );
    // drop parameters
    keySuggestions.add(
        PROPKEY_CUSTOM_ACTION_PREFIX + ".<INDEX>.parameter." + Drop.PARAMKEY_LOAD_HANDLING_DEVICE
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_ACTION_PREFIX + ".<INDEX>.parameter." + Drop.PARAMKEY_STATION_TYPE
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_ACTION_PREFIX + ".<INDEX>.parameter." + Drop.PARAMKEY_STATION_NAME
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_ACTION_PREFIX + ".<INDEX>.parameter." + Drop.PARAMKEY_LOAD_TYPE
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_ACTION_PREFIX + ".<INDEX>.parameter." + Drop.PARAMKEY_LOAD_ID
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_ACTION_PREFIX + ".<INDEX>.parameter." + Drop.PARAMKEY_HEIGHT
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_ACTION_PREFIX + ".<INDEX>.parameter." + Drop.PARAMKEY_DEPTH
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_ACTION_PREFIX + ".<INDEX>.parameter." + Drop.PARAMKEY_SIDE
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".drop.parameter." + Drop.PARAMKEY_LOAD_HANDLING_DEVICE
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".drop.parameter." + Drop.PARAMKEY_STATION_TYPE
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".drop.parameter." + Drop.PARAMKEY_STATION_NAME
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".drop.parameter." + Drop.PARAMKEY_LOAD_TYPE
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".drop.parameter." + Drop.PARAMKEY_LOAD_ID
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".drop.parameter." + Drop.PARAMKEY_HEIGHT
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".drop.parameter." + Drop.PARAMKEY_DEPTH
    );
    keySuggestions.add(
        PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".drop.parameter." + Drop.PARAMKEY_SIDE
    );

    for (ActionTrigger trigger : ActionTrigger.values()) {
      actionTriggers.add(trigger.name());
    }
    for (BlockingType type : BlockingType.values()) {
      blockingTypes.add(type.name());
    }
    parameterSuggestions.add("string:myString");
    parameterSuggestions.add("float:1.0");
    parameterSuggestions.add("integer:1");
    parameterSuggestions.add("boolean:false");
  }

  @Override
  public Set<String> getKeySuggestions() {
    return keySuggestions;
  }

  @Override
  public Set<String> getValueSuggestions() {
    return valueSuggestions;
  }

  @Override
  public Set<String> getValueSuggestionsFor(String key) {
    if (key.startsWith(PROPKEY_CUSTOM_ACTION_PREFIX)
        || key.startsWith(PROPKEY_CUSTOM_DEST_ACTION_PREFIX)) {
      if (key.endsWith(".when")) {
        return actionTriggers;
      }
      else if (key.endsWith(".blockingType")) {
        return blockingTypes;
      }
      else if (key.contains(".parameter.")) {
        return parameterSuggestions;
      }
    }
    return Set.of();
  }
}
