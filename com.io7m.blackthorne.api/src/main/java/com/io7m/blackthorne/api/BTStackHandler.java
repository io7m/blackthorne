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

import com.io7m.jaffirm.core.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.Locator2;

import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A stack-based SAX handler.
 *
 * The purpose of this handler is to receive events from a SAX parser. It maintains a stack of
 * elements with associated handlers, and relays calls to the correct handlers when elements are
 * started and completed. It also manages <i>ignoring</i> for elements; when an unrecognized child
 * element is received, and the parent handler specifies that it wants to ignore unrecognized
 * elements, this stack handler is the component responsible for <i>not</i> delivering unrecognized
 * elements to handlers.
 *
 * @param <T> The type of returned values
 */

public final class BTStackHandler<T>
{
  private static final Logger LOG = LoggerFactory.getLogger(BTStackHandler.class);

  private final LinkedList<StackElement> stack;
  private final Context context;
  private final Map<BTQualifiedName, BTElementHandlerConstructorType<?, T>> rootHandlers;
  private boolean failed;
  private T result;

  /**
   * Construct a new stack handler.
   *
   * @param locator2       The underlying document locator
   * @param inRootHandlers A set of root element handlers
   */

  public BTStackHandler(
    final Locator2 locator2,
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, T>> inRootHandlers)
  {
    this.stack = new LinkedList<>();
    this.rootHandlers = Objects.requireNonNull(inRootHandlers, "rootHandlers");
    this.context = new Context(locator2);
  }

  private static String handlerNames(
    final Map<BTQualifiedName, ?> childHandlers)
  {
    return childHandlers.keySet()
      .stream()
      .map(BTQualifiedName::localName)
      .collect(Collectors.joining("|"));
  }

  /**
   * @return The result of parsing, assuming that one was actually produced
   */

  public Optional<? extends T> result()
  {
    return Optional.ofNullable(this.result);
  }

  private void trace(
    final String format,
    final Object... args)
  {
    if (LOG.isTraceEnabled()) {
      final var stackElement = this.stack.peek();
      String name = null;
      if (stackElement != null) {
        name = stackElement.element.localName();
      }

      LOG.trace(
        "[{}][{}]: {}",
        name,
        Integer.valueOf(this.stack.size()),
        String.format(format, args));
    }
  }


  /**
   * An XML element has started.
   *
   * @param namespaceURI The namespace URI
   * @param localName    The local element name
   * @param attributes   The element attributes
   *
   * @throws Exception On parse errors
   */

  public void onElementStarted(
    final String namespaceURI,
    final String localName,
    final Attributes attributes)
    throws Exception
  {
    try {
      Objects.requireNonNull(namespaceURI, "namespaceURI");
      Objects.requireNonNull(localName, "localName");
      Objects.requireNonNull(attributes, "attributes");

      if (this.failed) {
        return;
      }

      final var qualifiedName =
        BTQualifiedName.of(namespaceURI, localName);

      /*
       * If the handler stack is empty, then try to find a handler suitable for use as a root
       * node. If one doesn't exist, fail.
       */

      if (this.stack.isEmpty()) {
        this.trace(
          "creating root handler for %s:%s",
          qualifiedName.namespaceURI(),
          qualifiedName.localName());

        final var rootHandlerConstructor = this.rootHandlers.get(qualifiedName);
        if (rootHandlerConstructor == null) {
          throw new SAXParseException(
            BTMessages.format(
              "errorRootElementNotAllowed",
              localName,
              namespaceURI),
            this.context.documentLocator());
        }

        final var handler = rootHandlerConstructor.create(this.context);
        this.trace("pushing root handler %s", handler.name());
        this.stack.push(new StackElement(qualifiedName, handler));
        handler.onElementStart(this.context, attributes);
        return;
      }

      /*
       * Otherwise, find the topmost stack element. If the topmost element has no handler,
       * then this means that some earlier handler wanted to ignore child elements. Push
       * the element onto the stack but don't otherwise do anything with it.
       */

      final var topMost = this.stack.peek();
      final var topMostHandler = topMost.handler;
      if (topMostHandler == null) {
        this.trace("pushing ignored element %s", qualifiedName.localName());
        this.stack.push(new StackElement(qualifiedName, null));
        return;
      }

      /*
       * If there is a handler on top of the stack, then ask it for child element handlers.
       */

      final var childHandlers =
        topMostHandler.onChildHandlersRequested(this.context);
      final var childHandlerConstructor =
        childHandlers.get(qualifiedName);

      /*
       * If the handler didn't provide a child element handler that can handle the current
       * element, and isn't prepared to ignore child elements, then fail.
       */

      if (childHandlerConstructor == null) {
        switch (topMostHandler.onShouldIgnoreUnrecognizedElements(this.context)) {
          case IGNORE_UNRECOGNIZED_ELEMENTS: {
            this.trace("pushing ignored element %s", qualifiedName.localName());
            this.stack.push(new StackElement(qualifiedName, null));
            return;
          }
          case DO_NOT_IGNORE_UNRECOGNIZED_ELEMENTS: {
            throw new SAXParseException(
              BTMessages.format(
                "errorHandlerUnrecognizedElement",
                topMostHandler.getClass().getCanonicalName(),
                namespaceURI,
                localName,
                handlerNames(childHandlers)),
              this.context.documentLocator());
          }
        }
      }

      /*
       * Otherwise, create a new handler, push it onto the stack, and tell it that an
       * element started.
       */

      final var newHandler =
        Objects.requireNonNull(
          childHandlerConstructor,
          "childHandlerConstructor")
          .create(this.context);

      Objects.requireNonNull(newHandler, "newHandler");
      this.trace("pushing handler %s", newHandler.name());
      this.stack.push(new StackElement(qualifiedName, newHandler));
      newHandler.onElementStart(this.context, attributes);
    } catch (final Exception e) {
      this.failed = true;
      throw e;
    }
  }

