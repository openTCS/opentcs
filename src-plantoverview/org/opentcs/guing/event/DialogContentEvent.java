/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.event;

import java.util.EventObject;

/**
 * An event that is sent from dialog content to the parent dialogs. The content
 * can force its parent to repaint or close.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class DialogContentEvent
		extends EventObject {

	/**
	 * Creates a new instance of DialogContentEvent.
   * 
   * @param dialogContent The dialog content.
	 */
	public DialogContentEvent(Object dialogContent) {
		super(dialogContent);
	}
}
