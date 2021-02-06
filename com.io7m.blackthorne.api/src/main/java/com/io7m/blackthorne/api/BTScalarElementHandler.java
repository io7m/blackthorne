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

import java.util.Objects;

/**
 * A convenient handler for converting the text content of elements into scalar values.
 *
 * @param <S> The type of returned values
 */

public final class BTScalarElementHandler<S> implements BTElementHandlerType<Object, S>
{
  private static final char[] CHARACTERS = new char[0];
  private final BTCharacterHandlerType<S> handler;
  private final BTQualifiedName name;
  private S result;

  /**
   * Construct a handler.
   *
   * @param inName    The name of elements handled by this handler
   * @param inHandler The character handler
   */

  public BTScalarElementHandler(
    final BTQualifiedName inName,
    final BTCharacterHandlerType<S> inHandler)
  {
    this.name =
      Objects.requireNonNull(inName, "name");
    this.handler =
      Objects.requireNonNull(inHandler, "handler");
  }

  @Override
  public String name()
  {
    return this.name.localName();
  }

  @Override
  public void onCharacters(
    final BTElementParsingContextType context,
    final char[] data,
    final int offset,
    final int length)
    throws Exception
  {
    final var parsed =
      this.handler.parse(context, data, offset, length);
    this.result =
      Objects.requireNonNull(parsed, "parsed");
  }

  @Override
  public S onElementFinished(
    final BTElementParsingContextType context)
    throws Exception
  {
    if (this.result == null) {
      final var parsed =
        this.handler.parse(context, CHARACTERS, 0, 0);
      this.result =
        Objects.requireNonNull(parsed, "parsed");
    }
    return this.result;
  }
}
