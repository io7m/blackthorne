/*
 * Copyright © 2018 Mark Raynsford <code@io7m.com> http://io7m.com
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

import com.io7m.junreachable.UnreachableCodeException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.Locator2;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * An abstract implementation of the content handler interface.
 *
 * @param <A> The type of values returned by child handlers
 * @param <B> The type of result values
 */

public abstract class BTContentHandlerAbstract<A, B> implements BTContentHandlerType<B>
{
  private final Locator2 locator;
  private final String directElementName;
  private final BTAcceptCharacters acceptCharacters;
  private BTContentHandlerType<? extends A> handler;
  private B result;

  protected BTContentHandlerAbstract(
    final Locator2 inLocator)
  {
    this(inLocator, "", BTAcceptCharacters.DO_NOT_ACCEPT_CHARACTERS);
  }

  protected BTContentHandlerAbstract(
    final Locator2 inLocator,
    final String inDirect,
    final BTAcceptCharacters inAcceptCharacters)
  {
    this.locator =
      Objects.requireNonNull(inLocator, "locator");
    this.directElementName =
      Objects.requireNonNull(inDirect, "inDirect");
    this.acceptCharacters =
      Objects.requireNonNull(inAcceptCharacters, "inAcceptCharacters");
  }

  @Override
  public final String toString()
  {
    return new StringBuilder(128)
      .append("[")
      .append(this.getClass().getCanonicalName())
      .append(" ")
      .append(this.directElementName)
      .append(" [")
      .append(String.join("|", this.onOverrideWantChildHandlers().keySet()))
      .append("]]")
      .toString();
  }

  @Override
  public final void onElementStarted(
    final String namespace,
    final String name,
    final String qname,
    final Attributes attributes)
    throws SAXException
  {
    Objects.requireNonNull(namespace, "namespace");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(qname, "qname");
    Objects.requireNonNull(attributes, "attributes");

    final var current = this.handler;
    if (current != null) {
      current.onElementStarted(namespace, name, qname, attributes);
      return;
    }

    final var handler_supplier = this.onOverrideWantChildHandlers().get(name);
    if (handler_supplier != null) {
      final var new_handler = handler_supplier.create(this.locator);
      Objects.requireNonNull(new_handler, "new_handler");
      this.handler = new_handler;
      new_handler.onElementStarted(namespace, name, qname, attributes);
      return;
    }

    if (Objects.equals(this.directElementName, name)) {
      this.onOverrideElementStartDirectly(namespace, name, qname, attributes);
      return;
    }

    throw new SAXParseException(
      BTMessages.format(
        "errorHandlerUnrecognizedElement",
        this.getClass().getCanonicalName(),
        name,
        String.join("|", this.onOverrideWantChildHandlers().keySet()),
        this.directElementName),
      this.locator);
  }

  private void finishChildHandlerIfNecessary(
    final String childElement,
    final Optional<? extends A> child_value)
  {
    if (child_value.isPresent()) {
      this.onOverrideChildResultReceived(childElement, child_value.get());
      this.handler = null;
    }
  }

  protected final Locator2 locator()
  {
    return this.locator;
  }

  /**
   * Return the handlers for child elements. The default implementation of this method returns an
   * empty map, meaning that no child elements are allowed.
   *
   * @return The handlers for child elements
   */

  protected Map<String, BTContentHandlerConstructorType<? extends A>> onOverrideWantChildHandlers()
  {
    return Map.of();
  }

  /**
   * Called when the XML element has finished. Implementations of this method should call {@link
   * #finish(Object)} if the method has not been called prior to this method.
   *
   * @param namespace The element namespace
   * @param name      The element local name
   * @param qname     The element qualified name
   *
   * @return The resulting value
   *
   * @throws SAXException On errors
   */

  protected Optional<B> onOverrideElementFinishDirectly(
    final String namespace,
    final String name,
    final String qname)
    throws SAXException
  {
    return Optional.of(this.get());
  }

  /**
   * Called when characters are to be consumed directly.
   *
   * @param ch     The character array
   * @param start  The starting offset
   * @param length The number of characters to consume
   *
   * @throws SAXException On errors
   */

  protected void onOverrideCharactersDirectly(
    final char[] ch,
    final int start,
    final int length)
    throws SAXException
  {
    // Ignored by default
  }

  /**
   * Called when the XML element is starting.
   *
   * @param namespace The element namespace
   * @param name      The element local name
   * @param qname     The element qualified name
   *
   * @throws SAXException On errors
   */

  protected void onOverrideElementStartDirectly(
    final String namespace,
    final String name,
    final String qname,
    final Attributes attributes)
    throws SAXException
  {
    // Ignored by default
  }

  /**
   * A value was received from a child handler.
   *
   * @param childElement The name of the child element that produced the value
   * @param value        The result value
   */

  protected void onOverrideChildResultReceived(
    final String childElement,
    final A value)
  {
    // Ignored by default
  }

  @Override
  public final Optional<B> onElementFinished(
    final String namespace,
    final String name,
    final String qname)
    throws SAXException
  {
    Objects.requireNonNull(namespace, "namespace");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(qname, "qname");

    final var current = this.handler;
    if (current != null) {
      final var sub_result = current.onElementFinished(namespace, name, qname);
      this.finishChildHandlerIfNecessary(name, sub_result);
      return Optional.empty();
    }

    if (Objects.equals(this.directElementName, name)) {
      final var result_opt = this.onOverrideElementFinishDirectly(namespace, name, qname);
      result_opt.ifPresent(this::finish);
      return result_opt;
    }

    throw new UnreachableCodeException();
  }

  protected final B finish(final B r)
  {
    this.result = Objects.requireNonNull(r, "r");
    return r;
  }

  @Override
  public final void onCharacters(
    final char[] ch,
    final int start,
    final int length)
    throws SAXException
  {
    Objects.requireNonNull(ch, "ch");

    final var current = this.handler;
    if (current != null) {
      current.onCharacters(ch, start, length);
      return;
    }

    switch (this.acceptCharacters) {
      case ACCEPT_CHARACTERS:
        this.onOverrideCharactersDirectly(ch, start, length);
        return;
      case DO_NOT_ACCEPT_CHARACTERS:
        break;
    }
  }

  @Override
  public final B get()
  {
    if (this.result == null) {
      throw new IllegalStateException(BTMessages.format("errorParserDidNotComplete"));
    }
    return this.result;
  }
}
