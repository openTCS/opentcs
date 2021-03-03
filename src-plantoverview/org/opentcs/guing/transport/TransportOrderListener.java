/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.transport;

import org.opentcs.data.order.TransportOrder;

/**
 * Klassen, die an Änderungen der Liste der Transportaufträge interessiert sind,
 * implementieren dieses Interface.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface TransportOrderListener {

	/**
	 * Botschaft, dass ein Transportauftrag hinzugefügt wurde.
	 *
	 * @param t der Transportauftrag
	 */
	void transportOrderAdded(TransportOrder t);

	/**
	 * Botschaft, dass ein Transportauftrag entfernt wurde.
	 *
	 * @param t der Transportauftrag
	 */
	void transportOrderRemoved(TransportOrder t);

	/**
	 * Botschaft, dass sich ein Transportauftrag geändert hat.
	 *
	 * @param t der Transportauftrag
	 */
	void transportOrderChanged(TransportOrder t);
}
