/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model.visualization;

import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Defines some reserved/commonly used property keys of
 * {@link LayoutElement LayoutElements}.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface ElementPropKeys {

  /**
   * X coordinate at which the point is to be drawn (in mm).
   * Type: int.
   * Default value: Physical coordinate of the point.
   */
  String POINT_POS_X = "POSITION_X";
  /**
   * Y coordinate at which the point is to be drawn (in mm).
   * Type: int.
   * Default value: Physical coordinate of the point.
   */
  String POINT_POS_Y = "POSITION_Y";
  /**
   * X offset of the label's position to the object's position (in lu).
   * Type: int.
   * Default value: ??
   */
  String POINT_LABEL_OFFSET_X = "LABEL_OFFSET_X";
  /**
   * Y offset of the label's position to the object's position (in lu).
   * Type: int.
   * Default value: ??
   */
  String POINT_LABEL_OFFSET_Y = "LABEL_OFFSET_Y";
  /**
   * Orientation angle of the label (in degrees).
   * Type: int [0..360].
   * Default value: 0.
   */
  String POINT_LABEL_ORIENTATION_ANGLE = "LABEL_ORIENTATION_ANGLE";
  /**
   * X coordinate at which the location is to be drawn (in mm).
   * Type: int.
   * Default value: ??.
   */
  String LOC_POS_X = "POSITION_X";
  /**
   * Y coordinate at which the location is to be drawn (in mm).
   * Type: int.
   * Default value: ??.
   */
  String LOC_POS_Y = "POSITION_Y";
  /**
   * X offset of the label's position to the object's position (in lu).
   * Type: int.
   * Default value: ??
   */
  String LOC_LABEL_OFFSET_X = "LABEL_OFFSET_X";
  /**
   * Y offset of the label's position to the object's position (in lu).
   * Type: int.
   * Default value: ??
   */
  String LOC_LABEL_OFFSET_Y = "LABEL_OFFSET_Y";
  /**
   * Orientation angle of the label (in degrees).
   * Type: int [0..360].
   * Default value: 0.
   */
  String LOC_LABEL_ORIENTATION_ANGLE = "LABEL_ORIENTATION_ANGLE";
  /**
   * The drawing type of the path.
   * Type: String {Elbow, Slanted, Curved, Bezier, Direct}
   * Default value: DIRECT.
   */
  String PATH_CONN_TYPE = "CONN_TYPE";
  /**
   * Control points describing the way the connection is being drawn (if the
   * connection type is not Direct).
   * Type: String. (List of comma-separated X/Y pairs, with the pairs being
   * separated by semicola. Example: "x1,x1;x2,y2;x3,y3")
   * Default value: empty.
   */
  String PATH_CONTROL_POINTS = "CONTROL_POINTS";
  /**
   * Position of the driving direction arrows (in percent).
   * Type: int [0..100].
   * Default value: 50.
   *
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed")
  String PATH_ARROW_POSITION = "ARROW_POSITION";
  /**
   * Color in which block elements are to be emphasized.
   * Type: String (pattern: #rrggbb).
   * Default value: Automatically assigned.
   */
  String BLOCK_COLOR = "COLOR";
  /**
   * Color in which vehicle routes are to be emphasized.
   * Type: String (pattern: #rrggbb).
   * Default value: Color.RED
   */
  String VEHICLE_ROUTE_COLOR = "ROUTE_COLOR";
}
