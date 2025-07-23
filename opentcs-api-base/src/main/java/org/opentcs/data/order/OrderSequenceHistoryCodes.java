// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data.order;

/**
 * Defines constants for basic history event codes related to {@link OrderSequence}s and documents
 * how the respective supplementary information is to be interpreted.
 */
public interface OrderSequenceHistoryCodes {

  /**
   * An event code indicating an order sequence has been created.
   * <p>
   * The history entry's supplements list is empty.
   * </p>
   */
  String SEQUENCE_CREATED = "tcs:history:sequenceCreated";

  /**
   * An event code indicating a transport order has been appended to an order sequence.
   * <p>
   * The history entry's supplements list contains one item with the name of the transport order
   * that was appended.
   * </p>
   */
  String SEQUENCE_ORDER_APPENDED = "tcs:history:sequenceOrderAppended";

  /**
   * An event code indicating an order sequence's processing vehicle changed.
   * <p>
   * The history entry's supplements list contains one item with the name of the new processing
   * vehicle, or the empty string, if the processing vehicle was unset.
   * </p>
   */
  String SEQUENCE_PROCESSING_VEHICLE_CHANGED = "tcs:history:sequenceProcVehicleChanged";

  /**
   * An event code indicating an order sequence has been completed and will not be extended by more
   * orders.
   * <p>
   * The history entry's supplements list is empty.
   * </p>
   */
  String SEQUENCE_COMPLETED = "tcs:history:sequenceCompleted";

  /**
   * An event code indicating an order sequence has been processed completely.
   * <p>
   * The history entry's supplements list is empty.
   * </p>
   */
  String SEQUENCE_FINISHED = "tcs:history:sequenceFinished";

}
