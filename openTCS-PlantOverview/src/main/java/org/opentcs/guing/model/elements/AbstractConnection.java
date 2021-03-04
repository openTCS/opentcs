/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model.elements;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.properties.event.NullAttributesChangeListener;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.event.ConnectionChangeEvent;
import org.opentcs.guing.event.ConnectionChangeListener;
import org.opentcs.guing.model.AbstractFigureComponent;
import org.opentcs.guing.model.ModelComponent;

/**
 * Abstract implementation for connections:
 * <ol>
 * <li>between two points {
 *
 * @see PathModel}</li>
 * <li>between a point and a location {@link LinkModel}</li>
 * </ol>
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class AbstractConnection
    extends AbstractFigureComponent
    implements AttributesChangeListener {

  /**
   * Key for the start component.
   */
  public static final String START_COMPONENT = "startComponent";
  /**
   * Key for the end component.
   */
  public static final String END_COMPONENT = "endComponent";
  /**
   * The start component (Location or Point).
   */
  private transient ModelComponent fStartComponent;
  /**
   * The end component (Location or Point).
   */
  private transient ModelComponent fEndComponent;
  /**
   * Listeners that are interested in changes of the connected objects.
   */
  private transient List<ConnectionChangeListener> fConnectionChangeListeners
      = new LinkedList<>();

  /**
   * Creates a new instance.
   */
  public AbstractConnection() {
    // Do nada.
  }

  /**
   * Returns this connection's start component.
   *
   * @return The start component.
   */
  public ModelComponent getStartComponent() {
    return fStartComponent;
  }

  /**
   * Returns this connection's end component.
   *
   * @return The end component.
   */
  public ModelComponent getEndComponent() {
    return fEndComponent;
  }

  /**
   * Sets this connection's start and end component.
   *
   * @param startComponent The start component.
   * @param endComponent The end component.
   */
  public void setConnectedComponents(ModelComponent startComponent,
                                     ModelComponent endComponent) {
    updateListenerRegistrations(startComponent, endComponent);
    updateComponents(startComponent, endComponent);

    // TODO: Points and locations are still missing an implementation of equals().
    if (!Objects.equals(fStartComponent, startComponent)
        || !Objects.equals(fEndComponent, endComponent)) {
      fStartComponent = startComponent;
      fEndComponent = endComponent;
      connectionChanged();
    }
  }

  /**
   * Notifies this connection that it is being removed.
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
   * Adds a listener.
   *
   * @param listener The new listener.
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
   * Removes a listener.
   *
   * @param listener The listener to remove.
   */
  public void removeConnectionChangeListener(ConnectionChangeListener listener) {
    fConnectionChangeListeners.remove(listener);
  }

  public StringProperty getPropertyStartComponent() {
    return (StringProperty) getProperty(START_COMPONENT);
  }

  public StringProperty getPropertyEndComponent() {
    return (StringProperty) getProperty(END_COMPONENT);
  }

  @Override // AttributesChangeListener
  public void propertiesChanged(AttributesChangeEvent e) {
    if (getStartComponent().getPropertyName().hasChanged()
        || getEndComponent().getPropertyName().hasChanged()) {
      if (nameFulfillsConvention()) {
        updateName();
      }
      else {
        // Don't forget to update these properties.
        getPropertyStartComponent().setText(fStartComponent.getName());
        getPropertyEndComponent().setText(fEndComponent.getName());
      }
    }
  }

  @Override
  public AbstractConnection clone()
      throws CloneNotSupportedException {
    AbstractConnection clone = (AbstractConnection) super.clone();
    clone.fConnectionChangeListeners = new LinkedList<>();

    return clone;
  }

  /**
   * Removes the current start and end components and establishes this connection
   * between the given components.
   *
   * @param startComponent The new start component.
   * @param endComponent The new end component.
   */
  private void updateComponents(ModelComponent startComponent,
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
   * Informs all listener that the start and/or end component have changed.
   */
  private void connectionChanged() {
    if (fConnectionChangeListeners == null) {
      return;
    }

    for (ConnectionChangeListener listener : fConnectionChangeListeners) {
      listener.connectionChanged(new ConnectionChangeEvent(this));
    }
  }

  /**
   * Deregistrates and reregistrates itself as a listener on the
   * connected components. This is important as the name of the connection
   * is dependant on the connected components.
   *
   * @param startComponent The new start component to register with.
   * @param endComponent The new end component to register with.
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
   * Refreshes the name of this connection.
   */
  public void updateName() {
    StringProperty property = getPropertyName();

    if (property != null) {
      String oldName = property.getText();
      String newName = getStartComponent().getName() + " --- " + getEndComponent().getName();
      property.setText(newName);

      if (!newName.equals(oldName)) {
        property.markChanged();
      }

      propertiesChanged(new NullAttributesChangeListener());
    }
    getPropertyStartComponent().setText(fStartComponent.getName());
    getPropertyEndComponent().setText(fEndComponent.getName());
  }

  /**
   * Checks if the name of this connection follows a defined naming scheme.
   * Definition: {@literal <startComponent> --- <endComponent>}
   *
   * @return {@code true}, if the name fulfills the convention, otherwise {@code false}.
   */
  private boolean nameFulfillsConvention() {
    StringProperty property = getPropertyName();
    if (property != null) {
      String name = property.getText();
      String nameByConvention = getPropertyStartComponent().getText() + " --- "
          + getPropertyEndComponent().getText();

      if (name.equals(nameByConvention)) {
        return true;
      }
    }

    return false;
  }
}
