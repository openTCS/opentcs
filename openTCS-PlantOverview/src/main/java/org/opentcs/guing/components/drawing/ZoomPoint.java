/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.drawing;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * Ein Punkt, der eine exakte Position wiedergibt, die sich auch durch das
 * Zoomen nicht verändert. Insbesondere ist diese Art von Punkt für das Zoomen
 * nötig, bei dem die Positionen der Figures geändert werden, nicht jedoch deren
 * Größe.
 * <p>
 * i. Auftretende Fälle bei symbolischer Zeichenweise: <br> (a)
 * Änderung des x- oder y-Attributes: keine Auswirkungen (b) Bewegung des
 * Punktes mit der Maus: genaue Position ist in dem Zoompunkt zu speichern (c)
 * Zoomen: die neue Position des Punktes wird anhand der genauen Werte des
 * Zoompunkts ermittelt
 * <p>
 * ii. Auftretende Fälle bei maßstabsgetreuer
 * Zeichenweise: <br> (a) Änderung des x- oder y-Attributes: der Punkt muss der
 * Zoomstufe entsprechend möglichst genau platziert werden; der exakte Wert ist
 * zu ermitteln und im Zoompunkt abzulegen (b) Bewegung des Punktes mit der
 * Maus: genaue Position ist in dem Zoompunkt zu speichern (c) Zoomen: die neue
 * Position des Punktes wird anhand der genauen Werte des Zoompunkts ermittelt;
 * das x- und y-Attribut darf davon nicht beeinflusst werden
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class ZoomPoint
    implements Serializable {

  /**
   * Der x-Wert in Pixel bei Zoomfaktor 1.
   */
  protected double fX;
  /**
   * Der y-Wert in Pixel bei Zoomfaktor 1.
   */
  protected double fY;
  /**
   * Die momentane Zoomstufe.
   */
  protected double fScale;

  /**
   * Creates a new instance of ZoomPoint
   */
  public ZoomPoint() {
    this(0, 0);
  }

  /**
   * Konstruktor mit Übergabe des x- und y-Werts.
   */
  public ZoomPoint(double x, double y) {
    this(x, y, 1.0);
  }

  /**
   * Konstruktor mit Übergabe des x-, y-Wertes und des momentanten Zoomfaktors.
   */
  public ZoomPoint(double x, double y, double scale) {
    fX = x / scale;
    fY = y / scale;
    fScale = scale;
  }

  /**
   * Liefert die aktuelle Zoomstufe.
   */
  public double scale() {
    return fScale;
  }

  /**
   * Setzt den Wert für x.
   */
  public void setX(double x) {
    fX = x;
  }

  /**
   * Setzt den y-Wert.
   */
  public void setY(double y) {
    fY = y;
  }

  /**
   * Liefert den x-Wert.
   */
  public double getX() {
    return fX;
  }

  /**
   * Liefert den y-Wert.
   */
  public double getY() {
    return fY;
  }

  /**
   * Liefert die Position des Punktes in Pixel bei der aktuellen Zoomstufe.
   *
   * @return die Position in Pixel
   */
  public Point getPixelLocation() {
    int x = (int) (getX() * scale());
    int y = (int) (getY() * scale());

    return new Point(x, y);
  }

  /**
   * Liefert die exakte Position des Punktes in Pixel bei der aktuellen
   * Zoomstufe.
   *
   * @return die Position in Pixel
   */
  public Point2D getPixelLocationExactly() {
    double x = getX() * scale();
    double y = getY() * scale();

    return new Point2D.Double(x, y);
  }

  /**
   * Mitteilung, dass sich die Zoomstufe geändert hat.
   *
   * @param scale Der neue Zoomfaktor.
   */
  public void scaleChanged(double scale) {
    fScale = scale;
  }

  /**
   * Nachricht, dass der Punkt direkt durch den Benutzer mit der Maus bewegt
   * wurde. Der Zoompunkt muss daraus die genaue Position ermittelt.
   *
   * @param x Die x-Koordinate in Pixel.
   * @param y Die y-Koordinate in Pixel.
   */
  public void movedByMouse(int x, int y) {
    fX = x / scale();
    fY = y / scale();
  }
}
