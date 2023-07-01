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

package com.io7m.blackthorne.tests;

import com.io7m.blackthorne.api.BTContentHandler;
import com.io7m.blackthorne.api.BTElementHandlerConstructorType;
import com.io7m.blackthorne.api.BTElementHandlerType;
import com.io7m.blackthorne.api.BTElementParsingContextType;
import com.io7m.blackthorne.api.BTException;
import com.io7m.blackthorne.api.BTIgnoreUnrecognizedElements;
import com.io7m.blackthorne.api.BTParseError;
import com.io7m.blackthorne.api.BTQualifiedName;
import com.io7m.blackthorne.api.BTScalarAttributeHandler;
import com.io7m.blackthorne.api.Blackthorne;
import com.io7m.blackthorne.jxe.BlackthorneJXE;
import com.io7m.jxe.core.JXEHardenedSAXParsers;
import com.io7m.jxe.core.JXESchemaDefinition;
import com.io7m.jxe.core.JXESchemaDefinitions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.io7m.blackthorne.api.BTIgnoreUnrecognizedElements.DO_NOT_IGNORE_UNRECOGNIZED_ELEMENTS;
import static com.io7m.blackthorne.api.BTIgnoreUnrecognizedElements.IGNORE_UNRECOGNIZED_ELEMENTS;
import static com.io7m.jxe.core.JXEXInclude.XINCLUDE_DISABLED;

/**
 * Tests for the API.
 */

