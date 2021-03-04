/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.rmi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of <code>UserAccountPersister</code> that stores user
 * account data in an XML file.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class XMLFileUserAccountPersister
    implements UserAccountPersister {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(XMLFileUserAccountPersister.class);
  /**
   * The name of the file containing the account data.
   */
  private static final String ACCOUNT_FILE_NAME = "accounts.xml";
  /**
   * The URL of the schema for XML model validataion.
   */
  private static final URL SCHEMA_URL = XMLFileUserAccountPersister.class.getResource(
      "/org/opentcs/util/persistence/useraccounts.xsd");
  /**
   * The directory in which the account file resides.
   */
  private final File dataDirectory;

  /**
   * Creates a new XMLFileUserAccountPersister.
   *
   * @param directory The directory
   */
  public XMLFileUserAccountPersister(File directory) {
    this.dataDirectory = requireNonNull(directory, "directory");
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<UserAccount> loadUserAccounts()
      throws IOException {
    Set<UserAccount> result = new HashSet<>();
    File accountFile = new File(dataDirectory, ACCOUNT_FILE_NAME);
    if (!accountFile.exists()) {
      LOG.info("Account data file does not exist, no user accounts available.");
      return result;
    }
    Document document;
    try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(accountFile),
                                                                  Charset.forName("UTF-8")))) {
      // Create a document builder that validates the XML input using our schema
      SAXBuilder builder = new SAXBuilder();
      builder.setFeature("http://xml.org/sax/features/validation", true);
      builder.setFeature(
          "http://apache.org/xml/features/validation/schema", true);
      builder.setFeature(
          "http://apache.org/xml/features/validation/schema-full-checking",
          true);
      builder.setProperty(
          "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
          SCHEMA_URL.toString());
      document = builder.build(reader);
    }
    catch (JDOMException exc) {
      throw new IOException("Exception parsing input", exc);
    }
    Element rootElement = document.getRootElement();
    if (!rootElement.getName().equals("userAccounts")) {
      throw new IllegalArgumentException("File does not contain account data");
    }
    List<Element> accountElements = rootElement.getChildren("account");
    for (Element accountElement : accountElements) {
      String userName = accountElement.getAttributeValue("name");
      String userPass = accountElement.getAttributeValue("password");
      List<Element> permElements = accountElement.getChildren("permission");
      Set<UserPermission> permissions = EnumSet.noneOf(UserPermission.class);
      for (Element permElement : permElements) {
        String perm = permElement.getAttributeValue("name");
        try {
          permissions.add(UserPermission.valueOf(perm));
        }
        catch (IllegalArgumentException exc) {
          LOG.warn("Unknown permission '" + perm + "' ignored.");
        }
      }
      result.add(new UserAccount(userName, userPass, permissions));
    }
    return result;
  }

  @Override
  public void saveUserAccounts(Set<UserAccount> accounts)
      throws IOException {
    requireNonNull(accounts, "accounts");

    // Sort the accounts alphabetically.
    SortedSet<UserAccount> sortedAccounts
        = new TreeSet<>((a1, a2) -> a1.getUserName().compareTo(a2.getUserName()));
    sortedAccounts.addAll(accounts);
    // Open the output file.
    if (!dataDirectory.isDirectory() && !dataDirectory.mkdirs()) {
      throw new IOException("Unable to create directory " + dataDirectory.getPath());
    }
    File file = new File(dataDirectory, ACCOUNT_FILE_NAME);
    try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),
                                                                   Charset.forName("UTF-8")))) {
      Element rootElement = new Element("userAccounts");
      // Add the account data.
      for (UserAccount curAccount : sortedAccounts) {
        Element accountElement = new Element("account");
        accountElement.setAttribute("name", curAccount.getUserName());
        accountElement.setAttribute("password", curAccount.getPassword());
        for (UserPermission curPermission : curAccount.getPermissions()) {
          Element permElement = new Element("permission");
          permElement.setAttribute("name", curPermission.name());
          accountElement.addContent(permElement);
        }
        rootElement.addContent(accountElement);
      }
      // Create the document and write it to the file.
      Format docFormat = Format.getPrettyFormat();
      docFormat.setLineSeparator(System.getProperty("line.separator"));
      XMLOutputter outputter = new XMLOutputter(docFormat);
      outputter.output(new Document(rootElement), writer);
    }
  }
}
