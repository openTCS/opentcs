/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.virtualvehicle;

import org.junit.*;

/**
 *
 * @author Hubert Buechter (Fraunhofer IML)
 */
public class SimulatingEnergyStorageTest {
  
  public SimulatingEnergyStorageTest() {
  }
  
  @Test
  public void testInvariants() {
    final double ENERGY = 123456.7;
    final double POWER = 12.34;
    final int DURATION_MS = 10000;
    int energyLevel;
    SimulatingEnergyStorage energyStorage;
    
    // energy and energyLevel invariant
    energyStorage = new SimulatingEnergyStorage(ENERGY);
    energyLevel = energyStorage.getEnergyLevel();
    energyStorage.discharge(ENERGY/2);
    energyStorage.charge(ENERGY/2);
    Assert.assertTrue(energyStorage.getEnergy() == ENERGY);
    Assert.assertTrue(energyStorage.getEnergyLevel() == energyLevel);

    // power and energyLevel invariant
    energyStorage = new SimulatingEnergyStorage(ENERGY);
    energyLevel = energyStorage.getEnergyLevel();
    energyStorage.discharge(POWER, DURATION_MS);
    energyStorage.charge(POWER, DURATION_MS);

    Assert.assertTrue(energyStorage.getEnergy() == ENERGY);
    Assert.assertTrue(energyStorage.getEnergyLevel() == energyLevel);
  }
    
  public void testLimits() {
    final double AMOUNT = 12.1;
    final int TICKS = 100;
    SimulatingEnergyStorage energyStorage1 = new SimulatingEnergyStorage(AMOUNT * TICKS);
    SimulatingEnergyStorage energyStorage2 = new SimulatingEnergyStorage(AMOUNT * TICKS);
  
    for (int i=1; i<TICKS; i++) {
      energyStorage1.charge(AMOUNT);
    }
  }
  
  @Before
  public void setUp() {
  }
  
  @After
  public void tearDown() {
  }
}
