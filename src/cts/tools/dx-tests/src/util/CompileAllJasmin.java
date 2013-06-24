/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package util;

import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Properties;

import jasmin.Main;

public class CompileAllJasmin {

    /**
     * @param args args[0] the (absolute or relative to the working dir) 
     * path to the (properties) file containing line by line
     * all jasmin files which need to be compiled.
     * args[1] is the target directory where to put the compiled .class files to
     */
    public static void main(String[] args) throws Exception {
        System.out.println("reading from "+args[0]+" and writing to "+args[1]);
        Properties p = new Properties();
        p.load(new FileInputStream(args[0]));
        
        //System.out.println("p:::"+p.toString());
        int i=0;
        for (Iterator<Object> it_keys = p.keySet().iterator(); it_keys.hasNext();) {
            String file = (String) it_keys.next();
            Main m = new jasmin.Main();
            //java -jar $project_lib/jasmin.jar -d $javac_out $ajasminfile
            if (i ==0) {
                m.main(new String[] {"-d" ,args[1], file });
            } else {
                // leave away -d option since saved into static field
                m.main(new String[] {file });
            }
            i++;
        }
    }

}
