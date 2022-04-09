/*
 * Copyright © 2018 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.blackthorne.tests;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class EqualsTest
{
  private static final Logger LOG = LoggerFactory.getLogger(EqualsTest.class);

  private static boolean isIgnoredClass(
    final Class<?> clazz)
  {
    switch (clazz.getCanonicalName()) {
      default:
        return false;
    }
  }

  private static Set<String> nonNullFieldsFor(
    final Class<?> clazz)
  {
    final var declaredFieldNames =
      Stream.of(clazz.getDeclaredFields())
        .map(Field::getName)
        .collect(Collectors.toSet());

    declaredFieldNames.remove("$VALUES");
    return declaredFieldNames;
  }

  private static Set<String> ignoreFieldsFor(
    final Class<?> clazz)
  {
    switch (clazz.getCanonicalName()) {
      default: {
        break;
      }
    }

    final var transientFieldNames =
      Stream.of(clazz.getDeclaredFields())
        .filter(field -> Modifier.isTransient(field.getModifiers()))
        .map(Field::getName)
        .collect(Collectors.toSet());

    final var declaredFieldNames =
      Stream.of(clazz.getDeclaredFields())
        .map(Field::getName)
        .collect(Collectors.toSet());

    if (declaredFieldNames.contains("type")) {
      transientFieldNames.add("type");
    }

    switch (clazz.getCanonicalName()) {
      default: {
        break;
      }
    }

    return transientFieldNames;
  }

  private static DynamicTest testForClass(final Class<?> clazz)
  {
    final var task = taskForClass(clazz);
    return DynamicTest.dynamicTest(
      "testEqualsReflectively: " + clazz.getCanonicalName(), task);
  }

  private static Executable taskForClass(
    final Class<?> clazz)
  {
    return () -> {
      final var nnFields = nonNullFieldsFor(clazz);
      final var nnArray = new String[nnFields.size()];
      nnFields.toArray(nnArray);

      final var igFields = ignoreFieldsFor(clazz);
      final var igArray = new String[igFields.size()];
      igFields.toArray(igArray);

      LOG.debug(
        "checking: {} (nonnull {}, ignoring {})",
        clazz.getCanonicalName(),
        nnFields,
        igFields);

      EqualsVerifier.forClass(clazz)
        .withNonnullFields(nnArray)
        .withIgnoredFields(igArray)
        .verify();
    };
  }

  @TestFactory
  public Stream<DynamicTest> testEqualsReflectively()
  {
    final var reflections = new Reflections("com.io7m.blackthorne");

    final var types =
      reflections.getTypesAnnotatedWith(Value.Immutable.class);

    final var enums =
      reflections.getSubTypesOf(Enum.class)
        .stream()
        .filter(Class::isEnum)
        .collect(Collectors.toList());

    types.addAll(enums);

    Assertions.assertTrue(types.size() > 1, "At least 1 subtypes must exist");

    final Collection<DynamicTest> executables = new ArrayList<>();

    for (final var type : types) {
      if (type.isEnum()) {
        executables.add(testForClass(type));
        continue;
      }

      if (!type.isInterface()) {
        continue;
      }

      final var subtypes = reflections.getSubTypesOf(type);
      for (final var subtype : subtypes) {
        if (isIgnoredClass(subtype)) {
          continue;
        }

        executables.add(testForClass(subtype));
      }
    }
    return executables.stream();
  }
}
