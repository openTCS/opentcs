/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.customizations;

import com.google.inject.BindingAnnotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A binding annotation that can be used to have the application's home directory injected.
 * In classes participating in dependency injection, annotate an injected <code>java.io.File</code>
 * with this annotation to get a reference to the application's home directory.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@BindingAnnotation
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApplicationHome {
  // Nothing here.
}
