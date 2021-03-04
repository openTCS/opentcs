/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.gui;

import java.awt.Component;
import javax.swing.JLabel;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import org.junit.*;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class StringTableCellRendererTest {

  private StringTableCellRenderer<TCSObjectReference<?>> renderer;

  @Before
  public void setUp() {
    renderer = new StringTableCellRenderer<>(x -> x == null ? "" : x.getName());
  }

  @Test
  public void returnsNullForNullValue() {
    Component result = renderer.getTableCellRendererComponent(null, null, false, false, 0, 0);
    assertThat(result, is(instanceOf(JLabel.class)));
    JLabel labelResult = (JLabel) result;
    assertThat(labelResult.getText(), is(equalTo("")));
  }

  @Test
  public void returnsLabelWithNameAsText() {
    Vehicle vehicle = new Vehicle("VehicleName");
    TCSObjectReference<Vehicle> vehicleReference = vehicle.getReference();
    Component result = renderer.getTableCellRendererComponent(null, vehicleReference, false, false, 0, 0);
    assertThat(result, is(instanceOf(JLabel.class)));
    JLabel labelResult = (JLabel) result;
    assertThat(labelResult.getText(), is(equalTo("VehicleName")));
  }

}
