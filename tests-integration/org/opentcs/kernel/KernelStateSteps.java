/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Pending;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.access.UnsupportedKernelOpException;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * Some steps for testing the kernel's state transitions.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public class KernelStateSteps {

  public KernelStateSteps() {
    // Nothing to do here...
  }

  private LocalKernel kernel;

  @Given("a local kernel without extensions")
  public void givenALocalKernelWithoutExtensions() {
    Injector injector = Guice.createInjector(new KernelInjectionModule());
    kernel = injector.getInstance(LocalKernel.class);
    kernel.initialize();
  }

  @Given("a minimal model")
  public void givenAMinimalModel() {
    TestModelGenerator modelGen = new TestModelGenerator(kernel);
    modelGen.createRingModel(5);
  }

  @Given("a vehicle named $name")
  public void givenAVehicleNamed(String name) {
    TCSObjectReference<Vehicle> vehicle = kernel.createVehicle().getReference();
    kernel.renameTCSObject(vehicle, name);
  }

  @Given("a vehicle driver associated with vehicle $vehicle")
  @Pending
  public void givenAVehicleDriverForVehicle(String vehicle) {

  }

  @When("I change the kernel's state to $state")
  public void whenIChangeTheKernelsStateTo(@Named("newState") Kernel.State state) {
    kernel.setState(state);
  }

  @When("I create a point named $name")
  public void whenICreateAPointNamed(String name) {
    Point point = kernel.createPoint();
    kernel.renameTCSObject(point.getReference(), name);
  }

  @When("the vehicle driver for vehicle $vehicle reports state $state")
  @Pending
  public void whenVehicleDriverReportsState(String vehicle, Kernel.State state) {

  }

  @Then("the kernel should have a point named $name")
  public void thenTheKernelShouldHaveAPointNamed(String name) {
    assertNotNull(kernel.getTCSObject(Point.class, name));
  }

  @Then("the kernel's reported state should be $state")
  public void thenTheKernelsReportedStateShouldBe(
      @Named("actualState") Kernel.State state) {
    assertEquals(state, kernel.getState());
  }

  @Then("creating a point named $name should fail with an exception")
  public void thenCreatingAPointShouldFail(String name) {
    try {
      Point point = kernel.createPoint();
      kernel.renameTCSObject(point.getReference(), name);
    }
    catch (CredentialsException |
        ObjectExistsException |
        ObjectUnknownException |
        UnsupportedKernelOpException e) {
      return;
    }
    fail("Expected exception not thrown.");
  }

  @Then("the state of vehicle $vehicle as returned by the kernel should be $state")
  public void thenVehicleShouldHaveState(String vehicle, Kernel.State state) {
    Vehicle v = kernel.getTCSObject(Vehicle.class, vehicle);
    assertEquals(state, v.getState());
  }

  @AfterScenario
  public void afterEachScenario() {
    if (kernel != null) {
      kernel.setState(Kernel.State.SHUTDOWN);
    }
  }
}
