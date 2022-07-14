/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.drawing;

import java.awt.Point;
import java.io.File;
import java.util.Set;
import javax.annotation.Nonnull;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Figure;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.components.drawing.figures.BitmapFigure;
import org.opentcs.util.event.EventHandler;

/**
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface OpenTCSDrawingView
    extends DrawingView,
            EventHandler {

  boolean isLabelsVisible();

  void setLabelsVisible(boolean newValue);

  /**
   * Called when the drawing options have changed.
   */
  void drawingOptionsChanged();

  /**
   * Returns if a given point on the screen is contained in this drawing view.
   *
   * @param p The reference point on the screen.
   * @return Boolean if this point is contained.
   */
  boolean containsPointOnScreen(Point p);

  /**
   * Adds a background image to this drawing view.
   *
   * @param file The file with the image.
   */
  void addBackgroundBitmap(File file);

  /**
   * Adds a background image to this drawing view.
   *
   * @param bitmapFigure The figure containing the image.
   */
  void addBackgroundBitmap(BitmapFigure bitmapFigure);

  /**
   * Scales the view to a value so the whole model fits.
   */
  void zoomViewToWindow();

  /**
   * Sets the elements of the blocks.
   *
   * @param blocks A <code>ModelComponent</code> which childs must be <code>BlockModels</code>.
   */
  void setBlocks(ModelComponent blocks);

  /**
   * Shows or hides the current route of a vehicle.
   *
   * @param vehicle The vehicle
   * @param visible <code>true</code> to set it to visible, <code>false</code> otherwise.
   */
  void displayDriveOrders(VehicleModel vehicle, boolean visible);

  /**
   * Updates the figures of a block.
   *
   * @param block The block.
   */
  void updateBlock(BlockModel block);

  /**
   * Scrolls to the given figure. Normally called when the user clicks on a model component in the
   * TreeView and wants to see the corresponding figure.
   *
   * @param figure The figure to be scrolled to.
   */
  void scrollTo(Figure figure);

  /**
   * Fixes the view on the vehicle and marks it and its destination with a colored circle.
   *
   * @param model The vehicle model.
   */
  void followVehicle(@Nonnull final VehicleModel model);

  /**
   * Releases the view and stops following the current vehicle.
   */
  void stopFollowVehicle();

  /**
   * Deletes the given model components from the drawing view.
   *
   * @param components The components to delete.
   */
  void delete(Set<ModelComponent> components);
}
