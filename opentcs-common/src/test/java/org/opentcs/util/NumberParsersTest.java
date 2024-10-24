// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.LongStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 */
class NumberParsersTest {

  @ParameterizedTest
  @MethodSource("paramsFactory")
  void parsesNumbers(long number) {
    assertEquals(number, NumberParsers.parsePureDecimalLong(Long.toString(number)));
  }

  static LongStream paramsFactory() {
    return LongStream.concat(
        LongStream.of(Long.MIN_VALUE, Long.MIN_VALUE + 1, Long.MAX_VALUE - 1, Long.MAX_VALUE),
        LongStream.rangeClosed(-100, 100)
    );
  }
}
