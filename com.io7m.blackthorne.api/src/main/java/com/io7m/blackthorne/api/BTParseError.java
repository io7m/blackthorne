/*
 * Copyright © 2023 Mark Raynsford <code@io7m.com> https://www.io7m.com
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
import com.io7m.jlexing.core.LexicalType;
import com.io7m.seltzer.api.SStructuredErrorType;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The type of parse errors.
 *
 * @param severity          The error severity
 * @param lexical           The lexical information
 * @param errorCode         The error code
 * @param message           The error message
 * @param attributes        The error attributes
 * @param remediatingAction The remediating action
 * @param exception         The exception
 */

public record BTParseError(
  LexicalPosition<URI> lexical,
  Severity severity,
  String errorCode,
  String message,
  Map<String, String> attributes,
  Optional<String> remediatingAction,
  Optional<Throwable> exception)
  implements LexicalType<URI>, SStructuredErrorType<String>
{
  /**
   * The type of parse errors.
   *
   * @param severity          The error severity
   * @param lexical           The lexical information
   * @param errorCode         The error code
   * @param message           The error message
   * @param attributes        The error attributes
   * @param remediatingAction The remediating action
   * @param exception         The exception
   */

  public BTParseError
  {
    Objects.requireNonNull(severity, "severity");
    Objects.requireNonNull(lexical, "lexical");
    Objects.requireNonNull(errorCode, "errorCode");
    Objects.requireNonNull(message, "message");
    Objects.requireNonNull(attributes, "attributes");
    Objects.requireNonNull(remediatingAction, "remediatingAction");
    Objects.requireNonNull(exception, "exception");
  }

  /**
   * The error severity
   */

  enum Severity
  {
    /**
     * The error is just a warning
     */

    WARNING,

    /**
     * The error is an error! Parsing as a whole will be considered to have failed.
     */

    ERROR
  }
}
