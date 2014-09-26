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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.stream.StreamSource;
import nl.mpi.tla.schemanon.SchemAnon.Message;
import org.apache.commons.io.FileUtils;

/**
 * @author Menzo Windhouwer
 */
public class Main {
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Arguments: <schema URL> < input file or directory> (extension (use when input directory)>*");
            System.exit(2);
        }
        
        URL schemaURL = null;
        try {
            schemaURL = new URL(args[0]);            
        } catch (MalformedURLException ex) {
            System.err.println("FATAL: loading schema["+args[1]+"]: "+ex);
            ex.printStackTrace(System.err);
            System.exit(3);
        }
        SchemAnon schemaTron = new SchemAnon(schemaURL);
        
        Collection<File> inputs = new ArrayList();
        File location = new File(args[1]);
        if (location.isDirectory()) {
            ArrayList<String> extensions = new ArrayList<String>();
            for (int e=2;e<args.length;e++)
                extensions.add(args[e]);
            inputs = FileUtils.listFiles(location,extensions.toArray(new String[]{}),true);
        } else {
            inputs.add(location);
        }
        
        int code = 0;
        for (File input:inputs) {
            try {
                if (schemaTron.validate(new StreamSource(input))) {
                    System.out.println("SchemAnon["+input+"]: VALID");
                } else {
                    System.out.println("SchemAnon["+input+"]: INVALID!");
                    code = 1;
                }
                for (Message msg : schemaTron.getMessages()) {
                    System.out.println("" + (msg.isError() ? "ERROR" : "WARNING") + (msg.getLocation() != null ? " at " + msg.getLocation() : ""));
                    System.out.println("  " + msg.getText());
                }
                System.out.println();
            } catch (SchemAnonException | IOException ex) {
                System.err.println("FATAL: validating file["+input+"]: "+ex);
                ex.printStackTrace(System.err);
                System.exit(4);
            }
        }
        System.exit(code);
    }
    
}
