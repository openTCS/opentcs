/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.gradle.plugin.integration.util;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

/**
 * Provides some methods to handle files.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PluginFileUtils {

  /**
   * This class' logger.
   */
  private static final Logger LOG = Logging.getLogger(PluginFileUtils.class);

  /**
   * Copies a file from a project directory to the given path.
   *
   * @param project The project to copy the file from.
   * @param relativeFilePath The file path relative to the project directory.
   * @param dest The destination to copy the file to.
   * @throws Exception
   */
  public static void copyFileFromProject(Project project,
                                         String relativeFilePath,
                                         Path dest)
      throws Exception {
    File srcFile = project.getProjectDir().toPath().resolve(relativeFilePath).toFile();
    File destFile = dest.resolve(relativeFilePath).toFile();
    FileUtils.copyFile(srcFile, destFile);

    LOG.quiet("Copied file '{}' to '{}'.", srcFile, destFile);
  }

  /**
   * Copies files from a project directory to the given path.
   *
   * @param project The project to copy the files from.
   * @param relativeFilePaths The file paths relative to the project directory.
   * @param dest The destination to copy the files to.
   * @throws Exception
   */
  public static void copyFilesFromProject(Project project,
                                          List<String> relativeFilePaths,
                                          Path dest)
      throws Exception {
    for (String relativeFilePath : relativeFilePaths) {
      copyFileFromProject(project, relativeFilePath, dest);
    }
  }

  /**
   * Copies a directory from a project directory to the given path.
   *
   * @param project The project to copy the directory from.
   * @param directoryName The name of the directory.
   * @param dest The destination to copy the directory to.
   * @throws Exception
   */
  public static void copyDirectoryFromProject(Project project, String directoryName, Path dest)
      throws Exception {
    // Check if there is such a directory
    File srcDir = project.getProjectDir().toPath().resolve(directoryName).toFile();
    File destDir = dest.resolve(directoryName).toFile();
    FileUtils.copyDirectory(srcDir, destDir);

    LOG.quiet("Copied directory '{}' to '{}'.", srcDir, destDir);
  }

  /**
   * Copies a file from the plugin's resources to the given path.
   *
   * @param resourceFolderName The name of the resources folder to copy a file from.
   * @param relativeFilePath The file path relative to the resources directory.
   * @param dest The destination to copy the file to.
   * @throws Exception
   */
  public static void copyFileFromResources(String resourceFolderName,
                                           String relativeFilePath,
                                           Path dest)
      throws Exception {
    InputStream is = PluginFileUtils.class.getResourceAsStream("/" + resourceFolderName + "/" + relativeFilePath);
    // Create the destination folder in case it doesn't exist
    dest.resolve(relativeFilePath).getParent().toFile().mkdirs();
    Files.copy(is, dest.resolve(relativeFilePath), StandardCopyOption.REPLACE_EXISTING);

    // If we copy a template file delete the original file if one exist
    if (relativeFilePath.endsWith(".tmpl")) {
      Files.deleteIfExists(dest.resolve(relativeFilePath.replace(".tmpl", "")));
    }

    LOG.quiet("Copied resource file to '{}'.", dest.resolve(relativeFilePath));
  }

  /**
   * Copies files from the plugin's resources to the given path.
   *
   * @param resourceFolderName The name of the resoruces folder to copy files from.
   * @param relativeDirPath The directory path relative to the resources folder to copy files from.
   * @param fileNames The files to copy.
   * @param dest The destination to copy the files to.
   * @throws Exception
   */
  public static void copyFilesFromResources(String resourceFolderName,
                                            String relativeDirPath,
                                            List<String> fileNames,
                                            Path dest)
      throws Exception {
    for (String fileName : fileNames) {
      String relativeFilePath = relativeDirPath.equals("")
          ? fileName
          : relativeDirPath + "/" + fileName;
      copyFileFromResources(resourceFolderName, relativeFilePath, dest);
    }
  }
}
