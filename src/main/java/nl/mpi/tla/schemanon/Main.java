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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.stream.StreamSource;
import nl.mpi.tla.schemanon.Message;
import org.apache.commons.io.FileUtils;

/**
 * @author Menzo Windhouwer
 */
public class Main {
    
    public static int validate(SchemAnon tron, File input) {
        int code = 0;
        try {
            if (tron.validate(input)) {
                System.out.println("SchemAnon["+input+"]: VALID");
            } else {
                System.out.println("SchemAnon["+input+"]: INVALID!");
                code = 1;
            }
            for (Message msg : tron.getMessages()) {
                System.out.println("" + (msg.isError() ? "ERROR" : "WARNING") + (msg.getLocation() != null ? " at " + msg.getLocation() : ""));
                System.out.println("  " + msg.getText());
            }
            System.out.println();
        } catch (SchemAnonException | IOException ex) {
            System.err.println("FATAL: validating file["+input+"]: "+ex);
            ex.printStackTrace(System.err);
            System.exit(4);
        }
        return code;
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Arguments: <schema URL> (<input file or directory> <extension (use when input directory)>*)?");
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
        SchemAnon tron = new SchemAnon(schemaURL);
        
        int code = 0;
        if (args.length>1) {
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
            for (File input:inputs)
                code = validate(tron,input)>0?1:code;
        } else {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                String line;
                while ((line = in.readLine()) != null && line.length() != 0) {
                    line = line.trim();
                    if (!line.startsWith("#")) {
                        File input = new File(line);
                        code = validate(tron,input)>0?1:code;
                    }
                }
            } catch(IOException ex) {
                System.err.println("FATAL: reading from STDIN: "+ex);
                ex.printStackTrace(System.err);
                System.exit(16);
            }
        }
        
        System.exit(code);
    }
    
}
