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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Objects;

/**
 * A convenient handler for converting the text content of attributes into scalar values.
 *
 * @param <S> The type of returned values
 */

public final class BTScalarAttributeHandler<S> implements BTElementHandlerType<Object, S>
{
  private final BTAttributesHandlerType<S> handler;
  private final BTQualifiedName name;
  private S result;

  /**
   * Construct a handler.
   *
   * @param inName    The name of elements handled by this handler
   * @param inHandler The character handler
   */

  public BTScalarAttributeHandler(
    final BTQualifiedName inName,
    final BTAttributesHandlerType<S> inHandler)
  {
    this.name = Objects.requireNonNull(inName, "name");
    this.handler = Objects.requireNonNull(inHandler, "handler");
  }

  @Override
  public String name()
  {
    return this.name.localName();
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
    throws SAXException
  {
    this.result = this.handler.parse(context, attributes);
  }

  @Override
  public S onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.result;
  }
}
