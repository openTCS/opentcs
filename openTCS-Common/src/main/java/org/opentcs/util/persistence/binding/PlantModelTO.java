/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence.binding;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@XmlRootElement(name = "model")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {"version", "name", "points", "paths", "vehicles", "locationTypes",
                      "locations", "blocks", "staticRoutes", "groups", "visualLayouts",
                      "properties"})
public class PlantModelTO
    extends PlantModelElementTO {

  private String version = "";
  private List<PointTO> points = new ArrayList<>();
  private List<PathTO> paths = new ArrayList<>();
  private List<VehicleTO> vehicles = new ArrayList<>();
  private List<LocationTypeTO> locationTypes = new ArrayList<>();
  private List<LocationTO> locations = new ArrayList<>();
  private List<BlockTO> blocks = new ArrayList<>();
  private List<StaticRouteTO> staticRoutes = new ArrayList<>();
  private List<GroupTO> groups = new ArrayList<>();
  private List<VisualLayoutTO> visualLayouts = new ArrayList<>();

  @XmlAttribute(required = true)
  public String getVersion() {
    return version;
  }

  public PlantModelTO setVersion(@Nonnull String version) {
    requireNonNull(version, "version");
    this.version = version;
    return this;
  }

  @XmlElement(name = "point")
  public List<PointTO> getPoints() {
    return points;
  }

  public PlantModelTO setPoints(@Nonnull List<PointTO> points) {
    requireNonNull(points, "points");
    this.points = points;
    return this;
  }

  @XmlElement(name = "path")
  public List<PathTO> getPaths() {
    return paths;
  }

  public PlantModelTO setPaths(@Nonnull List<PathTO> paths) {
    requireNonNull(paths, "paths");
    this.paths = paths;
    return this;
  }

  @XmlElement(name = "vehicle")
  public List<VehicleTO> getVehicles() {
    return vehicles;
  }

  public PlantModelTO setVehicles(@Nonnull List<VehicleTO> vehicles) {
    requireNonNull(vehicles, "vehicles");
    this.vehicles = vehicles;
    return this;
  }

  @XmlElement(name = "locationType")
  public List<LocationTypeTO> getLocationTypes() {
    return locationTypes;
  }

  public PlantModelTO setLocationTypes(@Nonnull List<LocationTypeTO> locationTypes) {
    requireNonNull(locationTypes, "locationTypes");
    this.locationTypes = locationTypes;
    return this;
  }

  @XmlElement(name = "location")
  public List<LocationTO> getLocations() {
    return locations;
  }

  public PlantModelTO setLocations(@Nonnull List<LocationTO> locations) {
    requireNonNull(locations, "locations");
    this.locations = locations;
    return this;
  }

  @XmlElement(name = "block")
  public List<BlockTO> getBlocks() {
    return blocks;
  }

  public PlantModelTO setBlocks(@Nonnull List<BlockTO> blocks) {
    requireNonNull(blocks, "blocks");
    this.blocks = blocks;
    return this;
  }

  @XmlElement(name = "staticRoute")
  public List<StaticRouteTO> getStaticRoutes() {
    return staticRoutes;
  }

  public PlantModelTO setStaticRoutes(@Nonnull List<StaticRouteTO> staticRoutes) {
    requireNonNull(staticRoutes, "staticRoutes");
    this.staticRoutes = staticRoutes;
    return this;
  }

  @XmlElement(name = "group")
  public List<GroupTO> getGroups() {
    return groups;
  }

  public PlantModelTO setGroups(@Nonnull List<GroupTO> groups) {
    requireNonNull(groups, "groups");
    this.groups = groups;
    return this;
  }

  @XmlElement(name = "visualLayout")
  public List<VisualLayoutTO> getVisualLayouts() {
    return visualLayouts;
  }

  public PlantModelTO setVisualLayouts(@Nonnull List<VisualLayoutTO> visualLayouts) {
    requireNonNull(visualLayouts, "visualLayouts");
    this.visualLayouts = visualLayouts;
    return this;
  }

  /**
   * Marshals this instance to its XML representation and writes it to the given writer.
   *
   * @param writer The writer to write this instance's XML representation to.
   * @throws IOException If there was a problem marshalling this instance.
   */
  public void toXml(@Nonnull Writer writer)
      throws IOException {
    requireNonNull(writer, "writer");

    try {
      createMarshaller().marshal(this, writer);
    }
    catch (JAXBException | SAXException exc) {
      throw new IOException("Exception marshalling data", exc);
    }
  }

  /**
   * Unmarshals an instance of this class from the given XML representation.
   *
   * @param reader Provides the XML representation to parse to an instance.
   * @return The instance unmarshalled from the given reader.
   * @throws IOException If there was a problem unmarshalling the given string.
   */
  public static PlantModelTO fromXml(@Nonnull Reader reader)
      throws IOException {
    requireNonNull(reader, "reader");

    try {
      return (PlantModelTO) createUnmarshaller().unmarshal(reader);
    }
    catch (JAXBException | SAXException exc) {
      throw new IOException("Exception unmarshalling data", exc);
    }
  }

  private static Marshaller createMarshaller()
      throws JAXBException, SAXException {
    Marshaller marshaller = createContext().createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    marshaller.setSchema(createSchema());
    return marshaller;
  }

  private static Unmarshaller createUnmarshaller()
      throws JAXBException, SAXException {
    Unmarshaller unmarshaller = createContext().createUnmarshaller();
    unmarshaller.setSchema(createSchema());
    return unmarshaller;
  }

  private static JAXBContext createContext()
      throws JAXBException {
    return JAXBContext.newInstance(PlantModelTO.class);
  }

  private static Schema createSchema()
      throws SAXException {
    URL schemaUrl = PlantModelTO.class.getResource("/org/opentcs/util/persistence/model-0.0.2.xsd");
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    return schemaFactory.newSchema(schemaUrl);
  }
}
