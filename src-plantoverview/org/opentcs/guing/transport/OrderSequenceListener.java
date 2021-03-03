/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.transport;

//import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.order.OrderSequence;

/**
 * Klassen, die an Änderungen der Liste der Transportaufträge interessiert sind,
 * implementieren dieses Interface.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface OrderSequenceListener {

	/**
	 * Botschaft, dass eine Transportauftragskette hinzugefügt wurde.
	 *
	 * @param os Die Transportauftragskette
	 */
	void orderSequenceAdded(OrderSequence os);

	/**
	 * Botschaft, dass eine Transportauftragskette entfernt wurde.
	 *
	 * @param os Die Transportauftragskette
	 */
	void orderSequenceRemoved(OrderSequence os);

	/**
	 * Botschaft, dass sich eine Transportauftragskette geändert hat.
	 *
	 * @param os Die Transportauftragskette
	 */
	void orderSequenceChanged(OrderSequence os);
}
