/**
 * Classes describing transport orders to be processed by vehicles.
 *
 * <h1>How to create a new transport order</h1>
 *
 * <pre>
 * <code>
 * import org.opentcs.data.TCSObjectReference;
 * import org.opentcs.data.model.Location;
 * import org.opentcs.data.order.DriveOrder.Destination;
 * import org.opentcs.data.order.TransportOrder;
 * import org.opentcs.kernel.Kernel;
 * 
 * // The Kernel instance we're working with
 * Kernel kernel = ...;
 * 
 * // A list of Destination instances the transport order shall consist of:
 * List&lt;Destination&gt; destinations = new LinkedList&lt;Destination&gt;();
 * 
 * // Every single destination of the order has to be added to the list like this:
 * 
 * // Get a reference to the location to move to...
 * // (You can get a set of all existing Location instances using
 * // kernel.getTCSObjects(Location.class).)
 * Location destLoc = ...;
 * TCSObjectReference&lt;Location&gt; destLocRef = destLoc.getReference();
 * // ...and an operation the vehicle should execute at the location.
 * // (You can get a list of all operations allowed at the chosen location by
 * // looking into the LocationType instance that destLoc.getType() references.)
 * String destOp = ...;
 * // Create a new Destination instance and add it to the list.
 * destinations.add(new Destination(destLocRef, destOp));
 * 
 * // Add as many destinations to the list like this as necessary.
 * 
 * // Eventually create a new transport order with these destinations:
 * TransportOrder newOrder = kernel.createTransportOrder(destinations);
 * 
 * // Assign a vehicle to the transport order (optional)
 * kernel.setTransportOrderIntendedVehicle(newOrder.getReference(),
 *                                         someVehicle.getReference());
 * 
 * // Assign a deadline to the transport order (optional)
 * kernel.setTransportOrderDeadline(newOrder.getReference(),
 *                                  someTimestamp);
 * 
 * // And at last activate the transport order
 * kernel.activateTransportOrder(newOrder.getReference());
 * 
 * // Once a vehicle is available and able to process the transport order, the
 * // kernel will assign it immediately.
 * </code>
 * </pre>
 * 
 * <h1>How to withdraw a transport order that is currently being processed</h1>
 * 
 * <pre>
 * <code>
 * import org.opentcs.data.model.Vehicle;
 * import org.opentcs.kernel.Kernel;
 * 
 * // The Kernel instance we're working with
 * Kernel kernel = ...;
 * 
 *  // Get the transport order to be withdrawn.
 * TransportOrder curOrder = ...;
 * // Withdraw the order
 * kernel.withdrawTransportOrder(curOrder.getReference());
 * </code>
 * </pre>
 * 
 * <h1>How to withdraw a transport order via a reference on the vehicle
 * processing it</h1>
 * 
 * <pre>
 * <code>
 * import org.opentcs.data.model.Vehicle;
 * import org.opentcs.kernel.Kernel;
 * 
 * // The Kernel instance we're working with
 * Kernel kernel = ...;
 * 
 * // Get the vehicle from which the transport order shall be withdrawn
 * Vehicle curVehicle = kernel.getVehicle(vehicleName);
 * // Withdraw the order
 * kernel.withdrawTransportOrderByVehicle(curVehicle.getReference());
 * </code>
 * </pre>
 * 
 * <h1>
 * How to create a transport order that sends a vehicle to a point instead of a
 * location
 * </h1>
 * 
 * <pre>
 * <code>
 * import org.opentcs.data.model.Vehicle;
 * import org.opentcs.data.order.TransportOrder;
 * 
 * // The point the vehicle shall be sent to
 * Point destPos = ...;
 * 
 * // Wrap the name of the point in a dummy location reference
 * TCSObjectReference&lt;Location&gt; dummyLocRef =
 *     TCSObjectReference.getDummyReference(Location.class, destPos.getName());
 * 
 * // Create a Destination instance using the dummy location reference and use
 * // Destination.OP_MOVE as the operation to be executed.
 * Destination dummyDest = new Destination(dummyLocRef, Destination.OP_MOVE);
 * // Wrap the Destination instance in a list.
 * List&lt;Destination&gt; dummyDests = Collections.singletonList(dummyDest);
 * 
 * // Create a transport order using the list
 * TransportOrder dummyOrder = kernel.createTransportOrder(dummyDests);
 * // Assign a specific vehicle to the transport order (optional)
 * kernel.setTransportOrderIntendedVehicle(dummyOrder.getReference(),
 *                                         vehicle.getReference());
 * 
 * // Activate the new transport order
 * kernel.activateTransportOrder(dummyOrder.getReference());
 * 
 * // Once a vehicle is available and able to process the transport order, the
 * // kernel will assign it immediately.
 * </code>
 * </pre>
 * 
 * <h1>Using order sequences</h1>
 * 
 * An order sequence can be used to force a single vehicle to process multiple
 * transport orders in a given order. Some rules for using order sequences
 * are described in the {@link org.opentcs.data.order.OrderSequence
 * OrderSequence class documentation}, but here is what you would do in general:
 * 
 * <pre>
 * // Create an order sequence.
 * OrderSequence orderSequence = kernel.createOrderSequence();
 * 
 * // Set the order sequence's failureFatal flag (optional).
 * kernel.setOrderSequenceFailureFatal(orderSequence.getReference, true);
 * 
 * // Create an order and set it up as usual, but do not activate it, yet!
 * TransportOrder order = ...
 * 
 * // Add the order to the sequence.
 * kernel.addOrderSequenceOrder(orderSequence.getReference(),
 *                              order.getReference());
 * 
 * // Activate the order when it may be processed by a vehicle.
 * kernel.activateTransportOrder(order.getReference());
 * 
 * // Create, add and activate more orders as necessary. As long as the sequence
 * // has not been marked as complete and finished completely, the vehicle
 * // selected for its first order will be tied to this sequence and will not
 * // process any orders not belonging to the same sequence.
 * 
 * // Eventually, set the order sequence's complete flag to indicate that more
 * // transport orders will not be added to it.
 * kernel.setOrderSequenceComplete(orderSequence.getReference());
 * 
 * // Once the complete flag of the sequence has been set and all transport
 * // orders belonging to it have been processed, its finished flag will be set
 * // by the kernel.
 * </pre>
 */
package org.opentcs.data.order;
