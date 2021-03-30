/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.drawing.course;

/**
 * Ein Interface für Zeichenmethoden. Mögliche Zeichenmethoden können sein:
 * <p>
 * <ul> <li> symbolisch: Zwischen der Realposition von Fahrkurselementen und der
 * Position von Figures besteht kein Zusammenhang <li> auf Koordinaten
 * basierend: Die Position der Figures entspricht genau der Position der
 * Realkoordinaten. </ul>
 *
 * Entwurfsmuster: Strategie. DrawingMethod ist eine abstrakte Strategie,
 * Unterklassen sind konkrete Strategien.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface DrawingMethod {

  /**
   * Liefert den Origin.
   */
  Origin getOrigin();
}
