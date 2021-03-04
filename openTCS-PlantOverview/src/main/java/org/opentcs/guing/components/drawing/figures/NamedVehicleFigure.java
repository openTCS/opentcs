/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.drawing.figures;

import com.google.inject.assistedinject.Assisted;
import java.awt.Graphics2D;
import javax.inject.Inject;
import org.opentcs.components.plantoverview.VehicleTheme;
import org.opentcs.guing.application.menus.MenuFactory;
import org.opentcs.guing.components.properties.SelectionPropertiesComponent;
import org.opentcs.guing.components.tree.ComponentsTreeViewManager;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.util.PlantOverviewApplicationConfiguration;

/**
 * A vehicle figure that adds the name of the vehicle into the image.
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class NamedVehicleFigure
    extends VehicleFigure {

  @Inject
  public NamedVehicleFigure(ComponentsTreeViewManager componentsTreeManager,
                            SelectionPropertiesComponent propertiesComponent,
                            VehicleTheme vehicleTheme,
                            MenuFactory menuFactory,
                            PlantOverviewApplicationConfiguration appConfig,
                            @Assisted VehicleModel model,
                            ToolTipTextGenerator textGenerator) {
    super(componentsTreeManager,
          propertiesComponent,
          vehicleTheme,
          menuFactory,
          appConfig,
          model,
          textGenerator);
  }

  @Override
  protected void drawFill(Graphics2D g2d) {
    super.drawFill(g2d);
    g2d.setFont(getVehicleTheme().labelFont());
    g2d.setPaint(getVehicleTheme().labelColor());
    g2d.drawString(getVehicleTheme().label(getModel().getVehicle()),
                   (int) displayBox().getCenterX() + getVehicleTheme().labelOffsetX(),
                   (int) displayBox().getCenterY() + getVehicleTheme().labelOffsetY());
  }
}
