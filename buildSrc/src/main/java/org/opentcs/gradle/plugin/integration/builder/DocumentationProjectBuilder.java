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
import static org.opentcs.gradle.plugin.integration.Constants.RESOURCE_FOLDER_DOCUMENTATION;
import org.opentcs.gradle.plugin.integration.util.PluginFileUtils;

/**
 * Builds the documentation project.
 * 
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DocumentationProjectBuilder
    extends AbstractProjectBuilder {

  /**
   * This class' project.
   */
  private static final Logger LOG = Logging.getLogger(DocumentationProjectBuilder.class);
  
  public DocumentationProjectBuilder(Path outputPath, Project projectBase) {
    super(outputPath, projectBase);
  }

  @Override
  public void build()
      throws Exception {
      PluginFileUtils.copyFilesFromResources(RESOURCE_FOLDER_DOCUMENTATION,
                                             "src/docs/asciidoc/images/icons",
                                             Arrays.asList("caution.svg",
                                                           "important.svg",
                                                           "note.svg",
                                                           "tip.svg",
                                                           "warning.svg"),
                                             getOutputPath());
      PluginFileUtils.copyFileFromResources(RESOURCE_FOLDER_DOCUMENTATION,
                                             "src/docs/asciidoc/opentcs-%DOC_FILE_PLACEHOLDER%-commadapter.adoc.tmpl",
                                             getOutputPath());
      PluginFileUtils.copyFileFromResources(RESOURCE_FOLDER_DOCUMENTATION,
                                             "build.gradle.tmpl",
                                             getOutputPath());
      LOG.quiet("Documentation project built at '{}'.", getOutputPath());
  }
}
