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

package com.io7m.blackthorne.tests;

import com.io7m.blackthorne.api.BTAcceptCharacters;
import com.io7m.blackthorne.api.BTContentHandlerAbstract;
import com.io7m.blackthorne.api.BTContentHandlerConstructorType;
import com.io7m.blackthorne.api.BTMessages;
import com.io7m.blackthorne.api.BTParseError;
import com.io7m.blackthorne.api.BTVersionedDispatchingHandler;
import com.io7m.blackthorne.api.Blackthorne;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.Locator2;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Tests for the API.
 */

public final class BlackthorneTest
{
  private static XMLReader createReader()
    throws Exception
  {
    final var parsers = SAXParserFactory.newInstance();
    parsers.setNamespaceAware(true);
    parsers.setXIncludeAware(false);
    parsers.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    final var parser = parsers.newSAXParser();
    return parser.getXMLReader();
  }

  private static InputSource resource(
    final String name)
    throws Exception
  {
    final var url =
      BlackthorneTest.class.getResource("/com/io7m/blackthorne/tests/" + name);

    final var stream = url.openStream();
    final var source = new InputSource(stream);
    source.setPublicId(url.toString());
    return source;
  }

  /**
   * A document that doesn't contain a usable schema declaration is unusable.
   *
   * @throws Exception On errors
   */

  @Test
  public void testNoSchema()
    throws Exception
  {
    final var errors = new ArrayList<BTParseError>();
    final var handler =
      new BTVersionedDispatchingHandler<Integer>(URI.create("urn:text"), errors::add, Map.of());

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("no_namespace.xml"));

    Assertions.assertTrue(handler.failed());
    Assertions.assertThrows(IllegalStateException.class, handler::result);
    Assertions.assertEquals(1, errors.size());

