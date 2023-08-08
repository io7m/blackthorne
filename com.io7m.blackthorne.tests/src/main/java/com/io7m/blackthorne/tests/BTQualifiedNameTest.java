/*
 * Copyright © 2019 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import com.io7m.blackthorne.core.BTQualifiedName;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Assertions;

import java.net.URI;

public final class BTQualifiedNameTest
{
  @Provide
  public Arbitrary<URI> uris()
  {
    return Combinators.combine(
      Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1),
      Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1),
      Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1))
      .as((s0, s1, s2) -> {
        try {
          return new URI(s0, s1, s2);
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
      });
  }

  @Property
  public void testOrdering(
    final @ForAll("uris") URI namespace0,
    final @ForAll("uris") URI namespace1,
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
      new BTQualifiedName(namespace0, localName0)
        .compareTo(new BTQualifiedName(namespace1, localName1));

    Assertions.assertEquals(c, r);
  }
}
