/*
 * Copyright (C) 2014 The Language Archive - Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.tla.schemanon;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author Menzo Windhouwer
 */
public class SaxonUtils {

    /**
     * The Saxon processor from which there should be only one. Any Saxon
     * related instance, e.g., an XML document or an XSLT transform, should
     * share this processor. Otherwise Saxon will complain as it can't used
     * shared constructs, like the NamePool.
     */
    static private Processor sxProcessor = null;
    /**
     * The Saxon XSLT compiler.
     */
    static private XsltCompiler sxXsltCompiler = null;
    /**
     * The Saxon XPath compiler.
     */
    static private XPathCompiler sxXPathCompiler = null;
    /**
     * The Saxon Document Builder
     */
    static private DocumentBuilder sxDocumentBuilder = null;

    /**
     * Get a Saxon processor, i.e., just-in-time create the Singleton.
     *
     * @return The Saxon processor
     */
    public static synchronized Processor getProcessor() {
        if (sxProcessor == null) {
            sxProcessor = new Processor(false);
        }
        return sxProcessor;
    }

    private static synchronized XsltCompiler getXsltCompiler() {
        if (sxXsltCompiler == null) {
            sxXsltCompiler = getProcessor().newXsltCompiler();
        }
        return sxXsltCompiler;
    }

    private static synchronized XPathCompiler getXPathCompiler() {
        if (sxXPathCompiler == null) {
            sxXPathCompiler = getProcessor().newXPathCompiler();
        }
        return sxXPathCompiler;
    }

    private static synchronized DocumentBuilder getDocumentBuilder() {
        if (sxDocumentBuilder == null) {
            sxDocumentBuilder = getProcessor().newDocumentBuilder();
        }
        return sxDocumentBuilder;
    }

    /**
     * Load an XML document.
     *
     * @param src The source of the document.
     * @return A Saxon XDM node
     * @throws SaxonApiException
     */
    static public XdmNode buildDocument(Source src) throws SaxonApiException {
        return getDocumentBuilder().build(src);
    }

    /**
     * Load an XML into a DOM.
     *
     * @param src The source of the document.
     * @return A DOM document node
     * @throws Exception
     */
    static public Document buildDOM(File src) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src);
    }

    /**
     * Compile an XLST document. To use compiled XSLT document use the load()
     * method to turn it into a XsltTransformer.
     *
     * @param xslStylesheet
     * @return An Saxon XSLT executable, which can be shared.
     * @throws SaxonApiException
     */
    static public XsltExecutable buildTransformer(XdmNode xslStylesheet) throws SaxonApiException {
        return getXsltCompiler().compile(xslStylesheet.asSource());
    }

    /**
     * Convenience method to build a XSLT transformer from a resource.
     *
     * @param uri The location of the resource
     * @return An executable XSLT
     * @throws Exception
     */
    static public XsltExecutable buildTransformer(File file) throws SchemAnonException {
	try {
	    XdmNode xslDoc = buildDocument(new javax.xml.transform.stream.StreamSource(file));
	    return buildTransformer(xslDoc);
	} catch (SaxonApiException ex) {
	    throw new SchemAnonException(ex);
	}
    }

    /**
     * Convenience method to build a XSLT transformer from a resource.
     *
     * @param uri The location of the resource
     * @return An executable XSLT
     * @throws Exception
     */
    static public XsltExecutable buildTransformer(URL url) throws SchemAnonException {
	try {
	    XdmNode xslDoc = buildDocument(new javax.xml.transform.stream.StreamSource(url.toExternalForm()));
	    return buildTransformer(xslDoc);
	} catch (SaxonApiException ex) {
	    throw new SchemAnonException(ex);
	}
    }

    /**
     * Convenience method to build a XSLT transformer from a resource.
     *
     * @param uri The location of the resource
     * @return An executable XSLT
     * @throws Exception
     */
    static public XsltExecutable buildTransformer(InputStream stream) throws SchemAnonException {
	try {
	    XdmNode xslDoc = buildDocument(new javax.xml.transform.stream.StreamSource(stream));
	    return buildTransformer(xslDoc);
	} catch (SaxonApiException ex) {
	    throw new SchemAnonException(ex);
	}
    }
        
    /**
     * Declare an XML namespace to be used in XPath expressions.
     *
     * @param nsPrefix The prefix used by the XPath expression to refer to the
     * namespace.
     * @param nsUri The actual namespace URI.
     */
    static public void declareXPathNamespace(String nsPrefix, String nsUri) {
        getXPathCompiler().declareNamespace(nsPrefix, nsUri);
    }

    /**
     * Compile an XPath expression. Use evaluate(), evaluateSingle() or
     * iterator() to actually execute the XPath expression.
     *
     * @param xp The XPath expression.
     * @return A compiled XPath expression.
     * @throws SaxonApiException
     */
    static public XPathSelector compileXPath(String xp) throws SaxonApiException {
        return getXPathCompiler().compile(xp).load();
    }

    /**
     * Compile an XPath expression and set the context item. Use evaluate(),
     * evaluateSingle() or iterator() to actually execute the XPath expression.
     *
     * @param ctxt The context item.
     * @param xp The XPath expression.
     * @return A compiled XPath expression.
     * @throws SaxonApiException
     */
    static public XPathSelector evaluateXPath(XdmItem ctxt, String xp) throws SaxonApiException {
        XPathSelector sxXPathSelector = compileXPath(xp);
        sxXPathSelector.setContextItem(ctxt);
        return sxXPathSelector;
    }
    
    /**
     * Wrap a DOM Node in a Saxon XDM node.
     */
    static public XdmNode wrapNode(Node node) {
        return getDocumentBuilder().wrap(node);
    }
}
