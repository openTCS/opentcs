/*
 * openTCS copyright information:
 * Copyright (c) 2008 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.algorithms;

import com.google.inject.BindingAnnotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the methods that a generic kernel extension must implement.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface KernelExtension {

  /**
   * Checks whether this extension is currently enabled or not.
   *
   * @return <code>true</code> if this extension is currently enabled, else
   * <code>false</code>.
   */
  boolean isPluggedIn();

  /**
   * Enable this extension.
   */
  void plugIn();

  /**
   * Disable this extension.
   */
  void plugOut();

  /**
   * Annotation type for marking/binding injectable extensions for modelling
   * mode.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Modelling {
    // Nothing here.
  }

  /**
   * Annotation type for marking/binding injectable extensions for operating
   * mode.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Operating {
    // Nothing here.
  }

  /**
   * Annotation type for marking/binding injectable extensions for all modes.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Permanent {
    // Nothing here.
  }
}
