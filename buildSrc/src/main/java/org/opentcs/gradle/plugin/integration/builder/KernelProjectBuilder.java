/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.gradle.plugin.integration.builder;

import java.nio.file.Path;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import static org.opentcs.gradle.plugin.integration.Constants.RESOURCE_FOLDER_KERNEL;
import org.opentcs.gradle.plugin.integration.util.PluginFileUtils;

/**
 * Builds the kernel project.
 * 
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class KernelProjectBuilder
    extends AbstractProjectBuilder {

  /**
   * This class' logger.
   */
  private static final Logger LOG = Logging.getLogger(KernelProjectBuilder.class);
  
  public KernelProjectBuilder(Path outputPath, Project projectBase) {
    super(outputPath, projectBase);
  }

  @Override
  public void build()
      throws Exception {
    PluginFileUtils.copyDirectoryFromProject(getProjectBase(), "src/dist", getOutputPath());
    PluginFileUtils.copyFileFromResources(RESOURCE_FOLDER_KERNEL, "build.gradle.tmpl", getOutputPath());
    PluginFileUtils.copyFileFromResources(RESOURCE_FOLDER_KERNEL,
                                          "src/guiceConfig/resources/META-INF/services/org.opentcs.customizations.kernel.KernelInjectionModule.tmpl",
                                          getOutputPath());
    PluginFileUtils.copyFileFromResources(RESOURCE_FOLDER_KERNEL,
                                          "src/guiceConfig/java/de/fraunhofer/iml/opentcs/FOLDER_PACKAGE_NAME/customization/CommAdaptersModule.java.tmpl",
                                          getOutputPath());
    PluginFileUtils.copyFileFromResources(RESOURCE_FOLDER_KERNEL,
                                          "src/guiceConfig/java/de/fraunhofer/iml/opentcs/FOLDER_PACKAGE_NAME/customization/%JAVA_FILE_PREFIX%KernelInjectionModule.java.tmpl",
                                          getOutputPath());
    LOG.quiet("Kernel project built at '{}'.", getOutputPath());
  }
}
