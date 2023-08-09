blackthorne
===

[![Maven Central](https://img.shields.io/maven-central/v/com.io7m.blackthorne/com.io7m.blackthorne.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.io7m.blackthorne%22)
[![Maven Central (snapshot)](https://img.shields.io/nexus/s/https/s01.oss.sonatype.org/com.io7m.blackthorne/com.io7m.blackthorne.svg?style=flat-square)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/io7m/blackthorne/)
[![Codecov](https://img.shields.io/codecov/c/github/io7m/blackthorne.svg?style=flat-square)](https://codecov.io/gh/io7m/blackthorne)

![blackthorne](./src/site/resources/blackthorne.jpg?raw=true)

| JVM | Platform | Status |
|-----|----------|--------|
| OpenJDK (Temurin) Current | Linux | [![Build (OpenJDK (Temurin) Current, Linux)](https://img.shields.io/github/actions/workflow/status/io7m/blackthorne/main.linux.temurin.current.yml)](https://github.com/io7m/blackthorne/actions?query=workflow%3Amain.linux.temurin.current)|
| OpenJDK (Temurin) LTS | Linux | [![Build (OpenJDK (Temurin) LTS, Linux)](https://img.shields.io/github/actions/workflow/status/io7m/blackthorne/main.linux.temurin.lts.yml)](https://github.com/io7m/blackthorne/actions?query=workflow%3Amain.linux.temurin.lts)|
| OpenJDK (Temurin) Current | Windows | [![Build (OpenJDK (Temurin) Current, Windows)](https://img.shields.io/github/actions/workflow/status/io7m/blackthorne/main.windows.temurin.current.yml)](https://github.com/io7m/blackthorne/actions?query=workflow%3Amain.windows.temurin.current)|
| OpenJDK (Temurin) LTS | Windows | [![Build (OpenJDK (Temurin) LTS, Windows)](https://img.shields.io/github/actions/workflow/status/io7m/blackthorne/main.windows.temurin.lts.yml)](https://github.com/io7m/blackthorne/actions?query=workflow%3Amain.windows.temurin.lts)|

## blackthorne

A library that handles the processing of, and extraction of data from, validated
XML. Data is processed from a standard streaming `XMLReader` and so doesn't
require holding the entire parsed AST in memory.

### Features

  * Functional API for composing together handlers for elements.
  * Stream-based: Documents do not require an expensive intermediate AST in memory.
  * Type-safe: Build data structures during parsing without losing type-safety.
  * No reflection: No need to worry about reflective serialization-based security issues.
  * Integrated with [jxe](https://github.com/io7m/jxe) for transparent, hardened, validation-enforcing SAX parsers.
  * Written in pure Java 17.
  * [OSGi](https://www.osgi.org/) ready.
  * [JPMS](https://en.wikipedia.org/wiki/Java_Platform_Module_System) ready.
  * ISC license.
  * High-coverage automated test suite.

### Motivation

Java provides many different ways to process XML. If you are working with XML
that has a well-defined schema (as you should be), it is desirable to have
a system that allows for efficiently extracting data from the XML without
any kind of manual validation.

One approach, included as part of the JDK, is to use the
[W3C DOM](https://docs.oracle.com/en/java/javase/17/docs/api/java.xml/org/w3c/dom/package-summary.html)
package. This is possibly the worst of all of the options as the API is
error-prone and requires reading the entire document into memory, parsing it
as an AST, and _then_ creating your application-specific domain objects on
top. It also, by default, does not preserve lexical information like line
and column numbers, so if your application needs to display an error that
relates to a specific element in the XML source document... You can't. There
are some rather type-unsafe ways of getting lexical information into the
resulting AST, as long as you are willing to write your own implementations
of the low-level content handling components.

Another approach is to use [XOM](http://www.xom.nu/). This
is better, but still suffers from the same issue of requiring an intermediate
AST to hold the parsed document before the application can make use of it,
and suffers from the same limitations with regards to lexical information. There
are no known methods to get lexical information into XOM ASTs.

Another approach is to use [xstream](https://x-stream.github.io/).
This, at the time of writing, has issues with the Java module system, and
requires a dangerous amount of core reflection to work. This is a fertile
ground for serialization-based security vulnerabilities. It also loses lexical
information.

Another approach is to use [Jakarta XML Binding](https://eclipse-ee4j.github.io/jaxb-ri/).
This has the advantage of generating a complete set of Java domain objects
from an existing XSD schema, and providing code to parse data into those
structures. Unfortunately, doing so also requires core reflection, has
unclear performance characteristics with regard to requiring an intermediate
AST, and loses lexical information.

Another approach, included as part of the JDK, is to use the
[SAX](http://www.saxproject.org/) API. This has the advantages of not requiring
parsing the entire document into memory before use, not requiring core
reflection and therefore not being subject to serialization-based security
issues, and makes lexical information available through a `DocumentLocator`
interface. Unfortunately, the API is difficult to work with; the user is
presented with a series of events published to callbacks, and the user must
make sense of the document structure themselves.

The `blackthorne` package provides a small API built on top of the SAX
API that allows for extracting data from XML documents by composing together
handler functions that can be used to build whatever data structures are
required as the document is parsed. The API is fully namespace-aware, and
assumes that documents will be validated against a schema during parsing (and
so users are free of the burden of performing manual validation in their own
code). This appears to be the best of all worlds; memory-efficiency,
type-safety, security, preserves lexical information, and provides an API
that aims for minimal suffering.

### Building

```
$ mvn clean verify
```

### Usage

The core of the `blackthorne` API is the `BTElementHandlerType` interface.

An instance of `BTElementHandlerType` is responsible for extracting data from
the current XML element (perhaps from attributes), creating child instances
of the `BTElementHandlerType` to handle XML child elements, receiving data
from those child elements, and finally returning data to whatever is the
parent handler of the current handler.

Consider an incredibly simple XSD schema:

```
<?xml version="1.0" encoding="UTF-8" ?>

<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            xmlns:ts="urn:tests"
            targetNamespace="urn:tests">
  <xsd:element name="integer">
    <xsd:simpleType>
      <xsd:restriction base="xsd:integer"/>
    </xsd:simpleType>
  </xsd:element>
</xsd:schema>
```

A document conforming to this schema might look like this:

```
<?xml version="1.0" encoding="UTF-8" ?>

<integer xmlns="urn:tests>23</integer>
```

An element handler that can handle a document like this, might look like
this:

```
class IntHandler
  implements BTElementHandlerType<Object, BigInteger>
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
```

There are several things to note here:

  * The `BTElementHandlerType` is parameterized by the types of values
    it can receive from child handlers, and the type of values it returns
    to its parent handler. Because the handler will never create a child
    handler, we can claim the handler accepts anything (because it will never
    receive anything) and so the type of child values is `Object`. The
    handler returns a value of type `BigInteger`.

  * The handler implements the (optional) `onCharacters` method that will
    be called by the XML stream parser in order to allow the handler to
    process text nodes. This handler parses the given range of characters
    as an integer.

  * The `onElementFinished` method returns the result parsed earlier.

The `Blackthorne` class contains numerous convenience methods that can be
used to succinctly implement handlers such as the above without requiring
entire class definitions. The above could be reduced to:

```
Blackthorne.forScalarFromString(..., BigInteger::new);
```

#### Child Elements

Some handlers will handle XML elements that have child elements. Consider
the following schema:


```
<?xml version="1.0" encoding="UTF-8" ?>

<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            xmlns:ts="urn:tests"
            targetNamespace="urn:tests">
  <xsd:element name="integer">
    <xsd:simpleType>
      <xsd:restriction base="xsd:integer"/>
    </xsd:simpleType>
  </xsd:element>

  <xsd:element name="integers">
    <xsd:sequence minOccurs="0" maxOccurs="unbounded">
      <xsd:element ref="ts:integer"/>
    </xsd:sequence>
  </xsd:element>
</xsd:schema>
```

A handler that can handle the `integers` element must be able to tell the
`blackthorne` API that there will be `integer` child elements, and it must
be prepared to receive child values. For example:

```
class IntsHandler
  implements BTElementHandlerType<BigInteger, List<BigInteger>>
{
  private ArrayList<BigInteger> results;

  IntsHandler(
    final BTElementParsingContextType context)
  {
    this.results = new ArrayList<BigInteger>();
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, BigInteger>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.ofEntries(
      Map.entry(elementName("integer"), IntHandler::new)
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final BigInteger result)
  {
    this.results.add(result);
  }

  @Override
  public List<BigInteger> onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.results;
  }
}
```

The `onChildHandlersRequested` method is implemented and returns a map
of handler constructors. When the `blackthorne` API encounters a child
element with name `integers`, it will instantiate an `IntHandler` to parse
it. The `onChildValueProduced` method is implemented and will receive
an integer value each time a child element is successfully processed.

Again, the `Blackthorne` class contains a method that can provide a simple
implementation for monomorphic lists such as the above. The above can be
reduced to:

```
Blackthorne.forListMono(
  elementName("integers"),
  elementName("integer"),
  IntHandler::new,
  DO_NOT_IGNORE_UNRECOGNIZED_ELEMENTS
);
```

Many parts of the `blackthorne` API accept a parameter of type
`BTIgnoreUnrecognizedElements`. This can be used to ignore unrecognized
elements in XML documents if the schema being used to validate the XML is
allows for extra elements (such as through the use of the `any` element).
If the parameter is set to `DO_NOT_IGNORE_UNRECOGNIZED_ELEMENTS`, an
error will be produced on unknown elements.

#### Heterogenity

It is the responsibility of the programmer and schema designer to design
documents that can be sensibly typed. A given element handler
`BTElementHandlerType<T, ...>` can only receive child elements of type `T`.
If the child elements of the handler can be of a set of different types, then
it would be desirable (for the sake of type-safety and readability) for those
child elements to have a common supertype that is not `Object`. The best way to
do this, schema allowing, is to use a sealedinterface:

```
sealed interface T { }
class Child0 implements T {}
class Child1 implements T {}
class Child2 implements T {}

class IntsHandler
  implements BTElementHandlerType<T, ...>
{
  private ArrayList<T> results;

  IntsHandler(
    final BTElementParsingContextType context)
  {
    this.results = new ArrayList<T>();
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, T>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.ofEntries(
      Map.entry(elementName("child0"), Child0Handler::new),
      Map.entry(elementName("child1"), Child1Handler::new),
      Map.entry(elementName("child2"), Child2Handler::new)
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final T result)
  {
    this.results.add(result);
  }

  @Override
  public List<T> onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.results;
  }
}
```

#### Functors

Element handlers are functors. Given a function `f` of type `B → C` with a
handler `h` that accepts child values of type `A` and produces values of type
`B`, the `Blackthorne.map` function can produce a handler of type `A → C`:

```
Function<B, C> f;
BTElementHandlerType<A, B> h;
BTElementHandlerType<A, C> g = Blackthorne.map(h, f);
```

