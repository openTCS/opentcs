/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.event;

/**
 * Interface for listener that want to be informed wenn a block area has
 * changed.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface BlockChangeListener {

	/**
   * Message that the course elements have changed.
   * 
   * @param e The fire event.
	 */
	void courseElementsChanged(BlockChangeEvent e);

	/**
   * Message that the color of the block area has changed.
   * 
   * @param e The fire event.
	 */
	void colorChanged(BlockChangeEvent e);

	/**
   * Message that a block area was removed.
   * 
   * @param e The fire event.
	 */
	void blockRemoved(BlockChangeEvent e);
}
