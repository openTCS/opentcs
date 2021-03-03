/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.guing.transport;

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class UneditableTableModel
		extends DefaultTableModel {

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
}
