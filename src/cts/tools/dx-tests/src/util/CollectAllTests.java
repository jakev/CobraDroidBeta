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

import dxc.junit.AllTests;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.textui.TestRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main class to generate data from the test suite to later run from a shell
 * script. the project's home folder.<br>
 * <project-home>/src must contain the java sources<br>
 * <project-home>/data/scriptdata will be generated<br>
 * <project-home>/src/<for-each-package>/Main_testN1.java will be generated<br>
 * (one Main class for each test method in the Test_... class
 */
public class CollectAllTests {

    private static String PROJECT_FOLDER = "";
    private static String PROJECT_FOLDER_OUT = "missing out folder!";
    private static String JAVASRC_FOLDER = PROJECT_FOLDER + "/src";
    private static HashSet<String> OPCODES = null;

    /*
     * a map. key: fully qualified class name, value: a list of test methods for
     * the given class
     */
    private TreeMap<String, List<String>> map = new TreeMap<String, List<String>>();

    private int testClassCnt = 0;
    private int testMethodsCnt = 0;

    private class MethodData {
        String methodBody, constraint, title;
    }

    /**
     * @param args
     *            args 0 must be the project root folder (where src, lib etc.
     *            resides)
     *            args 1 must be the project out root folder (where the Main_*.java files
     *            are put, and also data/scriptdata)
     */
    public static void main(String[] args) {
        if (args.length >= 2) {
            PROJECT_FOLDER = args[0];
            PROJECT_FOLDER_OUT = args[1];
            JAVASRC_FOLDER = PROJECT_FOLDER + "/src";
        } else {
            System.out.println("usage: args 0 must be the project root folder (where src, lib etc. resides)" +
                    "and args 1 must be the project out root folder (where the Main_*.java file" +
                    " are put, and also data/scriptdata)");
            return;
        }
        

        for (int i = 2; i < args.length; i++) {
            if (OPCODES == null) {
                OPCODES = new HashSet<String>();
            }
            OPCODES.add(args[i]);
        }
        
        System.out.println("using java src:"+JAVASRC_FOLDER);
        CollectAllTests cat = new CollectAllTests();
        cat.compose();
    }

    public void compose() {
        System.out.println("Collecting all junit tests...");
        new TestRunner() {
            @Override
            protected TestResult createTestResult() {
                return new TestResult() {
                    @Override
                    protected void run(TestCase test) {
                        addToTests(test);
                    }

                };
            }
        }.doRun(AllTests.suite());

        // for each combination of TestClass and method, generate a Main_testN1
        // etc.
        // class in the respective package.
        // for the report make sure all N... tests are called first, then B,
        // then
        // E, then VFE test methods.
        // so we need x Main_xxxx methods in a package, and x entries in the
        // global scriptdata file (read by a bash script for the tests)
        // e.g. dxc.junit.opcodes.aaload.Test_aaload - testN1() ->
        // File Main_testN1.java in package dxc.junit.opcodes.aaload
        // and entry dxc.junit.opcodes.aaload.Main_testN1 in class execution
        // table.
        //
        handleTests();
    }

    private void addToTests(TestCase test) {
        
        String packageName = test.getClass().getPackage().getName();
        packageName = packageName.substring(packageName.lastIndexOf('.')+1);
        if (OPCODES != null && !OPCODES.contains(packageName)) {
            return;
        }

        
        String method = test.getName(); // e.g. testVFE2
        String fqcn = test.getClass().getName(); // e.g.
        // dxc.junit.opcodes.iload_3.Test_iload_3
        // order: take the order of the test-suites for the classes,
        // TODO and for methods: take Nx, then Bx, then Ex, then VFEx
        //System.out.println("collecting test:" + test.getName() + ", class "
        //        + test.getClass().getName());
        testMethodsCnt++;
        List<String> li = map.get(fqcn);
        if (li == null) {
            testClassCnt++;
            li = new ArrayList<String>();
            map.put(fqcn, li);
        }
        li.add(method);
    }

    private void handleTests() {
        System.out.println("collected "+testMethodsCnt+" test methods in "+testClassCnt+" junit test classes");
        String datafileContent = "";

        for (Entry<String, List<String>> entry : map.entrySet()) {
            
            String fqcn = entry.getKey();
            int lastDotPos = fqcn.lastIndexOf('.');
            String pName = fqcn.substring(0, lastDotPos);
            String classOnlyName = fqcn.substring(lastDotPos + 1);
            String instPrefix = "new " + classOnlyName + "()";
            
            String[] nameParts = pName.split("\\.");
            if (nameParts.length != 4) {
                throw new RuntimeException(
                        "package name does not comply to naming scheme: " + pName);
            }
            
           
            List<String> methods = entry.getValue();
            Collections.sort(methods, new Comparator<String>() {
                public int compare(String s1, String s2) {
                    // TODO sort according: test ... N, B, E, VFE
                    return s1.compareTo(s2);
                }
            });
            for (String method : methods) {
                // e.g. testN1
                if (!method.startsWith("test")) {
                    throw new RuntimeException("no test method: " + method);
                }
                
                // generate the Main_xx java class

                // a Main_testXXX.java contains:
                // package <packagenamehere>;
                // public class Main_testxxx {
                // public static void main(String[] args) {
                // new dxc.junit.opcodes.aaload.Test_aaload().testN1();
                // }
                // }

                MethodData md = parseTestMethod(pName, classOnlyName, method);
                String methodContent = md.methodBody;

                Set<String> dependentTestClassNames = parseTestClassName(pName,
                        classOnlyName, methodContent);
                
                if (dependentTestClassNames.isEmpty())
                {
                    continue;
                }
                

                String content = "//autogenerated by "
                        + this.getClass().getName()
                        + ", do not change\n"
                        + "package "
                        + pName
                        + ";\n"
                        + "import "
                        + pName
                        + ".jm.*;\n"
                        + "import dxc.junit.*;\n"
                        + "public class Main_"
                        + method
                        + " extends DxAbstractMain {\n"
                        + "public static void main(String[] args) throws Exception {\n"
                        + "new Main_" + method + "()." + method + "();\n"
                        + "}\n" + methodContent + "\n}\n";

                writeToFile(getFileFromPackage(pName, method), content);

                // prepare the entry in the data file for the bash script.
                // e.g.
                // main class to execute; opcode/constraint; test purpose
                // dxc.junit.opcodes.aaload.Main_testN1;aaload;normal case test
                // (#1)

                char ca = method.charAt("test".length()); // either N,B,E, oradd_double
                // V (VFE)
                String comment;
                switch (ca) {
                case 'N':
                    comment = "Normal #" + method.substring(5);
                    break;
                case 'B':
                    comment = "Boundary #" + method.substring(5);
                    break;
                case 'E':
                    comment = "Exception #" + method.substring(5);
                    break;
                case 'V':
                    comment = "Verifier #" + method.substring(7);
                    break;
                default:
                    throw new RuntimeException("unknown test abbreviation:"
                            + method + " for " + fqcn);
                }

                String opcConstr = pName.substring(pName.lastIndexOf('.') + 1);
                // beautify test title
                if (opcConstr.startsWith("t4")) {
                    opcConstr = "verifier"; //  + opcConstr.substring(1);
                } else if (opcConstr.startsWith("pargs")) {
                    opcConstr = "sanity";
                } else if (opcConstr.startsWith("opc_")) {
                    // unescape reserved words
                    opcConstr = opcConstr.substring(4);
                }
                
                String line = pName + ".Main_" + method + ";";                
                for (String className : dependentTestClassNames) {
                    try {
                        Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(
                                "dependent class not found : " + className);
                    } catch (Throwable e) {
                        // ignore
                    }
                    
                    line += className + " ";
                }
                
                String details = (md.title != null ? md.title : "");
                if (md.constraint != null) {
                    details = "Constraint " + md.constraint + ", " + details;
                }
                if (details.length() != 0) {
                    details = details.substring(0, 1).toUpperCase() 
                            + details.substring(1);
                }

                line += ";" + opcConstr + ";"+ comment + ";" + details;
                
                datafileContent += line + "\n";
                
            }


        }
        new File(PROJECT_FOLDER_OUT + "/data").mkdirs();
        writeToFile(new File(PROJECT_FOLDER_OUT + "/data/scriptdata"),
                datafileContent);
    }



    /**
     * 
     * @param pName
     * @param classOnlyName
     * @param methodSource
     * @return a set
     */
    private Set<String> parseTestClassName(String pName, String classOnlyName,
            String methodSource) {
        Set<String> entries = new HashSet<String>();
        String opcodeName = classOnlyName.substring(5);

        Scanner scanner = new Scanner(methodSource);

        String[] patterns = new String[] {
                "new\\s(T_" + opcodeName + "\\w*)",
                "(T_" + opcodeName + "\\w*)", "new\\s(T\\w*)"};

        String token = null;
        for (String pattern : patterns) {
            token = scanner.findWithinHorizon(pattern, methodSource.length());
            if (token != null) {
                break;
            }
        }

        if (token == null) {
            System.err.println("warning: failed to find dependent test class name: "+pName+", "+classOnlyName);
            return entries;
        }

        MatchResult result = scanner.match();

        entries.add((pName + ".jm." + result.group(1)).trim());
        
        // search additional @uses directives
        Pattern p = Pattern.compile("@uses\\s+(.*)\\s+", Pattern.MULTILINE);
        Matcher m = p.matcher(methodSource);
        while (m.find()) {
            String res = m.group(1);
            entries.add(res.trim());
        }
        
        //lines with the form @uses dx.junit.opcodes.add_double.jm.T_add_double_2
        // one dependency per one @uses
        //TODO
        
        return entries;
    }

    private MethodData parseTestMethod(String pname, String classOnlyName,
            String method) {

        String path = pname.replaceAll("\\.", "/");
        String absPath = JAVASRC_FOLDER + "/" + path + "/" + classOnlyName
                + ".java";
        File f = new File(absPath);

        Scanner scanner;
        try {
            scanner = new Scanner(f);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("error while reading from file: "
                    + e.getClass().getName() + ", msg:" + e.getMessage());
        }

        String methodPattern = "public\\s+void\\s+" + method + "[^\\{]+\\{";

        String token = scanner.findWithinHorizon(methodPattern, (int) f
                .length());
        if (token == null) {
            throw new RuntimeException(
                    "cannot find method source of 'public void" + method
                            + "' in file '" + absPath + "'");
        }

        MatchResult result = scanner.match();
        result.start();
        result.end();

        StringBuilder builder = new StringBuilder();
        builder.append(token);

        try {
            FileReader reader = new FileReader(f);
            reader.skip(result.end());

            char currentChar;
            int blocks = 1;
            while ((currentChar = (char) reader.read()) != -1 && blocks > 0) {
                switch (currentChar) {
                case '}': {
                    blocks--;
                    builder.append(currentChar);
                    break;
                }
                case '{': {
                    blocks++;
                    builder.append(currentChar);
                    break;
                }
                default: {
                    builder.append(currentChar);
                    break;
                }
                }
            }
	    if (reader != null) {
		reader.close();
	    }
        } catch (Exception e) {
            throw new RuntimeException("failed to parse", e);
        }

        // find the @title/@constraint in javadoc comment for this method
        Scanner scanner2;
        try {
            // using platform's default charset
            scanner2 = new Scanner(f);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("error while reading from file: "
                    + e.getClass().getName() + ", msg:" + e.getMessage());
        }
        
        // using platform's default charset
        String all = new String(readFile(f));
        // System.out.println("grepping javadoc found for method "+method +
        // " in "+pname+","+classOnlyName);
        String commentPattern = "/\\*\\*([^{]*)\\*/\\s*" + methodPattern;
        Pattern p = Pattern.compile(commentPattern, Pattern.DOTALL);
        Matcher m = p.matcher(all);
        String title = null, constraint = null;
        if (m.find()) {
            String res = m.group(1);
            // System.out.println("res: "+res);
            // now grep @title and @constraint
            Matcher titleM = Pattern.compile("@title (.*)", Pattern.DOTALL)
                    .matcher(res);
            if (titleM.find()) {
                title = titleM.group(1).replaceAll("\\n     \\*", "");
                title = title.replaceAll("\\n", " ");
                title = title.trim();
                // System.out.println("title: " + title);
            } else {
                System.err.println("warning: no @title found for method "
                        + method + " in " + pname + "," + classOnlyName);
            }
            // constraint can be one line only
            Matcher constraintM = Pattern.compile("@constraint (.*)").matcher(
                    res);
            if (constraintM.find()) {
                constraint = constraintM.group(1);
                constraint = constraint.trim();
                // System.out.println("constraint: " + constraint);
            } else if (method.contains("VFE")) {
                System.err
                        .println("warning: no @constraint for for a VFE method:"
                                + method + " in " + pname + "," + classOnlyName);
            }
        } else {
            System.err.println("warning: no javadoc found for method " + method
                    + " in " + pname + "," + classOnlyName);
        }
        MethodData md = new MethodData();
        md.methodBody = builder.toString();
        md.constraint = constraint;
        md.title = title;
	if (scanner != null) {
	    scanner.close();
	}
	if (scanner2 != null) {
	    scanner2.close();
	}
        return md;
    }

    private void writeToFile(File file, String content) {
        //System.out.println("writing file " + file.getAbsolutePath());
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file), "utf-8"));
            bw.write(content);
            bw.close();
        } catch (Exception e) {
            throw new RuntimeException("error while writing to file: "
                    + e.getClass().getName() + ", msg:" + e.getMessage());
        }
    }

    private File getFileFromPackage(String pname, String methodName) {
        // e.g. dxc.junit.argsreturns.pargsreturn
        String path = pname.replaceAll("\\.", "/");
        String absPath = PROJECT_FOLDER_OUT + "/" + path;
        new File(absPath).mkdirs();
        return new File(absPath + "/Main_" + methodName + ".java");
    }
    
    private byte[] readFile(File file) {
        int len = (int) file.length();
        byte[] res = new byte[len];
        try {
            FileInputStream in = new FileInputStream(file);
            int pos = 0;
            while (len > 0) {
                int br = in.read(res, pos, len);
                if (br == -1) {
                    throw new RuntimeException("unexpected EOF for file: "+file);
                }
                pos += br;
                len -= br;
            }
            in.close();
        } catch (IOException ex) {
            throw new RuntimeException("error reading file:"+file, ex);
        }
        return res;
    }
}
