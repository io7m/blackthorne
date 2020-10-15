/*
 * Copyright © 2019 Mark Raynsford <code@io7m.com> http://io7m.com
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

import com.io7m.blackthorne.api.BTQualifiedName;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;

public final class BTQualifiedNameTest
{
  @Property
  public void testOrdering(
    final @ForAll String namespace0,
    final @ForAll String namespace1,
    final @ForAll String localName0,
    final @ForAll String localName1)
  {
    final var ns = namespace0.compareTo(namespace1);
    final int c;
    if (ns == 0) {
      c = localName0.compareTo(localName1);
    } else {
      c = ns;
    }

    final var r =
      BTQualifiedName.of(namespace0, localName0)
        .compareTo(BTQualifiedName.of(namespace1, localName1));

    Assertions.assertEquals(c, r);
  }

  @Property
  public void testBuilder(
    final @ForAll String namespace0,
    final @ForAll String namespace1,
    final @ForAll String namespace2,
    final @ForAll String localName0,
    final @ForAll String localName1,
    final @ForAll String localName2)
  {
    Assumptions.assumeFalse(namespace0.equals(namespace1));
    Assumptions.assumeFalse(namespace1.equals(namespace2));
    Assumptions.assumeFalse(localName0.equals(localName1));
    Assumptions.assumeFalse(localName1.equals(localName2));

    final var v0 =
      BTQualifiedName.builder()
        .setLocalName(localName0)
        .setLocalName(localName1)
        .setNamespaceURI(namespace0)
        .setNamespaceURI(namespace1)
        .build();

    final var v1 =
      BTQualifiedName.builder()
        .from(v0)
        .build()
        .withLocalName(localName2)
        .withNamespaceURI(namespace2);

    final var v2 =
      BTQualifiedName.copyOf(v1);

    Assertions.assertNotEquals(v0, v1);
    Assertions.assertNotEquals(v0, v2);
    Assertions.assertEquals(v2, v1);
    Assertions.assertEquals(v2.toString(), v1.toString());
  }
}
