/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.event;

import java.util.EventObject;
import org.opentcs.guing.model.elements.BlockModel;

/**
 * An event that informs listener about changes in a block area.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class BlockChangeEvent
		extends EventObject {

	/**
	 * Creates a new instance of BlockElementChangeEvent.
   * 
   * @param block The <code>BlockModel</code> that has changed.
	 */
	public BlockChangeEvent(BlockModel block) {
		super(block);
	}
}
