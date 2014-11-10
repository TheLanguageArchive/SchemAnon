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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * @author Twan Goosens (CMDValidate)
 * @author Menzo Windhouwer
 */
public class SchemAnon {
    
    /**
     * The immutable location of the CMD schema that is used in this instance
     */
    private final URL schemaURL;
    
    /**
     * The "immutable in-memory representation of [the XSD] grammar".
     */
    private Schema xsdSchema = null;
    
    /**
     * The "immutable, and therefore thread-safe," "compiled form of [the Schematron] stylesheet".
     */
    private XsltExecutable schemaTron = null;
    
    /**
     * The list of validation messages compiled a the last run of the validator.
     */
    private List<Message> msgList = null;

    /**
     * The Schematron SVRL validation report
     */
    private XdmNode validationReport = null;
    private LSResourceResolver resourceResolver = null;

    public SchemAnon(URL schemaURL) {
        this.schemaURL = schemaURL;
    }
    
        /**
     * Convenience method to build a XSLT transformer from a resource.
     *
     * @param uri The location of the resource
     * @return An executable XSLT
     * @throws Exception
     */
    static XsltExecutable buildTransformer(File file) throws SchemAnonException {
	try {
	    XdmNode xslDoc = SaxonUtils.buildDocument(new javax.xml.transform.stream.StreamSource(file));
	    return SaxonUtils.buildTransformer(xslDoc);
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
    static XsltExecutable buildTransformer(URL url) throws SchemAnonException {
	try {
	    XdmNode xslDoc = SaxonUtils.buildDocument(new javax.xml.transform.stream.StreamSource(url.toExternalForm()));
	    return SaxonUtils.buildTransformer(xslDoc);
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
    static XsltExecutable buildTransformer(InputStream stream) throws SchemAnonException {
	try {
	    XdmNode xslDoc = SaxonUtils.buildDocument(new javax.xml.transform.stream.StreamSource(stream));
	    return SaxonUtils.buildTransformer(xslDoc);
	} catch (SaxonApiException ex) {
	    throw new SchemAnonException(ex);
	}
    }
    
    /**
     * Returns the Schematron XSLT, and loads it just-in-time.
     *
     * @return The compiled Schematron XSLT
     * @throws Exception
     */
    private synchronized XsltExecutable getSchematron() throws SchemAnonException, IOException {
	if (schemaTron == null) {
	    try {
		// Load the schema
		XdmNode schema = SaxonUtils.buildDocument(new StreamSource(schemaURL.toString()));
		// Load the Schematron XSL to extract the Schematron rules;
		XsltTransformer extractSchXsl = buildTransformer(SchemAnon.class.getResource("/schematron/ExtractSchFromXSD-2.xsl")).load();
		// Load the Schematron XSLs to 'compile' Schematron rules;
		XsltTransformer includeSchXsl = buildTransformer(SchemAnon.class.getResource("/schematron/iso_dsdl_include.xsl")).load();
		XsltTransformer expandSchXsl = buildTransformer(SchemAnon.class.getResource("/schematron/iso_abstract_expand.xsl")).load();
		XsltTransformer compileSchXsl = buildTransformer(SchemAnon.class.getResource("/schematron/iso_svrl_for_xslt2.xsl")).load();
		// Setup the pipeline
		XdmDestination destination = new XdmDestination();
		extractSchXsl.setSource(schema.asSource());
		extractSchXsl.setDestination(includeSchXsl);
		includeSchXsl.setDestination(expandSchXsl);
		expandSchXsl.setDestination(compileSchXsl);
		compileSchXsl.setDestination(destination);
		// Extract the Schematron rules from the schema        
		extractSchXsl.transform();
		// Compile the Schematron rules XSL
		schemaTron = SaxonUtils.buildTransformer(destination.getXdmNode());
	    } catch (SaxonApiException ex) {
		throw new SchemAnonException(ex);
	    }
	}
	return schemaTron;
    }
    
    /**
     * Validation of a loaded document against the Schematron XSLT
     *
     * @param src The loaded document
     * @return Is the document valid or not?
     * @throws Exception
     */
    public boolean validateSchematron(File file) throws SchemAnonException, IOException {
	try {
	    XsltTransformer schematronXsl = getSchematron().load();
	    schematronXsl.setSource(new StreamSource(file));
	    XdmDestination destination = new XdmDestination();
	    schematronXsl.setDestination(destination);
	    schematronXsl.transform();

	    validationReport = destination.getXdmNode();

	    SaxonUtils.declareXPathNamespace("svrl", "http://purl.oclc.org/dsdl/svrl");
	    return ((net.sf.saxon.value.BooleanValue) SaxonUtils.evaluateXPath(validationReport, "empty(//svrl:failed-assert[(preceding-sibling::svrl:fired-rule)[last()][empty(@role) or @role!='warning']])").evaluateSingle().getUnderlyingValue()).getBooleanValue();
	} catch (SaxonApiException ex) {
	    throw new SchemAnonException(ex);
	}
    }
    
    /**
     * Returns the XSD schema, and loads it just-in-time.
     *
     * @return An in-memory representation of the grammar
     * @throws Exception
     */
    private synchronized Schema getXSD() throws SchemAnonException, IOException {
	if (xsdSchema == null) {
            System.setProperty("javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema/v1.1",
                "org.apache.xerces.jaxp.validation.XMLSchema11Factory");		
            try {
                SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1");
                sf.setErrorHandler(new SimpleErrorHandler(msgList,true));
                xsdSchema = sf.newSchema(schemaURL);
            } catch(Exception ex) {
                throw new SchemAnonException(ex);
            }
	}
	return xsdSchema;
    }
    
    public boolean validateXSD(File file) throws SchemAnonException, IOException {
	try {
            Validator validator = getXSD().newValidator();
            validator.setErrorHandler(new SimpleErrorHandler(msgList,false));
            validator.validate(new StreamSource(file));
            for (Message msg:msgList) {
                if (msg.isError())
                    return false;
            }
        } catch (org.xml.sax.SAXParseException ex) {
            Message msg = new Message();
            msg.context = null;
            msg.test = null;
            msg.location = ex.getSystemId() + ": line: "+ex.getLineNumber()+" column: "+ex.getColumnNumber();
            msg.error = true;
            msg.text = ex.getMessage();
            msgList.add(msg);
            return false;
	} catch (Exception ex) {
	    throw new SchemAnonException(ex);
	}
	return true;
    }

    /**
     * Validation of a loaded document
     *
     * After validation any messages can be accessed using the {@link getMessages()} method.
     * Notice that even if a document is valid there might be warning messages.
     *
     * @param src The input document
     * @return Is the document valid or not?
     * @throws Exception
     */
   public boolean validate(File src) throws SchemAnonException, IOException {
 	// Initalize
	msgList = new java.util.ArrayList<Message>();
	validationReport = null;
        
       	try {
            // step 1: validate against XML Schema
	    if (!this.validateXSD(src))
		return false;
	    // step 2: validate Schematron rules
	    return validateSchematron(src);
	} catch (Exception ex) {
            Message msg = new Message();
            msg.context = null;
            msg.test = null;
            msg.location = null;
            msg.error = true;
            msg.text = ex.getMessage();
            msgList.add(msg);
            return false;
	}

    }
    
    /**
     * Get the list of messages accumulated in the last validation run.
     *
     * @return The list of messages
     * @throws Exception
     */
    public List<Message> getMessages() throws SchemAnonException {
	if (validationReport != null) {
	    try {
		for (XdmItem assertion : SaxonUtils.evaluateXPath(validationReport, "//svrl:failed-assert")) {
		    Message msg = new Message();
		    msg.context = SaxonUtils.evaluateXPath(assertion, "(preceding-sibling::svrl:fired-rule)[last()]/@context").evaluateSingle().getStringValue();
		    msg.test = ((XdmNode) assertion).getAttributeValue(new QName("test"));
		    msg.location = ((XdmNode) assertion).getAttributeValue(new QName("location"));
		    msg.error = !((net.sf.saxon.value.BooleanValue) SaxonUtils.evaluateXPath(assertion, "(preceding-sibling::svrl:fired-rule)[last()]/@role='warning'").evaluateSingle().getUnderlyingValue()).getBooleanValue();
		    msg.text = assertion.getStringValue();
		    msgList.add(msg);
		}
		validationReport = null;
	    } catch (SaxonApiException ex) {
		throw new SchemAnonException(ex);
	    }
	}
	return msgList;
    }    
}
