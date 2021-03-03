/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.drawing.course;

/**
 * Ein Interface für Zeichenmethoden. Mögliche Zeichenmethoden können sein: <p>
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
