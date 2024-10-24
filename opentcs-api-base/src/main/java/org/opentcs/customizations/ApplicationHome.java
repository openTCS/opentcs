// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.customizations;

import jakarta.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A binding annotation that can be used to have the application's home directory injected.
 * In classes participating in dependency injection, annotate an injected <code>java.io.File</code>
 * with this annotation to get a reference to the application's home directory.
 * <p>
 * Example:
 * </p>
 * <pre>
 * public MyClass(@ApplicationHome File applicationHome) { ... }
 * </pre>
 */
@Qualifier
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApplicationHome {
}
