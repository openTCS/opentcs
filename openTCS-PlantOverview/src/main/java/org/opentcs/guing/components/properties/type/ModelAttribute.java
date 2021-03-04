/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.properties.type;

import java.io.Serializable;
import org.opentcs.guing.model.ModelComponent;

/**
 * Interface für Eigenschaften (Property) von ModelComponent-Objekten.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface ModelAttribute
    extends Serializable {

  public static enum ChangeState {

    NOT_CHANGED,
    CHANGED, // Das Attribut wurde in der Tabelle geändert
    DETAIL_CHANGED, // Das Attribut wurde über den Popup-Dialog geändert
  };

  /**
   * Liefert das Model, zu dem dieses Attribut gehört.
   *
   * @return
   */
  public ModelComponent getModel();

  /**
   *
   * @param model
   */
  public void setModel(ModelComponent model);

  /**
   * Kennzeichnet das Attribut als geändert. Wird vom Controller/View
   * aufgerufen, der die Änderung vorgenommen hat.
   */
  void markChanged();

  /**
   * Kennzeichnet ein Attribut als nicht geändert. Hebt damit markChanged() auf.
   * Wird vom Model aufgerufen, nachdem sich alle Views aktualisiert haben.
   */
  void unmarkChanged();

  /**
   *
   * @param changeState
   */
  void setChangeState(AbstractModelAttribute.ChangeState changeState);

  /**
   * Gibt zurück, ob sich der Zustand geändert hat oder nicht. Damit wissen
   * Views und Fahrkurselemente von Fahrzeugtypen, ob überhaupt eine
   * übernehmenswerte Änderung vorliegt.
   *
   * @return
   */
  boolean hasChanged();

  /**
   * Setzt die Bezeichnung dieser Zustandsrepräsentation.
   *
   * @param description
   */
  void setDescription(String description);

  /**
   * Liefert die Bezeichnung eines Zustands.
   *
   * @return
   */
  String getDescription();

  /**
   * Setzt den Hilfetext für einen Zustand.
   *
   * @param helptext
   */
  void setHelptext(String helptext);

  /**
   * Liefert den Hilfetext für diesen Zustand.
   *
   * @return
   */
  String getHelptext();

  /**
   * Sagt, ob das Attribut gleichzeitig mit den gleichnamigen Attributen anderer
   * Fahrkurselemente bearbeitet werden kann.
   *
   * @param collectiveEditable
   */
  void setCollectiveEditable(boolean collectiveEditable);

  /**
   * Zeigt an, ob das Attribut gleichzeitig mit den gleichnamigen Attributen
   * anderer Fahrkurselemente desselben Fahrzeugtyps bearbeitet werden kann.
   *
   * @return
   */
  boolean isCollectiveEditable();

  /**
   * @param editable true, wenn der Benutzer das Attribut im Kernel-Modus
   * "Modelling" verändern kann.
   */
  void setModellingEditable(boolean editable);

  /**
   * @return true, wenn der Benutzer das Property im Kernel-Modus "Modelling"
   * verändern kann, ansonsten false.
   */
  boolean isModellingEditable();

  /**
   * @param editable true, wenn der Benutzer das Attribut im Kernel-Modus
   * "Operating" verändern kann.
   */
  void setOperatingEditable(boolean editable);

  /**
   * @return true, wenn der Benutzer das Property im Kernel-Modus "Operating"
   * verändern kann, ansonsten false.
   */
  boolean isOperatingEditable();
  
  /**
   * @return true, if this attribute should be considered when persisting the model to a file or not.
   */
  default boolean isPersistent() {
    return true;
  }
}