public final class BlackthorneTest
{
  private static final Logger LOG = LoggerFactory.getLogger(BlackthorneTest.class);
  private ArrayList<BTParseError> errors;

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
    final URL url = resourceURL(name);
    final var stream = url.openStream();
    final var source = new InputSource(stream);
    source.setPublicId(url.toString());
    return source;
  }

  private static URL resourceURL(
    final String name)
  {
    return BlackthorneTest.class.getResource("/com/io7m/blackthorne/tests/" + name);
  }

  private static InputStream resourceStream(
    final String name)
    throws Exception
  {
    return resourceURL(name).openStream();
  }

  @BeforeEach
  public void testSetup()
  {
    this.errors = new ArrayList<BTParseError>();
  }

  private void logError(
    final BTParseError error)
  {
    LOG.debug("error: {}: ", error, error.exception().orElse(null));
    this.errors.add(error);
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
    final var handler =
      new BTContentHandler<Integer>(
        URI.create("urn:text"),
        this::logError,
        Map.of());

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("no_namespace.xml"));

    Assertions.assertTrue(handler.failed());
    Assertions.assertEquals(1, this.errors.size());

    {
      final var error = this.errors.remove(0);
      Assertions.assertTrue(error.message().contains(
        "not allowed as a root element"));
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
    final var handler =
      new BTContentHandler<Integer>(
        URI.create("urn:text"),
        this::logError,
        Map.of());

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("unknown_namespace.xml"));

    Assertions.assertTrue(handler.failed());
    Assertions.assertEquals(1, this.errors.size());

    {
      final var error = this.errors.remove(0);
      Assertions.assertTrue(error.message().contains(
        "not allowed as a root element"));
    }
  }

  /**
   * An invalid document won't be parsed.
   *
   * @throws Exception On errors
   */

  @Test
  public void testInvalid0()
    throws Exception
  {
    final var handler =
      BTContentHandler.<List<Number>>builder()
        .addHandler("urn:tests", "choices", ChoicesHandler::new)
        .build(URI.create("urn:text"), this::logError);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("not_valid.xml"));

    Assertions.assertTrue(handler.failed());
    Assertions.assertEquals(1, this.errors.size());

    {
      final var error = this.errors.remove(0);
      Assertions.assertTrue(error.message().contains(
        "not allowed as a root element"));
    }
  }

  /**
   * An invalid document won't be parsed.
   *
   * @throws Exception On errors
   */

  @Test
  public void testInvalid1()
    throws Exception
  {
    final var handler =
      BTContentHandler.<Number>builder()
        .addHandler("urn:tests", "int", IntHandler::new)
        .build(URI.create("urn:text"), this::logError);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("int_invalid.xml"));

    Assertions.assertTrue(handler.failed());
    Assertions.assertEquals(1, this.errors.size());

    {
      final var error = this.errors.remove(0);
      Assertions.assertTrue(error.message().contains(
        "does not recognize this element"));
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
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, BigInteger>> handlers =
      Map.ofEntries(
        Map.entry(BTQualifiedName.of("urn:tests", "int"), IntHandler::new)
      );

    final var handler =
      new BTContentHandler<>(URI.create("urn:text"), this::logError, handlers);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("int.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, this.errors.size());
    Assertions.assertEquals(BigInteger.valueOf(23L), handler.result().get());
  }

  /**
   * Integer values are parsed correctly.
   *
   * @throws Exception On errors
   */

  @Test
  public void testIntegerAttr0()
    throws Exception
  {
    final var intAttr =
      Blackthorne.forScalarAttribute(
        "urn:tests",
        "intA",
        BlackthorneTest::parseIntAttribute);

    final var handler =
      BTContentHandler.<Number>builder()
        .addHandler("urn:tests", "intA", intAttr)
        .build(URI.create("urn:text"), this::logError);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("intA.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, this.errors.size());
    Assertions.assertEquals(BigInteger.valueOf(23L), handler.result().get());
  }

  private static Number parseIntAttribute(
    final BTElementParsingContextType context,
    final Attributes attributes)
    throws SAXParseException
  {
    try {
      final var text = attributes.getValue("urn:tests", "value").trim();
      return new BigInteger(text);
    } catch (final Exception e) {
      throw context.parseException(e);
    }
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
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, Double>> handlers =
      Map.ofEntries(
        Map.entry(BTQualifiedName.of("urn:tests", "double"), DoubleHandler::new)
      );

    final var handler =
      new BTContentHandler<>(URI.create("urn:text"), this::logError, handlers);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("double.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, this.errors.size());
    Assertions.assertEquals(Double.valueOf(25.10), handler.result().get());
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
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, Byte>> handlers =
      Map.ofEntries(
        Map.entry(BTQualifiedName.of("urn:tests", "byte"), ByteHandler::new)
      );

    final var handler =
      new BTContentHandler<>(URI.create("urn:text"), this::logError, handlers);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("byte.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, this.errors.size());
    Assertions.assertEquals(Byte.valueOf((byte) 10), handler.result().get());
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
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, Number>> handlers =
      Map.ofEntries(
        Map.entry(BTQualifiedName.of("urn:tests", "choice"), ChoiceHandler::new)
      );

    final var handler =
      new BTContentHandler<>(URI.create("urn:text"), this::logError, handlers);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("choice0.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, this.errors.size());
    Assertions.assertEquals(BigInteger.valueOf(23L), handler.result().get());
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
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, Number>> handlers =
      Map.ofEntries(
        Map.entry(BTQualifiedName.of("urn:tests", "choice"), ChoiceHandler::new)
      );

    final var handler =
      new BTContentHandler<>(URI.create("urn:text"), this::logError, handlers);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("choice1.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, this.errors.size());
    Assertions.assertEquals(Double.valueOf(25.10), handler.result().get());
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
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, Number>> handlers =
      Map.ofEntries(
        Map.entry(BTQualifiedName.of("urn:tests", "choice"), ChoiceHandler::new)
      );

    final var handler =
      new BTContentHandler<>(URI.create("urn:text"), this::logError, handlers);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("choice2.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, this.errors.size());
    Assertions.assertEquals(Byte.valueOf((byte) 10), handler.result().get());
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
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, List<Number>>> handlers =
      Map.ofEntries(
        Map.entry(
          BTQualifiedName.of("urn:tests", "choices"),
          ChoicesHandler::new)
      );

    final var handler =
      new BTContentHandler<>(URI.create("urn:text"), this::logError, handlers);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("choices0.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, this.errors.size());

    final var numbers = handler.result().get();
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
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, List<Number>>> handlers =
      Map.ofEntries(
        Map.entry(
          BTQualifiedName.of("urn:tests", "choices"),
          ChoicesIgnoringHandler::new)
      );

    final var handler =
      new BTContentHandler<>(URI.create("urn:text"), this::logError, handlers);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("choices1.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, this.errors.size());

    final var numbers = handler.result().get();
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
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, List<Number>>> handlers =
      Map.ofEntries(
        Map.entry(
          BTQualifiedName.of("urn:tests", "choices"),
          ChoicesIgnoringHandler::new)
      );

    final var handler =
      new BTContentHandler<>(URI.create("urn:text"), this::logError, handlers);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("choices2.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, this.errors.size());

    final var numbers = handler.result().get();
    Assertions.assertEquals(BigInteger.valueOf(23L), numbers.get(0));
    Assertions.assertEquals(Double.valueOf(25.10), numbers.get(1));
    Assertions.assertEquals(Byte.valueOf((byte) 20), numbers.get(2));
    Assertions.assertEquals(3, numbers.size());
  }

  /**
   * Choices values are parsed correctly as scalars.
   *
   * @throws Exception On errors
   */

  @Test
  public void testChoicesScalars0()
    throws Exception
  {
    final var listHandler =
      Blackthorne.forListMono(
        BTQualifiedName.of("urn:tests", "choices"),
        BTQualifiedName.of("urn:tests", "choice"),
        ChoiceScalarHandler::new,
        DO_NOT_IGNORE_UNRECOGNIZED_ELEMENTS);

    final var handler =
      BTContentHandler.<List<Number>>builder()
        .addHandler(BTQualifiedName.of("urn:tests", "choices"), listHandler)
        .build(URI.create("urn:text"), this::logError);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("choices0.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, this.errors.size());

    final var numbers = handler.result().get();
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
  public void testChoicesList0()
    throws Exception
  {
    final var listHandler =
      Blackthorne.forListMono(
        BTQualifiedName.of("urn:tests", "choices"),
        BTQualifiedName.of("urn:tests", "choice"),
        ChoiceHandler::new,
        DO_NOT_IGNORE_UNRECOGNIZED_ELEMENTS);

    final var handler =
      new BTContentHandler<>(
        URI.create("urn:text"),
        this::logError,
        Map.of(BTQualifiedName.of("urn:tests", "choices"), listHandler));

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("choices0.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, this.errors.size());

    final var numbers = handler.result().get();
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
  public void testChoicesList1()
    throws Exception
  {
    final var listHandler =
      Blackthorne.forListMono(
        BTQualifiedName.of("urn:tests", "choices"),
        BTQualifiedName.of("urn:tests", "choice"),
        ChoiceIgnoringHandler::new,
        IGNORE_UNRECOGNIZED_ELEMENTS);

    final var handler =
      new BTContentHandler<>(
        URI.create("urn:text"),
        this::logError,
        Map.of(BTQualifiedName.of("urn:tests", "choices"), listHandler));

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("choices1.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, this.errors.size());

    final var numbers = handler.result().get();
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
  public void testChoicesList2()
    throws Exception
  {
    final var listHandler =
      Blackthorne.forListMono(
        BTQualifiedName.of("urn:tests", "choices"),
        BTQualifiedName.of("urn:tests", "choice"),
        ChoiceIgnoringHandler::new,
        IGNORE_UNRECOGNIZED_ELEMENTS);

    final var handler =
      new BTContentHandler<>(
        URI.create("urn:text"),
        this::logError,
        Map.of(BTQualifiedName.of("urn:tests", "choices"), listHandler));

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("choices2.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, this.errors.size());

    final var numbers = handler.result().get();
    Assertions.assertEquals(BigInteger.valueOf(23L), numbers.get(0));
    Assertions.assertEquals(Double.valueOf(25.10), numbers.get(1));
    Assertions.assertEquals(Byte.valueOf((byte) 20), numbers.get(2));
    Assertions.assertEquals(3, numbers.size());
  }

  /**
   * Choices values are parsed correctly.
   *
   * @throws Exception On errors
   */

  @Test
  public void testChoicesListPoly2()
    throws Exception
  {
    final var listHandler =
      Blackthorne.forListPoly(
        BTQualifiedName.of("urn:tests", "choices"),
        Map.ofEntries(
          Map.entry(
            BTQualifiedName.of("urn:tests", "choice"),
            ChoiceIgnoringHandler::new)
        ),
        IGNORE_UNRECOGNIZED_ELEMENTS);

    final var handler =
      new BTContentHandler<>(
        URI.create("urn:text"),
        this::logError,
        Map.of(BTQualifiedName.of("urn:tests", "choices"), listHandler));

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("choices2.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, this.errors.size());

    final var numbers = handler.result().get();
    Assertions.assertEquals(BigInteger.valueOf(23L), numbers.get(0));
    Assertions.assertEquals(Double.valueOf(25.10), numbers.get(1));
    Assertions.assertEquals(Byte.valueOf((byte) 20), numbers.get(2));
    Assertions.assertEquals(3, numbers.size());
  }

  /**
   * Handlers are functors.
   *
   * @throws Exception On errors
   */

  @Test
  public void testHandlersAreFunctors0()
    throws Exception
  {
    final var listHandler =
      Blackthorne.forListMono(
        BTQualifiedName.of("urn:tests", "choices"),
        BTQualifiedName.of("urn:tests", "choice"),
        Blackthorne.mapConstructor(
          ChoiceIgnoringHandler::new,
          number -> Double.valueOf(number.doubleValue() * 2.0)),
        IGNORE_UNRECOGNIZED_ELEMENTS);

    final var handler =
      new BTContentHandler<>(
        URI.create("urn:text"),
        this::logError,
        Map.of(BTQualifiedName.of("urn:tests", "choices"), listHandler));

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("choices0.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, this.errors.size());

    final var numbers = handler.result().get();
    Assertions.assertEquals(Double.valueOf(46.0), numbers.get(0).doubleValue());
    Assertions.assertEquals(
      Double.valueOf(50.20),
      numbers.get(1).doubleValue());
    Assertions.assertEquals(Double.valueOf(20.0), numbers.get(2).doubleValue());
    Assertions.assertEquals(3, numbers.size());
  }

  private static final class IntHandler implements BTElementHandlerType<Object, BigInteger>
  {
    private BigInteger result;

    IntHandler(
      final BTElementParsingContextType context)
    {

    }

    @Override
    public void onCharacters(
      final BTElementParsingContextType context,
      final char[] data,
      final int offset,
      final int length)
      throws SAXParseException
    {
      try {
        final var text = String.valueOf(data, offset, length).trim();
        this.result = new BigInteger(text);
      } catch (final Exception e) {
        throw context.parseException(e);
      }
    }

    @Override
    public BigInteger onElementFinished(
      final BTElementParsingContextType context)
    {
      return this.result;
    }
  }

  private static final class DoubleHandler implements BTElementHandlerType<Object, Double>
  {
    private Double result;

    DoubleHandler(
      final BTElementParsingContextType context)
    {

    }

    @Override
    public void onCharacters(
      final BTElementParsingContextType context,
      final char[] data,
      final int offset,
      final int length)
      throws SAXParseException
    {
      try {
        final var text = String.valueOf(data, offset, length).trim();
        this.result = Double.valueOf(text);
      } catch (final NumberFormatException e) {
        throw context.parseException(e);
      }
    }

    @Override
    public Double onElementFinished(
      final BTElementParsingContextType context)
    {
      return this.result;
    }
  }

  private static final class ByteHandler implements BTElementHandlerType<Object, Byte>
  {
    private Byte result;

    ByteHandler(
      final BTElementParsingContextType context)
    {

    }

    @Override
    public void onCharacters(
      final BTElementParsingContextType context,
      final char[] data,
      final int offset,
      final int length)
      throws SAXParseException
    {
      try {
        final var text = String.valueOf(data, offset, length).trim();
        this.result = Byte.valueOf(text);
      } catch (final NumberFormatException e) {
        throw context.parseException(e);
      }
    }

    @Override
    public Byte onElementFinished(
      final BTElementParsingContextType context)
    {
      return this.result;
    }
  }

  private static final class ChoiceHandler implements BTElementHandlerType<Number, Number>
  {
    private Number result;

    ChoiceHandler(
      final BTElementParsingContextType context)
    {

    }

    @Override
    public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends Number>> onChildHandlersRequested(
      final BTElementParsingContextType context)
    {
      return Map.ofEntries(
        Map.entry(BTQualifiedName.of("urn:tests", "byte"), ByteHandler::new),
        Map.entry(
          BTQualifiedName.of("urn:tests", "double"),
          DoubleHandler::new),
        Map.entry(BTQualifiedName.of("urn:tests", "int"), IntHandler::new)
      );
    }

    @Override
    public void onChildValueProduced(
      final BTElementParsingContextType context,
      final Number newResult)
    {
      this.result = Objects.requireNonNull(newResult, "result");
    }

    @Override
    public Number onElementFinished(final BTElementParsingContextType context)
    {
      return this.result;
    }
  }

  private static final class ChoiceScalarHandler implements BTElementHandlerType<Number, Number>
  {
    private Number result;

    ChoiceScalarHandler(
      final BTElementParsingContextType context)
    {

    }

    private static Byte parseByte(
      final BTElementParsingContextType context,
      final char[] chars,
      final int offset,
      final int length)
      throws SAXParseException
    {
      try {
        return Byte.valueOf(Byte.parseByte(String.valueOf(
          chars,
          offset,
          length)));
      } catch (final NumberFormatException e) {
        throw context.parseException(e);
      }
    }

    private static Double parseDouble(
      final BTElementParsingContextType context,
      final char[] chars,
      final int offset,
      final int length)
      throws SAXParseException
    {
      try {
        return Double.valueOf(Double.parseDouble(String.valueOf(
          chars,
          offset,
          length)));
      } catch (final NumberFormatException e) {
        throw context.parseException(e);
      }
    }

    private static BigInteger parseInt(
      final BTElementParsingContextType context,
      final char[] chars,
      final int offset,
      final int length)
      throws SAXParseException
    {
      try {
        return new BigInteger(String.valueOf(chars, offset, length));
      } catch (final Exception e) {
        throw context.parseException(e);
      }
    }

    @Override
    public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends Number>> onChildHandlersRequested(
      final BTElementParsingContextType context)
    {
      final var nameByte =
        BTQualifiedName.of("urn:tests", "byte");
      final var nameDouble =
        BTQualifiedName.of("urn:tests", "double");
      final var nameInt =
        BTQualifiedName.of("urn:tests", "int");

      return Map.ofEntries(
        Map.entry(
          nameByte,
          Blackthorne.forScalar(
            "urn:tests",
            "byte",
            ChoiceScalarHandler::parseByte)),
        Map.entry(
          nameDouble,
          Blackthorne.forScalar(
            "urn:tests",
            "double",
            ChoiceScalarHandler::parseDouble)),
        Map.entry(
          nameInt,
          Blackthorne.forScalar(
            "urn:tests",
            "int",
            ChoiceScalarHandler::parseInt))
      );
    }

    @Override
    public void onChildValueProduced(
      final BTElementParsingContextType context,
      final Number newResult)
    {
      this.result = Objects.requireNonNull(newResult, "result");
    }

    @Override
    public Number onElementFinished(final BTElementParsingContextType context)
    {
      return this.result;
    }
  }

  private static final class ChoiceIgnoringHandler implements
    BTElementHandlerType<Number, Number>
  {
    private Number result;

    ChoiceIgnoringHandler(
      final BTElementParsingContextType context)
    {

    }

    @Override
    public BTIgnoreUnrecognizedElements onShouldIgnoreUnrecognizedElements(
      final BTElementParsingContextType context)
    {
      return IGNORE_UNRECOGNIZED_ELEMENTS;
    }

    @Override
    public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends Number>> onChildHandlersRequested(
      final BTElementParsingContextType context)
    {
      return Map.ofEntries(
        Map.entry(BTQualifiedName.of("urn:tests", "byte"), ByteHandler::new),
        Map.entry(
          BTQualifiedName.of("urn:tests", "double"),
          DoubleHandler::new),
        Map.entry(BTQualifiedName.of("urn:tests", "int"), IntHandler::new)
      );
    }

    @Override
    public void onChildValueProduced(
      final BTElementParsingContextType context,
      final Number newResult)
    {
      this.result = Objects.requireNonNull(newResult, "result");
    }

    @Override
    public Number onElementFinished(final BTElementParsingContextType context)
    {
      return this.result;
    }
  }

  private static final class ChoicesHandler implements BTElementHandlerType<Number, List<Number>>
  {
    private List<Number> results;

    ChoicesHandler(
      final BTElementParsingContextType context)
    {
      this.results = new ArrayList<>();
    }

    @Override
    public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends Number>> onChildHandlersRequested(
      final BTElementParsingContextType context)
    {
      return Map.ofEntries(
        Map.entry(BTQualifiedName.of("urn:tests", "choice"), ChoiceHandler::new)
      );
    }

    @Override
    public void onChildValueProduced(
      final BTElementParsingContextType context,
      final Number newResult)
    {
      this.results.add(Objects.requireNonNull(newResult, "result"));
    }

    @Override
    public List<Number> onElementFinished(final BTElementParsingContextType context)
    {
      return this.results;
    }
  }

  private static final class ChoicesIgnoringHandler implements
    BTElementHandlerType<Number, List<Number>>
  {
    private List<Number> results;

    ChoicesIgnoringHandler(
      final BTElementParsingContextType context)
    {
      this.results = new ArrayList<>();
    }

    @Override
    public BTIgnoreUnrecognizedElements onShouldIgnoreUnrecognizedElements(
      final BTElementParsingContextType context)
    {
      return IGNORE_UNRECOGNIZED_ELEMENTS;
    }

    @Override
    public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends Number>> onChildHandlersRequested(
      final BTElementParsingContextType context)
    {
      return Map.ofEntries(
        Map.entry(
          BTQualifiedName.of("urn:tests", "choice"),
          ChoiceIgnoringHandler::new)
      );
    }

    @Override
    public void onChildValueProduced(
      final BTElementParsingContextType context,
      final Number newResult)
    {
      this.results.add(Objects.requireNonNull(newResult, "result"));
    }

    @Override
    public List<Number> onElementFinished(final BTElementParsingContextType context)
    {
      return this.results;
    }
  }

  /**
   * Integer values are parsed correctly.
   *
   * @throws Exception On errors
   */

  @Test
  public void testConvenience0()
    throws Exception
  {
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, BigInteger>> handlers =
      Map.ofEntries(
        Map.entry(BTQualifiedName.of("urn:tests", "int"), IntHandler::new)
      );

    final var mappings =
      JXESchemaDefinitions.mappingsOf(
        JXESchemaDefinition.of(
          URI.create("urn:tests"),
          "choice.xsd",
          resourceURL("choice.xsd")
        ));

    BlackthorneJXE.parseAll(
      URI.create("urn:test"),
      resourceStream("int.xml"),
      handlers,
      new JXEHardenedSAXParsers(),
      Optional.empty(),
      XINCLUDE_DISABLED,
      mappings
    );

    BlackthorneJXE.parse(
      URI.create("urn:test"),
      resourceStream("int.xml"),
      handlers,
      Optional.empty(),
      XINCLUDE_DISABLED,
      mappings
    );

    BlackthorneJXE.parse(
      URI.create("urn:test"),
      resourceStream("int.xml"),
      handlers,
      XINCLUDE_DISABLED,
      mappings
    );

    BlackthorneJXE.parse(
      URI.create("urn:test"),
      resourceStream("int.xml"),
      handlers,
      mappings
    );
  }

  /**
   * Unparseable documents raise exceptions.
   *
   * @throws Exception On errors
   */

  @Test
  public void testConvenience1()
    throws Exception
  {
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, BigInteger>> handlers =
      Map.ofEntries(
        Map.entry(BTQualifiedName.of("urn:tests", "int"), IntHandler::new)
      );

    final var ex =
      Assertions.assertThrows(BTException.class, () -> {
        Blackthorne.parse(
          URI.create("urn:test"),
          resourceStream("unparseable.xml"),
          () -> {
            return new JXEHardenedSAXParsers()
              .createXMLReaderNonValidating(
                Optional.empty(),
                XINCLUDE_DISABLED);
          },
          handlers
        );
      });

    Assertions.assertEquals(SAXParseException.class, ex.getCause().getClass());
    Assertions.assertEquals(2, ex.errors().size());
  }

  /**
   * Leaf element handlers work.
   *
   * @throws Exception On errors
   */

  @Test
  public void testLeafIntegerAttr0()
    throws Exception
  {
    final var intAttr =
      new BTScalarAttributeHandler<>(
        BTQualifiedName.of("urn:tests", "intA"),
        BlackthorneTest::parseIntAttribute
      );

    final var handler =
      BTContentHandler.<Number>builder()
        .addHandler("urn:tests", "intA", context -> intAttr)
        .build(URI.create("urn:text"), this::logError);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("intA.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, this.errors.size());
    Assertions.assertEquals(BigInteger.valueOf(23L), handler.result().get());
  }

  /**
   * Scalar element handlers work.
   *
   * @throws Exception On errors
   */

  @Test
  public void testText0()
    throws Exception
  {
    final var handler =
      BTContentHandler.<String>builder()
        .addHandler(
          "urn:tests",
          "string",
          Blackthorne.forScalarString("urn:tests", "string"))
        .build(URI.create("urn:text"), this::logError);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("string.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, this.errors.size());
    Assertions.assertEquals("This is some text.", handler.result().get());
  }

  /**
   * Scalar element handlers work.
   *
   * @throws Exception On errors
   */

  @Test
  public void testText1()
    throws Exception
  {
    final var name =
      BTQualifiedName.of("urn:tests", "string");

    final var handler =
      BTContentHandler.<String>builder()
        .addHandler(name, Blackthorne.forScalarString(name))
        .build(URI.create("urn:text"), this::logError);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("string.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, this.errors.size());
    Assertions.assertEquals("This is some text.", handler.result().get());
  }

  /**
   * Scalar element handlers work.
   *
   * @throws Exception On errors
   */

  @Test
  public void testText2()
    throws Exception
  {
    final var handler =
      BTContentHandler.<Exception>builder()
        .addHandler(
          "urn:tests",
          "string",
          Blackthorne.forScalarFromString(
            "urn:tests",
            "string",
            Exception::new))
        .build(URI.create("urn:text"), this::logError);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("string.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, this.errors.size());
    Assertions.assertEquals(
      "This is some text.",
      handler.result().get().getMessage());
  }

  /**
   * Scalar element handlers work.
   *
   * @throws Exception On errors
   */

  @Test
  public void testText3()
    throws Exception
  {
    final var name =
      BTQualifiedName.of("urn:tests", "string");

    final var handler =
      BTContentHandler.<Exception>builder()
        .addHandler(name, Blackthorne.forScalarFromString(name, Exception::new))
        .build(URI.create("urn:text"), this::logError);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("string.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, this.errors.size());
    Assertions.assertEquals(
      "This is some text.",
      handler.result().get().getMessage());
  }

  /**
   * Scalar element handlers work.
   *
   * @throws Exception On errors
   */

  @Test
  public void testText4()
    throws Exception
  {
    final var name =
      BTQualifiedName.of("urn:tests", "string");

    final var handler =
      BTContentHandler.<Exception>builder()
        .addHandler(name, Blackthorne.forScalarFromString(name, Exception::new))
        .build(URI.create("urn:text"), this::logError);

    final var reader = createReader();
    reader.setContentHandler(handler);
    reader.setErrorHandler(handler);
    reader.parse(resource("stringEmpty.xml"));

    Assertions.assertFalse(handler.failed());
    Assertions.assertEquals(0, this.errors.size());
    Assertions.assertEquals(
      "",
      handler.result().get().getMessage());
  }
}
