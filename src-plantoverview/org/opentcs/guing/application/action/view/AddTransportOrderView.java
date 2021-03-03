package org.opentcs.guing.application.action.view;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.opentcs.guing.application.OpenTCSView;

/**
 * An action for adding new transport order views.
 * 
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class AddTransportOrderView extends AbstractAction {
  
  public final static String ID = "view.addTOView";
  private final OpenTCSView view;
  
  public AddTransportOrderView(OpenTCSView view) {
    this.view = view;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    view.addTransportOrderView(true);
  }  
}
