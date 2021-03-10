/*
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.util;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure the naming convention for model elements.
 *
 * @author Leonard Schuengel (Fraunhofer IML)
 */
@ConfigurationPrefix(ElementNamingSchemeConfiguration.PREFIX)
public interface ElementNamingSchemeConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "elementnamingscheme";

  @ConfigurationEntry(
      type = "String",
      description = "The default prefix for a new point element.",
      orderKey = "0_point_0")
  String pointPrefix();

  @ConfigurationEntry(
      type = "String",
      description = "The numbering pattern for a new point element.",
      orderKey = "0_point_1")
  String pointNumberPattern();

  @ConfigurationEntry(
      type = "String",
      description = "The default prefix for a new path element.",
      orderKey = "1_path_0")
  String pathPrefix();

  @ConfigurationEntry(
      type = "String",
      description = "The numbering pattern for a new path element.",
      orderKey = "1_path_1")
  String pathNumberPattern();

  @ConfigurationEntry(
      type = "String",
      description = "The default prefix for a new location type element.",
      orderKey = "2_loctype_0")
  String locationTypePrefix();

  @ConfigurationEntry(
      type = "String",
      description = "The numbering pattern for a new location type element.",
      orderKey = "2_loctype_1")
  String locationTypeNumberPattern();

  @ConfigurationEntry(
      type = "String",
      description = "The default prefix for a new location element.",
      orderKey = "3_loc_0")
  String locationPrefix();

  @ConfigurationEntry(
      type = "String",
      description = "The numbering pattern for a new location element.",
      orderKey = "3_loc_1")
  String locationNumberPattern();

  @ConfigurationEntry(
      type = "String",
      description = "The default prefix for a new link element.",
      orderKey = "4_link_0")
  String linkPrefix();

  @ConfigurationEntry(
      type = "String",
      description = "The numbering pattern for a new link element.",
      orderKey = "4_link_1")
  String linkNumberPattern();

  @ConfigurationEntry(
      type = "String",
      description = "The default prefix for a new block.",
      orderKey = "5_block_0")
  String blockPrefix();

  @ConfigurationEntry(
      type = "String",
      description = "The numbering pattern for a new block.",
      orderKey = "5_block_1")
  String blockNumberPattern();

  @ConfigurationEntry(
      type = "String",
      description = "The default prefix for a new group.",
      orderKey = "6_group_0")
  String groupPrefix();

  @ConfigurationEntry(
      type = "String",
      description = "The numbering pattern for a new group.",
      orderKey = "6_group_1")
  String groupNumberPattern();

  @ConfigurationEntry(
      type = "String",
      description = "The default prefix for a new layout element.",
      orderKey = "7_layout_0")
  String layoutPrefix();

  @ConfigurationEntry(
      type = "String",
      description = "The numbering pattern for a new layout element.",
      orderKey = "7_layout_1")
  String layoutNumberPattern();

  @ConfigurationEntry(
      type = "String",
      description = "The default prefix for a new vehicle.",
      orderKey = "8_vehicle_0")
  String vehiclePrefix();

  @ConfigurationEntry(
      type = "String",
      description = "The numbering pattern for a new vehicle.",
      orderKey = "8_vehicle_1")
  String vehicleNumberPattern();

}
