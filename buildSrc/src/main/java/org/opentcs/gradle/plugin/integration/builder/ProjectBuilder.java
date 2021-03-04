/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.gradle.plugin.integration.builder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.opentcs.gradle.plugin.integration.Constants;
import static org.opentcs.gradle.plugin.integration.Constants.PLACEHOLDER_FOLDER;
import static org.opentcs.gradle.plugin.integration.Constants.PLACEHOLDER_PROJECT_NAME;
import static org.opentcs.gradle.plugin.integration.Constants.PLACEHOLDER_GROUP_NAME;
import static org.opentcs.gradle.plugin.integration.Constants.PLACEHOLDER_JAVA_FILE_PREFIX;
import static org.opentcs.gradle.plugin.integration.Constants.PLACEHOLDER_PACKAGE_NAME;
import static org.opentcs.gradle.plugin.integration.Constants.PLACEHOLDER_OPENTCS_BASELINE_VERSION;
import static org.opentcs.gradle.plugin.integration.Constants.SUBPROJECT_KERNEL;
import static org.opentcs.gradle.plugin.integration.Constants.SUBPROJECT_PLANTOVERVIEW;
import static org.opentcs.gradle.plugin.integration.Constants.PLACEHOLDER_DOC_FILE_NAME;
import static org.opentcs.gradle.plugin.integration.Constants.SUBPROJECT_KERNELCONTROLCENTER;
import org.opentcs.gradle.plugin.integration.util.ProjectPropertyReader;

