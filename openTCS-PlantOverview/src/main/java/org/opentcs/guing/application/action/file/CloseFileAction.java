/**
 * (c): IML, JHotDraw.
 *
 * Extended by IML: 1. Allow access to ResourceBundle 2. Save changes to actual
 * kernel model
 *
 *
 * @(#)CloseFileAction.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.opentcs.guing.application.action.file;

import java.awt.event.ActionEvent;
import java.net.URI;
import static java.util.Objects.requireNonNull;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jhotdraw.app.View;
import org.jhotdraw.app.action.file.NewFileAction;
import org.jhotdraw.app.action.file.NewWindowAction;
import org.jhotdraw.app.action.file.OpenDirectoryAction;
import org.jhotdraw.app.action.file.OpenFileAction;
import org.jhotdraw.net.URIUtil;
import org.opentcs.access.Kernel;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Closes the active view after letting the user save unsaved changes.
 * {@code DefaultSDIApplication} automatically exits when the user closes the
 * last view.
 * <p>
 * This action is called when the user selects the Close item in
 * the File menu. The menu item is automatically created by the application.
 * <p>
 * If you want this behavior in your application, you have to create it and put
 * it in your {@code ApplicationModel} in method
 * {@link org.jhotdraw.app.ApplicationModel#initApplication}.
 * <p>
 * You should
 * include this action in applications which use at least one of the following
 * actions, so that the user can close views that he/she created: {@link NewFileAction}, {@link NewWindowAction},
 * {@link OpenFileAction}, {@link OpenDirectoryAction}.
 * <p>
 *
 * @author Werner Randelshofer
 */
public class CloseFileAction
    extends AbstractAction {

  public final static String ID = "file.close";
  public final static String ID_WINDOW_CLOSING = "windowClosing";
  public final static String ID_MODEL_CLOSING = "modelClosing";
  /**
   * 0: Save file 1: Dont't save file 2: Canceled
   */
  private int fileSaved;
  private final OpenTCSView view;

  /**
   * Creates a new instance.
   *
   * @param view The openTCS view
   */
  public CloseFileAction(OpenTCSView view) {
    this.view = requireNonNull(view, "view");
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    labels.configureAction(this, ID);
  }

  public int getFileSavedStatus() {
    return fileSaved;
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    final ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.app.Labels");

    if (view.hasUnsavedChanges()) {
      URI unsavedURI = view.getURI();
      String message
          = "<html><b>"
          + labels.getFormatted("file.saveBefore.doYouWantToSave.message",
                                (unsavedURI == null) ? Kernel.DEFAULT_MODEL_NAME : URIUtil.getName(unsavedURI))
          + "</b><p>"
          + labels.getString("file.saveBefore.doYouWantToSave.details")
          + "</p></html>";

      Object[] options = {
        labels.getString("file.saveBefore.saveOption.text"),
        labels.getString("file.saveBefore.dontSaveOption.text"),
        labels.getString("file.saveBefore.cancelOption.text")
      };

      int option = JOptionPane.showOptionDialog(view.getComponent(), message, "", // title
                                                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
                                                null, // icon
                                                options, options[0]);

      fileSaved = JOptionPane.CANCEL_OPTION;

      switch (option) {
        case JOptionPane.YES_OPTION: // Save
          if (view.saveModel()) {
            fileSaved = JOptionPane.YES_OPTION;
            doIt(evt.getActionCommand(), view);
          }

          break;

        case JOptionPane.NO_OPTION:  // Don't save
          fileSaved = JOptionPane.NO_OPTION; // Der Benutzer will nicht speichern
          doIt(evt.getActionCommand(), view);
          break;

        default:
        case JOptionPane.CANCEL_OPTION:
          break;
      }
    }
    else {
      fileSaved = JOptionPane.NO_OPTION;
      doIt(evt.getActionCommand(), view);
    }
  }

  protected void doIt(String actionCommand, View view) {
    if (!actionCommand.equals(ID_MODEL_CLOSING) && view != null) {
      if (view.isShowing()) {
        view.setShowing(false);
        JFrame f = (JFrame) SwingUtilities.getWindowAncestor(view.getComponent());
        f.setVisible(false);
        f.remove(view.getComponent());
        f.dispose();
      }

      view.dispose();
    }
  }

  protected void doIt(View view) {
    doIt(ID_WINDOW_CLOSING, view);
  }
}
