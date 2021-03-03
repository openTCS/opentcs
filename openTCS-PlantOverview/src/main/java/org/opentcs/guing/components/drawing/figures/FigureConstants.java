/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.drawing.figures;

import org.jhotdraw.draw.AttributeKey;
import org.opentcs.guing.components.drawing.course.Origin;
import org.opentcs.guing.model.FigureComponent;

/**
 * Allgemeine Konstanten, die insbesondere Figures betreffen.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface FigureConstants {

  /**
   * Über diesen Schlüssel greifen Figures auf ihr Model zu.
   */
  AttributeKey<FigureComponent> MODEL = new AttributeKey<>("Model", FigureComponent.class);
  /**
   * Über dieses Attribut erhalten Figures Zugriff auf den Referenzpunkt.
   */
  AttributeKey<Origin> ORIGIN = new AttributeKey<>("Origin", Origin.class);
}
