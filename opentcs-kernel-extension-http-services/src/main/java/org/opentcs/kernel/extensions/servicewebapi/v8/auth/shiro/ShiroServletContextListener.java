// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.auth.shiro;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.env.WebEnvironment;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.opentcs.kernel.extensions.servicewebapi.v8.auth.AuthenticationServletContextListener;

/**
 * Initializes Shiro.
 */
public class ShiroServletContextListener
    extends
      EnvironmentLoaderListener
    implements
      AuthenticationServletContextListener {

  public ShiroServletContextListener() {
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    super.contextInitialized(sce);

    var filter = sce.getServletContext().addFilter("shiroFilter", ShiroFilter.class);
    filter.addMappingForUrlPatterns(null, true, "/*");
    // Since the web API also provides an endpoint for Server-Sent Events, we need to enable
    // support for asynchronous operations (i.e. allow the servlet to store incoming requests for
    // later response / enable the servlet to defer a response and allowing another thread to write
    // to it).
    filter.setAsyncSupported(true);
  }

  @Override
  protected Class<? extends WebEnvironment> getDefaultWebEnvironmentClass(
      ServletContext servletContext
  ) {
    return CustomWebEnvironment.class;
  }
}
