/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.application;

import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.opentcs.guing.model.FiguresFolder;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;

/**
 * Provides some central services for various parts of the plant overview
 * application.
 * 
 * @author Heinz Huber (Fraunhofer IML)
 */
public interface GuiManager {

  /**
   * @return
   */
  DrawingView getDrawingView();

  /**
   * @return Der DrawingEditor.
   */
  DrawingEditor getEditor();

  /**
   * Returns the plant overview's current mode of operation.
   *
   * @return The plant overview's current mode of operation.
   */
  OperationMode getOperationMode();
  
  /**
   * Checks whether the plant overview is currently in the given mode of
   * operation.
   *
   * @param mode The mode to be checked for.
   * @return <code>true</code> if, and only if, the plant overview is currently
   * in the given mode.
   */
  boolean hasOperationMode(OperationMode mode);

  /**
   * Wird aufgerufen, wenn das Objekt im Baum selektiert wurde.
   *
   * @param modelComponent A folder in the system model tree
   */
  void selectModelComponent(ModelComponent modelComponent);

  /**
   * Wird aufgerufen, wenn mehrere Objekte im Baum selektiert wurden.
   *
   * @param modelComponent
   */
  void addSelectedModelComponent(ModelComponent modelComponent);

  /**
   * Wird aufgerufen, wenn das Objekt aus dem Baum entfernt wurde (aufgrund
   * einer Nutzereingabe).
   *
   * @param fDataObject
   * @return true, wenn das Objekt entfernt wurde
   */
  boolean treeComponentRemoved(ModelComponent fDataObject);

  /**
   * Informiert die Applikation, dass im TreeView ein Figure-Objekt doppelt
   * angeklickt wurde.
   *
   * @param modelComponent
   */
  void figureSelected(ModelComponent modelComponent);

  /**
   * Zeigt einen Dialog mit den Eigenschaften eines Fahrkurselements.
   *
   * @param model das Fahrkurselement
   */
  void showPropertiesDialog(ModelComponent model);

  /**
   * Wird aufgerufen, wenn im TreeView eine Blockstrecke doppelt angeklickt
   * wurde. Selektiert alle Figures im DrawingView, die zu der Blockstrecke
   * gehören.
   *
   * @param blockFiguresFolder
   */
  void blockSelected(FiguresFolder blockFiguresFolder);

  /**
   * @return Das Systemmodell.
   */
  SystemModel getSystemModel();

  /**
   * Löscht alles aus dem openTCS-Kern und führt Initialisierungen zu einem neu
   * erstellten Modell durch. Das Modell muss bei Aufruf dieser Methode bereits
   * erzeugt sein.
   */
  void createEmptyModel();

  /**
   * Lädt ein Fahrkurs-Modell vom Kernel
   */
  void loadModel();

  /**
   * @return
   */
  boolean saveModel();

  /**
   *
   */
  void saveModelAs();

  /**
   *
   */
  void loadViewBookmark();

  /**
   * Die aktuelle Ansicht als "View Bookmark" im Kernel speichern.
   */
  void saveViewBookmark();

  /**
   *
   */
  void zoomViewToWindow();

  /**
   * Erzeugt eine neues ModelComponent-Objekt, für das es kein Figure-Pendant
   * gibt. Hierzu gehören Blockbereich, Fahrzeug und Stationstyp.
   *
   * @param clazz Der Typ des zu erzeugenden Objekts.
   * @return das erzeugte ModelComponent-Objekt
   */
  ModelComponent createModelComponent(Class<? extends ModelComponent> clazz);

  /**
   * Öffnet den neuen einfachen Transportauftrag zur Bearbeitung.
   */
  void createTransportOrder();

  /**
   * Resets the selection tool to the default selection tool.
   */
  void resetSelectionTool();

  /**
   * Sucht nach einem Fahrzeug. Stellt zunächst eine Liste aller Fahrzeuge
   * zusammen und delgegiert die Aufgabe dann an den OpenTCSDrawingEditor, der
   * einen Dialog anzeigt, in dem das zu findende Fahrzeug ausgewählt werden
   * kann.
   */
  void findVehicle();

  /**
   * Shows a dialog containing all vehicles.
   */
  void showVehicles();

  /**
   * Defines the plant overview's potential modes of operation.
   */
  public static enum OperationMode {

    /**
     * For cases in which the mode of operation has not been defined, yet.
     */
    UNDEFINED,
    /**
     * Used when modelling a driving course.
     */
    MODELLING,
    /**
     * Used when operating a plant/system.
     */
    OPERATING
  }

}
