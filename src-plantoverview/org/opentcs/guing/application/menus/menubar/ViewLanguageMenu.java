/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.menus.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.util.ApplicationConfiguration;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ViewLanguageMenu
    extends JMenu {

  private static final ResourceBundleUtil labels = ResourceBundleUtil.getBundle();

  @Inject
  public ViewLanguageMenu(final OpenTCSView view,
                          final ApplicationConfiguration appConfig) {
    requireNonNull(view, "view");

    final ButtonGroup bgLanguage = new ButtonGroup();

    final JCheckBoxMenuItem cbiLanguageDE = new JCheckBoxMenuItem(labels.getString("language.german"));
    add(cbiLanguageDE);

    if (Locale.getDefault().equals(Locale.GERMAN)) {
      cbiLanguageDE.setSelected(true);
    }

    bgLanguage.add(cbiLanguageDE);

    cbiLanguageDE.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            appConfig.setLocale(Locale.GERMAN);
            cbiLanguageDE.setSelected(true);
            JOptionPane.showMessageDialog(view,
                                          labels.getString("message.restart"));
          }
        }
    );

    final JCheckBoxMenuItem cbiLanguageEN = new JCheckBoxMenuItem(labels.getString("language.english"));
    add(cbiLanguageEN);

    if (Locale.getDefault().equals(Locale.ENGLISH)) {
      cbiLanguageEN.setSelected(true);
    }

    bgLanguage.add(cbiLanguageEN);

    cbiLanguageEN.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            appConfig.setLocale(Locale.ENGLISH);
            cbiLanguageEN.setSelected(true);
            JOptionPane.showMessageDialog(view,
                                          labels.getString("message.restart"));
          }
        }
    );
  }

  public void setOperationMode(OperationMode mode) {
    requireNonNull(mode, "mode");

  }

}
