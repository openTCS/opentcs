/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.gradle.plugin.integration.builder;

import java.nio.file.Path;
import java.util.Arrays;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import static org.opentcs.gradle.plugin.integration.Constants.RESOURCE_FOLDER_ROOT;
import org.opentcs.gradle.plugin.integration.util.PluginFileUtils;

/**
 * Builds the root project.
 * 
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class RootProjectBuilder
    extends AbstractProjectBuilder {

  /**
   * This class' logger.
   */
  private static final Logger LOG = Logging.getLogger(RootProjectBuilder.class);

  public RootProjectBuilder(Path outputPath, Project projectBase) {
    super(outputPath, projectBase);
  }

  @Override
  public void build()
      throws Exception {
    PluginFileUtils.copyFilesFromProject(getProjectBase(),
                                         Arrays.asList(".nb-gradle-properties",
                                                       "gradlew",
                                                       "gradlew.bat"),
                                         getOutputPath());
    PluginFileUtils.copyFilesFromResources(RESOURCE_FOLDER_ROOT,
                                           "",
                                           Arrays.asList(".gitignore.tmpl",
                                                         "build.gradle.tmpl",
                                                         "settings.gradle.tmpl"),
                                           getOutputPath());
    PluginFileUtils.copyDirectoryFromProject(getProjectBase(), "gradle", getOutputPath());
    PluginFileUtils.copyFilesFromResources(RESOURCE_FOLDER_ROOT,
                                           "gradle",
                                           Arrays.asList("common.gradle.tmpl",
                                                         "opentcs-baseline.gradle.tmpl",
                                                         "versioning.gradle.tmpl"),
                                           getOutputPath());
    PluginFileUtils.copyDirectoryFromProject(getProjectBase(), "config/checkstyle", getOutputPath());
    PluginFileUtils.copyFileFromResources(RESOURCE_FOLDER_ROOT, "config/LICENSE", getOutputPath());
    LOG.quiet("Root project built at '{}'.", getOutputPath());
  }
}
