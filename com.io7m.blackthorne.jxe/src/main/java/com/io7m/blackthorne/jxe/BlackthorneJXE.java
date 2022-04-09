/*
 * Copyright © 2020 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.blackthorne.jxe;

import com.io7m.blackthorne.api.BTElementHandlerConstructorType;
import com.io7m.blackthorne.api.BTException;
import com.io7m.blackthorne.api.BTQualifiedName;
import com.io7m.blackthorne.api.Blackthorne;
import com.io7m.jxe.core.JXEHardenedSAXParsers;
import com.io7m.jxe.core.JXESchemaResolutionMappings;
import com.io7m.jxe.core.JXEXInclude;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.io7m.jxe.core.JXEXInclude.XINCLUDE_DISABLED;

/**
 * Blackthorne JXE integration.
 */

public final class BlackthorneJXE
{
  private static final JXEHardenedSAXParsers PARSERS =
    new JXEHardenedSAXParsers();

  private BlackthorneJXE()
  {

  }

  /**
   * Parse a document.
   *
   * @param source        The source URI
   * @param stream        The input stream
   * @param parsers       A supplier of JXE hardened parsers
   * @param rootElements  The root element handlers
   * @param baseDirectory The base directory
   * @param xinclude      The xinclude configuration
   * @param schemas       The schemas
   * @param <T>           The type of returned values
   *
   * @return A parsed value
   *
   * @throws BTException On parse errors
   */

  public static <T> T parseAll(
    final URI source,
    final InputStream stream,
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, T>> rootElements,
    final JXEHardenedSAXParsers parsers,
    final Optional<Path> baseDirectory,
    final JXEXInclude xinclude,
    final JXESchemaResolutionMappings schemas)
    throws BTException
  {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(stream, "stream");
    Objects.requireNonNull(parsers, "parsers");
    Objects.requireNonNull(rootElements, "rootElements");
    Objects.requireNonNull(baseDirectory, "baseDirectory");
    Objects.requireNonNull(xinclude, "xinclude");
    Objects.requireNonNull(schemas, "schemas");

    return Blackthorne.parse(
      source,
      stream,
      () -> parsers.createXMLReader(baseDirectory, xinclude, schemas),
      rootElements
    );
  }

  /**
   * Parse a document. A default provider of hardened SAX parsers will be used.
   *
   * @param source        The source URI
   * @param stream        The input stream
   * @param rootElements  The root element handlers
   * @param baseDirectory The base directory
   * @param xinclude      The xinclude configuration
   * @param schemas       The schemas
   * @param <T>           The type of returned values
   *
   * @return A parsed value
   *
   * @throws BTException On parse errors
   */

  public static <T> T parse(
    final URI source,
    final InputStream stream,
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, T>> rootElements,
    final Optional<Path> baseDirectory,
    final JXEXInclude xinclude,
    final JXESchemaResolutionMappings schemas)
    throws BTException
  {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(stream, "stream");
    Objects.requireNonNull(rootElements, "rootElements");
    Objects.requireNonNull(baseDirectory, "baseDirectory");
    Objects.requireNonNull(xinclude, "xinclude");
    Objects.requireNonNull(schemas, "schemas");

    return parseAll(
      source, stream, rootElements, PARSERS, baseDirectory, xinclude, schemas
    );
  }

  /**
   * Parse a document. A default provider of hardened SAX parsers will be used.
   * No filesystem access is allowed.
   *
   * @param source       The source URI
   * @param stream       The input stream
   * @param rootElements The root element handlers
   * @param xinclude     The xinclude configuration
   * @param schemas      The schemas
   * @param <T>          The type of returned values
   *
   * @return A parsed value
   *
   * @throws BTException On parse errors
   */

  public static <T> T parse(
    final URI source,
    final InputStream stream,
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, T>> rootElements,
    final JXEXInclude xinclude,
    final JXESchemaResolutionMappings schemas)
    throws BTException
  {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(stream, "stream");
    Objects.requireNonNull(rootElements, "rootElements");
    Objects.requireNonNull(xinclude, "xinclude");
    Objects.requireNonNull(schemas, "schemas");

    return parseAll(
      source, stream, rootElements, PARSERS, Optional.empty(), xinclude, schemas
    );
  }

  /**
   * Parse a document. A default provider of hardened SAX parsers will be used.
   * No filesystem access is allowed. No XInclude is allowed.
   *
   * @param source       The source URI
   * @param stream       The input stream
   * @param rootElements The root element handlers
   * @param schemas      The schemas
   * @param <T>          The type of returned values
   *
   * @return A parsed value
   *
   * @throws BTException On parse errors
   */

  public static <T> T parse(
    final URI source,
    final InputStream stream,
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, T>> rootElements,
    final JXESchemaResolutionMappings schemas)
    throws BTException
  {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(stream, "stream");
    Objects.requireNonNull(rootElements, "rootElements");
    Objects.requireNonNull(schemas, "schemas");

    return parseAll(
      source,
      stream,
      rootElements,
      PARSERS,
      Optional.empty(),
      XINCLUDE_DISABLED,
      schemas
    );
  }
}
