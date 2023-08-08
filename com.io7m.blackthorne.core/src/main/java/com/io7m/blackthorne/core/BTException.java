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

import java.util.List;
import java.util.Objects;

/**
 * An exception raised by convenience methods.
 */

public final class BTException extends Exception
{
  private final List<BTParseError> errors;

  /**
   * Construct an exception.
   *
   * @param inMessage The exception message
   * @param inErrors  The parse errors
   */

  public BTException(
    final String inMessage,
    final List<BTParseError> inErrors)
  {
    super(Objects.requireNonNull(inMessage, "message"));
    this.errors = Objects.requireNonNull(inErrors, "errors");
  }

  /**
   * Construct an exception.
   *
   * @param inMessage The exception message
   * @param inCause   The cause
   * @param inErrors  The parse errors
   */

  public BTException(
    final String inMessage,
    final Throwable inCause,
    final List<BTParseError> inErrors)
  {
    super(
      Objects.requireNonNull(inMessage, "message"),
      Objects.requireNonNull(inCause, "cause"));
    this.errors = Objects.requireNonNull(inErrors, "errors");
  }

  /**
   * Construct an exception.
   *
   * @param inCause  The cause
   * @param inErrors The parse errors
   */

  public BTException(
    final Throwable inCause,
    final List<BTParseError> inErrors)
  {
    super(Objects.requireNonNull(inCause, "cause"));
    this.errors = Objects.requireNonNull(inErrors, "errors");
  }

  /**
   * @return The parse errors encountered during parsing
   */

  public List<BTParseError> errors()
  {
    return List.copyOf(this.errors);
  }
}
