/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static java.util.Objects.requireNonNull;
import org.jhotdraw.draw.Figure;

/**
 * Eine Komponente des Systemmodells, die nur Objekte vom Typ FigureComponent
 * enthält. Ein FigureComponent besitzt eine Referenz auf ein Figure.
 * FiguresFolder verwaltet ein Drawing, in dem die Figure-Objekte der
 * FigureComponents enthalten sind. Soll im DrawingEditor ein anderes Drawing
 * gesetzt werden, so holt sich die Applikation zunächst das entsprechende
 * Drawing von einem FiguresFolder.
 * <p>
 * <b>Entwurfsmuster:</b> Kompositum.
 * FiguresFolder ist ein konkretes Kompositum.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @see FigureComponent
 */
public class FiguresFolder
    extends CompositeModelComponent {

  /**
   * Creates a new instance of FiguresFolder
   */
  public FiguresFolder() {
    this("Figures");
  }

  /**
   * Erzeugt ein neues Objekt von FiguresFolder mit dem angegebenen Namen. Der
   * Name wird im TreeView angezeigt.
   *
   * @param name
   */
  public FiguresFolder(String name) {
    super(name);
  }

  /**
   * Liefert einen Vector mit den enthaltenen Figures zurück. Da Blockline
   * prinzipiell nur FigureComponents enthält, die wiederum die Figures
   * enthalten, müssen erst die Figures aus den FigureComponents
   * herausextrahiert werden.
   *
   * @return
   */
  public Iterator<Figure> figures() {
    List<Figure> figures = new ArrayList<>();

    List<ModelComponent> childComps = getChildComponents();
    synchronized (childComps) {
      for (ModelComponent component : childComps) {
        if (component instanceof FigureComponent) {
          Figure figure = ((FigureComponent) component).getFigure();
          if (figure != null) {
            figures.add(figure);
          }
        }
      }
    }

    return figures.iterator();
  }

  /**
   * Liefert das FigureComponent-Objekt, das eine Referenz auf das übergebene
   * Figure enthält. Liefert null, falls keines der enthaltenen
   * FigureComponent-Objekte eine Referenz auf das übergebene Figure besitzt.
   * Diese Methode kann daher auch für Überprüfungen nach einem Enthaltensein
   * verwendet werden.
   *
   * @param figure
   * @return
   */
  public ModelComponent getFigureComponent(Figure figure) {
    requireNonNull(figure, "figure");

    for (ModelComponent childComp : getChildComponents()) {
      FigureComponent component = (FigureComponent) childComp;

      if (figure.equals(component.getFigure())) {
        return component;
      }
    }

    return null;
  }
}
