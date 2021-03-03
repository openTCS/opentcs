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
 * Declares methods for mathematical operations on generic data types.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <E> The data type the mathematical operations apply to.
 */
public interface Arithmetic<E> {

  /**
   * Returns the neutral element for additions.
   *
   * @return The neutral element for additions.
   */
  E getNeutralAddElement();

  /**
   * Returns the neutral element for multiplications.
   *
   * @return The neutral element for multiplications.
   */
  E getNeutralMulElement();

  /**
   * Returns the sum of the given summands.
   * If any of the given summands is <code>null</code>, it is replaced by the
   * neutral element for additions.
   *
   * @param summand1 The first summand.
   * @param summand2 The second summand.
   * @return The sum of the given summands.
   */
  E add(E summand1, E summand2);

  /**
   * Returns the difference of the given parameters.
   * If any of the given summands is <code>null</code>, it is replaced by the
   * neutral element for additions.
   *
   * @param minuend The minuend.
   * @param subtrahend The subtrahend.
   * @return The difference of the given parameters.
   */
  E subtract(E minuend, E subtrahend);

  /**
   * Returns the product of the given parameters.
   * If any of the given summands is <code>null</code>, it is replaced by the
   * neutral element for multiplications.
   *
   * @param factor1 The first factor.
   * @param factor2 The second factor.
   * @return The product of the given parameters.
   */
  E multiply(E factor1, E factor2);

  /**
   * Returns the quotient of the given parameters.
   * If any of the given summands is <code>null</code>, it is replaced by the
   * neutral element for multiplications.
   *
   * @param dividend The dividend.
   * @param divisor The divisor.
   * @return The quotient of the given parameters.
   */
  E divide(E dividend, E divisor);
}
