/*
 * Copyright © 2020 Mark Raynsford <code@io7m.com> http://io7m.com
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

import org.xml.sax.Attributes;

/**
 * An element handler that takes the attributes of a single element and
 * produces an output value.
 *
 * @param <S> The type of returned values
 */

public interface BTLeafElementHandlerType<S>
{
  /**
   * Process the attributes of an element.
   *
   * @param context    The parse context
   * @param attributes The element attributes
   *
   * @return A value of {@code S}
   *
   * @throws Exception On errors
   */

  S onElement(
    BTElementParsingContextType context,
    Attributes attributes)
    throws Exception;
}
