/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import com.google.common.primitives.Ints;
import javax.annotation.Nullable;

/**
 * This class provides methods for parsing.
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class Parsers {

  /**
   * Parses a String to an int. If <code>toParse</code> could not be parsed successfully then the
   * default value <code>retOnFail</code> is returned.
   *
   * @param toParse the <code>String</code> to be parsed.
   * @param retOnFail default value that is returned if the <code>String</code> could not be parsed.
   * @return if the <code>String</code> could be parsed then the int value of the
   * <code>String</code> is returned, else retOnFail
   */
  public static int tryParseString(@Nullable String toParse, int retOnFail) {

    if (toParse == null) {
      return retOnFail;
    }
    Integer parseTry = Ints.tryParse(toParse);
    if (parseTry == null) {
      return retOnFail;
    }
    return parseTry;

  }

}
