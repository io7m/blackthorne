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
import com.io7m.blackthorne.core.BTIgnoreUnrecognizedElements;
import com.io7m.blackthorne.core.BTQualifiedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A convenience handler for constructing content handlers that produce lists of values from the
 * child elements of a single element. Child elements may be of different types, but values produced
 * by the content handlers for the child elements must have a common supertype.
 *
 * @param <S> The type of list values
 */

public final class BTListPolyHandler<S> implements BTElementHandlerType<S, List<S>>
{
  private final List<S> childElements;
  private final BTQualifiedName elementName;
  private final Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends S>> itemHandlers;
  private final BTIgnoreUnrecognizedElements ignoreUnrecognized;

  /**
   * Construct a handler.
   *
   * @param inElementName        The list element name
   * @param inItemHandlers       The child item handlers
   * @param inIgnoreUnrecognized Whether or not to ignore unrecognized elements
   */

  public BTListPolyHandler(
    final BTQualifiedName inElementName,
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends S>> inItemHandlers,
    final BTIgnoreUnrecognizedElements inIgnoreUnrecognized)
  {
    this.elementName =
      Objects.requireNonNull(inElementName, "elementName");
    this.itemHandlers =
      Objects.requireNonNull(inItemHandlers, "itemHandler");
    this.ignoreUnrecognized =
      Objects.requireNonNull(inIgnoreUnrecognized, "inIgnoreUnrecognized");

    this.childElements = new ArrayList<>();
  }

  @Override
  public String name()
  {
    return this.elementName.localName();
  }

  @Override
  public BTIgnoreUnrecognizedElements onShouldIgnoreUnrecognizedElements(
    final BTElementParsingContextType context)
  {
    return this.ignoreUnrecognized;
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends S>> onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.copyOf(this.itemHandlers);
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final S result)
  {
    this.childElements.add(result);
  }

  @Override
  public List<S> onElementFinished(final BTElementParsingContextType context)
  {
    return List.copyOf(this.childElements);
  }
}