  /**
   * Text has been received inside an element.
   *
   * @param data   A character array
   * @param offset The starting offset into {@code data}
   * @param length The length of the data in {@code data}
   *
   * @throws Exception On parse errors
   */

  public void onCharacters(
    final char[] data,
    final int offset,
    final int length)
    throws Exception
  {
    try {
      Objects.requireNonNull(data, "data");

      if (this.failed) {
        return;
      }

      Preconditions.checkPrecondition(
        !this.stack.isEmpty(),
        "Handler stack cannot be empty");

      final var topMost = this.stack.peek();

      @SuppressWarnings("unchecked") final var topMostHandler =
        (BTElementHandlerType<Object, Object>) topMost.handler;

      if (topMostHandler == null) {
        return;
      }

      topMostHandler.onCharacters(this.context, data, offset, length);
    } catch (final Exception e) {
      this.failed = true;
      throw e;
    }
  }

  /**
   * An XML element has finished.
   *
   * @param namespaceURI The namespace URI
   * @param localName    The local element name
   *
   * @throws Exception On parse errors
   */

  public void onElementFinished(
    final String namespaceURI,
    final String localName)
    throws Exception
  {
    try {
      Objects.requireNonNull(namespaceURI, "namespaceURI");
      Objects.requireNonNull(localName, "localName");

      if (this.failed) {
        return;
      }

      Preconditions.checkPrecondition(
        !this.stack.isEmpty(),
        "Handler stack cannot be empty");

      final var topMost = this.stack.peek();

      @SuppressWarnings("unchecked") final var topMostHandler =
        (BTElementHandlerType<Object, Object>) topMost.handler;

      if (topMostHandler == null) {
        this.trace("popping ignored element %s", topMost.element.localName());
        this.stack.pop();
        return;
      }

      final var childResult = topMostHandler.onElementFinished(this.context);
      this.trace("popping element with handler %s", topMostHandler.name());
      this.stack.pop();

      final var parentElement = this.stack.peek();
      if (parentElement == null) {
        @SuppressWarnings("unchecked") final var castResult = (T) childResult;
        this.result = castResult;
        return;
      }

      @SuppressWarnings("unchecked") final var parentHandler =
        (BTElementHandlerType<Object, Object>) parentElement.handler;
      if (parentHandler != null) {
        parentHandler.onChildValueProduced(this.context, childResult);
        return;
      }
    } catch (final Exception e) {
      this.failed = true;
      throw e;
    }
  }

  private static final class StackElement
  {
    private final BTQualifiedName element;
    private final BTElementHandlerType<?, ?> handler;

    StackElement(
      final BTQualifiedName inElement,
      final BTElementHandlerType<?, ?> inHandler)
    {
      this.element = Objects.requireNonNull(inElement, "element");
      this.handler = inHandler;
    }
  }

  private static final class Context implements BTElementParsingContextType
  {
    private final Locator2 locator2;

    private Context(final Locator2 inLocator)
    {
      this.locator2 = Objects.requireNonNull(inLocator, "locator2");
    }

    @Override
    public Locator2 documentLocator()
    {
      return this.locator2;
    }

    @Override
    public SAXParseException parseException(
      final Exception e)
    {
      return new SAXParseException(e.getLocalizedMessage(), this.locator2, e);
    }
  }
}
