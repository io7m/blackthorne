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

import com.io7m.jlexing.core.LexicalPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.ext.Locator2;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static com.io7m.blackthorne.api.BTParseError.Severity.ERROR;
import static com.io7m.blackthorne.api.BTParseError.Severity.WARNING;

/**
 * A dispatching handler that produces values of type {@code T}. The handler is responsible for
 * instantiating a content handler based on the received document namespace URI.
 *
 * @param <T> The type of returned values
 */

public final class BTContentHandler<T> extends DefaultHandler2
{
  private static final Logger LOG =
    LoggerFactory.getLogger(BTContentHandler.class);

  private final URI fileURI;
  private final Consumer<BTParseError> errorReceiver;
  private Locator2 locator;
  private Map<BTQualifiedName, BTElementHandlerConstructorType<?, T>> rootHandlers;
  private BTStackHandler<T> stackHandler;
  private boolean failed;

  /**
   * Construct a handler.
   *
   * @param inFileURI       The URI of the file being parsed
   * @param inErrorReceiver A receiver of error events
   * @param inRootHandlers  The root handlers
   */

  public BTContentHandler(
    final URI inFileURI,
    final Consumer<BTParseError> inErrorReceiver,
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, T>> inRootHandlers)
  {
    this.fileURI =
      Objects.requireNonNull(inFileURI, "fileURI");
    this.errorReceiver =
      Objects.requireNonNull(inErrorReceiver, "errorReceiver");
    this.rootHandlers =
      Map.copyOf(Objects.requireNonNull(inRootHandlers, "handlers"));
  }

  /**
   * Create a new content handler builder.
   *
   * @param <U> The type of resulting values
   *
   * @return A new builder
   */

  public static <U> BTContentHandlerBuilderType<U> builder()
  {
    return new Builder<>();
  }

  /**
   * Create a new content handler builder.
   *
   * @param clazz A class value used solely to aid type inference; otherwise ignored
   * @param <U>   The type of resulting values
   *
   * @return A new builder
   */

  public static <U> BTContentHandlerBuilderType<U> builder(
    final Class<U> clazz)
  {
    return new Builder<>();
  }

  private static String messageOrException(
    final Exception e)
  {
    final var messageOrNull = e.getMessage();
    if (messageOrNull == null) {
      return String.format(
        "No error message provided for exception %s",
        e.getClass().getName());
    }
    return messageOrNull;
  }

  @Override
  public void setDocumentLocator(
    final Locator in_locator)
  {
    this.locator =
      (Locator2) Objects.requireNonNull(in_locator, "locator");
    this.stackHandler =
      new BTStackHandler<>(this.locator, this.rootHandlers);
  }

  @Override
  public void startElement(
    final String namespaceURI,
    final String localName,
    final String qualifiedName,
    final Attributes attributes)
  {
    try {
      this.stackHandler.onElementStarted(namespaceURI, localName, attributes);
    } catch (final SAXParseException e) {
      this.error(e);
    } catch (final Exception e) {
      this.error(this.saxParseExceptionOf(e));
    }
  }

  @Override
  public void endElement(
    final String namespaceURI,
    final String localName,
    final String qualifiedName)
  {
    try {
      this.stackHandler.onElementFinished(namespaceURI, localName);
    } catch (final SAXParseException e) {
      this.error(e);
    } catch (final Exception e) {
      this.error(this.saxParseExceptionOf(e));
    }
  }

  @Override
  public void characters(
    final char[] ch,
    final int start,
    final int length)
  {
    try {
      this.stackHandler.onCharacters(ch, start, length);
    } catch (final SAXParseException e) {
      this.error(e);
    } catch (final Exception e) {
      this.error(this.saxParseExceptionOf(e));
    }
  }

  @Override
  public void warning(
    final SAXParseException e)
  {
    LOG.warn("parse exception: ", e);

    this.errorReceiver.accept(
      new BTParseError(
        this.currentLexical(),
        WARNING,
        "sax-warning",
        messageOrException(e),
        Map.of(),
        Optional.empty(),
        Optional.of(e)
      )
    );
  }

  @Override
  public void error(
    final SAXParseException e)
  {
    LOG.error("parse exception: ", e);

    this.failed = true;
    this.errorReceiver.accept(
      new BTParseError(
        this.currentLexical(),
        ERROR,
        "sax-error",
        messageOrException(e),
        Map.of(),
        Optional.empty(),
        Optional.of(e)
      )
    );
  }

  @Override
  public void fatalError(
    final SAXParseException e)
    throws SAXException
  {
    LOG.error("fatal parse exception: ", e);

    this.failed = true;
    this.errorReceiver.accept(
      new BTParseError(
        this.currentLexical(),
        ERROR,
        "sax-fatal-error",
        messageOrException(e),
        Map.of(),
        Optional.empty(),
        Optional.of(e)
      )
    );

    throw e;
  }

  private LexicalPosition<URI> currentLexical()
  {
    final LexicalPosition<URI> lexicalPosition;
    final var locateNow = this.locator;
    if (locateNow != null) {
      lexicalPosition =
        LexicalPosition.<URI>builder()
          .setColumn(locateNow.getColumnNumber())
          .setLine(locateNow.getLineNumber())
          .setFile(this.fileURI)
          .build();
    } else {
      lexicalPosition =
        LexicalPosition.<URI>builder()
          .setColumn(0)
          .setLine(0)
          .setFile(this.fileURI)
          .build();
    }
    return lexicalPosition;
  }

  private SAXParseException saxParseExceptionOf(
    final Exception e)
  {
    return new SAXParseException(e.getLocalizedMessage(), this.locator, e);
  }

  /**
   * @return The parsed value
   */

  public Optional<? extends T> result()
  {
    return this.stackHandler.result();
  }

  /**
   * @return {@code true} if any parse errors were encountered
   */

  public boolean failed()
  {
    return this.failed;
  }

  private static final class Builder<U> implements BTContentHandlerBuilderType<U>
  {
    private final HashMap<BTQualifiedName, BTElementHandlerConstructorType<?, U>> handlers;

    private Builder()
    {
      this.handlers = new HashMap<>(16);
    }

    @Override
    public BTContentHandlerBuilderType<U> addHandler(
      final BTQualifiedName name,
      final BTElementHandlerConstructorType<?, U> constructor)
    {
      this.handlers.put(
        Objects.requireNonNull(name, "name"),
        Objects.requireNonNull(constructor, "constructor"));
      return this;
    }

    @Override
    public BTContentHandler<U> build(
      final URI fileURI,
      final Consumer<BTParseError> errorConsumer)
    {
      return new BTContentHandler<>(
        Objects.requireNonNull(fileURI, "fileURI"),
        Objects.requireNonNull(errorConsumer, "errorConsumer"),
        Map.copyOf(this.handlers));
    }
  }
}
