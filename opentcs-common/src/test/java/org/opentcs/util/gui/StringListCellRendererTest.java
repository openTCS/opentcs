// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.gui;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
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
