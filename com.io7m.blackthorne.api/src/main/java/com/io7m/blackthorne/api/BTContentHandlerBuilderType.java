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

import java.net.URI;
import java.util.function.Consumer;

/**
 * A builder for SAX content handlers.
 *
 * @param <T> The type of returned values
 */

public interface BTContentHandlerBuilderType<T>
{
  /**
   * Add a handler for root elements with {@code name}.
   *
   * @param name        The fully-qualified root element name
   * @param constructor The handler constructor
   *
   * @return this
   */

  BTContentHandlerBuilderType<T> addHandler(
    BTQualifiedName name,
    BTElementHandlerConstructorType<?, T> constructor);

  /**
   * Add a handler for root elements with {@code name}.
   *
   * @param namespaceURI The element namespace
   * @param localName    The local name
   * @param constructor  The handler constructor
   *
   * @return this
   */

  default BTContentHandlerBuilderType<T> addHandler(
    final String namespaceURI,
    final String localName,
    final BTElementHandlerConstructorType<?, T> constructor)
  {
    return this.addHandler(BTQualifiedName.of(namespaceURI, localName), constructor);
  }

  /**
   * Build a content handler.
   *
   * @param fileURI       The URI of the file to be parsed
   * @param errorConsumer A consumer of errors
   *
   * @return A content handler
   */

  BTContentHandler<T> build(
    URI fileURI,
    Consumer<BTParseError> errorConsumer);
}
