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

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;
import org.opentcs.guing.components.drawing.figures.OriginFigure;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Der Ursprung des Koordinatensystems. Er ist sozusagen das Modell zur {
 *
 * @see OriginFigure}. Er kennt die Ausdehnung der Zeichnung in m, mm oder cm
 * und er kennt das gewï¿½nschte Koordinatensystem. Anhand des Koordinatensystems
 * kann die reale Position von anderen Figures berechnet werden (Umrechnung von
 * Pixel in eine Lï¿½ngeneinheit).
 * <p>
 * Die aktuelle Position des Ursprungs in
 * Pixel wird durch die OriginFigure bestimmt.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public final class Origin {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Origin.class);
  /**
   * Scale (in mm per pixel) of the layout.
   */
  public static final double DEFAULT_SCALE = 50.0;
  /**
   * Soviele mm entsprechen einem Pixel in waagerechter Richtung.
   */
  private double fScaleX = DEFAULT_SCALE;
  /**
   * Soviele mm entsprechen einem Pixel in senkrechter Richtung.
   */
  private double fScaleY = DEFAULT_SCALE;
  /**
   * Die aktuelle Position in Pixel.
   */
  private Point fPosition;
  /**
   * Das Koordinatensystem.
   */
  private CoordinateSystem fCoordinateSystem;
  /**
   * Liste aller Objekte, die an einer ï¿½nderung des Referenzpunktes interessiert
   * sind.
   */
  private final Set<OriginChangeListener> fListeners = new HashSet<>();
  /**
   * Die grafische Darstellung des Ursprungs.
   */
  private final OriginFigure fFigure = new OriginFigure();

  /**
   * Creates a new instance of Origin
   */
  public Origin() {
    setCoordinateSystem(new NormalCoordinateSystem());
    fFigure.setModel(this);
  }

  /**
   * Setzt die Werte der Millimeter pro Pixel. Diese Werte kï¿½nnen sich mit jedem
   * Zoom ï¿½ndern.
   */
  public void setScale(double scaleX, double scaleY) {
    if (fScaleX == scaleX && fScaleY == scaleY) {
      return;
    }
    fScaleX = scaleX;
    fScaleY = scaleY;
    notifyScaleChanged();
  }

  /**
   * Liefert die Millimeter pro Pixel waagrecht.
   *
   * @return den Wert
   */
  public double getScaleX() {
    return fScaleX;
  }

  /**
   * Liefert die Millimeter pro Pixel senkrecht.
   *
   * @return den Wert
   */
  public double getScaleY() {
    return fScaleY;
  }

  /**
   * Setzt das Koordinatensystem.
   */
  public void setCoordinateSystem(CoordinateSystem coordinateSystem) {
    fCoordinateSystem = coordinateSystem;
    notifyLocationChanged();
  }

  /**
   * Setzt die aktuelle Position des Ursprungs in Pixel.
   *
   * @param position die Position
   */
  public void setPosition(Point position) {
    fPosition = position;
  }

  /**
   * Liefert die aktuelle Position des Referenzpunktes in Pixel.
   *
   * @return die Position
   */
  public Point getPosition() {
    return fPosition;
  }

  /**
   * Wandelt echte Koordinaten in Pixelkoordinaten um. Die echten Koordinaten
   * gibt der Benutzer durch ï¿½ndern der Attribute vor. Daraufhin muss berechnet
   * werden, an welche Position in Pixel das entsprechende Figure zu setzen ist.
   */
  public Point calculatePixelPosition(LengthProperty xReal, LengthProperty yReal) {
    Point2D pixelExact = calculatePixelPositionExactly(xReal, yReal);

    return new Point((int) pixelExact.getX(), (int) pixelExact.getY());
  }

  /**
   * Wandelt echte Koordinaten in Pixelkoordinaten um. Die echten Koordinaten
   * gibt der Benutzer durch ï¿½ndern der Attribute vor. Daraufhin muss berechnet
   * werden, an welche Position in Pixel das entsprechende Figure zu setzen ist.
   *
   * @param xReal
   * @param yReal
   * @return Die exakte Pixelposition.
   */
  public Point2D calculatePixelPositionExactly(LengthProperty xReal, LengthProperty yReal) {
    Point2D realPosition = new Point2D.Double(
        xReal.getValueByUnit(LengthProperty.Unit.MM),
        yReal.getValueByUnit(LengthProperty.Unit.MM));

    Point2D pixelPosition = fCoordinateSystem.toPixel(fPosition, realPosition, fScaleX, fScaleY);

    return pixelPosition;
  }

  public Point2D calculatePixelPositionExactly(StringProperty xReal, StringProperty yReal) {
    try {
      double xPos = Double.parseDouble(xReal.getText());
      double yPos = Double.parseDouble(yReal.getText());
      Point2D realPosition = new Point2D.Double(xPos, yPos);
      Point2D pixelPosition = fCoordinateSystem.toPixel(fPosition, realPosition, fScaleX, fScaleY);

      return pixelPosition;
    }
    catch (NumberFormatException e) {
      LOG.info("Couldn't parse layout coordinates", e);
      return new Point2D.Double();
    }
  }

  /**
   * Wandelt Pixelkoordinaten in echte Koordinaten um. Der Benutzer verschiebt
   * ein Figure. Diese neue Position in Pixel muss nun in reale Koordinaten
   * umgerechnet werden.
   *
   * @param pixelPosition die Position des Figures in Pixeln
   * @param xReal das Lï¿½ngenattribut fï¿½r die x-Achse, in welches der errechnete
   * reale Wert geschrieben wird
   * @param yReal das Lï¿½ngenattribut fï¿½r die y-Achse, in welches der errechnete
   * reale Wert geschrieben wird
   */
  public Point2D calculateRealPosition(Point pixelPosition, LengthProperty xReal,
                                       LengthProperty yReal) {
    Point2D realPosition = fCoordinateSystem.toReal(fPosition, pixelPosition, fScaleX, fScaleY);

    LengthProperty.Unit unitX = xReal.getUnit();
    LengthProperty.Unit unitY = yReal.getUnit();

    xReal.setValueAndUnit((int) realPosition.getX(), LengthProperty.Unit.MM);
    yReal.setValueAndUnit((int) realPosition.getY(), LengthProperty.Unit.MM);
    xReal.convertTo(unitX);
    yReal.convertTo(unitY);

    return realPosition;
  }

  /**
   *
   * @param pixelPosition
   * @return
   */
  public Point2D calculateRealPosition(Point pixelPosition) {
    Point2D realPosition = fCoordinateSystem.toReal(fPosition, pixelPosition, fScaleX, fScaleY);

    return realPosition;
  }

  /**
   * Fï¿½gt einen Beobachter hinzu.
   */
  public void addListener(OriginChangeListener l) {
    fListeners.add(l);
  }

  /**
   * Entfernt einen Beobachter.
   */
  public void removeListener(OriginChangeListener l) {
    fListeners.remove(l);
  }

  /**
   * Prï¿½ft, ob ein bestimmter Beobachter vorhanden ist.
   *
   * @param l der zu prï¿½fende Boebachter
   * @return
   * <code> true </code>, wenn der Beobachter vorhanden ist
   */
  public boolean containsListener(OriginChangeListener l) {
    return fListeners.contains(l);
  }

  /**
   * Informiert alle Beobachter, dass sich die Position des Referenzpunktes
   * geï¿½ndert hat.
   */
  public void notifyLocationChanged() {
    for (OriginChangeListener l : fListeners) {
      l.originLocationChanged(new EventObject(this));
    }
  }

  /**
   * Informiert alle Beobachter, dass sich der Maï¿½stab geï¿½ndert hat.
   */
  public void notifyScaleChanged() {
    for (OriginChangeListener l : fListeners) {
      l.originScaleChanged(new EventObject(this));
    }
  }

  /**
   * Liefert die grafische Reprï¿½sentation des Ursprungs.
   *
   * @return das {@link OriginFigure} als grafische Reprï¿½sentation des
   * Referenzpunktes
   */
  public OriginFigure getFigure() {
    return fFigure;
  }
}
