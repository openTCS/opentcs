/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.math;

/**
 * Implements methods for mathematical operations on integer data types.
 *
 * @author Iryna Felko (Fraunhofer IML)
 */
public final class IntegerArithmetic
    implements Arithmetic<Integer> {

  /**
   * The neutral add element.
   */
  private static final Integer NEUTRAL_ADD_ELEMENT = 0;
  /**
   * The neutral multiply element.
   */
  private static final Integer NEUTRAL_MULTIPLY_ELEMENT = 1;
  
  /**
   * Creates a new IntegerArithmetic.
   */
  public IntegerArithmetic() {
    // Do nada.
  }

  @Override
  public Integer getNeutralAddElement() {
    return NEUTRAL_ADD_ELEMENT;
  }

  @Override
  public Integer getNeutralMulElement() {
    return NEUTRAL_MULTIPLY_ELEMENT;
  }

  @Override
  public Integer add(final Integer summand1, final Integer summand2) {
    Integer parameter1;
    Integer parameter2;
    if (summand1 == null) {
      parameter1 = getNeutralAddElement();
    }
    else {
      parameter1 = summand1;
    }
    if (summand2 == null) {
      parameter2 = getNeutralAddElement();
    }
    else {
      parameter2 = summand2;
    }
    return parameter1 + parameter2;
  }

  @Override
  public Integer subtract(final Integer minuend, final Integer subtrahend) {
    Integer parameterMinuend;
    Integer parameterSubtrahend;
    if (minuend == null) {
      parameterMinuend = getNeutralAddElement();
    }
    else {
      parameterMinuend = minuend;
    }
    if (subtrahend == null) {
      parameterSubtrahend = getNeutralAddElement();
    }
    else {
      parameterSubtrahend = subtrahend;
    }
    return parameterMinuend - parameterSubtrahend;
  }

  @Override
  public Integer multiply(final Integer factor1, final Integer factor2) {
    Integer parameterFactor1;
    Integer parameterFactor2;
    if (factor1 == null) {
      parameterFactor1 = getNeutralMulElement();
    }
    else {
      parameterFactor1 = factor1;
    }
    if (factor2 == null) {
      parameterFactor2 = getNeutralMulElement();
    }
    else {
      parameterFactor2 = factor2;
    }
    return parameterFactor1 * parameterFactor2;
  }

  @Override
  public Integer divide(final Integer dividend, final Integer divisor) {
    Integer parameterDividend;
    Integer parameterDivisor;
    if (dividend == null) {
      parameterDividend = getNeutralMulElement();
    }
    else {
      parameterDividend = dividend;
    }
    if (divisor == null) {
      parameterDivisor = getNeutralMulElement();
    }
    else {
      parameterDivisor = divisor;
    }
    return parameterDividend / parameterDivisor;
  }
}
