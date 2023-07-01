/*
 * Copyright © 2023 Mark Raynsford <code@io7m.com> https://www.io7m.com
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


package com.io7m.blackthorne.api;

import java.net.URI;
import java.util.Comparator;
import java.util.Objects;

/**
 * The type of fully-qualified XML element names.
 *
 * @param namespaceURI The XML namespace
 * @param localName    The local element name
 */

public record BTQualifiedName(
  URI namespaceURI,
  String localName)
  implements Comparable<BTQualifiedName>
{
  /**
   * The type of fully-qualified XML element names.
   *
   * @param namespaceURI The XML namespace
   * @param localName    The local element name
   */

  public BTQualifiedName
  {
    Objects.requireNonNull(namespaceURI, "namespaceURI");
    Objects.requireNonNull(localName, "localName");
  }

  /**
   * Create a qualified name.
   *
   * @param namespaceURI The URI
   * @param localName    The local name
   *
   * @return A qualified name
   */

  public static BTQualifiedName of(
    final URI namespaceURI,
    final String localName)
  {
    return new BTQualifiedName(namespaceURI, localName);
  }

  /**
   * Create a qualified name.
   *
   * @param namespaceURI The URI
   * @param localName    The local name
   *
   * @return A qualified name
   */

  public static BTQualifiedName of(
    final String namespaceURI,
    final String localName)
  {
    return of(URI.create(namespaceURI), localName);
  }

  @Override
  public int compareTo(final BTQualifiedName other)
  {
    Objects.requireNonNull(other, "other");
    return Comparator.comparing(BTQualifiedName::namespaceURI)
      .thenComparing(BTQualifiedName::localName)
      .compare(this, other);
  }
}
