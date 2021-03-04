/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.adminwebapi.v1;

import org.opentcs.util.Environment;

/**
 * Describes the version of the running kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Version {

  private String baselineVersion = Environment.getBaselineVersion();

  private String customizationName = Environment.getCustomizationName();

  private String customizationVersion = Environment.getCustomizationVersion();

  public Version() {
  }

  public String getBaselineVersion() {
    return baselineVersion;
  }

  public void setBaselineVersion(String baselineVersion) {
    this.baselineVersion = baselineVersion;
  }

  public String getCustomizationName() {
    return customizationName;
  }

  public void setCustomizationName(String customizationName) {
    this.customizationName = customizationName;
  }

  public String getCustomizationVersion() {
    return customizationVersion;
  }

  public void setCustomizationVersion(String customizationVersion) {
    this.customizationVersion = customizationVersion;
  }

}
