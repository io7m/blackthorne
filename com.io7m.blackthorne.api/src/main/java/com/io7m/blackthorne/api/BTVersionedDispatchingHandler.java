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

import com.io7m.blackthorne.api.BTParseErrorType.Severity;
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
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A dispatching handler that produces values of type {@code T}. The handler is responsible
 * for instantiating a content handler based on the received document namespace URI.
 *
 * @param <T> The type of returned values
 */

public final class BTVersionedDispatchingHandler<T> extends DefaultHandler2
{
  private static final Logger LOG = LoggerFactory.getLogger(BTVersionedDispatchingHandler.class);

  private final URI fileURI;
  private final Consumer<BTParseError> errorReceiver;
  private final Map<URI, BTContentHandlerConstructorType<? extends T>> handlers;
  private boolean failed;
  private BTContentHandlerType<? extends T> handler;
  private Locator2 locator;

  /**
   * Construct a new versioned dispatching handler builder.
   *
   * @param <T> The type of returned values
   *
   * @return A new builder
   */

  public static <T> BTVersionedDispatchingHandlerBuilderType<T> builder()
  {
    return new Builder<>();
  }

  /**
   * Construct a versioned handler.
   *
   * @param inFileURI       The URI of the file being parsed
   * @param inErrorReceiver A receiver of error events
   * @param inHandlers      The content handlers
   */

  public BTVersionedDispatchingHandler(
    final URI inFileURI,
    final Consumer<BTParseError> inErrorReceiver,
    final Map<URI, BTContentHandlerConstructorType<? extends T>> inHandlers)
  {
    this.fileURI =
      Objects.requireNonNull(inFileURI, "fileURI");
    this.errorReceiver =
      Objects.requireNonNull(inErrorReceiver, "errorReceiver");
    this.handlers =
      Objects.requireNonNull(inHandlers, "handlers");
    this.failed = false;
  }

  @Override
  public void startPrefixMapping(
    final String prefix,
    final String uri)
  {
    if (LOG.isTraceEnabled()) {
      LOG.trace("startPrefixMapping: {} {}", prefix, uri);
    }

    final URI receivedURI;
    try {
      receivedURI = new URI(uri);
    } catch (final URISyntaxException e) {
      this.error(new SAXParseException(
        BTMessages.format("errorParsingNamespace", uri),
        null,
        uri,
        1,
        0,
        e));
      return;
    }

    final var handlerSupplier = this.handlers.get(receivedURI);
    if (handlerSupplier == null) {
      this.error(new SAXParseException(
        BTMessages.format("errorUnrecognizedNamespace", uri),
        null,
        uri,
        1,
        0));
      return;
    }

    this.handler = handlerSupplier.create(this.locator);
  }

  @Override
  public void startElement(
    final String namespace_uri,
    final String local_name,
    final String qualified_name,
    final Attributes attributes)
    throws SAXException
  {
    if (LOG.isTraceEnabled()) {
      LOG.trace("startElement: {} {} {}", namespace_uri, local_name, qualified_name);
    }

    if (this.handler == null) {
      this.error(new SAXParseException(
        BTMessages.format("errorNoUsableNamespace"), this.locator));
      return;
    }

    if (this.failed()) {
      return;
    }

    this.handler.onElementStarted(namespace_uri, local_name, qualified_name, attributes);
  }

  @Override
  public void endElement(
    final String namespace_uri,
    final String local_name,
    final String qualified_name)
    throws SAXException
  {
    if (LOG.isTraceEnabled()) {
      LOG.trace("endElement:   {} {} {}", namespace_uri, local_name, qualified_name);
    }

    if (this.failed()) {
      return;
    }

    this.handler.onElementFinished(namespace_uri, local_name, qualified_name);
  }

  @Override
  public void characters(
    final char[] ch,
    final int start,
    final int length)
    throws SAXException
  {
    if (this.failed) {
      return;
    }

    this.handler.onCharacters(ch, start, length);
  }

  @Override
  public void setDocumentLocator(
    final Locator in_locator)
  {
    this.locator = (Locator2) Objects.requireNonNull(in_locator, "locator");
  }

  @Override
  public void warning(
    final SAXParseException e)
  {
    this.errorReceiver.accept(
      BTParseError.builder()
        .setException(e)
        .setSeverity(Severity.WARNING)
        .setMessage(e.getMessage())
        .setLexical(LexicalPosition.<URI>builder()
                      .setColumn(this.locator.getColumnNumber())
                      .setLine(this.locator.getLineNumber())
                      .setFile(this.fileURI)
                      .build())
        .build());
  }

  @Override
  public void error(
    final SAXParseException e)
  {
    this.failed = true;
    this.errorReceiver.accept(
      BTParseError.builder()
        .setException(e)
        .setSeverity(Severity.ERROR)
        .setMessage(e.getMessage())
        .setLexical(LexicalPosition.<URI>builder()
                      .setColumn(this.locator.getColumnNumber())
                      .setLine(this.locator.getLineNumber())
                      .setFile(this.fileURI)
                      .build())
        .build());
  }

  @Override
  public void fatalError(
    final SAXParseException e)
    throws SAXException
  {
    this.failed = true;
    this.errorReceiver.accept(
      BTParseError.builder()
        .setException(e)
        .setSeverity(Severity.ERROR)
        .setMessage(e.getMessage())
        .setLexical(LexicalPosition.<URI>builder()
                      .setColumn(this.locator.getColumnNumber())
                      .setLine(this.locator.getLineNumber())
                      .setFile(this.fileURI)
                      .build())
        .build());
    throw e;
  }

  /**
   * @return The parsed value
   */

  public T result()
  {
    if (this.failed()) {
      throw new IllegalStateException(BTMessages.format("errorParserDidNotComplete"));
    }

    return this.handler.get();
  }

  /**
   * @return {@code true} if any parse errors were encountered
   */

  public boolean failed()
  {
    return this.failed;
  }

  private static final class Builder<T> implements BTVersionedDispatchingHandlerBuilderType<T>
  {
    private final Map<URI, BTContentHandlerConstructorType<? extends T>> handlers;

    Builder()
    {
      this.handlers = new HashMap<>();
    }

    @Override
    public BTVersionedDispatchingHandlerBuilderType<T> removeHandler(
      final URI uri)
    {
      Objects.requireNonNull(uri, "uri");

      this.handlers.remove(uri);
      return this;
    }

    @Override
    public BTVersionedDispatchingHandlerBuilderType<T> addHandler(
      final URI uri,
      final BTContentHandlerConstructorType<? extends T> handler)
    {
      Objects.requireNonNull(uri, "uri");
      Objects.requireNonNull(handler, "handler");

      this.handlers.put(uri, handler);
      return this;
    }

    @Override
    public BTVersionedDispatchingHandler<T> build(
      final URI fileURI,
      final Consumer<BTParseError> errorConsumer)
    {
      return new BTVersionedDispatchingHandler<>(fileURI, errorConsumer, Map.copyOf(this.handlers));
    }
  }
}
