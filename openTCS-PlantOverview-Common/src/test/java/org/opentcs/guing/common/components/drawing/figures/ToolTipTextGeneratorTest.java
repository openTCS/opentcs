/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.drawing.figures;

import java.util.ArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.guing.base.components.properties.type.KeyValueProperty;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.common.model.SystemModel;
import org.opentcs.guing.common.persistence.ModelManager;

/**
 * Tests the {@link ToolTipTextGenerator}.
 */
class ToolTipTextGeneratorTest {

  private ModelManager modelManager;

  private ToolTipTextGenerator toolTipTextGenerator;

  @BeforeEach
  void setUp() {
    SystemModel systemModel = mock(SystemModel.class);
    when(systemModel.getBlockModels()).thenReturn(new ArrayList<>());

    modelManager = mock(ModelManager.class);
    when(modelManager.getModel()).thenReturn(systemModel);
    toolTipTextGenerator = new ToolTipTextGenerator(modelManager);
  }

  @Test
  void sortsPropertiesLexicographically() {
    final String propKey1 = "prop1";
    final String propKey2 = "prop2";
    final String propKey3 = "prop3";
    final String propkey4 = "prop4";

    PointModel pointModel = new PointModel();
    pointModel.getPropertyMiscellaneous().addItem(new KeyValueProperty(pointModel,
                                                                       propkey4,
                                                                       "some-value"));
    pointModel.getPropertyMiscellaneous().addItem(new KeyValueProperty(pointModel,
                                                                       propKey2,
                                                                       "some-value"));
    pointModel.getPropertyMiscellaneous().addItem(new KeyValueProperty(pointModel,
                                                                       propKey3,
                                                                       "some-value"));
    pointModel.getPropertyMiscellaneous().addItem(new KeyValueProperty(pointModel,
                                                                       propKey1,
                                                                       "some-value"));

    String toolTipText = toolTipTextGenerator.getToolTipText(pointModel);

    assertThat(toolTipText.indexOf(propKey1), is(lessThan(toolTipText.indexOf(propKey2))));
    assertThat(toolTipText.indexOf(propKey2), is(lessThan(toolTipText.indexOf(propKey3))));
    assertThat(toolTipText.indexOf(propKey3), is(lessThan(toolTipText.indexOf(propkey4))));
  }
}
