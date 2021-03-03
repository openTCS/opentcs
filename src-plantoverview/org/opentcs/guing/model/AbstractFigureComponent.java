/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.model;

import java.util.ArrayList;
import org.jhotdraw.draw.Figure;
import org.opentcs.guing.components.tree.elements.FigureUserObject;
import org.opentcs.guing.components.tree.elements.UserObject;
import org.opentcs.guing.model.elements.AbstractConnection;

/**
 * Verwaltet eine Referenz auf ein Figure. Ist so eine Art Adapter, da Figure
 * nicht zum ModelComponent-Interface passt. Jedoch erfolgt keine Delegation
 * durch FigureComponent an sein referenziertes Figure, weshalb es sich um keine
 * wirkliche Instanz des Adapter-Musters handelt.
 * <b>Entwurfsmuster:</b> Kompositum. FigureComponent ist eine Blattkomponente.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public abstract class AbstractFigureComponent
    extends AbstractModelComponent
    implements FigureComponent, Comparable<AbstractFigureComponent> {

  /**
   * The cordinates of the associated Point / Location in the Kernel model.
   */
  public static final String MODEL_X_POSITION = "modelXPosition";
  public static final String MODEL_Y_POSITION = "modelYPosition";

  /**
   * The referenced Figure.
   */
  private transient Figure fFigure;
  /**
   * The Links and Paths connected with this Figure (Location or Point).
   */
  private ArrayList<AbstractConnection> fConnections;

  /**
   * Creates a new instance.
   */
  public AbstractFigureComponent() {
    super();
    this.fConnections = new ArrayList<>();
  }

  /**
   * Creates a new instance.
   *
   * @param figure The figure to be referenced.
   */
  public AbstractFigureComponent(Figure figure) {
    this.fConnections = new ArrayList<>();
    fFigure = figure;
  }

  @Override // FigureComponent
  public void addConnection(AbstractConnection connection) {
    fConnections.add(connection);
  }

  @Override // FigureComponent
  public void removeConnection(AbstractConnection connection) {
    fConnections.remove(connection);
  }

  @Override // FigureComponent
  public ArrayList<AbstractConnection> getConnections() {
    return fConnections;
  }

  /**
   * Prüft, ob bereits eine Verbindung zu einem anderen Knoten besteht.
   *
   * @param component der andere Knoten
   * @return
   * <code> true </code>, wenn zwischen den beiden Knoten bereits eine
   * Verbindung besteht, ansonsten
   * <code> false </code>
   */
  public boolean hasConnectionTo(FigureComponent component) {
    return getConnectionTo(component) != null;
  }

  /**
   * Liefert die Verbindung zu einem anderen Knoten.
   *
   * @param component der andere Knoten
   * @return die gefundene Verbindung oder
   * <code>null</code>, wenn zwischen den beiden keine Verbindung besteht
   */
  public AbstractConnection getConnectionTo(FigureComponent component) {
    for (AbstractConnection connection : fConnections) {
      if (connection.getStartComponent() == this && connection.getEndComponent() == component) {
        return connection;
      }
    }

    return null;
  }

  @Override // ModelComponent
  public UserObject createUserObject() {
    fUserObject = new FigureUserObject(this);

    return fUserObject;
  }

  @Override // FigureComponent
  public Figure getFigure() {
    return fFigure;
  }

  @Override // FigureComponent
  public void setFigure(Figure figure) {
    fFigure = figure;
  }

  @Override // AbstractModelComponent
  public AbstractModelComponent clone() throws CloneNotSupportedException {
    AbstractFigureComponent clone = (AbstractFigureComponent) super.clone();
    clone.fConnections = new ArrayList<>();

    return clone;
  }
}
