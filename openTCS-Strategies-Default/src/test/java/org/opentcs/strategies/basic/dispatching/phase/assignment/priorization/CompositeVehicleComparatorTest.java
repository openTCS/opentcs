/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.assignment.priorization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.theInstance;
import org.junit.*;
import static org.junit.Assert.assertThat;
import org.mockito.Mockito;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeVehicleComparator;
import org.opentcs.strategies.basic.dispatching.priorization.vehicle.VehicleComparatorIdleFirst;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class CompositeVehicleComparatorTest {

  private CompositeVehicleComparator comparator;
  private DefaultDispatcherConfiguration configuration;
  private Map<String, Comparator<Vehicle>> availableComparators;

  @Before
  public void setUp() {
    configuration = Mockito.mock(DefaultDispatcherConfiguration.class);
    availableComparators = new HashMap<>();

  }

  @Test
  public void sortNamesUpForOtherwiseEqualInstances() {
    Mockito.when(configuration.vehiclePriorities())
        .thenReturn(new LinkedList<>());
    comparator = new CompositeVehicleComparator(configuration, availableComparators);

    Vehicle candidate1 = new Vehicle("AA");
    Vehicle candidate2 = new Vehicle("CC");
    Vehicle candidate3 = new Vehicle("AB");

    List<Vehicle> list = new ArrayList<>();
    list.add(candidate1);
    list.add(candidate2);
    list.add(candidate3);

    Collections.sort(list, comparator);

    assertThat(list.get(0), is(theInstance(candidate1)));
    assertThat(list.get(1), is(theInstance(candidate3)));
    assertThat(list.get(2), is(theInstance(candidate2)));
  }

  @Test
  public void sortsByNameAndEnergylevel() {
    Mockito.when(configuration.vehiclePriorities())
        .thenReturn(new LinkedList<>());
    comparator = new CompositeVehicleComparator(configuration, availableComparators);

    Vehicle candidate1 = new Vehicle("AA").withEnergyLevel(1);
    Vehicle candidate2 = new Vehicle("CC").withEnergyLevel(2);
    Vehicle candidate3 = new Vehicle("BB").withEnergyLevel(2);

    List<Vehicle> list = new ArrayList<>();
    list.add(candidate1);
    list.add(candidate2);
    list.add(candidate3);

    Collections.sort(list, comparator);

    assertThat(list.get(0), is(theInstance(candidate3)));
    assertThat(list.get(1), is(theInstance(candidate2)));
    assertThat(list.get(2), is(theInstance(candidate1)));
  }

  @Test
  public void sortsByNameEnergylevelRoutingCost() {

    Mockito.when(configuration.vehiclePriorities())
        .thenReturn(Arrays.asList("IDLE_FIRST"));
    availableComparators.put("IDLE_FIRST",
                             new VehicleComparatorIdleFirst());

    comparator = new CompositeVehicleComparator(configuration, availableComparators);

    Vehicle candidate1 = new Vehicle("AA").withEnergyLevel(30).withState(Vehicle.State.EXECUTING);
    Vehicle candidate2 = new Vehicle("BB").withEnergyLevel(30).withState(Vehicle.State.IDLE);
    Vehicle candidate3 = new Vehicle("CC").withEnergyLevel(60).withState(Vehicle.State.IDLE);
    Vehicle candidate4 = new Vehicle("DD").withEnergyLevel(60).withState(Vehicle.State.IDLE);

    List<Vehicle> list = new ArrayList<>();
    list.add(candidate1);
    list.add(candidate2);
    list.add(candidate3);
    list.add(candidate4);

    Collections.sort(list, comparator);

    assertThat(list.get(0), is(theInstance(candidate3)));
    assertThat(list.get(1), is(theInstance(candidate4)));
    assertThat(list.get(2), is(theInstance(candidate2)));
    assertThat(list.get(3), is(theInstance(candidate1)));
  }
}
