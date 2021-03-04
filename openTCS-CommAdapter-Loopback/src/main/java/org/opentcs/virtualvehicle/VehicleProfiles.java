/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.opentcs.util.configuration.ConfigurationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages {@link org.opentcs.virtualvehicle.VehicleProfile VehicleProfiles}. Profiles
 * can be loaded, saved and deleted via static methods. In the background a XML
 * file with the profiles is maintained. The file is written whenever a
 * <code>remove</code>, <code>save</code> or <code>set</code> method is called.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public abstract class VehicleProfiles {

  /**
   * The default properties file name.
   */
  private static final String xmlFileDefault = "virtualvehicle-profiles.xml";
  /**
   * This class's ConfigurationStore.
   */
  private static final ConfigurationStore configStore
      = ConfigurationStore.getStore(VehicleProfiles.class.getName());
  /**
   * This class's Logger.
   */
  private static final Logger log
      = LoggerFactory.getLogger(VehicleProfiles.class);
  /**
   * VehicleProfile's XML configuration file.
   */
  private static final String xmlFile;
  /**
   * The name of the property which may contain the name of the desired
   * properties file.
   */
  private static final String xmlFileProperty
      = "org.opentcs.virtualvehicle.profiles.file";
  /**
   * The maximum allowed size for the xmlFile (in bytes). If the file is larger
   * than this will not be parsed.
   */
  private static final int maxAllowedFileSize;
  /**
   * A map of profile names to profiles.
   */
  private static final Map<String, VehicleProfile> profiles = new TreeMap<>();
  /**
   * Name of the currently selected profile.
   */
  private static String selectedProfile;

  /**
   * Initialize VehicleProfile from the xml configuration file.
   */
  static {
    maxAllowedFileSize = configStore.getInt("maxAllowedFileSize", 100 * 1024);
    xmlFile = System.getProperty(xmlFileProperty, xmlFileDefault);
    //loadProfilesFromFile();
  }

  /**
   * Get a sorted list of the available profile names.
   *
   * @return profile names
   */
  public static List<String> getProfileNames() {
    return new LinkedList<>(profiles.keySet());
  }

  /**
   * Get a copy of the profile with the specified name.
   *
   * @param name Name of the profile
   * @return the loaded profile, {@literal null} if there is no such profile.
   */
  public static VehicleProfile getProfile(String name) {
    VehicleProfile origProfile = profiles.get(Objects.requireNonNull(name));
    if (origProfile == null) {
      return null;
    }
    else {
      return new VehicleProfile(origProfile);
    }
  }

  /**
   * Delete the specified profile. Will have no effect if profile with this name
   * does not exist. If this is the currently selected profile, it is
   * deselected.
   *
   * @param name name of the profile to delete
   */
  public static void remove(String name) {
    if (profiles.containsKey(name)) {
      profiles.remove(name);
      if (selectedProfile != null && selectedProfile.equals(name)) {
        selectedProfile = null;
      }
      saveProfilesToFile();
    }
  }

  /**
   * Remove all profiles. The currently selected profile is deselected.
   */
  public static void removeAll() {
    profiles.clear();
    selectedProfile = null;
    saveProfilesToFile();
  }

  /**
   * Save the specified profile. If there already exists a profile with this
   * name, it will be overwritten. For convenience, the whole list of profiles
   * is updated (i.e. removed and saved again) in the xml file. If
   * {@literal profile} is {@literal null} no new profile is added, but the
   * existing profiles are updated.
   *
   * @param profile the new profile
   */
  public static void saveProfile(VehicleProfile profile) {
    if (profile != null) {
      profiles.put(profile.getName(), new VehicleProfile(profile));
    }
    saveProfilesToFile();
  }

  /**
   * Set the currently selected profile. This just sets a property in the xml
   * configuration which is not used or maintained internally by the
   * VehicleProfiles class. If no profile with the specified name exists, the
   * configuaration remains unchanged.
   *
   * @param name name of the currently selected profile, might be null, to
   * indicate that no profile is selected
   */
  public static void setSelectedProfile(String name) {
    if (getProfileNames().contains(name)) {
      selectedProfile = name;
      saveProfilesToFile();
    }
  }

  /**
   * Get the currently selected profile.
   *
   * @see #setSelectedProfile(String)
   * @return the name of the currently selected profile or null if no profile
   * selected (e.g. when profile list is empty).
   */
  public static String getSelectedProfile() {
    return selectedProfile;
  }

  /**
   * (Re-)initalize the {@literal VehicleProfiles} by loading the profiles from
   * the xml configuration. If the configuration does not contain any profiles,
   * a default profile will be created and selected.
   */
  private static void loadProfilesFromFile() {
    File file = new File(xmlFile);
    boolean loadDefault = false;
    InputStream inStream = null;
    // Clear any values in case VehicleProfiles was initialized before
    profiles.clear();
    selectedProfile = null;
    try {
      // Handle some file related errors
      if (!file.exists()) {
        throw new FileNotFoundException(file.getAbsolutePath() + ": file not found.");
      }
      if (!file.isFile()) {
        throw new IOException(file.getAbsolutePath() + ": not a normal file.");
      }
      if (!file.canRead()) {
        throw new IOException(file.getAbsolutePath() + ": file not readable.");
      }
      int fileSize = (int) file.length();
      if (fileSize > maxAllowedFileSize) {
        StringBuilder message = new StringBuilder();
        message.append(file.getAbsolutePath());
        message.append(": file size exceeds limit.\nIs: ").append(fileSize);
        message.append(", limit: ").append(maxAllowedFileSize).append(" Bytes.");
        throw new IOException(message.toString());
      }
      // Read the configuration from file.
      byte[] buffer = new byte[fileSize];
      inStream = new FileInputStream(file);
      int bytesRead = inStream.read(buffer);
      if (bytesRead != fileSize) {
        StringBuilder message = new StringBuilder();
        message.append("read() returned unexpected value: ").append(bytesRead);
        message.append(", should be :").append(fileSize);
        throw new IOException(message.toString());
      }
      String xmlData = new String(buffer, Charset.forName("UTF-8"));
      StringReader stringReader = new StringReader(xmlData);
      VehicleProfilesXML profileData;
      try {
        JAXBContext jc = JAXBContext.newInstance(VehicleProfilesXML.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        profileData = (VehicleProfilesXML) unmarshaller.unmarshal(stringReader);
      }
      catch (JAXBException e) {
        throw new IOException("Unmarshalling of XML data failed.", e);
      }
      // Save read configuration in members
      if (profileData != null) {
        for (VehicleProfile profile : profileData.getProfiles()) {
          profiles.put(profile.getName(), profile);
        }
        String selected = profileData.getSelectedProfile();
        if (selected != null && profiles.containsKey(selected)) {
          selectedProfile = selected;
        }
      }
      else {
        loadDefault = true;
      }
    }
    // Catch exceptions
    catch (FileNotFoundException e) {
      loadDefault = true;
      StringBuilder message = new StringBuilder();
      message.append("Vehicle profile configuration file does not exist: ");
      message.append(file.getAbsolutePath());
      message.append(". Using default configuration instead.");
      log.info(message.toString());
    }
    catch (IOException e) {
      loadDefault = true;
      StringBuilder message = new StringBuilder();
      message.append("Can't read vehicle profile configuration from file. ");
      message.append(e.getMessage());
      log.warn(message.toString());
    }
    finally {
      if (inStream != null) {
        try {
          inStream.close();
        }
        catch (IOException e) {
          log.warn("Can't close InputStream: " + e.getMessage());
        }
      }
    }
    // Load the default configuration as we weren't able to read it from file
    if (loadDefault) {
      VehicleProfile defaultProfile = new VehicleProfile();
      saveProfile(defaultProfile);
      setSelectedProfile(defaultProfile.getName());
    }
  }

  /**
   * Writes the profile configuration to the XML file.
   */
  private static void saveProfilesToFile() {
    OutputStream outStream = null;
    File file = new File(xmlFile);
    StringWriter stringWriter = new StringWriter();
    // Create VehicleProfilsXML object with current profiles
    VehicleProfilesXML profileData = new VehicleProfilesXML();
    if (selectedProfile != null) {
      profileData.setSelectedProfile(selectedProfile);
    }
    profileData.setProfiles(new LinkedList<>(profiles.values()));
    try {
      // Create XML data   
      try {
        JAXBContext jc = JAXBContext.newInstance(VehicleProfilesXML.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(profileData, stringWriter);
      }
      catch (JAXBException e) {
        throw new IOException("Marshalling of XML data failed." + e.toString());
      }
      // Write to file
      outStream = new FileOutputStream(file);
      outStream.write(stringWriter.toString().getBytes(Charset.forName("UTF-8")));
      outStream.flush();
    }
    catch (IOException e) {
      StringBuilder message = new StringBuilder();
      message.append("Can't write vehicle profile configuration to file. ");
      message.append(e.getMessage());
      log.error(message.toString());
    }
    finally {
      if (outStream != null) {
        try {
          outStream.close();
        }
        catch (IOException e) {
          log.warn("Can't close OutputStream: " + e.getMessage());
        }
      }
    }

  }

  /**
   * JAXB compatible class that wraps a list of profiles and optionally the name
   * of the selected profile.
   */
  @XmlRootElement(name = "VehicleProfiles")
  @XmlAccessorType(XmlAccessType.NONE)
  private static final class VehicleProfilesXML {

    /**
     * List of vehicle profiles.
     */
    @XmlElement(name = "profile", required = false)
    private List<VehicleProfile> profiles = new LinkedList<>();
    /**
     * Name of the selected profile.
     */
    @XmlAttribute(name = "selectedProfile", required = false)
    private String selectedProfile;

    /**
     * Create instance.
     */
    public VehicleProfilesXML() {
      // Do nothing.
    }

    /**
     * Set the profiles.
     *
     * @param profiles List of profiles
     */
    public void setProfiles(List<VehicleProfile> profiles) {
      this.profiles = profiles;
    }

    /**
     * Get the profiles.
     *
     * @return List of profiles.
     */
    public List<VehicleProfile> getProfiles() {
      return profiles;
    }

    /**
     * Set the selected profile.
     *
     * @param name Name of the selected profile. Might be <code>null</code>.
     */
    public void setSelectedProfile(String name) {
      selectedProfile = name;
    }

    /**
     * Get the name of the selected profile.
     *
     * @return profile name
     */
    public String getSelectedProfile() {
      return selectedProfile;
    }
  }
}
