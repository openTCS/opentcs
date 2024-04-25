/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.math.path;

/**
 * Provides the euclidean distance and methods to evaluate the position of a point of a Bezier curve
 * with n control points at a given parameter value.
 */
public class PathLengthMath {

  public PathLengthMath() {
  }

  /**
   * Approximates the length of a cubic Bezier curve (described via its control points) by
   * discretization of the curve.
   *
   * @param cp0 The control point with index 0 (i.e. the start point of the Bezier curve).
   * @param cp1 The control point with index 1.
   * @param cp2 The control point with index 2.
   * @param cp3 The control point with index 3 (i.e. the end point of the Bezier curve).
   * @param granularity The granularity of the discretized curve. A higher granularity results in a
   * more precise approximation.
   *
   * @return The approximated length of the Bezier curve.
   */
  public double approximateCubicBezierCurveLength(Coordinate cp0,
                                                  Coordinate cp1,
                                                  Coordinate cp2,
                                                  Coordinate cp3,
                                                  double granularity) {
    double length = 0.0;

    for (int i = 0; i < granularity; i++) {
      length += euclideanDistance(
          evaluatePointOnCubicBezierCurve((double) i / granularity, cp0, cp1, cp2, cp3),
          evaluatePointOnCubicBezierCurve((double) (i + 1) / granularity, cp0, cp1, cp2, cp3)
      );
    }

    return length;
  }

  /**
   * Calculates a point's position on a cubic Bezier curve (described via its control points) using
   * Bernstein basis polynomials.
   *
   * @param t The parameter (with a value between 0 and 1) that defines the position of a point
   * along the Bezier curve. A value of 0 corresponds to the position of the first control point and
   * a value of 1 to the position of the last control point.
   * @param cp0 The control point with index 0 (i.e. the start point of the Bezier curve).
   * @param cp1 The control point with index 1.
   * @param cp2 The control point with index 2.
   * @param cp3 The control point with index 3 (i.e. the end point of the Bezier curve).
   * @return A point's position on a cubic Bezier curve.
   */
  public Coordinate evaluatePointOnCubicBezierCurve(double t,
                                                    Coordinate cp0,
                                                    Coordinate cp1,
                                                    Coordinate cp2,
                                                    Coordinate cp3) {
    return new Coordinate(
        bernsteinPolynomialOfDegree3(t, cp0.getX(), cp1.getX(), cp2.getX(), cp3.getX()),
        bernsteinPolynomialOfDegree3(t, cp0.getY(), cp1.getY(), cp2.getY(), cp3.getY())
    );
  }

  /**
   * Calculates the distance between two given points using the euclidean algorithm.
   *
   * @param p1 The first point.
   * @param p2 The second point.
   * @return The distance between these two points.
   */
  public double euclideanDistance(Coordinate p1, Coordinate p2) {
    return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
  }

  private double bernsteinPolynomialOfDegree3(double t,
                                              double cp0,
                                              double cp1,
                                              double cp2,
                                              double cp3) {
    double u = 1.0 - t;
    return Math.pow(u, 3) * cp0
        + 3 * Math.pow(u, 2) * t * cp1
        + 3 * u * Math.pow(t, 2) * cp2
        + Math.pow(t, 3) * cp3;
  }
}
