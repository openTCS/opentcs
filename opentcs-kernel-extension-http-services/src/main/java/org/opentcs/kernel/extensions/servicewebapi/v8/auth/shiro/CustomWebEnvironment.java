// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.auth.shiro;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.lang.io.ResourceUtils;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.text.PropertiesRealm;
import org.apache.shiro.web.env.DefaultWebEnvironment;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.opentcs.util.FileSystems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A custom configuration for Shiro's web environment.
 *
 * <p>
 * User and role information are loaded from a properties file named
 * {@code config/webapi-v8-users.properties} located in the kernel's configuration directory.
 * </p>
 */
public class CustomWebEnvironment
    extends
      DefaultWebEnvironment {

  private static final Logger LOG = LoggerFactory.getLogger(CustomWebEnvironment.class);
  private static final String REALM_FILE_NAME = "webapi-v8-users.properties";

  @SuppressWarnings("this-escape")
  public CustomWebEnvironment() {
    DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager(createRealm());

    DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
    sessionManager.setGlobalSessionTimeout(180 * 60 * 1000); // 3h
    sessionManager.setSessionIdCookieEnabled(true);
    securityManager.setSessionManager(sessionManager);

    setSecurityManager(securityManager);
    SecurityUtils.setSecurityManager(securityManager);
  }

  private Realm createRealm() {
    try {
      File realmFile = FileSystems.getApplicationHome().toPath()
          .resolve("config")
          .resolve(REALM_FILE_NAME)
          .normalize()
          .toFile();

      if (!realmFile.isFile()) {
        throw new FileNotFoundException(
            "Realm file not found: '" + realmFile.getCanonicalPath() + "'"
        );
      }

      LOG.info("Loading realm configuration from '{}'...", realmFile.getCanonicalPath());

      PropertiesRealm propsRealm = new PropertiesRealm();
      propsRealm.setResourcePath(ResourceUtils.FILE_PREFIX + realmFile.getCanonicalPath());
      propsRealm.init();

      return propsRealm;
    }
    catch (IOException exc) {
      throw new IllegalStateException("Failed to initialize realm.", exc);
    }
  }
}
