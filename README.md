SchemAnon
=========

Schematron validation tool, which extracts the Schematron rules from an
XSD and validates one or more input files:

$ java -jar target/SchemAnon.jar <URL to XSD> <XML file>

or

$ java -jar target/SchemAnon.jar <URL to XSD> <directory> <extension>*

Build
-----

$ mvn clean install

Some plans
----------
* accept also local file paths for the schema
* let it also do the XSD or RNG validation
* sniff what is needed: XSD, XSD+Schematron, RNG, RNG+Schematron, Schematron
* ...

License
-------
This software is released under the GPL v3.0 license (see LICENSE).

It makes use of the Schematron XSL2 implementation (see src/main/resources/schematron/, available at schematron.com), which is released under a "Open Source (OSI compliant zlib/libpng license or Apache License)" (http://www.opensource.org/licenses/zlib-license.php).

---
google tip: tron anon
