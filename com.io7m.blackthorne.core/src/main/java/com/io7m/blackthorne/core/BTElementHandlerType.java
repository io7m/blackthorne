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


package com.io7m.blackthorne.core;

import com.io7m.blackthorne.core.internal.BTFunctorHandler;
import org.xml.sax.Attributes;

import java.util.Map;
import java.util.function.Function;

/**
 * The type of element handlers. The purpose of an element handler is to respond to calls made by an
 * XML stream parser.
 *
 * An element handler handles a single XML element, producing a value of type {@code RT} when the
 * element is finished by the calling stream parser. The element receives values of type {@code CT}
 * from any declared direct child elements. An element will only receive values from child elements
 * for which it provides a handler constructor (see {@link #onChildHandlersRequested(BTElementParsingContextType)}).
 * Handlers can declare whether or not they want unrecognized child elements ignored, or for
 * unrecognized child elements to result in a parse error (see {@link
 * #onShouldIgnoreUnrecognizedElements(BTElementParsingContextType)}).
 *
 * @param <CT> The type of values returned by child elements
 * @param <RT> The type of returned values
 */

public interface BTElementHandlerType<CT, RT>
{
  /**
   * Return a set of handler constructors for direct child elements of this element.
   *
   * @param context The parsing context
   *
   * @return A set of handler constructors
   */

  default Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends CT>> onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.of();
  }

  /**
   * @return The name of this handler, used for diagnostic purposes
   */

  default String name()
  {
    return this.getClass().getCanonicalName();
  }

  /**
   * @param context The parsing context
   *
   * @return A specification of how unrecognized child elements should be handled
   */

  default BTIgnoreUnrecognizedElements onShouldIgnoreUnrecognizedElements(
    final BTElementParsingContextType context)
  {
    return BTIgnoreUnrecognizedElements.DO_NOT_IGNORE_UNRECOGNIZED_ELEMENTS;
  }

  /**
   * The parser has reached the start of this element.
   *
   * @param context    The parsing context
   * @param attributes The element's attributes
   *
   * @throws Exception On parse errors
   */

  default void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
    throws Exception
  {

  }

  /**
   * The parser has reached the end of this element. No further methods will be called on this
   * handler.
   *
   * @param context The parsing context
   *
   * @return The value produced by all of the data received up to this point
   *
   * @throws Exception On parse errors
   */

  RT onElementFinished(BTElementParsingContextType context)
    throws Exception;

  /**
   * A child element has been parsed successfully and produced a result.
   *
   * @param context The parsing context
   * @param result  The value produced by the child element
   *
   * @throws Exception On parse errors
   */

  default void onChildValueProduced(
    final BTElementParsingContextType context,
    final CT result)
    throws Exception
  {

  }

  /**
   * The parser has reached some raw text content.
   *
   * @param context The parsing context
   * @param data    A character array
   * @param offset  The start of the data within {@code data}
   * @param length  The length of the data within {@code data}
   *
   * @throws Exception On parse errors
   */

  default void onCharacters(
    final BTElementParsingContextType context,
    final char[] data,
    final int offset,
    final int length)
    throws Exception
  {

  }

  /**
   * Functor {@code map} for handlers.
   *
   * @param function A function to apply to handler results
   * @param <RX>     The type of mapped results
   *
   * @return A mapped handler
   */

  default <RX> BTElementHandlerType<CT, RX> map(
    final Function<RT, RX> function)
  {
    return new BTFunctorHandler<>(this, function);
  }
}
