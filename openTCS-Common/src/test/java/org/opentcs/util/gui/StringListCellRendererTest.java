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
import javax.swing.JList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;

/**
 */
class StringListCellRendererTest {

  private JList<TCSObjectReference<?>> list;
  private StringListCellRenderer<TCSObjectReference<?>> renderer;

  @BeforeEach
  void setUp() {
    list = new JList<>();
    renderer = new StringListCellRenderer<>(x -> x == null ? "" : x.getName());
  }

  /**
   * Test of getListCellRendererComponent method, of class FunctionalListCellRenderer.
   */
  @Test
  void returnsNullForNullValue() {
    Component result = renderer.getListCellRendererComponent(list, null, 0, true, true);
    assertThat(result, is(instanceOf(JLabel.class)));
    JLabel labelResult = (JLabel) result;
    assertThat(labelResult.getText(), is(equalTo("")));
  }

  @Test
  void returnsLabelWithNameAsText() {
    Vehicle vehicle = new Vehicle("VehicleName");
    TCSObjectReference<Vehicle> vehicleReference = vehicle.getReference();
    Component result = renderer.getListCellRendererComponent(list, vehicleReference, 0, true, true);
    assertThat(result, is(instanceOf(JLabel.class)));
    JLabel labelResult = (JLabel) result;
    assertThat(labelResult.getText(), is(equalTo("VehicleName")));
  }

}
