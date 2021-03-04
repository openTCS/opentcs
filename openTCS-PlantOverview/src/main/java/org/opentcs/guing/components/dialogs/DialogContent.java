/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.dialogs;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Basisimplementierung für Dialog- und Registerkarteninhalte.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public abstract class DialogContent
    extends JPanel {

  /**
   * Der Titel der Komponente, wenn sie in einem Dialog angezeigt wird.
   */
  protected String fDialogTitle;
  /**
   * Der Titel der Komponente, wenn sie in einer Registerkarte angezeigt wird.
   */
  protected String fTabTitle;
  /**
   * Zeigt an, ob das Parsen eines Textes fehlgeschlagen ist.
   */
  protected boolean fParsingFailed;
  /**
   * Zeigt an, ob der Dialog, der diese Komponente enthält, modal sein soll oder
   * nicht. Der Standardwert ist
   * <code>
   * true</code>.
   */
  protected boolean fModal;
  /**
   * Der Dialog, in den dieser Inhalt eingebettet ist.
   */
  protected StandardContentDialog fDialog;

  /**
   * Creates a new instance of AbstractDialogContent.
   */
  public DialogContent() {
    setModal(true);
  }

  /**
   * Liefert die Komponente.
   *
   * @return die Komponente
   */
  public JComponent getComponent() {
    return this;
  }

  /**
   * Vermerkt den Wunsch, in einem modalen bzw. nicht modalen Dialog angezeigt
   * zu werden.
   *
   * @param modal ist
   * <code>true</code>, wenn die Komponente in einem modalen Dialog angezeigt
   * werden möchte
   */
  public final void setModal(boolean modal) {
    fModal = modal;
  }

  /**
   * Zeigt an, ob die Komponente in einem modalen bzw. nicht modalen Dialog
   * angezeigt werden möchte.
   *
   * @return
   * <code>true</code>, wenn die Komponente in einem modalen Dialog angezeigt
   * werden möchte
   */
  public boolean getModal() {
    return fModal;
  }

  /**
   * Liefert den Titel der Komponente, falls sie in einer Registerkarte
   * angezeigt wird.
   *
   * @return den Registerkartentitel
   */
  public String getTabTitle() {
    return fTabTitle;
  }

  /**
   * Liefert den Titel des Dialogs, der diese Komponente anzeigt.
   *
   * @return den Dialogtitel
   */
  public String getDialogTitle() {
    return fDialogTitle;
  }

  /**
   * Setzt den Titel des Dialogs, der diese Komponente anzeigt.
   *
   * @param title der Titel
   */
  protected void setDialogTitle(String title) {
    fDialogTitle = title;
  }

  /**
   * Setzt den Titel der Registerkarte, der diese Komponente anzeigt.
   *
   * @param title der Titel
   */
  protected void setTabTitle(String title) {
    fTabTitle = title;
  }

  /**
   * Benachrichtigt den registrierten Listener, dass der Dialoginhalt gern
   * möchte, dass sein Dialog geschlossen wird.
   */
  protected void notifyRequestClose() {
    fDialog.requestClose();
  }

  /**
   * Extrahiert aus einem Text mit einer Einheit einen numerischen Wert. Diese
   * Funktion sollte nur aus der Methode update() aufgerufen werden.
   *
   * @param text der Text, der den numerischen Wert enthält
   * @param unit die Einheit hinter dem Wert
   * @param field eine Bezeichnung des Feldes, das geparst wird (wird nur im
   * Fehlerfall verwendet)
   * @param example ein richtiges Beispiel, wie ein einzugebender Wert
   * auszusehen hat (wird nur im Fehlerfall verwendet)
   * @return den geparsten Wert
   */
  protected double parseDouble(String text, String unit, String field, String example) {
    int index;

    if (!unit.isEmpty()) {
      index = text.indexOf(unit);

      if (index == -1) {
        showErrorMessage(field, example);
        fParsingFailed = true;

        return -1;
      }
    }
    else {
      index = text.length();
    }

    return parseDouble(text.substring(0, index), field, example);
  }

  /**
   * Extrahiert aus einem Text einen numerischen Wert. Diese Funktion sollte nur
   * aus der Methode update() aufgerufen werden.
   *
   * @param text der Text, der den numerischen Wert enthält
   * @param field eine Bezeichnung des Feldes, das geparst wird (wird nur im
   * Fehlerfall verwendet)
   * @param example ein richtiges Beispiel, wie ein einzugebender Wert
   * auszusehen hat (wird nur im Fehlerfall verwendet)
   * @return den geparsten Wert
   */
  protected double parseDouble(String text, String field, String example) {
    try {
      fParsingFailed = false;
      return Double.parseDouble(text);
    }
    catch (NumberFormatException e) {
      showErrorMessage(field, example);
      fParsingFailed = true;
    }

    return -1;
  }

  /**
   * Extrahiert aus einem Text mit einer Einheit einen numerischen Wert. Diese
   * Funktion sollte nur aus der Methode update() aufgerufen werden.
   *
   * @param text der Text, der den numerischen Wert enthält
   * @param unit die Einheit hinter dem Wert
   * @param field eine Bezeichnung des Feldes, das geparst wird (wird nur im
   * Fehlerfall verwendet)
   * @param example ein richtiges Beispiel, wie ein einzugebender Wert
   * auszusehen hat (wird nur im Fehlerfall verwendet)
   * @return den geparsten Wert
   */
  protected int parseInt(String text, String unit, String field, String example) {
    return (int) parseDouble(text, unit, field, example);
  }

  /**
   * Extrahiert aus einem Text einen numerischen Wert. Diese Funktion sollte nur
   * aus der Methode update() aufgerufen werden.
   *
   * @param text der Text, der den numerischen Wert enthält
   * @param field eine Bezeichnung des Feldes, das geparst wird (wird nur im
   * Fehlerfall verwendet)
   * @param example ein richtiges Beispiel, wie ein einzugebender Wert
   * auszusehen hat (wird nur im Fehlerfall verwendet)
   * @return den geparsten Wert
   */
  protected int parseInt(String text, String field, String example) {
    return (int) parseDouble(text, field, example);
  }

  /**
   * Zeigt ein Fenster mit der Meldung, dass das Parsen fehlgeschlagen ist.
   *
   * @param field eine Bezeichnung des Feldes, das geparst wird (wird nur im
   * Fehlerfall verwendet)
   * @param example ein richtiges Beispiel, wie ein einzug
   * = ResourceBundleUtil.getBundle().getString("DialogContent.parseError.example");
   * // String title = ResourceBundleUtil.getBundle().getString("DialogContent.title");
   * // "Eingabefehler";
   * // String message = "Fehler beim Parsen des Feldes '" + field + "'.\n"
   * // + "Korrektes Eingabebeispiel: " + example;
   * JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
   * }
   *
   * /**
   * Zeigt an, ob das Parsen eines Wertes fehlgeschlagen ist.
   *
   * @returnebender Wert
   * auszusehen hat (wird nur im Fehlerfall verwendet)
   */
  protected void showErrorMessage(String field, String example) {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    String title = bundle.getString("DialogContent.parseError.title");
    String parseErrorMessage
        = bundle.getFormatted("DialogContent.parseError.message", field);
    String validExample
        = bundle.getFormatted("DialogContent.parseError.example", example);
    String message = parseErrorMessage + "\n" + validExample;
    JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Zeigt an, ob das Parsen eines Wertes fehlgeschlagen ist.
   *
   * @return
   * <code> true </code>, wenn beim Parsen ein Fehler aufgetreten ist
   */
  public boolean parsingFailed() {
    return fParsingFailed;
  }

  /**
   * Zeigt an, ob das Übernehmen der Werte aus den Bedienelementen
   * fehlgeschlagen ist.
   *
   * @return
   * <code> true </code>, wenn ein Fehler aufgetreten ist
   */
  public boolean updateFailed() {
    return parsingFailed();
  }

  /**
   * Initialisiert die Dialogelemente.
   */
  public abstract void initFields();

  /**
   * Übernimmt die Werte aus den Dialogelementen.
   */
  public abstract void update();
}
