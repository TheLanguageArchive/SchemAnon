/*
 * Copyright (C) 2014 menzowindhouwer
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

/**
 *
 * @author menzowindhouwer
 */
public class Message {
    /**
     * Is the message and error or an warning?
     */
    boolean error = false;
    /**
     * The context of the message (might be null).
     */
    String context = null;
    /**
     * The test that triggered the message (might be null).
     */
    String test = null;
    /**
     * The location that triggered the test (might be null).
     */
    String location = null;
    /**
     * The actual message.
     */
    String text = null;

    /**
     * @return the error
     */
    public boolean isError() {
        return error;
    }

    /**
     * @return the context
     */
    public String getContext() {
        return context;
    }

    /**
     * @return the test
     */
    public String getTest() {
        return test;
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }
}
