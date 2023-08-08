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


package com.io7m.blackthorne.core.internal;

import com.io7m.blackthorne.core.BTElementHandlerConstructorType;
import com.io7m.blackthorne.core.BTElementHandlerType;
import com.io7m.blackthorne.core.BTElementParsingContextType;
import com.io7m.blackthorne.core.BTQualifiedName;

import java.util.Map;
import java.util.Objects;

/**
 * A convenience handler for constructing content handlers that produce lists of values from the
 * child elements of a single element. Child elements may be of different types, but values produced
 * by the content handlers for the child elements must have a common supertype.
 *
 * @param <A> The common supertype
 * @param <B> The type of values produced by child elements
 */

public final class BTOneOfHandler<A, B extends A>
  implements BTElementHandlerType<B, A>
{
  private final Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends B>> itemHandlers;
  private B childElement;

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends B>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return this.itemHandlers;
  }

  @Override
  public String name()
  {
    return "[OneOf]";
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final B result)
  {
    this.childElement = Objects.requireNonNull(result, "result");
  }

  /**
   * Construct a handler.
   *
   * @param inItemHandlers The child item handlers
   */

  public BTOneOfHandler(
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends B>> inItemHandlers)
  {
    this.itemHandlers =
      Map.copyOf(Objects.requireNonNull(inItemHandlers, "itemHandler"));
  }

  @Override
  public A onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.childElement;
  }
}
