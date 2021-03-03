/**
 * Interfaces and implementations of components needed for controlling physical
 * vehicles and processing information sent by them.
 *
 * <h1>How to create a vehicle driver for openTCS</h1>
 *
 * <h2>Steps to create a new vehicle driver</h2>
 *
 * <ol>
 * <li>Create a new class which extends <code>BasicCommunicationAdapter</code>:
 * <ol>
 * <li>
 * Implement the abstract methods of <code>BasicCommunicationAdapter</code> in
 * the extended class.
 * </li>
 * <li>
 * Let the driver call
 * {@link org.opentcs.drivers.BasicCommunicationAdapter#commandExecuted(AdapterCommand)}
 * when the controlled vehicle's reported state indicates that it has finished
 * an order. This method should be called for <code>AdapterCommand</code>s in
 * the same order they were given to the driver instance.
 * </li>
 * <li>
 * In situations in which the state of a controlled vehicle changes in a way
 * that is relevant for the driver's GUI, your driver should call
 * {@link org.opentcs.drivers.BasicCommunicationAdapter#updateViews()}.
 * </li>
 * <li>
 * Optional: Override the methods <code>enable()</code> and
 * <code>disable()</code>. (Make sure to call <code>super.enable()</code> or
 * <code>super.disable()</code> if you override them.)
 * </li>
 * <li>
 * Optional: Override the methods <code>setCommandQueueCapacity()</code> and
 * <code>setSentQueueCapacity()</code>. (The former sets the maximum number of
 * commands that the openTCS kernel may send to the communication adapter; the
 * latter sets the maximum number of commands that the driver may send to the
 * vehicle.)
 * </li>
 * <li>
 * Optional: Override the method <code>canSendNextCommand()</code>.
 * </li>
 * <li>
 * Optional: Override the method <code>getCustomDisplayPanels()</code> to make
 * custom GUI panels available which allow you to manually send vehicle specific
 * commands/telegrams or show vehicle specific data.
 * </li>
 * </ol>
 * </li>
 * <li>
 * Create a new class which implements <code>CommunicationAdapterFactory</code>:
 * <ol>
 * <li>
 * Implement all methods of the interface, so instances of your new
 * <code>CommunicationAdapter</code> implementation can be created, configured
 * and returned.
 * </li>
 * </ol>
 * </li>
 * <li>
 * Create panels that the kernel GUI should display for the adapter:
 * <ol>
 * <li>
 * Each panel must extend <code>javax.swing.JPanel</code> and implement the
 * interface <code>CommunicationAdapterView</code>.
 * </li>
 * <li>
 * Create and return instances of the implemented panel classes in the
 * <code>getCustomDisplayPanels()</code> of your adapter class.
 * </li>
 * </ol>
 * </li>
 * </ol>
 * 
 * <p>
 * For an example, please look at the implementation of the loopback adapter for
 * virtual vehicles in package <code>org.opentcs.virtualvehicle</code> of the
 * openTCS source distribution.
 * </p>
 *
 * <h2>Requirements for a vehicle driver to be found at runtime</h2>
 *
 * <ol>
 * <li>
 * The JAR file containing your driver and all resources needed by it must also
 * contain a folder named <code>META-INF/services/</code> with a file named
 * <code>org.opentcs.drivers.CommunicationAdapterFactory</code>. This file
 * should consist of a single line of text holding simply the name of the
 * factory class, e.g.:
 * <blockquote>
 * <code>
 * org.opentcs.virtualvehicle.LoopbackCommunicationAdapterFactory
 * </code>
 * </blockquote>
 * <p>
 * Background: openTCS uses <code>java.util.ServiceLoader</code> to find vehicle
 * drivers dynamically on startup, which depends on
 * <code>META-INF/services/org.opentcs.drivers.CommunicationAdapterFactory</code>.
 * </p>
 * <p>
 * Hint: If you're using <a href="http://ant.apache.org">Apache Ant</a> to build
 * your communication adapter, you can use its <code>jar</code> task to register
 * your adapter factory implementation as a service.
 * </p>
 * </li>
 * <li>
 * The JAR file of your driver, containing all neccessary resources, has to be
 * placed in the subdirectory <code>lib/openTCS-extensions</code> of the openTCS
 * kernel application's installation directory <em>before</em> the kernel is
 * started.
 * <br/>
 * (The openTCS start scripts include all JAR files in that directory in the
 * application's classpath.)
 * </li>
 * </ol>
 *
 * <p>
 * For an example, please look at the <code>buildkernel</code> target in the Ant
 * build file (<code>build.xml</code>) in the openTCS source distribution.
 * </p>
 * <p>
 * If your driver meets these requirements, it should be found automatically
 * when you start the kernel.
 * </p>
 * 
 * <h2>Communication between kernel clients and communication adapters</h2>
 * 
 * <p>
 * Sometimes it is required to have some influence on the behaviour of a
 * communication adapter (and thus the vehicle it is associated with) directly
 * from a kernel client - to send a special telegram to the vehicle, for
 * instance. For these cases,
 * {@link org.opentcs.access.Kernel#sendCommAdapterMessage(org.opentcs.data.TCSObjectReference, java.lang.Object)}
 * provides a one-way communication channel for a client to send a message
 * object to a communication adapter currently associated with a vehicle; an
 * adapter overriding <code>processMessage()</code> may interpret message
 * objects sent to it and react in an appropriate way. Note that the client
 * sending the message may not know which communication adapter implementation
 * is currently associated with the vehicle, so the adapter may or may not be
 * able to understand the message.
 * </p>
 * <p>
 * For messages from the communication adapter to a client there is no direct
 * channel for the adapter to use. A communication adapter may, however, set
 * values in a vehicle's properties to deliver information to all connected
 * clients.
 * </p>
 * 
 * <h2>Supportive classes</h2>
 * 
 * <p>
 * If the communication protocol of the vehicle you want to create a driver for
 * is based on TCP/IP, you may also want to have a look at some of the utility
 * classes in the package {@link org.opentcs.util.communication.tcp}.
 * </p>
 */
package org.opentcs.drivers;
