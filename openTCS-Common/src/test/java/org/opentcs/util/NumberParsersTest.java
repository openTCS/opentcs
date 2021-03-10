/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import java.util.stream.LongStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class NumberParsersTest {

  @ParameterizedTest
  @MethodSource("paramsFactory")
  public void parsesNumbers(long number) {
    assertEquals(number, NumberParsers.parsePureDecimalLong(Long.toString(number)));
  }

  public static LongStream paramsFactory() {
    return LongStream.concat(
        LongStream.of(Long.MIN_VALUE, Long.MIN_VALUE + 1, Long.MAX_VALUE - 1, Long.MAX_VALUE),
        LongStream.rangeClosed(-100, 100)
    );
  }
}
