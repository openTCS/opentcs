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
import javax.swing.JCheckBoxMenuItem;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.model.elements.VehicleModel;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class FollowVehicleAction
    extends AbstractAction {

  /**
   * Automatically moves the drawing so a vehicle is always visible.
   */
  public static final String ID = "course.vehicle.follow";
  /**
   * The vehicle.
   */
  private final VehicleModel vehicleModel;
  /**
   * The drawing editor.
   */
  private final OpenTCSDrawingEditor drawingEditor;

  /**
   * Creates a new instance.
   *
   * @param vehicle The selected vehicle.
   * @param drawingEditor The application's drawing editor.
   */
  @Inject
  public FollowVehicleAction(@Assisted VehicleModel vehicle,
                             OpenTCSDrawingEditor drawingEditor) {
    this.vehicleModel = requireNonNull(vehicle, "vehicle");
    this.drawingEditor = requireNonNull(drawingEditor, "drawingEditor");
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    JCheckBoxMenuItem checkBox = (JCheckBoxMenuItem) evt.getSource();
    OpenTCSDrawingView drawingView = drawingEditor.getActiveView();

    if (drawingView != null) {
      if (checkBox.isSelected()) {
        drawingView.followVehicle(vehicleModel);
      }
      else {
        drawingView.stopFollowVehicle();
      }
    }
  }

}
