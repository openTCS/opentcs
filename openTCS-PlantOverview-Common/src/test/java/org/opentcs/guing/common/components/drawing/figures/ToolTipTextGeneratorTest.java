/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.drawing.figures;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import org.junit.*;
import org.mockito.Mockito;
import org.opentcs.guing.base.components.properties.type.KeyValueProperty;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.persistence.ModelManager;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ToolTipTextGeneratorTest {

  private ModelManager modelManager;

  private ToolTipTextGenerator toolTipTextGenerator;

  @Before
  public void setUp() {
    modelManager = Mockito.mock(ModelManager.class);
    toolTipTextGenerator = new ToolTipTextGenerator(modelManager);
  }

  @Test
  public void sortsPropertiesLexicographically() {
    final String PROP1_KEY = "prop1";
    final String PROP2_KEY = "prop2";
    final String PROP3_KEY = "prop3";
    final String PROP4_KEY = "prop4";

    VehicleModel vehicleModel = new VehicleModel();
    vehicleModel.getPropertyMiscellaneous().addItem(new KeyValueProperty(vehicleModel,
                                                                         PROP4_KEY,
                                                                         "some-value"));
    vehicleModel.getPropertyMiscellaneous().addItem(new KeyValueProperty(vehicleModel,
                                                                         PROP2_KEY,
                                                                         "some-value"));
    vehicleModel.getPropertyMiscellaneous().addItem(new KeyValueProperty(vehicleModel,
                                                                         PROP3_KEY,
                                                                         "some-value"));
    vehicleModel.getPropertyMiscellaneous().addItem(new KeyValueProperty(vehicleModel,
                                                                         PROP1_KEY,
                                                                         "some-value"));

    String toolTipText = toolTipTextGenerator.getToolTipText(vehicleModel);

    assertThat(toolTipText.indexOf(PROP1_KEY), is(lessThan(toolTipText.indexOf(PROP2_KEY))));
    assertThat(toolTipText.indexOf(PROP2_KEY), is(lessThan(toolTipText.indexOf(PROP3_KEY))));
    assertThat(toolTipText.indexOf(PROP3_KEY), is(lessThan(toolTipText.indexOf(PROP4_KEY))));
  }
}
