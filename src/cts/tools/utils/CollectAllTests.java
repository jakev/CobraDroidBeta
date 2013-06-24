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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import vogar.Expectation;
import vogar.ExpectationStore;
import vogar.ModeId;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.textui.ResultPrinter;
import junit.textui.TestRunner;

public class CollectAllTests extends DescriptionGenerator {

    static final String ATTRIBUTE_RUNNER = "runner";
    static final String ATTRIBUTE_PACKAGE = "appPackageName";
    static final String ATTRIBUTE_NS = "appNameSpace";
    static final String ATTRIBUTE_TARGET = "targetNameSpace";
    static final String ATTRIBUTE_TARGET_BINARY = "targetBinaryName";
    static final String ATTRIBUTE_HOST_SIDE_ONLY = "hostSideOnly";
    static final String ATTRIBUTE_JAR_PATH = "jarPath";

    static final String JAR_PATH = "LOCAL_JAR_PATH :=";
    static final String TEST_TYPE = "LOCAL_TEST_TYPE :";

    static final int HOST_SIDE_ONLY = 1;
    static final int DEVICE_SIDE_ONLY = 2;

    private static String runner;
    private static String packageName;
    private static String target;
    private static String xmlName;
    private static int testType;
    private static String jarPath;

    private static Map<String,TestClass> testCases;
    private static Set<String> failed = new HashSet<String>();

    private static class MyXMLGenerator extends XMLGenerator {

        MyXMLGenerator(String outputPath) throws ParserConfigurationException {
            super(outputPath);

            Node testPackageElem = mDoc.getDocumentElement();

            setAttribute(testPackageElem, ATTRIBUTE_NAME, xmlName);
            setAttribute(testPackageElem, ATTRIBUTE_RUNNER, runner);
            setAttribute(testPackageElem, ATTRIBUTE_PACKAGE, packageName);
            setAttribute(testPackageElem, ATTRIBUTE_NS, packageName);

            if (testType == HOST_SIDE_ONLY) {
                setAttribute(testPackageElem, ATTRIBUTE_HOST_SIDE_ONLY, "true");
                setAttribute(testPackageElem, ATTRIBUTE_JAR_PATH, jarPath);
            }

            if (!packageName.equals(target)) {
                setAttribute(testPackageElem, ATTRIBUTE_TARGET, target);
                setAttribute(testPackageElem, ATTRIBUTE_TARGET_BINARY, target);
            }
        }
    }

    private static String OUTPUTFILE = "";
    private static String MANIFESTFILE = "";
    private static String TESTSUITECLASS = "";
    private static String ANDROID_MAKE_FILE = "";
    private static String LIBCORE_EXPECTATION_DIR = null;

    private static Test TESTSUITE;

    static XMLGenerator xmlGenerator;
    private static ExpectationStore libcoreVogarExpectationStore;
    private static ExpectationStore ctsVogarExpectationStore;

    public static void main(String[] args) {
        if (args.length > 2) {
            OUTPUTFILE = args[0];
            MANIFESTFILE = args [1];
            TESTSUITECLASS = args[2];
            if (args.length > 3) {
                LIBCORE_EXPECTATION_DIR = args[3];
            }
            if (args.length > 4) {
                ANDROID_MAKE_FILE = args[4];
            }
        } else {
            System.out.println("usage: \n" +
                "\t... CollectAllTests <output-file> <manifest-file> <testsuite-class-name> <makefile-file> <expectation-dir>");
            System.exit(1);
        }

        if (ANDROID_MAKE_FILE.length() > 0) {
            testType = getTestType(ANDROID_MAKE_FILE);
        }

        Document manifest = null;
        try {
            manifest = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new FileInputStream(MANIFESTFILE));
        } catch (Exception e) {
            System.err.println("cannot open manifest");
            e.printStackTrace();
            System.exit(1);;
        }

        Element documentElement = manifest.getDocumentElement();

