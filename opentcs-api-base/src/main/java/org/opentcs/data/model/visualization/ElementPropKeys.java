// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data.model.visualization;

/**
 * Defines some reserved/commonly used property keys of elements in a {@link VisualLayout}.
 */
public interface ElementPropKeys {

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
