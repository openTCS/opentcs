/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter.exchange;

import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.swing.JOptionPane;
import org.opentcs.common.KernelClientApplication;
import org.opentcs.common.PortalManager;
import org.opentcs.components.kernel.services.ServiceUnavailableException;
import org.opentcs.util.CallWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default {@link CallWrapper} implementation used for calling kernel service methods.
 * <p>
 * If a service method is called using this implementation and the corresponding service is no
 * longer available, this implementation will ask to retry the service method call:
 * <ul>
 * <li>
 * If 'Retry - Yes' is selected, this implementation will handle reestablishment of the kernel
 * connection and (upon successful reestablishment) try to call the service method again.
 * </li>
 * <li>
 * If 'Retry - No' is selected, this implementation will throw the exception thrown by the service
 * mehtod call itself.
 * </li>
 * <li>
 * If 'Cancel' is selected, this implementation will throw the exception thrown by the service
 * mehtod call itself and additionally will notify the application it's no longer online, i.e.
 * connected to the kernel.
 * </li>
 * </ul>
 * </p>
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DefaultServiceCallWrapper
    implements CallWrapper {

  /**
   * This class' logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultServiceCallWrapper.class);
  /**
   * This class' resource bundle.
   */
  private static final ResourceBundle BUNDLE
      = ResourceBundle.getBundle("org/opentcs/kernelcontrolcenter/Bundle");
  /**
   * The application using this utility.
   */
  private final KernelClientApplication application;
  /**
   * The portal manager taking care of the portal connection.
   */
  private final PortalManager portalManager;

  /**
   * Creates a new instance.
   *
   * @param application The application.
   * @param portalManager The portal manager.
   */
  @Inject
  public DefaultServiceCallWrapper(KernelClientApplication application,
                                   PortalManager portalManager) {
    this.application = requireNonNull(application, "application");
    this.portalManager = requireNonNull(portalManager, "portalManager");
  }

  @Override
  public <R> R call(Callable<R> callable)
      throws Exception {
    boolean retry = true;
    Exception failureReason = null;

    while (retry) {
      try {
        return callable.call();
      }
      catch (Exception ex) {
        LOG.warn("Failed to call remote service method: {}", callable, ex);
        failureReason = ex;

        if (ex instanceof ServiceUnavailableException) {
          portalManager.disconnect();
          retry = showRetryDialog();
        }
      }
    }

    // At this point the method call failed and we don't want to try it again anymore.
    // Therefore throw the exception we caught last.
    throw failureReason;
  }

  @Override
  public void call(Runnable runnable)
      throws Exception {
    Callable<Object> callable = Executors.callable(runnable);
    call(callable);
  }

  private boolean showRetryDialog() {
    int dialogSelection
        = JOptionPane.showConfirmDialog(null,
                                        BUNDLE.getString("DefaultServiceUtility.RetryDialog.text"),
                                        BUNDLE.getString("DefaultServiceUtility.RetryDialog.title"),
                                        JOptionPane.YES_NO_CANCEL_OPTION);

    switch (dialogSelection) {
      case JOptionPane.YES_OPTION:
        // Only retry if we connected successfully
        return portalManager.connect(PortalManager.ConnectionMode.RECONNECT);
      case JOptionPane.NO_OPTION:
        return false;
      case JOptionPane.CANCEL_OPTION:
        application.offline();
        return false;
      case JOptionPane.CLOSED_OPTION:
        return false;
      default:
        return false;
    }
  }
}
