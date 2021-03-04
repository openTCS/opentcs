/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.documentation;

import static com.google.common.base.Preconditions.checkArgument;
import java.io.File;
import java.io.IOException;
import static java.util.Objects.requireNonNull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

/**
 * Generates XML Schema files for a given list of JAXB classes.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class JAXBSchemaGenerator {

  /**
   * Prevents external instantiation.
   */
  private JAXBSchemaGenerator() {
  }

  /**
   * Generates an XML Schema file for the given list of classes and writes it to
   * the file with the given name.
   *
   * @param args The first argument is expected to be the name of the file to
   * write the schema to. All following arguments are expected to be fully
   * qualified names of classes to include.
   * @throws Exception If an exception occurs.
   */
  public static void main(String[] args)
      throws Exception {
    checkArgument(args.length >= 2,
                  "Expected at least 2 arguments, got %s.",
                  args.length);

    File outputFile = new File(args[0]);

    Class<?>[] classes = new Class<?>[args.length - 1];
    for (int i = 1; i < args.length; i++) {
      classes[i - 1]
          = JAXBSchemaGenerator.class.getClassLoader().loadClass(args[i]);
    }

    JAXBContext jc = JAXBContext.newInstance(classes);
    jc.generateSchema(new FixedSchemaOutputResolver(outputFile));
  }

  /**
   * A schema resolver that always resolves to a fixed file.
   */
  private static class FixedSchemaOutputResolver
      extends SchemaOutputResolver {

    /**
     * The output file.
     */
    private final File outputFile;

    /**
     * Creates a new instance.
     *
     * @param outputFile The output file.
     */
    public FixedSchemaOutputResolver(File outputFile) {
      this.outputFile = requireNonNull(outputFile, "outputFile");
    }

    @Override
    public Result createOutput(String namespaceURI, String suggestedFileName)
        throws IOException {
      StreamResult result = new StreamResult(outputFile.getPath());
      //StreamResult result = new StreamResult(outputFile);
      //result.setSystemId(outputFile.toURI().toURL().toString());
      return result;
    }
  }
}
