// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.components.kernel.PositionDeviationPolicy;
import org.opentcs.components.kernel.PositionDeviationPolicyFactory;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * Test for {@link PositionDeviationPolicyRegistry}.
 */
public class PositionDeviationPolicyRegistryTest {

  private VehiclePositionResolverConfiguration configuration;

  @BeforeEach
  public void setup() {
    configuration = mock(VehiclePositionResolverConfiguration.class);

    when(configuration.deviationXY()).thenReturn(0);
    when(configuration.deviationTheta()).thenReturn(0);
  }

  @Test
  void returnDefaultPolicyIfNoFactoriesRegistered() {
    PositionDeviationPolicyRegistry registry
        = new PositionDeviationPolicyRegistry(Set.of(), configuration);

    PositionDeviationPolicy policy
        = registry.getPolicyForVehicle(new Vehicle("some-vehicle"));

    assertThat(policy.allowedDeviationDistance(new Point("some-point")), is(0L));
    assertThat(policy.allowedDeviationAngle(new Point("some-point")), is(0L));
  }

  @Test
  void returnRegisteredPolicyIfMatchingForVehicle() {
    PositionDeviationPolicyFactory factory = vehicle -> {
      if (vehicle.getName().equals("matching-vehicle")) {
        return Optional.of(new FixedPositionDeviationPolicy(42, 23));
      }
      return Optional.empty();
    };
    PositionDeviationPolicyRegistry registry
        = new PositionDeviationPolicyRegistry(Set.of(factory), configuration);

    PositionDeviationPolicy policyForNonmatchingVehicle
        = registry.getPolicyForVehicle(new Vehicle("some-vehicle"));

    assertThat(
        policyForNonmatchingVehicle.allowedDeviationDistance(new Point("some-point")),
        is(0L)
    );
    assertThat(
        policyForNonmatchingVehicle.allowedDeviationAngle(new Point("some-point")),
        is(0L)
    );

    PositionDeviationPolicy policyForMatchingVehicle
        = registry.getPolicyForVehicle(new Vehicle("matching-vehicle"));

    assertThat(
        policyForMatchingVehicle.allowedDeviationDistance(new Point("some-point")),
        is(42L)
    );
    assertThat(
        policyForMatchingVehicle.allowedDeviationAngle(new Point("some-point")),
        is(23L)
    );
  }
}
