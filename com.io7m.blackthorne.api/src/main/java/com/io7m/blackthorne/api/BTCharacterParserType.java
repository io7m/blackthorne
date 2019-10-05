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


package com.io7m.blackthorne.api;

import org.xml.sax.SAXException;
import org.xml.sax.ext.Locator2;

/**
 * A function that, given a string, returns a {@code T}.
 *
 * @param <T> The type of returned values
 */

public interface BTCharacterParserType<T>
{
  /**
   * Parse a text value.
   *
   * @param locator    The document locator
   * @param characters The character array
   * @param offset     The offset into the character array of the start of the data
   * @param length     The number of characters in the data
   *
   * @return A value of {@code T}
   *
   * @throws SAXException On errors
   */

  T parse(
    Locator2 locator,
    char[] characters,
    int offset,
    int length)
    throws SAXException;
}
