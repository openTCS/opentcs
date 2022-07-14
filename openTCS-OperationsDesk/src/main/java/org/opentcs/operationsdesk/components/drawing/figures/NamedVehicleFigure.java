/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.components.drawing.figures;

import com.google.inject.assistedinject.Assisted;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import org.jhotdraw.draw.Figure;
import org.opentcs.components.plantoverview.VehicleTheme;
import org.opentcs.data.model.Triple;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.application.ApplicationState;
import org.opentcs.guing.common.components.drawing.figures.FigureOrdinals;
import org.opentcs.guing.common.components.drawing.figures.ToolTipTextGenerator;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.operationsdesk.application.menus.MenuFactory;
import org.opentcs.operationsdesk.util.PlantOverviewOperatingApplicationConfiguration;

/**
 * A vehicle figure that adds the name of the vehicle into the image.
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class NamedVehicleFigure
    extends VehicleFigure {

  @Inject
  public NamedVehicleFigure(VehicleTheme vehicleTheme,
                            MenuFactory menuFactory,
                            PlantOverviewOperatingApplicationConfiguration appConfig,
                            @Assisted VehicleModel model,
                            ToolTipTextGenerator textGenerator,
                            ModelManager modelManager,
                            ApplicationState applicationState) {
    super(vehicleTheme,
          menuFactory,
          appConfig,
          model,
          textGenerator,
          modelManager,
          applicationState);
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

  @Override
  protected void updateFigureDetails(VehicleModel model) {
    super.updateFigureDetails(model);

    PointModel point = model.getPoint();
    Triple precisePosition = model.getPrecisePosition();

    if (point == null && precisePosition == null) {
      // If neither the point nor the precise position is known, don't draw the figure.
      SwingUtilities.invokeLater(() -> setVisible(false));
    }
    else if (precisePosition != null && !isIgnorePrecisePosition()) {
      // If a precise position exists, it is set in setBounds(), so it doesn't need any coordinates.
      SwingUtilities.invokeLater(() -> {
        setVisible(true);
        setBounds(new Point2D.Double(), null);
      });

      setFigureDetailsChanged(true);
    }
    else if (point != null) {
      SwingUtilities.invokeLater(() -> {
        setVisible(true);
        Figure pointFigure = getModelManager().getModel().getFigure(point);
        Rectangle2D.Double r = pointFigure.getBounds();
        Point2D.Double pCenter = new Point2D.Double(r.getCenterX(), r.getCenterY());
        // Draw figure in the center of the node.
        // Angle is set in setBounds().
        setBounds(pCenter, null);
      });

      setFigureDetailsChanged(true);
    }
    else {
      SwingUtilities.invokeLater(() -> setVisible(false));
    }
  }

  @Override
  public boolean isVisible() {
    if (getModel().getPoint() == null) {
      return super.isVisible();
    }

    return super.isVisible()
        && getModelManager().getModel().getFigure(getModel().getPoint()).isVisible();
  }

  @Override
  public int getLayer() {
    return FigureOrdinals.VEHICLE_FIGURE_ORDINAL;
  }
}
