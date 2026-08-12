// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.ordermapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_INTERFACE_NAME;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_MANUFACTURER;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_SERIAL_NUMBER;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.opentcs.commadapter.vehicle.vda5050.common.OptionalParameterSupport;
import org.opentcs.data.model.Vehicle;


class UnsupportedPropertiesExtractorTest {

  @Test
  public void extractOptionalOrderMessageFieldsAndShortenKeyToJsonPath() {
    Vehicle someVehicle = new Vehicle("Some vehicle")
        .withProperty(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX
                + ".zoneSetId", "NOT_SUPPORTED"
        )
        .withProperty(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX
                + ".nodes.nodeDescription", "NOT_SUPPORTED"
        )
        .withProperty(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX
                + ".nodes.nodePosition.allowedDeviationTheta", "NOT_SUPPORTED"
        )
        .withProperty(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX
                + ".edges.maxSpeed", "NOT_SUPPORTED"
        );


    Map<String, OptionalParameterSupport> result
        = new UnsupportedPropertiesExtractor().apply(someVehicle);

    assertThat(result.entrySet(), hasSize(4));
    assertThat(
        result, hasEntry(
            "zoneSetId",
            OptionalParameterSupport.NOT_SUPPORTED
        )
    );
    assertThat(
        result, hasEntry(
            "nodes.nodeDescription",
            OptionalParameterSupport.NOT_SUPPORTED
        )
    );
    assertThat(
        result, hasEntry(
            "nodes.nodePosition.allowedDeviationTheta",
            OptionalParameterSupport.NOT_SUPPORTED
        )
    );
    assertThat(
        result, hasEntry(
            "edges.maxSpeed",
            OptionalParameterSupport.NOT_SUPPORTED
        )
    );
  }

  @Test
  public void shouldNotIncludeFieldsNotSpecifiedAsOptional() {
    Vehicle someVehicle = new Vehicle("Some vehicle")
        .withProperty(PROPKEY_VEHICLE_INTERFACE_NAME, "openTCS")
        .withProperty(PROPKEY_VEHICLE_MANUFACTURER, "iml")
        .withProperty(PROPKEY_VEHICLE_SERIAL_NUMBER, "0001")
        .withProperty(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX
                + ".edges.trajectory.degree", "NOT_SUPPORTED"
        );

    Map<String, OptionalParameterSupport> result
        = new UnsupportedPropertiesExtractor().apply(someVehicle);

    assertThat(result.entrySet(), hasSize(0));
    assertThat(
        result, not(
            hasEntry(
                "edges.trajectory.degree",
                OptionalParameterSupport.NOT_SUPPORTED
            )
        )
    );
  }
}
