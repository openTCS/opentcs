/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.model.elements;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.jhotdraw.draw.Figure;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.properties.event.NullAttributesChangeListener;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.event.ConnectionChangeEvent;
import org.opentcs.guing.event.ConnectionChangeListener;
import org.opentcs.guing.model.AbstractFigureComponent;
import org.opentcs.guing.model.ModelComponent;

/**
 * Basisimplementierungen für Verbindungen:
 * 1. zwischen zwei Meldepunkten (-> PathModel)
 * 2. zwischen Punkt und Station (-> LinkModel)
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public abstract class AbstractConnection
    extends AbstractFigureComponent
    implements AttributesChangeListener {

  /**
   * The start component (Location or Point).
   */
  private transient ModelComponent fStartComponent;
  /**
   * The end component (Location or Point).
   */
  private transient ModelComponent fEndComponent;
  /**
   * Objekte, die an Änderungen hinsichtlich der verbundenen Komponenten
   * interessiert sind.
   */
  private transient List<ConnectionChangeListener> fConnectionChangeListeners
      = new LinkedList<>();

  /**
   * Creates a new instance.
   */
  public AbstractConnection() {
    this(null);
  }

  /**
   * Creates a new instance using the given Figure.
   *
   * @param figure The Figure to be used.
   */
  public AbstractConnection(Figure figure) {
    super(figure);
  }

  /**
   * @return The start component (Location or Point)
   */
  public ModelComponent getStartComponent() {
    return fStartComponent;
  }

  /**
   * @return The end component (Location or Point)
   */
  public ModelComponent getEndComponent() {
    return fEndComponent;
  }

  /**
   * Setzt den Start- und Endknoten.
   *
   * @param startComponent die Startkomponente
   * @param endComponent die Endkomponente
   */
  public void setConnectedComponents(ModelComponent startComponent, ModelComponent endComponent) {
    updateListenerRegistrations(startComponent, endComponent);
    updateComponents(startComponent, endComponent);

    // TODO: Points and locations are still missing an implementation of equals().
    if (!Objects.equals(fStartComponent, startComponent)
        || !Objects.equals(fEndComponent, endComponent)) {
      fStartComponent = startComponent;
      fEndComponent = endComponent;
      updateName();
      connectionChanged();
    }
  }

  /**
   * Nachricht des Figures, dass die Verbindung gelöscht wurde.
   */
  public void removingConnection() {
    if (fStartComponent != null) {
      fStartComponent.removeAttributesChangeListener(this);

      if (fStartComponent instanceof AbstractFigureComponent) {
        ((AbstractFigureComponent) fStartComponent).removeConnection(this);
      }
    }

    if (fEndComponent != null) {
      fEndComponent.removeAttributesChangeListener(this);

      if (fEndComponent instanceof AbstractFigureComponent) {
        ((AbstractFigureComponent) fEndComponent).removeConnection(this);
      }
    }
  }

  /**
   * Fügt einen Listener hinzu.
   *
   * @param listener
   */
  public void addConnectionChangeListener(ConnectionChangeListener listener) {
    if (fConnectionChangeListeners == null) {
      fConnectionChangeListeners = new LinkedList<>();
    }

    if (!fConnectionChangeListeners.contains(listener)) {
      fConnectionChangeListeners.add(listener);
    }
  }

  /**
   * Entfernt einen Listener.
   *
   * @param listener
   */
  public void removeConnectionChangeListener(ConnectionChangeListener listener) {
    fConnectionChangeListeners.remove(listener);
  }

  /**
   * Benachrichtigt alle registrierten Listener, dass Start- und oder
   * Endkomponente gewechselt haben.
   */
  public void connectionChanged() {
    if (fConnectionChangeListeners == null) {
      return;
    }

    for (ConnectionChangeListener listener : fConnectionChangeListeners) {
      listener.connectionChanged(new ConnectionChangeEvent(this));
    }
  }

  @Override // AttributesChangeListener
  public void propertiesChanged(AttributesChangeEvent e) {
    if (getStartComponent().getProperty(NAME).hasChanged()
        || getEndComponent().getProperty(NAME).hasChanged()) {
      updateName();
    }
  }

  @Override
  public int compareTo(AbstractFigureComponent other) {
    return getName().compareTo(other.getName());
  }

  @Override
  public AbstractConnection clone() throws CloneNotSupportedException {
    AbstractConnection clone = (AbstractConnection) super.clone();
    clone.fConnectionChangeListeners = new LinkedList<>();
    
    return clone;
  }

  /**
   * Fügt diese Strecke den verbundenen Knoten mit oder entfernt sie von diesen.
   *
   * @param startComponent die Startkomponente
   * @param endComponent die Endkomponente
   */
  protected void updateComponents(ModelComponent startComponent,
                              ModelComponent endComponent) {
    if (fStartComponent instanceof AbstractFigureComponent) {
      ((AbstractFigureComponent) fStartComponent).removeConnection(this);
    }

    if (fEndComponent instanceof AbstractFigureComponent) {
      ((AbstractFigureComponent) fEndComponent).removeConnection(this);
    }

    if (startComponent instanceof AbstractFigureComponent) {
      ((AbstractFigureComponent) startComponent).addConnection(this);
    }

    if (endComponent instanceof AbstractFigureComponent) {
      ((AbstractFigureComponent) endComponent).addConnection(this);
    }
  }

  /**
   * Führt die Registrierungen und Deregistrierungen als Listener bei den
   * angeschlossenen Komponenten durch. Ist wichtig, da der Name der Verbindung
   * aus den Namen der angeschlossenen Komponenten zusammengesetzt sein soll.
   *
   * @param startComponent
   * @param endComponent
   */
  private void updateListenerRegistrations(ModelComponent startComponent,
                                           ModelComponent endComponent) {
    if (fStartComponent != null) {
      fStartComponent.removeAttributesChangeListener(this);
    }

    if (fEndComponent != null) {
      fEndComponent.removeAttributesChangeListener(this);
    }

    startComponent.addAttributesChangeListener(this);
    endComponent.addAttributesChangeListener(this);
  }

  /**
   * Aktualisiert den Namen der Verbindung, der sich aus den Namen der
   * verbundenen Komponenten zusammensetzt.
   */
  private void updateName() {
    StringProperty property = (StringProperty) getProperty(NAME);

    if (property != null) {
      String oldName = property.getText();
      String newName = getStartComponent().getName() + " --- " + getEndComponent().getName();
      property.setText(newName);

      if (!newName.equals(oldName)) {
        property.markChanged();
      }

      propertiesChanged(new NullAttributesChangeListener());
    }
  }
}
