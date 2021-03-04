/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.opentcs.util.annotations.ScheduledApiChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class realizes configuration management in XML files.
 * <hr>
 * <p>
 * System properties:
 * </p>
 * <dl>
 * <dt><b>
 * de.fraunhofer.iml.toolbox.configuration.xml.file:
 * </b></dt>
 * <dd>May be set to the name of the configuration file. The default value is
 * {@value #XML_FILE_DEFAULT}.</dd>
 * <dt><b>
 * de.fraunhofer.iml.toolbox.configuration.xml.writeEmpty:
 * </b></dt>
 * <dd>May be set to <code>true</code>/<code>false</code> to turn writing of
 * empty namespaces to the configuration file on/off. The default is
 * <code>false</code>.</dd>
 * </dl>
 * <hr>
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Use interface bindings and configuration mechanism provided via applications'
 * dependency injection.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public class XMLConfiguration
    extends Configuration {

  /**
   * The default properties file name.
   */
  public static final String XML_FILE_DEFAULT = "configuration.xml";
  /**
   * The name of the property for turning saving of empty elements on/off.
   */
  public static final String WRITE_EMPTY_ELEMENTS_PROP =
      "org.opentcs.util.configuration.xml.writeEmpty";
  /**
   * This class's Logger instance.
   */
  private static final Logger log =
      LoggerFactory.getLogger(XMLConfiguration.class);
  /**
   * The name of the property which may contain the name of the desired
   * properties file.
   */
  private static final String xmlFileProperty =
      "org.opentcs.util.configuration.xml.file";
  /**
   * The properties file.
   */
  private static final File xmlFile;
  /**
   * Whether to write empty elements to the XML file or not.
   */
  private static final boolean writeEmptyElements;
  /**
   * The name of the root element in the XML document.
   */
  private static final String ROOT_ELEM = "configuration";
  /**
   * The name of a configuration store in the XML document.
   */
  private static final String STORE_ELEM = "namespace";
  /**
   * The 'name' attribute of a configuration store in the XML document.
   */
  private static final String STORE_NAMESPACE_ATTR = "name";
  /**
   * The name of a configuration item in the XML document.
   */
  private static final String ITEM_ELEM = "item";
  /**
   * The 'name' attribute of a configuration item in the XML document.
   */
  private static final String ITEM_KEY_ATTR = "name";
  /**
   * The 'value' attribute of a configuration item in the XML document.
   */
  private static final String ITEM_VALUE_ATTR = "value";
  /**
   * The 'description' attribute of a configuration item in the XML document.
   */
  private static final String ITEM_DESCR_ATTR = "description";
  /**
   * The 'constraint' of a configuration item in the XML document.
   */
  private static final String CONSTRAINT_ELEM = "constraint";
  /**
   * The 'type' attribute of a configuration item in the XML document.
   */
  private static final String CONSTRAINT_TYPE_ATTR = "type";
  /**
   * The 'Property' of a configuration item constraint in the XML document.
   */
  private static final String CONSTRAINT_PROPERTY = "property";
  /**
   * The 'name' of property attribute item in the XML document.
   */
  private static final String PROPERTY_NAME_ATTR = "name";
  /**
   * The 'value' of a property attribute item in the XML document.
   */
  private static final String PROPERTY_VALUE_ATTR = "value";

  /* Initialize static components. */
  static {
    xmlFile =
        new File(System.getProperty(xmlFileProperty, XML_FILE_DEFAULT));
    writeEmptyElements =
        Boolean.valueOf(System.getProperty(WRITE_EMPTY_ELEMENTS_PROP,
                                           "false"));
  }

  /**
   * Creates a new PropertiesConfiguration.
   */
  public XMLConfiguration() {
    super();
    log.debug("method entry");
    // Try to read the configuration from the file.
    Document document;
    try {
      InputStream inStream = new FileInputStream(xmlFile);
      SAXBuilder builder = new SAXBuilder();
      document = builder.build(inStream);
      inStream.close();
    }
    catch (FileNotFoundException exc) {
      log.info("Configuration file " + xmlFile.getPath()
          + " not found, using empty configuration.");
      document = getEmptyDocument();
    }
    catch (IOException exc) {
      log.info("Unable to read from configuration file " + xmlFile.getPath()
          + ", using empty configuration.");
      document = getEmptyDocument();
    }
    catch (JDOMException exc) {
      log.info("Unable to parse configuration file " + xmlFile.getPath()
          + ", using empty configuration.");
      document = getEmptyDocument();
    }
    Element rootElement = document.getRootElement();
    // Verify that this is a configuration document.
    if (!rootElement.getName().equals(ROOT_ELEM)) {
      log.info(xmlFile.getPath() + " does not contain configuration data, "
          + "using empty configuration.");
      document = getEmptyDocument();
      rootElement = document.getRootElement();
    }

    // Suppress warning about unchecked conversion/cast here. There's nothing we
    // can do about it since JDOM doesn't work with generics.
    @SuppressWarnings("unchecked")
    List<Element> namespaceElements = rootElement.getChildren(STORE_ELEM);

    for (Element curNamespace : namespaceElements) {
      String name = curNamespace.getAttributeValue(STORE_NAMESPACE_ATTR);
      ConfigurationStore configStore = getStore(name);

      // Suppress warning about unchecked conversion/cast here. There's nothing
      // we can do about it since JDOM doesn't work with generics.
      @SuppressWarnings("unchecked")
      List<Element> itemElements = curNamespace.getChildren(ITEM_ELEM);
      for (Element curItem : itemElements) {
        ArrayList<String> constraintValuesSet = new ArrayList<>();
        String key = curItem.getAttributeValue(ITEM_KEY_ATTR);
        String value = curItem.getAttributeValue(ITEM_VALUE_ATTR);
        String description = curItem.getAttributeValue(ITEM_DESCR_ATTR);
        Element constraintElem = curItem.getChild(CONSTRAINT_ELEM);
        String typeString =
            constraintElem.getAttributeValue(CONSTRAINT_TYPE_ATTR);
        List<Element> constraintValues =
            constraintElem.getChildren(CONSTRAINT_PROPERTY);
        for (Element curConstraint : constraintValues) {
          String propertyValue =
              curConstraint.getAttributeValue(PROPERTY_VALUE_ATTR);
          constraintValuesSet.add(propertyValue);
        }
        parseItem(key, typeString, value, description, constraintValuesSet,
                  configStore);
      }
    }
  }

  @Override
  synchronized void persist() {
    log.debug("method entry");
    Document document = getEmptyDocument();
    Element rootElement = document.getRootElement();
    for (ConfigurationStore curStore : getStores()) {
      if (!curStore.isEmpty() || writeEmptyElements) {
        Element namespaceElement = new Element(STORE_ELEM);
        namespaceElement.setAttribute(STORE_NAMESPACE_ATTR,
                                      curStore.getNamespace());
        for (ConfigurationItem configItem
             : curStore.getConfigurationItems().values()) {
          Element itemElement = new Element(ITEM_ELEM);
          itemElement.setAttribute(ITEM_KEY_ATTR, configItem.getKey());
          itemElement.setAttribute(ITEM_VALUE_ATTR, configItem.getValue());
          itemElement.setAttribute(ITEM_DESCR_ATTR,
                                   configItem.getDescription());
          ItemConstraint constraint = configItem.getConstraint();
          Element constraintElement = new Element(CONSTRAINT_ELEM);
          constraintElement.setAttribute(CONSTRAINT_TYPE_ATTR,
                                         constraint.getType().name());
          ConfigurationDataType type = constraint.getType();
          if (type == ConfigurationDataType.ENUM) {
            for (String constraints : constraint.getEnum()) {
              Element constraintProperty = new Element(CONSTRAINT_PROPERTY);
              constraintProperty.setAttribute(PROPERTY_NAME_ATTR, "element");
              constraintProperty.setAttribute(PROPERTY_VALUE_ATTR, constraints);
              constraintElement.addContent(constraintProperty);
            }
          }
          else if (type != ConfigurationDataType.BOOLEAN
              && type != ConfigurationDataType.STRING) {
            String minVal;
            String maxVal;
            if (type == ConfigurationDataType.FLOAT
                || type == ConfigurationDataType.DOUBLE) {
              minVal = constraint.getMinVal() + "";
              maxVal = constraint.getMaxVal() + "";
            }
            else {
              minVal = (int) constraint.getMinVal() + "";
              maxVal = (int) constraint.getMaxVal() + "";
            }
            Element constraintProperty = new Element(CONSTRAINT_PROPERTY);
            constraintProperty.setAttribute(PROPERTY_NAME_ATTR, "minValue");
            constraintProperty.setAttribute(PROPERTY_VALUE_ATTR, minVal);
            constraintElement.addContent(constraintProperty);
            constraintProperty = new Element(CONSTRAINT_PROPERTY);
            constraintProperty.setAttribute(PROPERTY_NAME_ATTR, "maxValue");
            constraintProperty.setAttribute(PROPERTY_VALUE_ATTR, maxVal);
            constraintElement.addContent(constraintProperty);
          }
          itemElement.addContent(constraintElement);
          namespaceElement.addContent(itemElement);
        }
        rootElement.addContent(namespaceElement);
      }
    }
    Format docFormat = Format.getPrettyFormat();

    docFormat.setLineSeparator(System.getProperty("line.separator"));
    XMLOutputter outputter = new XMLOutputter(docFormat);


    try {
      // Write properties to a temporary file first. If successful, rename it
      // to the actual properties file.
      File tempFile =
          File.createTempFile("xmlConfig", ".tmp", xmlFile.getParentFile());
      OutputStream outStream = new FileOutputStream(tempFile);
      outputter.output(document, outStream);
      outStream.close();
      boolean deleted = xmlFile.delete();
      if (!deleted) {
        log.warn("Could not delete original configuration file "
            + xmlFile.getAbsolutePath());
      }
      boolean renamed = tempFile.renameTo(xmlFile);
      if (!renamed) {
        log.warn("Could not rename temporary configuration file "
            + tempFile.getAbsolutePath() + " to " + xmlFile.getAbsolutePath());
      }
    }
    catch (IOException exc) {
      log.warn("IOException writing configuration to " + xmlFile.getPath(), exc);
    }
  }

  /**
   * Creates a plain new JDOM document with only a configuration root element.
   *
   * @return A plain new JDOM document with only a configuration root element.
   */
  private Document getEmptyDocument() {
    Element rootElement = new Element(ROOT_ELEM);
    Document document = new Document(rootElement);
    return document;
  }

  /**
   * Parses the given attributes and creates an item in the given configuration
   * store from them.
   *
   * @param key The key attribute.
   * @param typeString The type attribute.
   * @param value The value attribute.
   * @param description The description attribute.
   * @param constraintValues Properties of constraint element.
   * @param configStore The configuration store to create the new configuration
   * item in.
   */
  @SuppressWarnings("unchecked")
  private void parseItem(String key,
                         String typeString,
                         String value,
                         String description,
                         ArrayList<String> constraintValues,
                         ConfigurationStore configStore) {
    ConfigurationDataType type;
    try {
      if (typeString == null) {
        log.warn("Type for key '" + key + "' is null, defaulting to String");
        type = ConfigurationDataType.STRING;
      }
      else {
        type = ConfigurationDataType.valueOf(typeString);
      }
    }
    catch (IllegalArgumentException exc) {
      log.warn("Could not parse type value '" + typeString
          + "' for key '" + key + "', falling back to String");
      type = ConfigurationDataType.STRING;
    }
    switch (type) {
      case BOOLEAN:
        configStore.setBoolean(key,
                               Boolean.parseBoolean(value),
                               description, new ItemConstraintBoolean());
        break;
      case BYTE:
        configStore.setByte(key, Byte.parseByte(value), description,
                            new ItemConstraintByte(
            Byte.parseByte(constraintValues.get(0)),
            Byte.parseByte(constraintValues.get(1))));
        break;
      case SHORT:

        configStore.setShort(key, Short.parseShort(value), description,
                             new ItemConstraintShort(
            Short.parseShort(constraintValues.get(0)),
            Short.parseShort(constraintValues.get(1))));
        break;
      case INTEGER:
        configStore.setInt(key, Integer.parseInt(value), description,
                           new ItemConstraintInteger(
            Integer.parseInt(constraintValues.get(0)),
            Integer.parseInt(constraintValues.get(1))));
        break;
      case LONG:
        configStore.setLong(key, Long.parseLong(value), description,
                            new ItemConstraintLong(
            Long.parseLong(constraintValues.get(0)),
            Long.parseLong(constraintValues.get(1))));
        break;
      case FLOAT:
        configStore.setFloat(key, Float.parseFloat(value), description,
                             new ItemConstraintFloat(
            Float.parseFloat(constraintValues.get(0)),
            Float.parseFloat(constraintValues.get(1))));
        break;
      case DOUBLE:
        configStore.setDouble(key, Double.parseDouble(value), description,
                              new ItemConstraintDouble(
            Double.parseDouble(constraintValues.get(0)),
            Double.parseDouble(constraintValues.get(1))));
        break;
      case STRING:
        configStore.setString(key, value, description,
                              new ItemConstraintString());
        break;
      case ENUM:
        configStore.setEnum(key, value, description,
                            new HashSet<>(constraintValues));
        break;
      default:
        throw new IllegalArgumentException("Unhandled data type " + type);
    }
  }
}
