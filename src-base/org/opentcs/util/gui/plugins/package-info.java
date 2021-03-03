/**
 * Interfaces and classes to support plugin-like extension of the openTCS plant
 * overview client.
 *
 * <h1>How to create a plugin panel for the plant overview client</h1>
 *
 * <ol>
 * <li>Create an implementation of {@link org.opentcs.util.gui.plugins.PanelFactory} 
 * that produces instances of the <code>JPanel</code> you want to display.</li>
 * <li>Build and package the <code>PanelFactory</code> implementation and the
 * classes it depends on into a JAR file.</li>
 * <li>In the JAR file, register the factory class as a service of type
 * <code>org.opentcs.util.gui.plugins.PanelFactory</code>. To do that, the JAR
 * file must contain a folder named <code>META-INF/services/</code> with a file
 * named <code>org.opentcs.util.gui.plugins.PanelFactory</code>. This file
 * should consist of a single line of text holding simply the name of the
 * factory class, e.g.:
 * <blockquote>
 * <code>
 * org.opentcs.guing.genericclient.panels.loadgenerator.ContinuousLoadPanelFactory
 * </code>
 * </blockquote>
 * </li>
 * <li>Place the JAR file in the generic client's class path (subdirectory
 * <code>lib/openTCS-extensions</code> of the generic client's installation
 * directory) and start the client.</li>
 * </ol>
 *
 * <h1>How to create a location/vehicle theme for openTCS</h1>
 *
 * <h2>Creating the theme implementation</h2>
 *
 * Create a new class which implements <code>LocationTheme</code> or
 * <code>VehicleTheme</code>. The class must have a a zero-argument constructor.
 *
 * <h2>Requirements for a theme implementation to be found at runtime</h2>
 *
 * <ol>
 * <li>
 * The JAR file containing your theme and all resources needed by it must also
 * contain a folder named <code>META-INF/services/</code> with a file named
 * <code>org.opentcs.util.gui.plugins.LocationTheme</code> (or
 * <code>org.opentcs.util.gui.plugins.VehicleTheme</code>). This file
 * should consist of a single line of text holding simply the name of the
 * implementing class, e.g.:
 * <blockquote>
 * <code>
 * org.opentcs.custom.MyLocationTheme
 * </code>
 * </blockquote>
 * <p>
 * Background: openTCS uses <code>java.util.ServiceLoader</code> to find themes
 * dynamically on startup, which depends on e.g.
 * <code>META-INF/services/org.opentcs.util.gui.LocationTheme</code>.
 * </p>
 * <p>
 * Hint: If you're using <a href="http://ant.apache.org">Apache Ant</a> to build
 * your theme, you can use its <code>jar</code> task to register your theme
 * implementation as a service.
 * </p>
 * </li>
 * <li>
 * The JAR file of your theme, containing all neccessary resources, has to be
 * placed in the subdirectory <code>lib/openTCS-extensions</code> of the openTCS
 * plant overview application's installation directory <em>before</em> the
 * application is started.
 * <br/>
 * (The openTCS start scripts include all JAR files in that directory in the
 * application's classpath.)
 * </li>
 * </ol>
 *
 * <p>
 * For an example, please look at the <code>buildplantoverview</code> target in
 * the Ant build file (<code>build.xml</code>) in the openTCS source
 * distribution.
 * </p>
 * <p>
 * If your theme meets these requirements, it should be found automatically
 * when you start the plant overview client.
 * </p>
 */
package org.opentcs.util.gui.plugins;
