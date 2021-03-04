/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model.visualization;

import java.io.Serializable;
import java.util.Objects;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A bookmarked view on the layout.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@Deprecated
@ScheduledApiChange(when = "5.0")
public class ViewBookmark
    implements Serializable {

  /**
   * The X coordinate of the bookmarked position.
   */
  private int centerX;
  /**
   * The Y coordinate of the bookmarked position.
   */
  private int centerY;
  /**
   * A scale/zoom factor for the X axis.
   */
  private double viewScaleX = 1.0;
  /**
   * A scale/zoom factor for the Y axis.
   */
  private double viewScaleY = 1.0;
  /**
   * A rotation angle for the view (in degrees, 0 meaning no rotation).
   */
  private int viewRotation;
  /**
   * A label/name for the bookmarked view.
   */
  private String label = "";

  /**
   * Creates a new ViewBookmark.
   */
  public ViewBookmark() {
    // Do nada.
  }

  /**
   * Returns a label/name for this bookmarked view.
   *
   * @return A label/name for this bookmarked view.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Sets a label/name for this bookmarked view.
   *
   * @param label The new label.
   */
  public void setLabel(String label) {
    this.label = Objects.requireNonNull(label, "label is null");
  }

  /**
   * Returns this bookmarked view's X coordinate.
   *
   * @return This bookmarked view's X coordinate.
   */
  public int getCenterX() {
    return centerX;
  }

  /**
   * Sets this bookmarked view's X coordinate.
   *
   * @param centerX The new X coordinate.
   */
  public void setCenterX(int centerX) {
    this.centerX = centerX;
  }

  /**
   * Returns this bookmarked view's X coordinate.
   *
   * @return This bookmarked view's X coordinate.
   */
  public int getCenterY() {
    return centerY;
  }

  /**
   * Sets this bookmarked view's Y coordinate.
   *
   * @param centerY The new Y coordinate.
   */
  public void setCenterY(int centerY) {
    this.centerY = centerY;
  }

  /**
   * Returns this bookmarked view's scale/zoom factor for the X axis.
   *
   * @return This bookmarked view's scale/zoom factor for the X axis.
   */
  public double getViewScaleX() {
    return viewScaleX;
  }

  /**
   * Sets this bookmarked view's scale/zoom factor for the X axis.
   *
   * @param viewScaleX The new scale/zoom factor.
   */
  public void setViewScaleX(double viewScaleX) {
    this.viewScaleX = viewScaleX;
  }

  /**
   * Returns this bookmarked view's scale/zoom factor for the Y axis.
   *
   * @return This bookmarked view's scale/zoom factor for the Y axis.
   */
  public double getViewScaleY() {
    return viewScaleY;
  }

  /**
   * Sets this bookmarked view's scale/zoom factor for the Y axis.
   *
   * @param viewScaleY The new scale/zoom factor.
   */
  public void setViewScaleY(double viewScaleY) {
    this.viewScaleY = viewScaleY;
  }

  /**
   * Returns this bookmarked view's rotation angle (in degrees, 0 meaning no
   * rotation).
   *
   * @return This view's rotation angle.
   */
  public int getViewRotation() {
    return viewRotation;
  }

  /**
   * Sets this bookmarked view's rotation angle (in degrees, 0 meaning no
   * rotation).
   *
   * @param viewRotation The new rotation angle.
   */
  public void setViewRotation(int viewRotation) {
    this.viewRotation = viewRotation;
  }
}
