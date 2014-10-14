SchemAnon
=========

Schematron validation tool, which does both XSD and Schematron
validation of one or more input files:

$ java -jar target/SchemAnon.jar \<URL to XSD\> \<XML file\>

or

$ java -jar target/SchemAnon.jar \<URL to XSD\> \<directory\> \<extension\>*

Build
-----

$ mvn clean install

Notes
-----

As the XSD 1.1 validation feature of Xerces2 isn't available on a Maven
repository this git repository includes a local compiled copy.

As the Ecplise Maven repository is in disarray also a local compiled copy
of the PsychoPath XPah 2.0 processor is included.

Some plans
----------
* accept also local file paths for the schema
* let it also do the RNG validation
* sniff what is needed: XSD, XSD+Schematron, RNG, RNG+Schematron, Schematron
* ...

History
-------
Parts of this code base are based on the [Component MetaData (CMD)](http://www.clarin.eu/cmdi/) validator
developed for [CLARIN](http://www.clarin.eu).

The XSD validation code is based on the [XSD 1.1 validation
tool](http://jeszysblog.wordpress.com/2012/09/27/free-and-open-source-xsd-1-1-validation-tool/) by Jeszy.

License
-------
This software is released under the GPL v3.0 license (see LICENSE).

It makes use of the Schematron XSL2 implementation (see src/main/resources/schematron/, available at schematron.com), which is released under a "Open Source (OSI compliant zlib/libpng license or Apache License)" (http://www.opensource.org/licenses/zlib-license.php).

---
google tip: tron anon
