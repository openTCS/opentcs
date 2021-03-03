/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.guing.application.action.view;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.opentcs.guing.application.OpenTCSView;

/**
 * An action for adding new transport order sequence views.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class AddTransportOrderSequenceView
    extends AbstractAction {

  public final static String ID = "view.addOSView";
  private final OpenTCSView view;

  public AddTransportOrderSequenceView(OpenTCSView view) {
    this.view = view;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    view.addTransportOrderSequenceView(true);
  }
}
