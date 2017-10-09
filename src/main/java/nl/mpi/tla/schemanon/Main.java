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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.sf.saxon.s9api.SaxonApiException;
import org.apache.commons.io.comparator.SizeFileComparator;


/**
 * @author Menzo Windhouwer
 */
public class Main {
    
    public static int validate(SchemAnon tron, File input, boolean svrl, boolean quiet) {
        int code = 0;
        try {
            if (!tron.validate(input))
                code = 1;
            if (!quiet) {
                for (Message msg : tron.getMessages()) {
                    System.out.println("SchemAnon["+input+"]: "+(code==0?"VALID":"INVALID!"));
                    System.out.println("" + (msg.isError() ? "ERROR" : "WARNING") + (msg.getLocation() != null ? " at " + msg.getLocation() : ""));
                    System.out.println("  " + msg.getText());
                }
                System.out.println();
            }
            if (svrl) {
                File output = new File(input.getPath()+".svrl");
                SaxonUtils.save(tron.getReport().asSource(),output);
            }
        } catch (SaxonApiException | SchemAnonException | IOException ex) {
            System.err.println("FATAL: validating file["+input+"]: "+ex);
            ex.printStackTrace(System.err);
        }
        return code;
    }

    private static void showHelp() {
        System.err.println("INF: SchemAnon <options> -- <URL> <INPUT>? <EXT>*");
        System.err.println("INF: <URL>      URL to the XSD Schema and/or Schematron rules");
        System.err.println("INF: <INPUT>    input directory, a single file or, via STDIN, a list of files to be processed");
        System.err.println("INF: <EXT>      file extension to filter on in the input directory (default: xml)");
        System.err.println("INF: SchemAnon options:");
        System.err.println("INF: -p=<PHASE> Schematron phase to use (optional)");
        System.err.println("INF: -s         Save the Schematron SVRL report (default: don't save)");
        System.err.println("INF: -i         Print progress info (default: on progress info)");
        System.err.println("INF: -q         Be quiet (default: print validation info)");
    }

    public static void main(String[] args) {
        boolean quiet = false;
        boolean svrl = false;
        boolean iter = false;
        String phase = null;
        // check command line
        OptionParser parser = new OptionParser( "p:sqi?*" );
        OptionSet options = parser.parse(args);
        if (options.has("p"))
            phase = (String)options.valueOf("p");
        svrl = options.has("s");
        quiet = options.has("q");
        iter = options.has("i");
        if (options.has("?")) {
            showHelp();
            System.exit(0);
        }
        
        List arg = options.nonOptionArguments();
        if (arg.size()<1) {
            System.err.println("FTL: no XSD Schema or Schematron rules specified!");
            showHelp();
            System.exit(1);
        }
        
        URL schemaURL = null;
        try {
            schemaURL = new URL((String)arg.get(0));            
        } catch (MalformedURLException ex) {
            System.err.println("FATAL: loading schema["+arg.get(0)+"]: "+ex);
            ex.printStackTrace(System.err);
            System.exit(3);
        }
        SchemAnon tron = new SchemAnon(schemaURL,phase);
        
        int code = 0;
        if (arg.size()>1) {
            Collection<File> inputs = new ArrayList();
            File location = new File((String)arg.get(1));
            if (location.isDirectory()) {
                ArrayList<String> extensions = new ArrayList<String>();
                for (int e=2;e<arg.size();e++)
                    extensions.add((String)arg.get(e));
                if (extensions.isEmpty())
                    extensions.add("xml");
                inputs = FileUtils.listFiles(location,extensions.toArray(new String[]{}),true);
                File[] files = {};
                files = inputs.toArray(files);
                Arrays.sort(files, SizeFileComparator.SIZE_COMPARATOR);
                inputs = new ArrayList(Arrays.asList(files));
            } else {
                inputs.add(location);
            }
            int i = 0;
            for (File input:inputs) {
                if (iter) {
                    System.err.print("INF: ["+(++i)+"/"+inputs.size()+"]"+input+" ("+input.length()+" bytes)");
                    if (!quiet)
                        System.err.println();
                }
                code = validate(tron,input,svrl,quiet)>0?1:code;
                if (iter && quiet)
                    System.err.println(">> "+(code>0?"INVALID":"VALID"));
            }
        } else {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                String line;
                while ((line = in.readLine()) != null && line.length() != 0) {
                    line = line.trim();
                    if (!line.startsWith("#")) {
                        File input = new File(line);
                        code = validate(tron,input,svrl,quiet)>0?1:code;
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
