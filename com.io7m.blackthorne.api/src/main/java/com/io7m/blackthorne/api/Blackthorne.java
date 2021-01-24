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

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.junreachable.UnreachableCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static com.io7m.blackthorne.api.BTParseErrorType.Severity.ERROR;

/**
 * Convenience functions.
 */

public final class Blackthorne
{
  private static final Logger LOG =
    LoggerFactory.getLogger(Blackthorne.class);

  private Blackthorne()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Functor map for handlers.
   *
   * @param handler  The handler
   * @param function A function to apply to the result of handlers produced by {@code constructor}
   * @param <CT>     The type of child handler values
   * @param <RT>     The type of result values
   * @param <RX>     The type of mapped result values
   *
   * @return A new handler
   */

  public static <CT, RT, RX> BTElementHandlerType<CT, RX> map(
    final BTElementHandlerType<CT, RT> handler,
    final Function<RT, RX> function)
  {
    return handler.map(function);
  }

  /**
   * Type widening for handlers. If {@code RX <: RT}, then any handler that produces a value of
   * {@code RX} implicitly produces a value of {@code RT}.
   *
   * @param handler The handler
   * @param <CT>    The type of child handler values
   * @param <RT>    The type of result values
   * @param <RX>    A subtype of result values
   *
   * @return A widened handler
   */

  @SuppressWarnings("unchecked")
  public static <CT, RT, RX extends RT> BTElementHandlerType<CT, RT> widen(
    final BTElementHandlerType<CT, RX> handler)
  {
    return (BTElementHandlerType<CT, RT>) handler;
  }

  /**
   * Functor map for handler constructors.
   *
   * @param constructor The handler constructor
   * @param function    A function to apply to the result of handlers produced by {@code
   *                    constructor}
   * @param <CT>        The type of child handler values
   * @param <RT>        The type of result values
   * @param <RX>        The type of mapped result values
   *
   * @return A new handler constructor
   */

  public static <CT, RT, RX> BTElementHandlerConstructorType<CT, RX> mapConstructor(
    final BTElementHandlerConstructorType<CT, RT> constructor,
    final Function<RT, RX> function)
  {
    return context -> {
      @SuppressWarnings("unchecked") final var newHandler =
        (BTElementHandlerType<CT, RT>) constructor.create(context);
      return (BTElementHandlerType<CT, RX>) map(newHandler, function);
    };
  }

  /**
   * Type widening for handler constructors. If {@code RX <: RT}, then any handler that produces a
   * value of {@code RX} implicitly produces a value of {@code RT}.
   *
   * @param constructor The handler
   * @param <CT>        The type of child handler values
   * @param <RT>        The type of result values
   * @param <RX>        A subtype of result values
   *
   * @return A widened handler constructor
   */

