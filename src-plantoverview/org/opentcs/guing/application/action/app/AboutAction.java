/**
 * (c): IML.
 *
 */
package org.opentcs.guing.application.action.app;

import java.awt.event.ActionEvent;
import java.util.Objects;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.exchange.DefaultKernelProxyManager;
import org.opentcs.guing.exchange.KernelProxyManager;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.util.Environment;

/**
 * Displays a dialog showing information about the application.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class AboutAction
    extends AbstractAction {

  /**
   * This action's ID.
   */
  public final static String ID = "application.about";
  /**
   * The application's view.
   */
  private final OpenTCSView view;
  /**
   * The kernel proxy/connection manager to be used.
   */
  private final KernelProxyManager kernelProxyManager;

  /**
   * Creates a new instance.
   *
   * @param view The application's view.
   */
  public AboutAction(OpenTCSView view) {
    this.view = Objects.requireNonNull(view, "view is null");
    this.kernelProxyManager = DefaultKernelProxyManager.instance();
    ResourceBundleUtil.getBundle().configureAction(this, ID);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    JOptionPane.showMessageDialog(
        view,
        "<html><p><b>" + OpenTCSView.NAME + " - " + Environment.getVersionString() + "</b><br>"
        + OpenTCSView.COPYRIGHT + "<br>"
        + "Running on<br>"
        + "Java: " + System.getProperty("java.version") + ", " + System.getProperty("java.vendor") + "<br>"
        + "JVM: " + System.getProperty("java.vm.version") + ", " + System.getProperty("java.vm.vendor") + "<br>"
        + "OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + ", " + System.getProperty("os.arch") + "<br>"
        + "<b>Kernel</b><br>"
        + kernelProxyManager.getHost() + ":" + kernelProxyManager.getPort()
        + "<br>Mode: " + view.getOperationMode()
        + "</p></html>",
        "About",
        JOptionPane.PLAIN_MESSAGE,
        new ImageIcon(getClass().getResource("/org/opentcs/guing/res/symbols/openTCS/openTCS.300x132.gif")));
  }
}
