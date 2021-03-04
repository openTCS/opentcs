/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.storage;

import org.jhotdraw.xml.DefaultDOMFactory;
import org.opentcs.guing.components.drawing.figures.LabeledLocationFigure;
import org.opentcs.guing.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.components.drawing.figures.LinkConnection;
import org.opentcs.guing.components.drawing.figures.PathConnection;

/**
 * Vgl. JHotDraw Sample "NetFactory".
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class OpenTCSFactory
    extends DefaultDOMFactory {

  private final static Object[][] classTagArray = {
    // openTCS Objects
    {LabeledPointFigure.class, "Point"},
    {LabeledLocationFigure.class, "Location"},
    {PathConnection.class, "Path"},
    {LinkConnection.class, "Link"}
  };
//  private final static Object[][] enumTagArray = {
//    {
//      AttributeKeys.StrokeType.class, "strokeType"
//    }
//  };

  /**
   * Creates a new instance.
   */
  public OpenTCSFactory() {
    for (Object[] o : classTagArray) {
      addStorableClass((String) o[1], (Class) o[0]);
    }

//    for (Object[] o : enumTagArray) {
//      addEnumClass((String) o[1], (Class) o[0]);
//    }
  }
}