        documentElement.getAttribute("package");

        xmlName = new File(OUTPUTFILE).getName();
        runner = getElementAttribute(documentElement, "instrumentation", "android:name");
        packageName = documentElement.getAttribute("package");
        target = getElementAttribute(documentElement, "instrumentation", "android:targetPackage");

        Class<?> testClass = null;
        try {
            testClass = Class.forName(TESTSUITECLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("test class not found");
            e.printStackTrace();
            System.exit(1);;
        }

        Method method = null;
        try {
            method = testClass.getMethod("suite", new Class<?>[0]);
        } catch (SecurityException e) {
            System.err.println("failed to get suite method");
            e.printStackTrace();
            System.exit(1);;
        } catch (NoSuchMethodException e) {
            System.err.println("failed to get suite method");
            e.printStackTrace();
            System.exit(1);;
        }

        try {
            TESTSUITE = (Test) method.invoke(null, (Object[])null);
        } catch (IllegalArgumentException e) {
            System.err.println("failed to get suite method");
            e.printStackTrace();
            System.exit(1);;
        } catch (IllegalAccessException e) {
            System.err.println("failed to get suite method");
            e.printStackTrace();
            System.exit(1);;
        } catch (InvocationTargetException e) {
            System.err.println("failed to get suite method");
            e.printStackTrace();
            System.exit(1);;
        }

        try {
            xmlGenerator = new MyXMLGenerator(OUTPUTFILE + ".xml");
        } catch (ParserConfigurationException e) {
            System.err.println("Can't initialize XML Generator");
            System.exit(1);
        }

        try {
            libcoreVogarExpectationStore = VogarUtils.provideExpectationStore(LIBCORE_EXPECTATION_DIR);
            ctsVogarExpectationStore = VogarUtils.provideExpectationStore(CTS_EXPECTATION_DIR);
        } catch (IOException e) {
            System.err.println("Can't initialize vogar expectation store");
            e.printStackTrace(System.err);
            System.exit(1);
        }

        testCases = new LinkedHashMap<String, TestClass>();
        CollectAllTests cat = new CollectAllTests();
        cat.compose();

        if (!failed.isEmpty()) {
            System.err.println("The following classes have no default constructor");
            for (Iterator<String> iterator = failed.iterator(); iterator.hasNext();) {
                String type = iterator.next();
                System.err.println(type);
            }
            System.exit(1);
        }

        for (Iterator<TestClass> iterator = testCases.values().iterator(); iterator.hasNext();) {
            TestClass type = iterator.next();
            xmlGenerator.addTestClass(type);
        }

        try {
            xmlGenerator.dump();
        } catch (Exception e) {
            System.err.println("cannot dump xml");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static int getTestType(String makeFileName) {

        int type = DEVICE_SIDE_ONLY;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(makeFileName));
            String line;

            while ((line =reader.readLine())!=null) {
                if (line.startsWith(TEST_TYPE)) {
                    type = HOST_SIDE_ONLY;
                } else if (line.startsWith(JAR_PATH)) {
                    jarPath = line.substring(JAR_PATH.length(), line.length()).trim();
                }
            }
            reader.close();
        } catch (IOException e) {
        }

        return type;
    }

    private static Element getElement(Element element, String tagName) {
        NodeList elements = element.getElementsByTagName(tagName);
        if (elements.getLength() > 0) {
            return (Element) elements.item(0);
        } else {
            return null;
        }
    }

    private static String getElementAttribute(Element element, String elementName, String attributeName) {
        Element e = getElement(element, elementName);
        if (e != null) {
            return e.getAttribute(attributeName);
        } else {
            return "";
        }
    }