    {
      final var error = errors.remove(0);
      Assertions.assertTrue(error.message().startsWith(BTMessages.format("errorNoUsableNamespace")));
    }
  }

  /**
   * A document that doesn't contain a known schema declaration is unusable.
   *
   * @throws Exception On errors
   */

  @Test
  public void testUnknownSchema()
    throws Exception
  {
    final var errors = new ArrayList<BTParseError>();
    final var handler =
      new BTVersionedDispatchingHandler<Integer>(URI.create("urn:text"), errors::add, Map.of());

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("unknown_namespace.xml"));

    Assertions.assertTrue(handler.failed());
    Assertions.assertThrows(IllegalStateException.class, handler::result);
    Assertions.assertEquals(2, errors.size());

    {
      final var error = errors.remove(0);
      Assertions.assertTrue(error.message().startsWith("Unrecognized"));
    }

    {
      final var error = errors.remove(0);
      Assertions.assertTrue(error.message().startsWith(BTMessages.format("errorNoUsableNamespace")));
    }
  }

  /**
   * Integer values are parsed correctly.
   *
   * @throws Exception On errors
   */

  @Test
  public void testInteger0()
    throws Exception
  {
    final var errors = new ArrayList<BTParseError>();

    final var handler =
      Blackthorne.<Number>versionedDispatching()
        .addHandler("urn:tests", Blackthorne.widenConstructor(IntHandler::new))
        .build(URI.create("urn:text"), errors::add);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("int.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, errors.size());
    Assertions.assertEquals(BigInteger.valueOf(23L), handler.result());
  }

  /**
   * Integer values are parsed correctly.
   *
   * @throws Exception On errors
   */

  @Test
  public void testInteger1()
    throws Exception
  {
    final var errors = new ArrayList<BTParseError>();

    final var handler =
      Blackthorne.versionedDispatching(Number.class)
        .addHandler("urn:tests", Blackthorne.forScalar("int", BlackthorneTest::parseInteger))
        .build(URI.create("urn:text"), errors::add);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("int.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, errors.size());
    Assertions.assertEquals(BigInteger.valueOf(23L), handler.result());
  }

  /**
   * Byte values are parsed correctly.
   *
   * @throws Exception On errors
   */

  @Test
  public void testByte0()
    throws Exception
  {
    final var errors = new ArrayList<BTParseError>();

    final var handler =
      BTVersionedDispatchingHandler.<Number>builder()
        .addHandler("urn:tests", Blackthorne.widenConstructor(ByteHandler::new))
        .build(URI.create("urn:text"), errors::add);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("byte.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, errors.size());
    Assertions.assertEquals(Byte.valueOf((byte) 10), handler.result());
  }

  /**
   * Byte values are parsed correctly.
   *
   * @throws Exception On errors
   */

  @Test
  public void testByte1()
    throws Exception
  {
    final var errors = new ArrayList<BTParseError>();

    final var handler =
      BTVersionedDispatchingHandler.<Number>builder()
        .addHandler("urn:tests", Blackthorne.forScalar("byte", BlackthorneTest::parseByte))
        .build(URI.create("urn:text"), errors::add);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("byte.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, errors.size());
    Assertions.assertEquals(Byte.valueOf((byte) 10), handler.result());
  }

  /**
   * Double values are parsed correctly.
   *
   * @throws Exception On errors
   */

  @Test
  public void testDouble0()
    throws Exception
  {
    final var errors = new ArrayList<BTParseError>();

    final var handler =
      BTVersionedDispatchingHandler.<Number>builder()
        .addHandler("urn:tests", Blackthorne.widenConstructor(DoubleHandler::new))
        .build(URI.create("urn:text"), errors::add);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("double.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, errors.size());
    Assertions.assertEquals(Double.valueOf(25.10), handler.result());
  }

  /**
   * Double values are parsed correctly.
   *
   * @throws Exception On errors
   */

  @Test
  public void testDouble1()
    throws Exception
  {
    final var errors = new ArrayList<BTParseError>();

    final var handler =
      BTVersionedDispatchingHandler.<Number>builder()
        .addHandler("urn:tests", Blackthorne.forScalar("double", BlackthorneTest::parseDouble))
        .build(URI.create("urn:text"), errors::add);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("double.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, errors.size());
    Assertions.assertEquals(Double.valueOf(25.10), handler.result());
  }

  /**
   * Choice values are parsed correctly.
   *
   * @throws Exception On errors
   */

  @Test
  public void testChoice0()
    throws Exception
  {
    final var errors = new ArrayList<BTParseError>();

    final var handler =
      BTVersionedDispatchingHandler.<Number>builder()
        .addHandler("urn:tests", ChoiceHandler::new)
        .build(URI.create("urn:text"), errors::add);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("choice0.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, errors.size());
    Assertions.assertEquals(BigInteger.valueOf(23L), handler.result());
  }

  /**
   * Choice values are parsed correctly.
   *
   * @throws Exception On errors
   */

  @Test
  public void testChoice1()
    throws Exception
  {
    final var errors = new ArrayList<BTParseError>();

    final var handler =
      BTVersionedDispatchingHandler.<Number>builder()
        .addHandler("urn:tests", ChoiceHandler::new)
        .build(URI.create("urn:text"), errors::add);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("choice1.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, errors.size());
    Assertions.assertEquals(Double.valueOf(25.10), handler.result());
  }

  /**
   * Choice values are parsed correctly.
   *
   * @throws Exception On errors
   */

  @Test
  public void testChoice2()
    throws Exception
  {
    final var errors = new ArrayList<BTParseError>();

    final var handler =
      BTVersionedDispatchingHandler.<Number>builder()
        .addHandler("urn:tests", ChoiceHandler::new)
        .build(URI.create("urn:text"), errors::add);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("choice2.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, errors.size());
    Assertions.assertEquals(Byte.valueOf((byte) 10), handler.result());
  }

  /**
   * Choices values are parsed correctly.
   *
   * @throws Exception On errors
   */

  @Test
  public void testChoices0()
    throws Exception
  {
    final var errors = new ArrayList<BTParseError>();

    final var handler =
      BTVersionedDispatchingHandler.<List<Number>>builder()
        .addHandler("urn:tests", ChoicesHandler::new)
        .build(URI.create("urn:text"), errors::add);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("choices0.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, errors.size());

    final var numbers = handler.result();
    Assertions.assertEquals(BigInteger.valueOf(23L), numbers.get(0));
    Assertions.assertEquals(Double.valueOf(25.10), numbers.get(1));
    Assertions.assertEquals(Byte.valueOf((byte) 10), numbers.get(2));
    Assertions.assertEquals(3, numbers.size());
  }

  /**
   * Choices values are parsed correctly.
   *
   * @throws Exception On errors
   */

  @Test
  public void testChoices1()
    throws Exception
  {
    final var errors = new ArrayList<BTParseError>();

    final var handler =
      BTVersionedDispatchingHandler.<List<Number>>builder()
        .addHandler(
          "urn:tests",
          Blackthorne.forListPoly(
            "choices",
            Map.ofEntries(
              Map.entry("choice", ChoiceHandler::new)
            )))
        .build(URI.create("urn:text"), errors::add);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("choices0.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, errors.size());

    final var numbers = handler.result();
    Assertions.assertEquals(BigInteger.valueOf(23L), numbers.get(0));
    Assertions.assertEquals(Double.valueOf(25.10), numbers.get(1));
    Assertions.assertEquals(Byte.valueOf((byte) 10), numbers.get(2));
    Assertions.assertEquals(3, numbers.size());
  }

  /**
   * Choices values are parsed correctly.
   *
   * @throws Exception On errors
   */

  @Test
  public void testChoices2()
    throws Exception
  {
    final var errors = new ArrayList<BTParseError>();

    final var handler =
      BTVersionedDispatchingHandler.<List<Number>>builder()
        .addHandler(
          "urn:tests",
          Blackthorne.forListMono("choices", "choice", ChoiceHandler::new))
        .build(URI.create("urn:text"), errors::add);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("choices0.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, errors.size());

    final var numbers = handler.result();
    Assertions.assertEquals(BigInteger.valueOf(23L), numbers.get(0));
    Assertions.assertEquals(Double.valueOf(25.10), numbers.get(1));
    Assertions.assertEquals(Byte.valueOf((byte) 10), numbers.get(2));
    Assertions.assertEquals(3, numbers.size());
  }

  private static final class ByteHandler extends BTContentHandlerAbstract<Void, Byte>
  {
    ByteHandler(
      final Locator2 inLocator)
    {
      super(inLocator, "byte", BTAcceptCharacters.ACCEPT_CHARACTERS);
    }

    @Override
    protected void onOverrideCharactersDirectly(
      final char[] ch,
      final int start,
      final int length)
      throws SAXParseException
    {
      try {
        this.finish(parseByte(this.locator(), ch, start, length));
      } catch (final Exception e) {
        throw new SAXParseException(e.getLocalizedMessage(), this.locator(), e);
      }
    }
  }

  private static final class IntHandler extends BTContentHandlerAbstract<Void, BigInteger>
  {
    IntHandler(
      final Locator2 inLocator)
    {
      super(inLocator, "int", BTAcceptCharacters.ACCEPT_CHARACTERS);
    }

    @Override
    protected void onOverrideCharactersDirectly(
      final char[] ch,
      final int start,
      final int length)
      throws SAXParseException
    {
      try {
        this.finish(parseInteger(this.locator(), ch, start, length));
      } catch (final Exception e) {
        throw new SAXParseException(e.getLocalizedMessage(), this.locator(), e);
      }
    }
  }

  private static final class DoubleHandler extends BTContentHandlerAbstract<Void, Double>
  {
    DoubleHandler(
      final Locator2 inLocator)
    {
      super(inLocator, "double", BTAcceptCharacters.ACCEPT_CHARACTERS);
    }

    @Override
    protected void onOverrideCharactersDirectly(
      final char[] ch,
      final int start,
      final int length)
      throws SAXException
    {
      try {
        this.finish(parseDouble(this.locator(), ch, start, length));
      } catch (final Exception e) {
        throw new SAXParseException(e.getLocalizedMessage(), this.locator(), e);
      }
    }
  }

  private static final class ChoiceHandler extends BTContentHandlerAbstract<Number, Number>
  {
    private static final Logger LOG = LoggerFactory.getLogger(ChoiceHandler.class);

    ChoiceHandler(
      final Locator2 inLocator)
    {
      super(inLocator, "choice", BTAcceptCharacters.DO_NOT_ACCEPT_CHARACTERS);
    }

    @Override
    protected Map<String, BTContentHandlerConstructorType<? extends Number>> onOverrideWantChildHandlers()
    {
      return Map.ofEntries(
        Map.entry("byte", locator -> new ByteHandler(locator).map(x -> x)),
        Map.entry("double", locator -> new DoubleHandler(locator).map(x -> x)),
        Map.entry("int", locator -> new IntHandler(locator).map(x -> x))
      );
    }

    @Override
    protected void onOverrideChildResultReceived(
      final String childElement,
      final Number value)
    {
      LOG.debug("onOverrideChildResultReceived: {} {}", childElement, value);
      this.finish(value);
    }
  }

  private static final class ChoicesHandler extends BTContentHandlerAbstract<Number, List<Number>>
  {
    private final List<Number> numbers = new ArrayList<>();

    ChoicesHandler(
      final Locator2 inLocator)
    {
      super(inLocator, "choices", BTAcceptCharacters.DO_NOT_ACCEPT_CHARACTERS);
    }

    @Override
    protected Map<String, BTContentHandlerConstructorType<? extends Number>> onOverrideWantChildHandlers()
    {
      return Map.ofEntries(
        Map.entry("choice", locator -> new ChoiceHandler(locator).map(x -> x))
      );
    }

    @Override
    protected Optional<List<Number>> onOverrideElementFinishDirectly(
      final String namespace,
      final String name,
      final String qname)
    {
      return Optional.of(this.finish(this.numbers));
    }

    @Override
    protected void onOverrideChildResultReceived(
      final String childElement,
      final Number value)
    {
      this.numbers.add(value);
    }
  }

  private static BigInteger parseInteger(
    final Locator2 locator,
    final char[] characters,
    final int offset,
    final int length)
  {
    final var text = String.valueOf(characters, offset, length).trim();
    return new BigInteger(text);
  }

  private static Byte parseByte(
    final Locator2 locator,
    final char[] characters,
    final int offset,
    final int length)
  {
    final var text = String.valueOf(characters, offset, length).trim();
    return Byte.valueOf(Byte.parseByte(text));
  }

  private static Double parseDouble(
    final Locator2 locator,
    final char[] characters,
    final int offset,
    final int length)
  {
    final var text = String.valueOf(characters, offset, length).trim();
    return Double.valueOf(text);
  }
}
