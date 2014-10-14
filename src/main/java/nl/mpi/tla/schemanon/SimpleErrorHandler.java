package nl.mpi.tla.schemanon;

import java.util.List;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.xml.sax.ErrorHandler;

public class SimpleErrorHandler implements ErrorHandler {

    private List<Message> msgList;
    private boolean	failOnError = false;

    public SimpleErrorHandler(List<Message> msgList) {
        this.msgList = msgList;
    }

    public SimpleErrorHandler(List<Message> msgList, boolean failOnError) {
        this(msgList);
        this.failOnError = failOnError;
    }

    public void warning(SAXParseException e) throws SAXException {
        addException(false, e);
    }

    public void error(SAXParseException e) throws SAXException {
        addException(true, e);
        if (failOnError) throw e;
    }

    public void fatalError(SAXParseException e) throws SAXException {
        addException(true, e);
        throw e;
    }

    private void addException(boolean error, SAXParseException e) throws SAXException {
        Message msg = new Message();
        msg.context = null;
        msg.test = null;
        msg.location = e.getSystemId() + ": line: "+e.getLineNumber()+" column: "+e.getColumnNumber();
        msg.error = error;
        msg.text = e.getMessage();
        msgList.add(msg);
    }

}