    public void compose() {
        TestRunner runner = new TestRunner() {
            @Override
            protected TestResult createTestResult() {
                return new TestResult() {
                    @Override
                    protected void run(TestCase test) {
                        addToTests(test);
                    }
                };
            }

            @Override
            public TestResult doRun(Test test) {
                return super.doRun(test);
            }



        };

        runner.setPrinter(new ResultPrinter(System.out) {
            @Override
            protected void printFooter(TestResult result) {
            }

            @Override
            protected void printHeader(long runTime) {
            }
        });
        runner.doRun(TESTSUITE);
    }

    private String getKnownFailure(final Class<? extends TestCase> testClass,
            final String testName) {
        return getAnnotation(testClass, testName, KNOWN_FAILURE);
    }

    private boolean isKnownFailure(final Class<? extends TestCase> testClass,
            final String testName) {
        return getAnnotation(testClass, testName, KNOWN_FAILURE) != null;
    }

    private boolean isBrokenTest(final Class<? extends TestCase> testClass,
            final String testName)  {
        return getAnnotation(testClass, testName, BROKEN_TEST) != null;
    }

    private boolean isSuppressed(final Class<? extends TestCase> testClass,
            final String testName)  {
        return getAnnotation(testClass, testName, SUPPRESSED_TEST) != null;
    }

    private boolean hasSideEffects(final Class<? extends TestCase> testClass,
            final String testName) {
        return getAnnotation(testClass, testName, SIDE_EFFECT) != null;
    }

    private String getAnnotation(final Class<? extends TestCase> testClass,
            final String testName, final String annotationName) {
        try {
            Method testMethod = testClass.getMethod(testName, (Class[])null);
            Annotation[] annotations = testMethod.getAnnotations();
            for (Annotation annot : annotations) {

                if (annot.annotationType().getName().equals(annotationName)) {
                    String annotStr = annot.toString();
                    String knownFailure = null;
                    if (annotStr.contains("(value=")) {
                        knownFailure =
                            annotStr.substring(annotStr.indexOf("=") + 1,
                                    annotStr.length() - 1);

                    }

                    if (knownFailure == null) {
                        knownFailure = "true";
                    }

                    return knownFailure;
                }

            }

        } catch (java.lang.NoSuchMethodException e) {
        }

        return null;
    }

    private void addToTests(TestCase test) {

        String testClassName = test.getClass().getName();
        String testName = test.getName();
        String knownFailure = getKnownFailure(test.getClass(), testName);

        if (isKnownFailure(test.getClass(), testName)) {
            System.out.println("ignoring known failure: " + test);
            return;
        } else if (isBrokenTest(test.getClass(), testName)) {
            System.out.println("ignoring broken test: " + test);
            return;
        } else if (isSuppressed(test.getClass(), testName)) {
            System.out.println("ignoring suppressed test: " + test);
            return;
        } else if (hasSideEffects(test.getClass(), testName)) {
            System.out.println("ignoring test with side effects: " + test);
            return;
        } else if (VogarUtils.isVogarKnownFailure(libcoreVogarExpectationStore, test.getClass().getName(), testName)) {
            System.out.println("ignoring libcore expectation known failure: " + test);
            return;
        } else if (VogarUtils.isVogarKnownFailure(ctsVogarExpectationStore, test.getClass().getName(), testName)) {
            System.out.println("ignoring cts expectation known failure: " + test);
            return;
        }

        if (!testName.startsWith("test")) {
            try {
                test.runBare();
            } catch (Throwable e) {
                e.printStackTrace();
                return;
            }
        }
        TestClass testClass = null;
        if (testCases.containsKey(testClassName)) {
            testClass = testCases.get(testClassName);
        } else {
            testClass = new TestClass(testClassName, new ArrayList<TestMethod>());
            testCases.put(testClassName, testClass);
        }

        testClass.mCases.add(new TestMethod(testName, "", "", knownFailure, false, false));

        try {
            test.getClass().getConstructor(new Class<?>[0]);
        } catch (SecurityException e) {
            failed.add(test.getClass().getName());
        } catch (NoSuchMethodException e) {
            failed.add(test.getClass().getName());
        }
    }
}
