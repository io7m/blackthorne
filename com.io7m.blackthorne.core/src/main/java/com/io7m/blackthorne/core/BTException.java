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

package com.io7m.blackthorne.core;

import com.io7m.seltzer.api.SStructuredErrorExceptionType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * An exception raised by convenience methods.
 */

public final class BTException extends Exception
  implements SStructuredErrorExceptionType<String>
{
  private final List<BTParseError> errors;
  private final String errorCode;
  private final Map<String, String> attributes;
  private final Optional<String> remediatingAction;

  /**
   * Construct an exception.
   *
   * @param message             The message
   * @param inErrorCode         The error code
   * @param inAttributes        The error attributes
   * @param inRemediatingAction The remediating action, if any
   * @param inErrors            The parse errors
   */

  public BTException(
    final String message,
    final String inErrorCode,
    final Map<String, String> inAttributes,
    final Optional<String> inRemediatingAction,
    final List<BTParseError> inErrors)
  {
    super(Objects.requireNonNull(message, "message"));

    this.errors =
      List.copyOf(Objects.requireNonNull(inErrors, "errors"));
    this.errorCode =
      Objects.requireNonNull(inErrorCode, "errorCode");
    this.attributes =
      Map.copyOf(Objects.requireNonNull(inAttributes, "attributes"));
    this.remediatingAction =
      Objects.requireNonNull(inRemediatingAction, "remediatingAction");
  }

  /**
   * Construct an exception.
   *
   * @param message             The message
   * @param cause               The cause
   * @param inErrorCode         The error code
   * @param inAttributes        The error attributes
   * @param inRemediatingAction The remediating action, if any
   * @param inErrors            The parse errors
   */

  public BTException(
    final String message,
    final Throwable cause,
    final String inErrorCode,
    final Map<String, String> inAttributes,
    final Optional<String> inRemediatingAction,
    final List<BTParseError> inErrors)
  {
    super(
      Objects.requireNonNull(message, "message"),
      Objects.requireNonNull(cause, "cause")
    );

    this.errors =
      List.copyOf(Objects.requireNonNull(inErrors, "errors"));
    this.errorCode =
      Objects.requireNonNull(inErrorCode, "errorCode");
    this.attributes =
      Map.copyOf(Objects.requireNonNull(inAttributes, "attributes"));
    this.remediatingAction =
      Objects.requireNonNull(inRemediatingAction, "remediatingAction");
  }

  /**
   * Construct an exception.
   *
   * @param message      The message
   * @param inErrorCode  The error code
   * @param inAttributes The error attributes
   * @param inErrors     The parse errors
   */

  public BTException(
    final String message,
    final String inErrorCode,
    final Map<String, String> inAttributes,
    final List<BTParseError> inErrors)
  {
    this(
      message,
      inErrorCode,
      inAttributes,
      Optional.empty(),
      inErrors
    );
  }

  /**
   * Construct an exception.
   *
   * @param message     The message
   * @param inErrorCode The error code
   * @param inErrors    The parse errors
   */

  public BTException(
    final String message,
    final String inErrorCode,
    final List<BTParseError> inErrors)
  {
    this(
      message,
      inErrorCode,
      Map.of(),
      Optional.empty(),
      inErrors
    );
  }

  @Override
  public String errorCode()
  {
    return this.errorCode;
  }

  @Override
  public Map<String, String> attributes()
  {
    return this.attributes;
  }

  @Override
  public Optional<String> remediatingAction()
  {
    return this.remediatingAction;
  }

  @Override
  public Optional<Throwable> exception()
  {
    return Optional.of(this);
  }

  /**
   * @return The parse errors encountered during parsing
   */

  public List<BTParseError> errors()
  {
    return List.copyOf(this.errors);
  }
}
