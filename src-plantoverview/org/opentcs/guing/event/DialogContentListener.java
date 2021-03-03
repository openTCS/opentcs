/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.event;

/**
 * Interface for dialogs that want to react on messages of its content.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface DialogContentListener {

	/**
   * Message of the content that it needs repainting.
	 *
	 * @param evt The fired event.
	 */
	void requestLayoutUpdate(DialogContentEvent evt);

	/**
	 * Message of the content that it is ready to be closed.
	 *
	 * @param evt The fired event.
	 */
	void requestClose(DialogContentEvent evt);
}
