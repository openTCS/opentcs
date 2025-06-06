// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.persistence;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.Reader;
import org.xml.sax.SAXException;

/**
 * Allows reading a model file to access basic information (such as the model version) for
 * validation purposes.
 */
@XmlRootElement(name = "model")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ProbePlantModelTO
    extends
      BasePlantModelTO {

  /**
   * Creates a new instance.
   */
  public ProbePlantModelTO() {
  }

  /**
   * Unmarshals an instance of this class from the given XML representation.
   *
   * @param reader Provides the XML representation to parse to an instance.
   * @return The instance unmarshalled from the given reader.
   * @throws IOException If there was a problem unmarshalling the given string.
   */
  public static ProbePlantModelTO fromXml(
      @Nonnull
      Reader reader
  )
      throws IOException {
    requireNonNull(reader, "reader");

    try {
      return (ProbePlantModelTO) createUnmarshaller().unmarshal(reader);
    }
    catch (JAXBException | SAXException exc) {
      throw new IOException("Exception unmarshalling data", exc);
    }
  }

  private static Unmarshaller createUnmarshaller()
      throws JAXBException,
        SAXException {
    return createContext().createUnmarshaller();
  }

  private static JAXBContext createContext()
      throws JAXBException {
    return JAXBContext.newInstance(ProbePlantModelTO.class);
  }
}
