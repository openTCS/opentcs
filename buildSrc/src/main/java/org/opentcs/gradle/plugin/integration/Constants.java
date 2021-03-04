/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.gradle.plugin.integration;

/**
 * Defines some constants for this plugin.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface Constants {

  /**
   * The property key for defining the project name.
   */
  String PROPKEY_PROJECTNAME = "customizationName";
  /**
   * The property key for defining the baseline version.
   */
  String PROPKEY_BASELINEVERSION = "baselineVersion";
  /**
   * The property key for defining the java file prefix.
   */
  String PROPKEY_JAVA_FILE_PREFIX = "javaFilePrefix";

  /**
   * The default property value for the project name.
   */
  String PROPVAL_PROJECTNAME_DEFAULT = "MyProject";

  /**
   * The default property value for the java file prefix.
   */
  String PROPVAL_JAVA_FILE_PREFIX_DEFAULT = "Custom";

  /**
   * A text placeholder for the project name.
   */
  String PLACEHOLDER_PROJECT_NAME = "%PROJECT_NAME%";
  /**
   * A text placeholder for the group name.
   */
  String PLACEHOLDER_GROUP_NAME = "%GROUP_NAME%";
  /**
   * A text placeholder for the package name.
   */
  String PLACEHOLDER_PACKAGE_NAME = "%PACKAGE_NAME%";
  /**
   * A text placeholder for the openTCS baseline version.
   */
  String PLACEHOLDER_OPENTCS_BASELINE_VERSION = "%OPENTCS_BASELINE_VERSION%";
  /**
   * A file name placeholder for the asciidoc file.
   */
  String PLACEHOLDER_DOC_FILE_NAME = "%DOC_FILE_PLACEHOLDER%";
  /**
   * A file name placeholder for the prefix of java files.
   */
  String PLACEHOLDER_JAVA_FILE_PREFIX = "%JAVA_FILE_PREFIX%";
  /**
   * A directory name placeholder for the package name.
   */
  String PLACEHOLDER_FOLDER = "FOLDER_PACKAGE_NAME";

  /**
   * The name of the resources folder containing files for the root project.
   */
  String RESOURCE_FOLDER_ROOT = "openTCS";
  /**
   * The name of the resources folder containing files for the kernel project.
   */
  String RESOURCE_FOLDER_KERNEL = "openTCS-Kernel";
  /**
   * The name of the resources folder containing files for the plant overview project.
   */
  String RESOURCE_FOLDER_PLANTOVERVIEW = "openTCS-PlantOverview";
  /**
   * The name of the resources folder containing files for the kernel control center project.
   */
  String RESOURCE_FOLDER_KERNELCONTROLCENTER = "openTCS-KernelControlCenter";
  /**
   * The name of the resources folder containing files for the common project.
   */
  String RESOURCE_FOLDER_COMMON = "openTCS-Common";
  /**
   * The name of the resources folder containing files for the vehicle comm adapter project.
   */
  String RESOURCE_FOLDER_COMM_ADAPTER_VEHICLE = "openTCS-CommAdapter-Vehicle";
  /**
   * The name of the resources folder containing files for the documentation project.
   */
  String RESOURCE_FOLDER_DOCUMENTATION = "openTCS-Documentation";

  /**
   * The name of the kernel subproject in the openTCS baseline project.
   */
  String SUBPROJECT_KERNEL = "openTCS-Kernel";
  /**
   * The name of the plantoverview subproject in the openTCS baseline project.
   */
  String SUBPROJECT_PLANTOVERVIEW = "openTCS-PlantOverview";
  /**
   * The name of the kernel control center subproject in the openTCS baseline project.
   */
  String SUBPROJECT_KERNELCONTROLCENTER = "openTCS-KernelControlCenter";

  /**
   * The prefix for all generated projects.
   */
  String PROJECT_PREFIX = "openTCS-";
  /**
   * The additional prefix for the generated root project.
   */
  String PROJECT_PREFIX_INTEGRATION = "Integration-";
  /**
   * The additional prefix for the generated java files marked with
   * {@link #PLACEHOLDER_JAVA_FILE_PREFIX}.
   */
  String JAVA_FILE_PREFIX = "Custom";
  /**
   * The suffix for the generated kernel proejct.
   */
  String PROJECT_SUFFIX_KERNEL = "-Kernel";
  /**
   * The suffix for the generated plant overview proejct.
   */
  String PROJECT_SUFFIX_PLANTOVERVIEW = "-PlantOverview";
  /**
   * The suffix for the generated kernel control center proejct.
   */
  String PROJECT_SUFFIX_KERNELCONTROLCENTER = "-KernelControlCenter";
  /**
   * The suffix for the generated common proejct.
   */
  String PROJECT_SUFFIX_COMMON = "-Common";
  /**
   * The suffix for the generated vehicle comm adapter proejct.
   */
  String PROJECT_SUFFIX_VEHICLECOMMADAPTER = "-CommAdapter-Vehicle";
  /**
   * The suffix for the generated documentation proejct.
   */
  String PROJECT_SUFFIX_DOCUMENTATION = "-Documentation";
}
