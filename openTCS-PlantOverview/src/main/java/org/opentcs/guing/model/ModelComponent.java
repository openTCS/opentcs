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

import java.util.List;
import java.util.Map;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.properties.type.Property;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.tree.TreeViewManager;

/**
 * Interface für alle Komponenten des Systemmodells. Konkrete Implementierungen
 * sind entweder Komposita oder Blätter. Eine Komponente ist für folgende Dinge
 * zuständig:
 * - Bereitstellung einer JComponent (in der Regel eines JPanels), auf dem
 * die Eigenschaften der Komponente veränderbar sind
 * - Bereitstellung eines passenden UserObjects, das für die Anzeige der
 * Komponente im TreeView eingesetzt wird
 * - Verwaltung der Kindelemente, wenn es sich um ein Kompositum handelt
 * - Wiederherstellung des TreeViews nach dem Laden von der Festplatte
 *
 * <b>Entwurfsmuster:</b> Kompositum. ModelComponent ist die Komponente.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface ModelComponent
    extends Cloneable {

  /**
   * Der Schlüssel für das Namensattribut.
   */
  String NAME = "Name";
  /**
   * Der Schlüssel für sonstige Eigenschaften.
   */
  String MISCELLANEOUS = "Miscellaneous";

  /**
   * Fügt das eigene Objekt dem TreeView hinzu und ruft die restore()- Methode
   * auf alle Kindobjekte auf. Wird für die Wiederherstellung der Baumansicht
   * nach dem Laden von der Festplatte verwendet.
   *
   * @param parent
   * @param treeViewManager
   */
  void treeRestore(ModelComponent parent, TreeViewManager treeViewManager);

  /**
   * Fügt ein Kindobjekt hinzu.
   *
   * @param component
   */
  void add(ModelComponent component);

  /**
   * Entfernt ein Kindobjekt.
   *
   * @param component
   */
  void remove(ModelComponent component);

  /**
   * Liefert die Kindobjekte.
   *
   * @return
   */
  List<ModelComponent> getChildComponents();

  /**
   * Liefert einen String, der in der Baumansicht angezeigt wird.
   *
   * @return
   */
  String getTreeViewName();

  /**
   * Gibt an, ob die übergebene Komponente eine direkte Komponente ist.
   *
   * @param component
   * @return
   */
  boolean contains(ModelComponent component);

  /**
   * Liefert die direkte Elternkomponente.
   *
   * @return die direkte Elternkomponente
   */
  ModelComponent getParent();

  /**
   * Returns the actual parent of this component. PropertiesCollection e.g.
   * overwrites it. May be null.
   *
   * @return The actual parent.
   */
  ModelComponent getActualParent();

  /**
   * Setzt die direkte Elternkomponente.
   *
   * @param parent die direkte Elternkomponente
   */
  void setParent(ModelComponent parent);

  /**
   * Liefert true zurück, wenn die Komponente im TreeView dargestellt werden
   * soll, ansonsten false.
   *
   * @return
   */
  boolean isTreeViewVisible();

  /**
   * Setzt die TreeView-Sichtbarkeit der Komponente.
   *
   * @param visibility true, wenn die Komponente im TreeView angezeigt werden
   * soll; false, wenn die Komponente nicht angezeigt werden soll
   */
  void setTreeViewVisibility(boolean visibility);

  /**
   * Liefert eine ganz kurze Beschreibung, um was für ein Objekt es sich
   * handelt.
   *
   * @return
   */
  String getDescription();

  /**
   * Liefert den Namen des Objekts.
   *
   * @return
   */
  String getName();

  /**
   * Sets this model component's name.
   *
   * @param name The new name.
   */
  void setName(String name);

  /**
   * Liefert zum aktuellen Schlüssel das Attribut mit dem übergebenen Namen.
   *
   * @param name
   * @return
   */
  Property getProperty(String name);

  /**
   * Liefert eine Hashtable mit den Attributen des aktuell gesetzten Schlüssels.
   *
   * @return
   */
  Map<String, Property> getProperties();

  /**
   * Fügt unter dem übergebenen Namen einen Beutel mit Attributen hinzu.
   *
   * @param name
   * @param property
   */
  void setProperty(String name, Property property);
  
  /**
   * Returns this component's name property.
   *
   * @return This component's name property.
   */
  default StringProperty getPropertyName() {
    return (StringProperty) getProperty(NAME);
  };

  /**
   * Fügt den übergebenen AttributesChangeListener hinzu und informiert diesen
   * fortan, wenn sich die Eigenschaften oder Zustände des ModelComponent
   * geändert haben.
   *
   * @param l der hinzuzufügende AttributesChangeListener
   */
  void addAttributesChangeListener(AttributesChangeListener l);

  /**
   * Entfernt den übergebenen AttributesChangeListener und informiert diesen
   * fortan nicht mehr, wenn sich die Eigenschaften oder Zustände des
   * ModelComponent geändert haben.
   *
   * @param l der zu entfernende AttributesChangeListener
   */
  void removeAttributesChangeListener(AttributesChangeListener l);

  /**
   * Prüft, ob ein bestimmter AttributesChangeListener vorhanden ist.
   *
   * @param l der zu prüfende AttributesChangeListener
   * @return
   * <code> true </code>, wenn der AttributesChangeListener vorhanden ist
   */
  boolean containsAttributesChangeListener(AttributesChangeListener l);

  /**
   * Benachrichtigt alle registrierten AttributesChangeListener, dass sich die
   * Eigenschaften des Models geändert haben.
   *
   * @param l Der Initiator der Änderung.
   */
  void propertiesChanged(AttributesChangeListener l);

  /**
   * Clones this ModelComponent.
   *
   * @return A clone of this ModelComponent.
   * @throws java.lang.CloneNotSupportedException
   */
  ModelComponent clone()
      throws CloneNotSupportedException;
}
