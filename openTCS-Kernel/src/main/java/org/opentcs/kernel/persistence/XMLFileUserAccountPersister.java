/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.opentcs.data.user.UserAccount;
import org.opentcs.data.user.UserPermission;
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
  private static final Logger log = LoggerFactory.getLogger(XMLFileUserAccountPersister.class);
  /**
   * The name of the file containing the account data.
   */
  private static final String accountFileName = "accounts.xml";
  /**
   * The URL of the schema for XML model validataion.
   */
  private static final URL schemaUrl = XMLFileUserAccountPersister.class.getResource(
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
    if (directory == null) {
      throw new NullPointerException("directory is null");
    }
    if (!directory.isDirectory()) {
      throw new IllegalArgumentException(
          "directory[" + directory.getPath() + "] is not a directory");
    }
    dataDirectory = directory;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<UserAccount> loadUserAccounts()
      throws IOException {
    Set<UserAccount> result = new HashSet<>();
    File accountFile = new File(dataDirectory, accountFileName);
    if (!accountFile.exists()) {
      log.info("Account data file does not exist, no user accounts available.");
      return result;
    }
    InputStream inStream = new FileInputStream(accountFile);
    Document document;
    try {
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
          schemaUrl.toString());
      document = builder.build(inStream);
    }
    catch (JDOMException exc) {
      throw new IOException("Exception parsing input: " + exc.getMessage());
    }
    inStream.close();
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
          log.warn("Unknown permission '" + perm + "' ignored.");
        }
      }
      result.add(new UserAccount(userName, userPass, permissions));
    }
    return result;
  }

  @Override
  public void saveUserAccounts(Set<UserAccount> accounts)
      throws IOException {
    if (accounts == null) {
      throw new NullPointerException("accounts is null");
    }
    // Sort the accounts alphabetically.
    SortedSet<UserAccount> sortedAccounts
        = new TreeSet<>((a1, a2) -> a1.getUserName().compareTo(a2.getUserName()));
    sortedAccounts.addAll(accounts);
    // Open the output file.
    File accountFile = new File(dataDirectory, accountFileName);
    OutputStream outStream = new FileOutputStream(accountFile);
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
    Document document = new Document(rootElement);
    Format docFormat = Format.getPrettyFormat();
    docFormat.setLineSeparator(System.getProperty("line.separator"));
    XMLOutputter outputter = new XMLOutputter(docFormat);
    outputter.output(document, outStream);
    // Clean up.
    outStream.close();
  }
}
