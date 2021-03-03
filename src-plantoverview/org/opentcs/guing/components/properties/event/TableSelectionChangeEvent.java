/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.event;

import java.util.EventObject;
import org.opentcs.guing.components.properties.table.AttributesTable;

/**
 * Ein Event, der generiert wird, wenn der Benutzer eine Tabellenzeile
 * selektiert. Das Event enthält die Tabelle, in der die Selektierung stattfand,
 * sowie das Attribut, das sich in der selektierten Tabellenzeile befindet.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class TableSelectionChangeEvent
		extends EventObject {

	/**
	 * Das Attribut.
	 */
	protected Object fSelectedValue;

	/**
	 * Creates a new instance of TableSelectionChangeEvent
	 */
	public TableSelectionChangeEvent(AttributesTable table, Object selectedValue) {
		super(table);
		fSelectedValue = selectedValue;
	}

	/**
	 * Liefert das Attribut, das sich in der selektierten Tabellenzeile befindet.
	 */
	public Object getSelectedValue() {
		return fSelectedValue;
	}
}
