/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import java.util.ArrayList;
import org.opentcs.access.Kernel;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;

/**
 * A generator for simple models.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public class TestModelGenerator {

  private final Kernel kernel;

  /**
   * Create a TestModelGenerator.
   *
   * @param kernel The kernel that will be used to create the models.
   */
  public TestModelGenerator(Kernel kernel) {
    this.kernel = kernel;
  }

  /**
   * Creates a ring model in which all points form a ring connected with paths
   * that can be used to travel the ring in one direction.
   * The current model of the kernel will be replaced by this one!
   *
   * @param pointCount number of points in the ring
   * @throws IllegalArgumentException if pointCount is less than three
   */
  public void createRingModel(int pointCount) {
    if (pointCount < 3) {
      throw new IllegalArgumentException("Number of points less than three.");
    }
    kernel.createModel("RingModel");
    ArrayList<TCSObjectReference<Point>> points = new ArrayList<>(pointCount);
    points.add(0, kernel.createPoint().getReference());
    for (int i = 1; i < pointCount; i++) {
      points.add(i, kernel.createPoint().getReference());
      kernel.createPath(points.get(i - 1), points.get(i));
    }
    kernel.createPath(points.get(pointCount - 1), points.get(0));
  }
}
