/*
 *
 * Created on 18.09.2013 09:46:46
*/
package org.opentcs.guing.application.action.view;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.opentcs.guing.application.OpenTCSView;

/**
 * An action for adding new drawing views.
 * 
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class AddDrawingViewAction extends AbstractAction {

  public final static String ID = "view.addDrawingView";
	private final OpenTCSView view;

	/**
	 * Creates a new instance.
	 */
	public AddDrawingViewAction(OpenTCSView view) {
		this.view = view;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		view.addDrawingView();
	}
}


