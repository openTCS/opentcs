/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.view;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import org.opentcs.guing.application.OpenTCSView;

/**
 * Actions for adding background bitmaps to the drawing view.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class AddBitmapAction
    extends AbstractAction {

  public final static String ID = "view.addBitmap";
  private final OpenTCSView view;

  /**
   * Creates a new instance.
   *
   * @param view The openTCS view
   */
  public AddBitmapAction(OpenTCSView view) {
    this.view = view;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // Create a file chooser
    final JFileChooser fc = new JFileChooser(System.getProperty("opentcs.home"));
    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    // In response to a button click:
    int returnVal = fc.showOpenDialog(null);

    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File file = fc.getSelectedFile();
      view.addBackgroundBitmap(file);
    }
  }
}
