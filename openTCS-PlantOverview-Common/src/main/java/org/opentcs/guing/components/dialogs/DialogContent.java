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
import javax.swing.JPanel;

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
   * Zeigt an, ob das Übernehmen der Werte aus den Bedienelementen fehlgeschlagen ist.
   */
  protected boolean updateFailed;
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
   * Zeigt an, ob das Übernehmen der Werte aus den Bedienelementen
   * fehlgeschlagen ist.
   *
   * @return
   * <code> true </code>, wenn ein Fehler aufgetreten ist
   */
  public boolean updateFailed() {
    return updateFailed;
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
