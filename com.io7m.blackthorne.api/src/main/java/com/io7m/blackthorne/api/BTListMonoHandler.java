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


package com.io7m.blackthorne.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A convenience handler for constructing content handlers that produce lists of values from the
 * child elements of a single element. All child elements are expected to be of the same type.
 *
 * @param <S> The type of list values
 */

public final class BTListMonoHandler<S> implements BTElementHandlerType<S, List<S>>
{
  private final List<S> childElements;
  private final BTQualifiedName elementName;
  private final BTQualifiedName childElementName;
  private final BTElementHandlerConstructorType<?, ? extends S> itemHandler;
  private final BTIgnoreUnrecognizedElements ignoreUnrecognized;

  /**
   * Construct a handler.
   *
   * @param inElementName        The list element name
   * @param inChildElementName   The name of child elements
   * @param inItemHandler        The handler used for child elements
   * @param inIgnoreUnrecognized Whether or not to ignore unrecognized elements
   */

  public BTListMonoHandler(
    final BTQualifiedName inElementName,
    final BTQualifiedName inChildElementName,
    final BTElementHandlerConstructorType<?, ? extends S> inItemHandler,
    final BTIgnoreUnrecognizedElements inIgnoreUnrecognized)
  {
    this.elementName =
      Objects.requireNonNull(inElementName, "elementName");
    this.childElementName =
      Objects.requireNonNull(inChildElementName, "childElementName");
    this.itemHandler =
      Objects.requireNonNull(inItemHandler, "itemHandler");
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
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends S>> onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.ofEntries(Map.entry(this.childElementName, this.itemHandler));
  }

  @Override
  public BTIgnoreUnrecognizedElements onShouldIgnoreUnrecognizedElements(
    final BTElementParsingContextType context)
  {
    return this.ignoreUnrecognized;
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
