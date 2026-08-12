// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.controlcenter.action.prefill;

import java.awt.Component;

/**
 * A factory for creating prefill dialog panels.
 */
public interface PrefillDialogFactory {

  InitPositionPrefillDialog createInitPositionPrefillDialog(Component parent, boolean modal);
}
