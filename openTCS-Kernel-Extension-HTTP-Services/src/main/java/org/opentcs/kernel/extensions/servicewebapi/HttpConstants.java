/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi;

/**
 * Defines some HTTP-related constants.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface HttpConstants {

  /**
   * Name of the header that is expected to contain the API access keys.
   */
  String HEADER_NAME_ACCESS_KEY = "X-Api-Access-Key";
  /**
   * Content type for plain text.
   */
  String CONTENT_TYPE_TEXT_PLAIN_UTF8 = "text/plain; charset=utf-8";
  /**
   * Content type for JSON structures.
   */
  String CONTENT_TYPE_APPLICATION_JSON_UTF8 = "application/json; charset=utf-8";
}
