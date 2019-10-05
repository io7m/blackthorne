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
 * A builder for versioned dispatching handlers.
 *
 * @param <T> The type of returned values
 */

public interface BTVersionedDispatchingHandlerBuilderType<T>
{
  /**
   * Remove any registered handlers for the given namespace URI.
   *
   * @param uri The namespace URI
   *
   * @return The current builder
   */

  BTVersionedDispatchingHandlerBuilderType<T> removeHandler(
    URI uri);

  /**
   * Remove any registered handlers for the given namespace URI.
   *
   * @param uri The namespace URI
   *
   * @return The current builder
   */

  default BTVersionedDispatchingHandlerBuilderType<T> removeHandler(
    final String uri)
  {
    return this.removeHandler(URI.create(uri));
  }

  /**
   * Add a handler for the given namespace URI.
   *
   * @param uri     The namespace URI
   * @param handler A handler that will handle documents with the given namespace
   *
   * @return The current builder
   */

  BTVersionedDispatchingHandlerBuilderType<T> addHandler(
    URI uri,
    BTContentHandlerConstructorType<? extends T> handler);

  /**
   * Add a handler for the given namespace URI.
   *
   * @param uri     The namespace URI
   * @param handler A handler that will handle documents with the given namespace
   *
   * @return The current builder
   */

  default BTVersionedDispatchingHandlerBuilderType<T> addHandler(
    final String uri,
    final BTContentHandlerConstructorType<? extends T> handler)
  {
    return this.addHandler(URI.create(uri), handler);
  }

  /**
   * @param fileURI       The URI of the file being parsed
   * @param errorConsumer A consumer of errors
   *
   * @return A new handler based on all of the values given so far
   */

  BTVersionedDispatchingHandler<T> build(
    URI fileURI,
    Consumer<BTParseError> errorConsumer);
}
