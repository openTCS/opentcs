/**
 * (c): IML.
 *
 */
package org.opentcs.guing.application.action.view;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.opentcs.guing.application.GuiManager;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * An action to zoom the drawing to a scalefactor so that it fits the
 * window size.
 * 
 * @author Heinz Huber (Fraunhofer IML)
 */
public class ZoomViewToWindowAction
		extends AbstractAction {

	public final static String ID = "view.zoomViewToWindow";
	private final GuiManager view;

	/**
	 * Creates a new instance.
	 */
	public ZoomViewToWindowAction(GuiManager view) {
		this.view = view;
		ResourceBundleUtil.getBundle().configureAction(this, ID);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		view.zoomViewToWindow();
	}
}
