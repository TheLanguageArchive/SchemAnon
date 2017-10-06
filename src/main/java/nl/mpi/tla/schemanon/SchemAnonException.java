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

/**
 * @author Twan Goosens (CMDValidate)
 * @author Menzo Windhouwer
 */
public class SchemAnonException extends Exception {
    
    public SchemAnonException() {
    }
    
    public SchemAnonException(String message) {
	super(message);
    }

    public SchemAnonException(Throwable cause) {
	super(cause);
    }

    public SchemAnonException(String message, Throwable cause) {
	super(message, cause);
    }
    
}
