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

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * A handler that implements {@code fmap} for handlers.
 *
 * @param <A> The type of child values
 * @param <B> The type of result values
 * @param <C> The type of result values after a function is applied
 */

public final class BTFunctorHandler<A, B, C> implements BTElementHandlerType<A, C>
{
  private final BTElementHandlerType<A, B> handler;
  private final Function<B, C> function;

  /**
   * Construct a functor handler.
   *
   * @param inHandler  The inner handler
   * @param inFunction The mapping function
   */

  public BTFunctorHandler(
    final BTElementHandlerType<A, B> inHandler,
    final Function<B, C> inFunction)
  {
    this.handler = Objects.requireNonNull(inHandler, "handler");
    this.function = Objects.requireNonNull(inFunction, "function");
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends A>> onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return this.handler.onChildHandlersRequested(context);
  }

  @Override
  public String name()
  {
    return String.format("[map %s]", this.handler.name());
  }

  @Override
  public BTIgnoreUnrecognizedElements onShouldIgnoreUnrecognizedElements(
    final BTElementParsingContextType context)
  {
    return this.handler.onShouldIgnoreUnrecognizedElements(context);
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
    throws SAXException
  {
    this.handler.onElementStart(context, attributes);
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final A result)
    throws SAXException
  {
    this.handler.onChildValueProduced(context, result);
  }

  @Override
  public void onCharacters(
    final BTElementParsingContextType context,
    final char[] data,
    final int offset,
    final int length)
    throws SAXException
  {
    this.handler.onCharacters(context, data, offset, length);
  }

  @Override
  public C onElementFinished(final BTElementParsingContextType context)
    throws SAXException
  {
    return this.function.apply(this.handler.onElementFinished(context));
  }
}
