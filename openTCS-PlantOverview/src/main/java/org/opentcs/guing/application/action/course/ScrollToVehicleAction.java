/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.course;

import com.google.inject.assistedinject.Assisted;
import java.awt.event.ActionEvent;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import org.jhotdraw.draw.Figure;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.persistence.ModelManager;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ScrollToVehicleAction
    extends AbstractAction {

  /**
   * Scrolls to a vehicle in the drawing.
   */
  public static final String ID = "course.vehicle.scrollTo";
  /**
   * The vehicle.
   */
  private final VehicleModel vehicleModel;
  /**
   * The drawing editor.
   */
  private final OpenTCSDrawingEditor drawingEditor;
  /**
   * The model manager.
   */
  private final ModelManager modelManager;

  /**
   * Creates a new instance.
   *
   * @param vehicle The selected vehicle.
   * @param drawingEditor The application's drawing editor.
   * @param modelManager The model manager.
   */
  @Inject
  public ScrollToVehicleAction(@Assisted VehicleModel vehicle,
                               OpenTCSDrawingEditor drawingEditor,
                               ModelManager modelManager) {
    this.vehicleModel = requireNonNull(vehicle, "vehicle");
    this.drawingEditor = requireNonNull(drawingEditor, "drawingEditor");
    this.modelManager = requireNonNull(modelManager, "modelManager");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Figure figure = modelManager.getModel().getFigure(vehicleModel);
    OpenTCSDrawingView drawingView = drawingEditor.getActiveView();

    if (drawingView != null && figure != null) {
      drawingView.clearSelection();
      drawingView.addToSelection(figure);
      drawingView.scrollTo(figure);
    }
  }
}