  @SuppressWarnings("unchecked")
  public static <CT, RT, RX extends RT> BTElementHandlerConstructorType<CT, RT> widenConstructor(
    final BTElementHandlerConstructorType<CT, RX> constructor)
  {
    return (BTElementHandlerConstructorType<CT, RT>) constructor;
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

  public static <S> BTElementHandlerConstructorType<?, S> forScalar(
    final BTQualifiedName elementName,
    final BTCharacterHandlerType<S> parser)
  {
    Objects.requireNonNull(elementName, "elementName");
    Objects.requireNonNull(parser, "parser");
    return context -> new BTScalarElementHandler<>(elementName, parser);
  }

  /**
   * A convenience function for constructing content handlers that produce a scalar value from the
   * text content of a single XML element.
   *
   * @param namespaceURI The namespace of the element
   * @param localName    The local element name
   * @param parser       A function that receives text and returns a value of type {@code S}
   * @param <S>          The type of returned scalar values
   *
   * @return A content handler constructor
   */

  public static <S> BTElementHandlerConstructorType<?, S> forScalar(
    final String namespaceURI,
    final String localName,
    final BTCharacterHandlerType<S> parser)
  {
    Objects.requireNonNull(namespaceURI, "namespaceURI");
    Objects.requireNonNull(localName, "localName");
    Objects.requireNonNull(parser, "parser");
    return forScalar(BTQualifiedName.of(namespaceURI, localName), parser);
  }

  /**
   * A convenience function for constructing content handlers that produce a scalar value from the
   * text content of a single XML element.
   *
   * @param elementName The name of the element
   * @param parser      A function that receives attributes and returns a value of type {@code S}
   * @param <S>         The type of returned scalar values
   *
   * @return A content handler constructor
   */

  public static <S> BTElementHandlerConstructorType<?, S> forScalarAttribute(
    final BTQualifiedName elementName,
    final BTAttributesHandlerType<S> parser)
  {
    Objects.requireNonNull(elementName, "elementName");
    Objects.requireNonNull(parser, "parser");
    return context -> new BTScalarAttributeHandler<>(elementName, parser);
  }

  /**
   * A convenience function for constructing content handlers that produce a scalar value from the
   * text content of a single XML element.
   *
   * @param namespaceURI The namespace of the element
   * @param localName    The local element name
   * @param parser       A function that receives attributes and returns a value of type {@code S}
   * @param <S>          The type of returned scalar values
   *
   * @return A content handler constructor
   */

  public static <S> BTElementHandlerConstructorType<?, S> forScalarAttribute(
    final String namespaceURI,
    final String localName,
    final BTAttributesHandlerType<S> parser)
  {
    Objects.requireNonNull(namespaceURI, "namespaceURI");
    Objects.requireNonNull(localName, "localName");
    Objects.requireNonNull(parser, "parser");
    return forScalarAttribute(
      BTQualifiedName.of(namespaceURI, localName),
      parser);
  }

  /**
   * A convenience function for constructing content handlers that produce lists of values from the
   * child elements of a single element. All child elements are expected to be of the same type.
   *
   * @param elementName        The name of the element
   * @param childElementName   The name of the child element
   * @param itemHandler        A handler for child elements
   * @param ignoreUnrecognized Whether or not unrecognized child elements should be ignored
   * @param <S>                The type of returned scalar values
   *
   * @return A content handler constructor
   */

  public static <S> BTElementHandlerConstructorType<S, List<S>> forListMono(
    final BTQualifiedName elementName,
    final BTQualifiedName childElementName,
    final BTElementHandlerConstructorType<?, ? extends S> itemHandler,
    final BTIgnoreUnrecognizedElements ignoreUnrecognized)
  {
    Objects.requireNonNull(elementName, "elementName");
    Objects.requireNonNull(childElementName, "childElementName");
    Objects.requireNonNull(itemHandler, "itemHandler");
    return context ->
      new BTListMonoHandler<>(
        elementName,
        childElementName,
        itemHandler,
        ignoreUnrecognized);
  }

  /**
   * A convenience function for constructing content handlers that produce lists of values from the
   * child elements of a single element. Child elements may be of different types, but values
   * produced by the content handlers for the child elements must have a common supertype.
   *
   * @param elementName        The name of the element
   * @param itemHandlers       Handlers for child elements
   * @param ignoreUnrecognized Whether or not unrecognized child elements should be ignored
   * @param <S>                The type of returned scalar values
   *
   * @return A content handler constructor
   */

  public static <S> BTElementHandlerConstructorType<S, List<S>> forListPoly(
    final BTQualifiedName elementName,
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends S>> itemHandlers,
    final BTIgnoreUnrecognizedElements ignoreUnrecognized)
  {
    Objects.requireNonNull(elementName, "elementName");
    Objects.requireNonNull(itemHandlers, "itemHandlers");
    return context -> new BTListPolyHandler<S>(
      elementName,
      itemHandlers,
      ignoreUnrecognized);
  }

  /**
   * A convenience function for constructing content handlers that produce a scalar value from the
   * text content of a single XML element.
   *
   * @param elementName The name of the element
   *
   * @return A content handler constructor
   */

  public static BTElementHandlerConstructorType<?, String> forScalarString(
    final BTQualifiedName elementName)
  {
    return forScalar(elementName, (context, characters, offset, length) -> {
      // CHECKSTYLE:OFF
      return new String(characters, offset, length);
      // CHECKSTYLE:ON
    });
  }

  /**
   * A convenience function for constructing content handlers that produce a scalar value from the
   * text content of a single XML element.
   *
   * @param namespaceURI The namespace of the element
   * @param localName    The local element name
   *
   * @return A content handler constructor
   */

  public static BTElementHandlerConstructorType<?, String> forScalarString(
    final String namespaceURI,
    final String localName)
  {
    return forScalar(
      namespaceURI,
      localName,
      (context, characters, offset, length) -> {
        // CHECKSTYLE:OFF
        return new String(characters, offset, length);
        // CHECKSTYLE:ON
      });
  }

  /**
   * A convenience function for constructing content handlers that produce a scalar value from the
   * text content of a single XML element.
   *
   * @param elementName The name of the element
   * @param parser      A function from strings to values of type {@code S}
   * @param <S>         The type of returned values
   *
   * @return A content handler constructor
   */

  public static <S> BTElementHandlerConstructorType<?, S> forScalarFromString(
    final BTQualifiedName elementName,
    final Function<String, S> parser)
  {
    return mapConstructor(forScalarString(elementName), parser);
  }

  /**
   * A convenience function for constructing content handlers that produce a scalar value from the
   * text content of a single XML element.
   *
   * @param namespaceURI The namespace of the element
   * @param localName    The local element name
   * @param parser       A function from strings to values of type {@code S}
   * @param <S>          The type of returned values
   *
   * @return A content handler constructor
   */

  public static <S> BTElementHandlerConstructorType<?, S> forScalarFromString(
    final String namespaceURI,
    final String localName,
    final Function<String, S> parser)
  {
    return mapConstructor(forScalarString(namespaceURI, localName), parser);
  }

  /**
   * A convenience method to configure and execute a parser.
   *
   * @param source       The source URI
   * @param stream       The input stream
   * @param xmlReaders   A supplier of XML readers
   * @param rootElements The root element handlers
   * @param <T>          The type of returned values
   *
   * @return The parsed value
   *
   * @throws BTException On parse errors
   */

  public static <T> T parse(
    final URI source,
    final InputStream stream,
    final Callable<XMLReader> xmlReaders,
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, T>> rootElements)
    throws BTException
  {
    final var errors =
      new ArrayList<BTParseError>(32);
    final var contentHandler =
      new BTContentHandler<>(source, errors::add, rootElements);

    try {
      final var reader = xmlReaders.call();
      reader.setContentHandler(contentHandler);
      reader.setErrorHandler(contentHandler);

      final var inputSource = new InputSource(stream);
      inputSource.setPublicId(source.toString());

      reader.parse(inputSource);

      final var resultOpt = contentHandler.result();
      if (resultOpt.isEmpty()) {
        throw new BTException("Parse failed.", new IOException(), errors);
      }

      return resultOpt.get();
    } catch (final SAXParseException e) {
      LOG.error("error encountered during parsing: ", e);

      final var position =
        LexicalPosition.of(
          e.getLineNumber(),
          e.getColumnNumber(),
          Optional.of(source)
        );

      errors.add(
        BTParseError.builder()
          .setLexical(position)
          .setSeverity(ERROR)
          .setMessage(e.getMessage())
          .build()
      );

      throw new BTException(e.getMessage(), e, errors);
    } catch (final Exception e) {
      LOG.error("error encountered during parsing: ", e);

      final var position =
        LexicalPosition.of(-1, -1, Optional.of(source));

      errors.add(
        BTParseError.builder()
          .setLexical(position)
          .setSeverity(ERROR)
          .setMessage(e.getMessage())
          .build()
      );
      throw new BTException(e.getMessage(), e, errors);
    }
  }
}
