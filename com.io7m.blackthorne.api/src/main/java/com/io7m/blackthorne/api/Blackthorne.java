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

import com.io7m.junreachable.UnreachableCodeException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.Locator2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.io7m.blackthorne.api.BTAcceptCharacters.ACCEPT_CHARACTERS;
import static com.io7m.blackthorne.api.BTAcceptCharacters.DO_NOT_ACCEPT_CHARACTERS;

/**
 * Convenience functions.
 */

public final class Blackthorne
{
  private Blackthorne()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Construct a new versioned dispatching handler builder.
   *
   * @param <T> The type of returned values
   *
   * @return A new builder
   */

  public static <T> BTVersionedDispatchingHandlerBuilderType<T> versionedDispatching()
  {
    return BTVersionedDispatchingHandler.builder();
  }

  /**
   * Widen the type of the given content handler. If {@code A <: B}, then any content handler that
   * produces a value of {@code A} implicitly produces a value of {@code B}.
   *
   * @param handler The content handler
   * @param <B>     The supertype
   * @param <A>     The subtype
   *
   * @return A content handler that returns {@code B}
   */

  public static <B, A extends B> BTContentHandlerType<B> widen(
    final BTContentHandlerType<A> handler)
  {
    return Objects.requireNonNull(handler, "handler").map(x -> x);
  }

  /**
   * Widen the type of the given content handler constructor. If {@code A <: B}, then any content
   * handler that produces a value of {@code A} implicitly produces a value of {@code B}.
   *
   * @param handler The content handler
   * @param <B>     The supertype
   * @param <A>     The subtype
   *
   * @return A content handler constructor that returns {@code B}
   */

  public static <B, A extends B> BTContentHandlerConstructorType<B> widenConstructor(
    final BTContentHandlerConstructorType<A> handler)
  {
    Objects.requireNonNull(handler, "handler");
    return locator -> widen(handler.create(locator));
  }

  /**
   * Create a builder for a versioned dispatching handler. The given class parameter is simply there
   * to aid type inference and is otherwise ignored.
   *
   * @param clazz The class
   * @param <T>   The type of returned values
   *
   * @return A builder
   */

  public static <T> BTVersionedDispatchingHandlerBuilderType<T> versionedDispatching(
    final Class<T> clazz)
  {
    return BTVersionedDispatchingHandler.builder();
  }

  /**
   * A convenience function for constructing content handlers that produce a scalar value from the
   * text content of a single XML element.
   *
   * @param elementName The name of the element
   * @param parser      A function that receives text and returns a value of type {@code S}
   * @param <S>         The type of returned scalar values
   *
   * @return A content handler constructor
   */

  public static <S> BTContentHandlerConstructorType<S> forScalar(
    final String elementName,
    final BTCharacterParserType<S> parser)
  {
    Objects.requireNonNull(elementName, "elementName");
    Objects.requireNonNull(parser, "parser");

    return locator -> new ScalarHandler<>(locator, elementName, parser);
  }

  /**
   * A convenience function for constructing content handlers that produce lists of values from the
   * child elements of a single element. All child elements are expected to be of the same type.
   *
   * @param elementName      The name of the element
   * @param childElementName The name of the child element
   * @param itemHandler      A handler for child elements
   * @param <S>              The type of returned scalar values
   *
   * @return A content handler constructor
   */

  public static <S> BTContentHandlerConstructorType<List<S>> forListMono(
    final String elementName,
    final String childElementName,
    final BTContentHandlerConstructorType<? extends S> itemHandler)
  {
    Objects.requireNonNull(elementName, "elementName");
    Objects.requireNonNull(childElementName, "childElementName");

    return locator -> {
      final List<S> childElements = new ArrayList<>();
      return new ListHandler<>(locator, elementName, childElementName, itemHandler, childElements);
    };
  }

  /**
   * A convenience function for constructing content handlers that produce lists of values from the
   * child elements of a single element. Child elements may be of different types, but values
   * produced by the content handlers for the child elements must have a common supertype.
   *
   * @param elementName  The name of the element
   * @param itemHandlers Handlers for child elements
   * @param <S>          The type of returned scalar values
   *
   * @return A content handler constructor
   */