/**
 * Builds the whole integration project.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class ProjectBuilder
    extends AbstractProjectBuilder {

  /**
   * This class' logger.
   */
  private static final Logger LOG = Logging.getLogger(ProjectBuilder.class);
  /**
   * The name of this project. (Actually not the full name, just the project-specific part.)
   */
  private final String projectName;
  /**
   * All subproject mapped to their names.
   */
  private final Map<String, Project> subProjects = new HashMap<>();

  /**
   * Creates a new instance.
   *
   * @param projectName The name of this project. (Actually not the full name, just the
   * project-specific part.)
   * @param outputPath The path where to output the created project.
   * @param projectBase The project the created project is based on.
   */
  public ProjectBuilder(String projectName, Path outputPath, Project projectBase) {
    super(outputPath, projectBase);

    this.projectName = projectName;
    projectBase.getSubprojects().stream()
        .forEach(subProject -> subProjects.put(subProject.getName(), subProject));
  }

  @Override
  public void build() {
    try {
      new RootProjectBuilder(getOutputPath(), getProjectBase()).build();
      new KernelProjectBuilder(getKernelProjectPath(), subProjects.get(SUBPROJECT_KERNEL)).build();
      new KernelControlCenterProjectBuilder(getKernelControlCenterProjectPath(),
                                            subProjects.get(SUBPROJECT_KERNELCONTROLCENTER))
          .build();
      new PlantOverviewProjectBuilder(getPlantOverviewProjectPath(),
                                      subProjects.get(SUBPROJECT_PLANTOVERVIEW))
          .build();
      new CommonProjectBuilder(getCommonProjectPath(), null).build();
      new VehicleCommAdapterProjectBuilder(getVehicleCommAdapterProjectPath(), null).build();
      new DocumentationProjectBuilder(getDocumentationProjectPath(), null).build();

      refactorFiles();
      LOG.quiet("Integration project built at '{}'.", getOutputPath());
    }
    catch (Exception ex) {
      LOG.error("Error during building process.", ex);
    }
  }

  private void refactorFiles()
      throws IOException {
    String projectNameSubstitute = projectName;
    String packageNameSubstitute = projectName.toLowerCase().replaceAll("-", "_");
    String javaFileNameSubstitute = getJavaFilePrefix();

    // Collect all directories that need to be renamed
    Set<String> directoriesToRename
        = FileUtils.listFiles(getOutputPath().toFile(), new String[] {"tmpl"}, true).stream()
            .map(file -> file.getAbsolutePath())
            .filter(path -> path.contains(PLACEHOLDER_FOLDER))
            .map(path -> path.split(PLACEHOLDER_FOLDER)[0] + PLACEHOLDER_FOLDER)
            .collect(Collectors.toSet());

    // Rename all directories that need to be renamed
    for (String directory : directoriesToRename) {
      File srcDir = new File(directory);
      File destDir = new File(directory.replace(PLACEHOLDER_FOLDER, packageNameSubstitute));
      FileUtils.moveDirectory(srcDir, destDir);
      LOG.quiet("Renamed directory {} to {}.", srcDir, destDir);
    }

    // Collect all files that need to be renamed
    Collection<File> filesToRename
        = FileUtils.listFiles(getOutputPath().toFile(), new String[] {"tmpl"}, true);

    // Rename all files that need to be renamed
    Charset charset = StandardCharsets.UTF_8;
    for (File file : filesToRename) {
      // Substitute placeholders in file content
      String content = new String(FileUtils.readFileToByteArray(file), charset)
          .replaceAll(PLACEHOLDER_PROJECT_NAME, projectNameSubstitute)
          .replaceAll(PLACEHOLDER_GROUP_NAME, packageNameSubstitute)
          .replaceAll(PLACEHOLDER_PACKAGE_NAME, packageNameSubstitute)
          .replaceAll(PLACEHOLDER_DOC_FILE_NAME, projectName.toLowerCase())
          .replaceAll(PLACEHOLDER_JAVA_FILE_PREFIX, javaFileNameSubstitute)
          .replaceAll(PLACEHOLDER_OPENTCS_BASELINE_VERSION, getBaselineVersion());
      FileUtils.writeByteArrayToFile(file, content.getBytes(charset));

      // Rename file / remove .tmpl extension
      String newFileName = file.getName()
          .replace(PLACEHOLDER_DOC_FILE_NAME, projectName.toLowerCase())
          .replace(PLACEHOLDER_JAVA_FILE_PREFIX, javaFileNameSubstitute)
          .replace(".tmpl", "");
      Path newPath = Files.move(file.toPath(), file.toPath().resolveSibling(newFileName));

      LOG.quiet("Renamed file {} to {}.", file, newPath);
    }
  }

  private String getBaselineVersion() {
    return ProjectPropertyReader.getProjectProperty(getProjectBase(),
                                                    Constants.PROPKEY_BASELINEVERSION,
                                                    getProjectBase().getVersion().toString());
  }

  private String getJavaFilePrefix() {
    return ProjectPropertyReader.getProjectProperty(getProjectBase(),
                                                    Constants.PROPKEY_JAVA_FILE_PREFIX,
                                                    Constants.PROPVAL_JAVA_FILE_PREFIX_DEFAULT);
  }

  private Path getKernelProjectPath() {
    return getOutputPath().resolve(getProjectNameFor(Constants.PROJECT_SUFFIX_KERNEL));
  }

  private Path getKernelControlCenterProjectPath() {
    return getOutputPath().resolve(getProjectNameFor(Constants.PROJECT_SUFFIX_KERNELCONTROLCENTER));
  }

  private Path getPlantOverviewProjectPath() {
    return getOutputPath().resolve(getProjectNameFor(Constants.PROJECT_SUFFIX_PLANTOVERVIEW));
  }

  private Path getCommonProjectPath() {
    return getOutputPath().resolve(getProjectNameFor(Constants.PROJECT_SUFFIX_COMMON));
  }

  private Path getVehicleCommAdapterProjectPath() {
    return getOutputPath().resolve(getProjectNameFor(Constants.PROJECT_SUFFIX_VEHICLECOMMADAPTER));
  }

  private Path getDocumentationProjectPath() {
    return getOutputPath().resolve(getProjectNameFor(Constants.PROJECT_SUFFIX_DOCUMENTATION));
  }

  private String getProjectNameFor(String projectSuffix) {
    return Constants.PROJECT_PREFIX + projectName + projectSuffix;
  }
}
