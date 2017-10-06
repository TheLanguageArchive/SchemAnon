/*
 * Copyright (C) 2014 - 2017 The Language Archive - Max Planck Institute for Psycholinguistics, Meertens Institute
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