  public static <S> BTContentHandlerConstructorType<List<S>> forListPoly(
    final String elementName,
    final Map<String, BTContentHandlerConstructorType<? extends S>> itemHandlers)
  {
    Objects.requireNonNull(elementName, "elementName");

    final var itemHandlersCaptured =
      Map.copyOf(Objects.requireNonNull(itemHandlers, "itemHandlers"));

    return locator -> {
      final List<S> childElements = new ArrayList<>();
      return new ListPolyHandler<>(locator, elementName, itemHandlersCaptured, childElements);
    };
  }

  private static final class ScalarHandler<S> extends BTContentHandlerAbstract<Void, S>
  {
    private final Locator2 locator;
    private final BTCharacterParserType<S> parser;

    private ScalarHandler(
      final Locator2 inLocator,
      final String elementName,
      final BTCharacterParserType<S> inParser)
    {
      super(inLocator, elementName, ACCEPT_CHARACTERS);
      this.locator = Objects.requireNonNull(inLocator, "locator");
      this.parser = Objects.requireNonNull(inParser, "parser");
    }

    @Override
    protected void onOverrideCharactersDirectly(
      final char[] ch,
      final int start,
      final int length)
      throws SAXException
    {
      try {
        this.finish(this.parser.parse(this.locator, ch, start, length));
      } catch (final Exception e) {
        throw new SAXParseException(e.getLocalizedMessage(), this.locator(), e);
      }
    }
  }

  private static final class ListHandler<S> extends BTContentHandlerAbstract<S, List<S>>
  {
    private final String childElementName;
    private final BTContentHandlerConstructorType<? extends S> itemHandler;
    private final List<S> childElements;

    private ListHandler(
      final Locator2 locator,
      final String elementName,
      final String inChildElementName,
      final BTContentHandlerConstructorType<? extends S> inItemHandler,
      final List<S> inChildElements)
    {
      super(locator, elementName, DO_NOT_ACCEPT_CHARACTERS);

      this.childElementName =
        Objects.requireNonNull(inChildElementName, "childElementName");
      this.itemHandler =
        Objects.requireNonNull(inItemHandler, "itemHandler");
      this.childElements =
        Objects.requireNonNull(inChildElements, "childElements");
    }

    @Override
    protected Map<String, BTContentHandlerConstructorType<? extends S>> onOverrideWantChildHandlers()
    {
      return Map.of(this.childElementName, this.itemHandler);
    }

    @Override
    protected Optional<List<S>> onOverrideElementFinishDirectly(
      final String namespace,
      final String name,
      final String qname)
    {
      return Optional.of(this.finish(this.childElements));
    }

    @Override
    protected void onOverrideChildResultReceived(
      final String childElement,
      final S value)
    {
      this.childElements.add(value);
    }
  }

  private static final class ListPolyHandler<S> extends BTContentHandlerAbstract<S, List<S>>
  {
    private final Map<String, BTContentHandlerConstructorType<? extends S>> itemHandlersCaptured;
    private final List<S> childElements;

    private ListPolyHandler(
      final Locator2 locator,
      final String elementName,
      final Map<String, BTContentHandlerConstructorType<? extends S>> inItemHandlersCaptured,
      final List<S> inChildElements)
    {
      super(locator, elementName, DO_NOT_ACCEPT_CHARACTERS);
      this.itemHandlersCaptured =
        Objects.requireNonNull(inItemHandlersCaptured, "inItemHandlersCaptured");
      this.childElements =
        Objects.requireNonNull(inChildElements, "inChildElements");
    }

    @Override
    protected Map<String, BTContentHandlerConstructorType<? extends S>> onOverrideWantChildHandlers()
    {
      return this.itemHandlersCaptured;
    }

    @Override
    protected Optional<List<S>> onOverrideElementFinishDirectly(
      final String namespace,
      final String name,
      final String qname)
    {
      return Optional.of(this.finish(this.childElements));
    }

    @Override
    protected void onOverrideChildResultReceived(
      final String childElement,
      final S value)
    {
      this.childElements.add(value);
    }
  }
}
